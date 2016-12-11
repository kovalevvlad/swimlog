var app = angular.module("swimlog");

app.controller("UserEditingController", function ($scope) {
    var originalUsername = $scope.user.username;
    var originalRole = $scope.user.role;

    $scope.resetUserState = function() {
        $scope.user.username = originalUsername;
        $scope.user.role = originalRole;
        $scope.user.password = '';
    };
});