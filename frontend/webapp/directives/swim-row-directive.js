var app = angular.module('swimlog');

app.directive('swimRow', function() {
    return {
        restrict: 'AE',
        scope: {
            slOnUpdate: '=',
            slDate: '=',
            slDuration: '=',
            slDistance: '=',
            slUsername: '=',
            slUserId: '=',
            slHideUsername: '=',
            slSwimId: '='
        },
        templateUrl: 'templates/swim-row-template.html'
    };
});