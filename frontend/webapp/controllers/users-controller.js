var app = angular.module("swimlog");

app.controller("UsersController", function ($scope, Users, Feedback, ConfigurationRepository) {
    $scope.roles = ["Admin", "Manager", "User"];

    $scope.config = ConfigurationRepository;
    $scope.passwordRegex = ConfigurationRepository.validPasswordRegex;
    $scope.usernameRegex = ConfigurationRepository.validUsernameRegex;

    $scope.reloadUsers = function() {
        Users.allUsers(function (users) {
            $scope.users = users;
        });
    };

    $scope.updateUser = function(userid, newUsername, newPassword, newRole) {
        Users.updateUser(userid, newUsername, newPassword, newRole, function() {
            $scope.reloadUsers();
            Feedback.successWithMessage("{0} updated successfully".format(newUsername));
        })
    };

    $scope.deleteUser = function(userid) {
        Users.deleteUser(userid, $scope.reloadUsers)
    };

    $scope.reloadUsers();
});