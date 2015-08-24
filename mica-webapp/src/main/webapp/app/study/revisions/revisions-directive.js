'use strict';

mica.revisions
  .directive('revisions', [function () {
    return {
      restrict: 'AE',
      replace: true,
      scope: {
        title: '@',
        id: '@',
        onFetchRevisions: '&',
        onViewRevision: '&',
        onRestoreRevision: '&'
      },
      templateUrl: 'app/study/revisions/revisions-template.html',
      controller: 'RevisionsController'
    };
  }]);