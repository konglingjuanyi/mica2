/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

mica.admin

  .controller('AdminViewController', [
    '$rootScope',
    '$scope',
    '$translate',
    function () {
    }])

  .controller('MetricsController', ['$rootScope', '$scope', 'MetricsService', 'ThreadDumpService',
    function ($rootScope, $scope, MetricsService, ThreadDumpService) {
      $scope.refresh = function () {

        $scope.metrics = MetricsService.get();

        $scope.metrics.$get({}, function (items) {

          $scope.servicesStats = {};
          $scope.cachesStats = {};
          angular.forEach(items.timers, function (value, key) {
            if (key.indexOf('org.obiba') !== -1 && key.indexOf('net.sf.ehcache') === -1) {
              $scope.servicesStats[key.replace('org.obiba.mica','o.o.m')] = value;
            }

            if (key.indexOf('net.sf.ehcache') !== -1) {
              // remove gets or puts
              var index = key.lastIndexOf('.');
              var newKey = key.substr(0, index);

              // Keep the name of the domain
              $scope.cachesStats[newKey] = {
                'name': newKey.replace('net.sf.ehcache.Cache.',''),
                'value': value
              };
            }
          });
        });
      };

      $scope.refresh();

      $scope.threadDump = [];
      $scope.dumpThread = function () {
        ThreadDumpService.dump().then(function (data) {
          $scope.threadDump = data;
          $scope.threadDumpRunnable = 0;
          $scope.threadDumpWaiting = 0;
          $scope.threadDumpTimedWaiting = 0;
          $scope.threadDumpBlocked = 0;

          angular.forEach(data, function (value) {
            if (value.threadState === 'RUNNABLE') {
              $scope.threadDumpRunnable += 1;
            } else if (value.threadState === 'WAITING') {
              $scope.threadDumpWaiting += 1;
            } else if (value.threadState === 'TIMED_WAITING') {
              $scope.threadDumpTimedWaiting += 1;
            } else if (value.threadState === 'BLOCKED') {
              $scope.threadDumpBlocked += 1;
            }
          });

          $scope.threadDumpAll = $scope.threadDumpRunnable + $scope.threadDumpWaiting +
            $scope.threadDumpTimedWaiting + $scope.threadDumpBlocked;

        });
      };

      $scope.getLabelClass = function (threadState) {
        if (threadState === 'RUNNABLE') {
          return 'label-success';
        }
        if (threadState === 'WAITING') {
          return 'label-info';
        }
        if (threadState === 'TIMED_WAITING') {
          return 'label-warning';
        }
        if (threadState === 'BLOCKED') {
          return 'label-danger';
        }
      };
    }])

  .controller('LogsController', ['$scope', '$timeout', 'LogsService', 'cfpLoadingBar',
    function ($scope, $timeout, LogsService, cfpLoadingBar) {

      var findAll = function() {
        cfpLoadingBar.start();

        LogsService.findAll(
          function onSuccess(response) {
            $scope.loggers = response;
            cfpLoadingBar.complete();
          },
          function onError() {
            cfpLoadingBar.complete();
          }
        );
      };

      var changeLevel = function (name, level) {
        LogsService.changeLevel({name: name, level: level}, function () {
          findAll();
        });
      };

      $scope.changeLevel = changeLevel;

      $timeout(findAll, 250);

    }])

  .controller('CachingController', ['$scope', '$rootScope', 'CacheService', 'NOTIFICATION_EVENTS',
    function ($scope, $rootScope, CacheService, NOTIFICATION_EVENTS) {

      $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (ev, callback) {
        callback();
      });

      function withConfirm(onConfirm, opts) {
        var defaults = {message : 'Are you sure to clear this cache?'};
        var args = angular.extend({}, defaults, opts);

        $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
          {title: 'Clear cache', message: args.message}, onConfirm);
      }

      $scope.clearAll = function () {
        withConfirm(function () {
          CacheService.caches.clear();
        });
      };

      $scope.clearMicaConfig = function () {
        withConfirm(function () {
          CacheService.cache.clear({id: 'micaConfig'});
        });
      };

      $scope.clearOpalTaxonomies = function () {
        withConfirm(function () {
          CacheService.cache.clear({id: 'opalTaxonomies'});
        });
      };

      $scope.clearAggregationsMetadata = function () {
        withConfirm(function () {
          CacheService.cache.clear({id: 'aggregationsMetadata'});
        });
      };

      $scope.clearDatasetVariables = function () {
        withConfirm(function () {
          CacheService.cache.clear({id: 'datasetVariables'});
        });
      };

      $scope.clearAuthorization = function () {
        withConfirm(function () {
          CacheService.cache.clear({id: 'authorization'});
        });
      };

      $scope.buildDatasetVariables = function () {
        withConfirm(function () {
          CacheService.cache.build({id: 'datasetVariables'});
        }, {message: 'Are you sure you want to build this cache?'});
      };
    }])

  .controller('IndexingController', ['$scope', '$rootScope', 'IndexService', 'NOTIFICATION_EVENTS',
    function ($scope, $rootScope, IndexService, NOTIFICATION_EVENTS) {

      $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (ev, callback) {
        callback();
      });

      function withConfirm(onConfirm, opts) {
        var defaults = {message : 'Do you want to rebuild this index?'};
        var args = angular.extend({}, defaults, opts);

        $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
          {title: 'Rebuild index', message: args.message}, onConfirm);
      }

      $scope.indexAll = function () {
        withConfirm(function () {
          IndexService.all.build();
        }, {message: 'Do you want to rebuild all the indices?'});
      };

      $scope.indexNetworks = function () {
        withConfirm(function () {
          IndexService.networks.build();
        });
      };

      $scope.indexStudies = function () {
        withConfirm(function () {
          IndexService.studies.build();
        });
      };

      $scope.indexDatasets = function () {
        withConfirm(function () {
          IndexService.datasets.build();
        });
      };

      $scope.indexStudyDatasets = function () {
        withConfirm(function () {
          IndexService.studyDatasets.build();
        });
      };

      $scope.indexHarmonizationDatasets = function () {
        withConfirm(function () {
          IndexService.harmonizationDatasets.build();
        });
      };

      $scope.indexTaxonomies = function () {
        withConfirm(function () {
          IndexService.taxonomies.build();
        });
      };

    }]);
