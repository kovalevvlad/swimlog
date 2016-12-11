var app = angular.module('swimlog');

app.directive('createSwim', function() {
    return {
        restrict: 'AE',
        scope: {
            slOnSubmit: '=',
            slHideUsername: '='
        },
        templateUrl: 'templates/create-swim-form.html'
    };
});