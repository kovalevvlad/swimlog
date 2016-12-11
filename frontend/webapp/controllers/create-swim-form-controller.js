var app = angular.module("swimlog");

app.controller("CreateSwimFormController", function ($scope, Users, DateUtil) {
    if (!$scope.slHideUsername) {
        Users.allUsers(function (users) {
            $scope.existingUsers = new Set(users.map(function (user) {
                return user.username;
            }));
        });

        $scope.validUser = function (user) {
            return $scope.existingUsers.has(user)
        };
    }

    $scope.dateIsValid = DateUtil.isoStringDateIsValid;

    $scope.onSuccessCallbackFromForm = function(form) {
        return function() {
            if (!$scope.slHideUsername) {
                $scope.username = '';
            }
            $scope.date = '';
            $scope.distance = '';
            $scope.duration = '';
            form.$setPristine();
            form.$setUntouched();
        }
    };
});
