var app = angular.module("swimlog", ['base64', 'ngRoute', 'toastr']);

app.config(function($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'views/login.html'
        })
        .when('/main', {
            templateUrl: 'views/main.html'
        })
});

app.config(function(toastrConfig) {
    angular.extend(toastrConfig, {
        autoDismiss: true,
        positionClass: 'toast-top-center',
        maxOpened: 1
    });
});