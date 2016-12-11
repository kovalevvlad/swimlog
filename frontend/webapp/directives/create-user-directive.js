var app = angular.module('swimlog');

app.directive('createUser', function() {
    return {
        restrict: 'AE',
        scope: {
            onInsert: '='
        },
        templateUrl: 'templates/create-user-form.html'
    };
});