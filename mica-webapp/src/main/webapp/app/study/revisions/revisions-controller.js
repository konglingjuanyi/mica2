'use strict';

mica.revisions
  .controller('RevisionsController', [
    '$rootScope',
    '$scope',
    '$filter',
    'LocaleStringUtils',
    function ($rootScope, $scope) {
      var onSuccess = function(revisions) {
        $scope.commitInfos = revisions;
        viewRevision($scope.active.index, $scope.id, $scope.commitInfos[$scope.active.index]);
      };

      var viewRevision = function(index, id, commitInfo) {
        $scope.active.realIndex = $scope.commitInfos.indexOf(commitInfo);
        $scope.active.index = index;
        $scope.active.page = $scope.pages.index;
        $scope.onViewRevision()(id, commitInfo);
      };

      var restoreRevision = function(id, commitInfo) {
        $scope.onRestoreRevision()(id, commitInfo, function() {
          $scope.onFetchRevisions()($scope.id, onSuccess);
        });
      };

      var onWatchId = function() {
        if ($scope.id) {
          $scope.onFetchRevisions()($scope.id, onSuccess);
        }
      };

      var canPaginate = function() {
        return $scope.commitInfos && $scope.commitInfos.length > $scope.pages.perPage;
      };

      $scope.pages = {index: 1, perPage: 5};
      $scope.active = {index: 0, realIndex: 0, page: 1};
      $scope.$watch('id', onWatchId);
      $scope.viewRevision = viewRevision;
      $scope.restoreRevision = restoreRevision;
      $scope.canPaginate = canPaginate;
    }]);
