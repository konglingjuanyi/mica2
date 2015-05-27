/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

mica.dataAccessRequest

  .controller('DataAccessRequestListController', ['$rootScope', '$scope', 'DataAccessRequestsResource', 'DataAccessRequestResource', 'DataAccessRequestService', 'NOTIFICATION_EVENTS',

    function ($rootScope, $scope, DataAccessRequestsResource, DataAccessRequestResource, DataAccessRequestService, NOTIFICATION_EVENTS) {

      $scope.requests = DataAccessRequestsResource.query();
      $scope.actions = DataAccessRequestService.actions;

      $scope.deleteRequest = function (request) {
        $scope.requestToDelete = request.id;
        $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
          {
            titleKey: 'data-access-request.delete-dialog.title',
            messageKey:'data-access-request.delete-dialog.message',
            messageArgs: [request.title, request.applicant]
          }, request.id
        );
      };

      $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (event, id) {
        if ($scope.requestToDelete === id) {
          DataAccessRequestResource.delete({id: $scope.requestToDelete},
            function () {
              $scope.requests = DataAccessRequestsResource.query();
            });

          delete $scope.requestToDelete;
        }
      });
    }])

  .controller('DataAccessRequestViewController', ['$scope', '$routeParams', 'DataAccessRequestResource',

    function ($scope, $routeParams, DataAccessRequestResource) {

      $scope.dataAccessRequest = $routeParams.id ?
        DataAccessRequestResource.get({id: $routeParams.id}, function(dataAccessRequest) {
          return dataAccessRequest;
        }) : {};

    }])

  .controller('DataAccessRequestEditController', ['$log', '$scope', '$routeParams', '$location', 'DataAccessRequestsResource', 'DataAccessRequestResource', 'DataAccessFormResource', 'AlertService', 'ServerErrorUtils', 'Session', 'DataAccessRequestService',

    function ($log, $scope, $routeParams, $location, DataAccessRequestsResource, DataAccessRequestResource, DataAccessFormResource, AlertService, ServerErrorUtils, Session, DataAccessRequestService) {

      var onSuccess = function(response, getResponseHeaders) {
        var parts = getResponseHeaders().location.split('/');
        $location.path('/data-access-request/' + parts[parts.length - 1]).replace();
      };

      var onError = function(response) {
        AlertService.alert({
          id: 'DataAccessRequestEditController',
          type: 'danger',
          msg: ServerErrorUtils.buildMessage(response)
        });
      };

      var submit = function(form) {
        $scope.$broadcast('schemaFormValidate');

        if (form.$valid) {
          $scope.dataAccessRequest.content = JSON.stringify($scope.form.model);

          if ($scope.newRequest) {
            DataAccessRequestsResource.save($scope.dataAccessRequest, onSuccess, onError);
          } else {
            DataAccessRequestResource.save($scope.dataAccessRequest, onSuccess, onError);
          }
        }
      };

      var cancel = function() {
        $location.path('/data-access-request' + ($scope.dataAccessRequest.id ? '/' + $scope.dataAccessRequest.id : 's')).replace();
      };

      // Retrieve form data
      DataAccessFormResource.get(
        function onSuccess(dataAccessForm) {
          $scope.form.definition = JSON.parse(dataAccessForm.definition);
          $scope.form.schema = JSON.parse(dataAccessForm.schema);

          $scope.dataAccessRequest = $routeParams.id ?
            DataAccessRequestResource.get({id: $routeParams.id}, function onSuccess(request) {
              $scope.form.model = request.content ? JSON.parse(request.content) : {};
              $scope.canEdit = DataAccessRequestService.actions.canEdit(request);
              $scope.form.schema.readonly = !$scope.canEdit;
              $scope.$broadcast('schemaFormRedraw');
              $scope.newRequest = false;
              return request;
            }) : {
              applicant: Session.login,
              status: DataAccessRequestService.status.OPENED
          };
        },
        onError
        );

      $scope.form = {
        schema: {},
        definition: {},
        model: {}
      };

      $scope.newRequest = true;
      $scope.cancel = cancel;
      $scope.editable = true;
      $scope.submit = submit;
    }]);
