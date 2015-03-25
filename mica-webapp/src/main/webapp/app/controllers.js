'use strict';

/* Controllers */

mica.controller('MainController', [ function () {} ]);

mica.controller('AdminController', [ function () {} ]);

mica.controller('LanguageController', ['$scope', '$translate',
  function ($scope, $translate) {
    $scope.changeLanguage = function (languageKey) {
      $translate.use(languageKey);
    };
  }]);

mica.controller('MenuController', [ function () {} ]);

mica.controller('LoginController', ['$scope', '$location', 'AuthenticationSharedService',
  function ($scope, $location, AuthenticationSharedService) {
    $scope.login = function () {
      AuthenticationSharedService.login({
        username: $scope.username,
        password: $scope.password,
        success: function () {
          $location.path('');
        }
      });
    };
  }]);

mica.controller('LogoutController', ['$location', 'AuthenticationSharedService',
  function ($location, AuthenticationSharedService) {
    AuthenticationSharedService.logout({
      success: function () {
        $location.path('');
      }
    });
  }]);

mica.controller('SettingsController', ['$scope', 'Account',
  function ($scope, Account) {
    $scope.success = null;
    $scope.error = null;
    $scope.settingsAccount = Account.get();

    $scope.save = function () {
      Account.save($scope.settingsAccount,
        function () {
          $scope.error = null;
          $scope.success = 'OK';
          $scope.settingsAccount = Account.get();
        },
        function () {
          $scope.success = null;
          $scope.error = 'ERROR';
        });
    };
  }]);

mica.controller('PasswordController', ['$scope', 'Password',
  function ($scope, Password) {
    $scope.success = null;
    $scope.error = null;
    $scope.doNotMatch = null;
    $scope.changePassword = function () {
      if ($scope.password !== $scope.confirmPassword) {
        $scope.doNotMatch = 'ERROR';
      } else {
        $scope.doNotMatch = null;
        Password.save($scope.password,
          function () {
            $scope.error = null;
            $scope.success = 'OK';
          },
          function () {
            $scope.success = null;
            $scope.error = 'ERROR';
          });
      }
    };
  }]);

mica.controller('SessionsController', ['$scope', 'resolvedSessions', 'Sessions',
  function ($scope, resolvedSessions, Sessions) {
    $scope.success = null;
    $scope.error = null;
    $scope.sessions = resolvedSessions;
    $scope.invalidate = function (series) {
      Sessions.delete({series: encodeURIComponent(series)},
        function () {
          $scope.error = null;
          $scope.success = 'OK';
          $scope.sessions = Sessions.get();
        },
        function () {
          $scope.success = null;
          $scope.error = 'ERROR';
        });
    };
  }]);

mica.controller('MetricsController', ['$rootScope', '$scope', 'MetricsService', 'HealthCheckService', 'ThreadDumpService',
  function ($rootScope, $scope, MetricsService, HealthCheckService, ThreadDumpService) {
    $scope.refresh = function () {
      HealthCheckService.check().then(function (data) {
        $scope.healthCheck = data;
      });

      $scope.metrics = MetricsService.get();

      $scope.metrics.$get({}, function (items) {

        $scope.servicesStats = {};
        $scope.cachesStats = {};
        angular.forEach(items.timers, function (value, key) {
          if (key.indexOf('web.rest') !== -1) {
            $scope.servicesStats[key] = value;
          }

          if (key.indexOf('net.sf.ehcache.Cache') !== -1) {
            // remove gets or puts
            var index = key.lastIndexOf('.');
            var newKey = key.substr(0, index);

            // Keep the name of the domain
            index = newKey.lastIndexOf('.');
            $scope.cachesStats[newKey] = {
              'name': newKey.substr(index + 1),
              'value': value
            };
          }
        });
      });
    };

    $scope.refresh();

    $scope.threadDump = function () {
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
  }]);

mica.controller('LogsController', ['$scope', 'resolvedLogs', 'LogsService',
  function ($scope, resolvedLogs, LogsService) {
    $scope.loggers = resolvedLogs;

    $scope.changeLevel = function (name, level) {
      LogsService.changeLevel({name: name, level: level}, function () {
        $scope.loggers = LogsService.findAll();
      });
    };
  }]);

mica.controller('CachingController', ['$scope', '$rootScope', 'CacheService', 'NOTIFICATION_EVENTS',
  function ($scope, $rootScope, CacheService, NOTIFICATION_EVENTS) {

    $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (ev, callback) {
      callback();
    });

    function withConfirm(onConfirm, opts) {
      var defaults = {message : 'Are you sure to clear this cache?'};
      var args = angular.extend({}, defaults, opts)

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

    $scope.buildDatasetVariables = function () {
      withConfirm(function () {
        CacheService.cache.build({id: 'datasetVariables'});
      }, {message: 'Are you sure you want to build this cache?'});
    };
  }]);

mica.controller('AuditsController', ['$scope', '$translate', '$filter', 'AuditsService',
  function ($scope, $translate, $filter, AuditsService) {
    $scope.onChangeDate = function () {
      AuditsService.findByDates($scope.fromDate, $scope.toDate).then(function (data) {
        $scope.audits = data;
      });
    };

    // Date picker configuration
    $scope.today = function () {
      // Today + 1 day - needed if the current day must be included
      var today = new Date();
      var tomorrow = new Date(today.getFullYear(), today.getMonth(), today.getDate() + 1); // create new increased date

      $scope.toDate = $filter('date')(tomorrow, 'yyyy-MM-dd');
    };

    $scope.previousMonth = function () {
      var fromDate = new Date();
      if (fromDate.getMonth() === 0) {
        fromDate = new Date(fromDate.getFullYear() - 1, 0, fromDate.getDate());
      } else {
        fromDate = new Date(fromDate.getFullYear(), fromDate.getMonth() - 1, fromDate.getDate());
      }

      $scope.fromDate = $filter('date')(fromDate, 'yyyy-MM-dd');
    };

    $scope.today();
    $scope.previousMonth();

    AuditsService.findByDates($scope.fromDate, $scope.toDate).then(function (data) {
      $scope.audits = data;
    });
  }]);

