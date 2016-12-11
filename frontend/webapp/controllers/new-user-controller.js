var app = angular.module("swimlog");

app.controller("NewUserController", function ($scope, Users, Feedback, ConfigurationRepository) {
    $scope.escape = function(text) {
        return text.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&");
    };

    $scope.config = ConfigurationRepository;
    $scope.passwordRegex = ConfigurationRepository.validPasswordRegex;
    $scope.usernameRegex = ConfigurationRepository.validUsernameRegex;

    $scope.insertUser = function(username, password, form) {
        Users.insertUser(
            username,
            password,
            function() {
                $scope.username = '';
                $scope.password = '';
                $scope.repeatedPassword = '';
                form.$setPristine();
                form.$setUntouched();

                if($scope.onInsert != null) {
                    $scope.onInsert();
                }

                Feedback.successWithMessage("User {0} created successfully!".format(username));
            });
    };
});