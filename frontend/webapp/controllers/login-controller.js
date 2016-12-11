var app = angular.module("swimlog");

app.controller("LoginController", function ($scope, UserState, Users, $location) {
    $scope.doLogin = function(username, password) {
        Users.login(username, password, function(userInfo) {
            UserState.setState(username, password, userInfo.id, userInfo.role);
            $location.path('/main');
        });
    };
});