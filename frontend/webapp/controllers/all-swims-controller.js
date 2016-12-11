var app = angular.module("swimlog");

app.controller("AllSwimsController", function ($scope, Swims, Users, Feedback) {

    $scope.getSwims = function(fromDateString, toDateString) {
        var fromDate = new Date(fromDateString);
        var toDate = new Date(toDateString);
        Swims.allSwims(fromDate, toDate, function (swims) {
            $scope.swims = swims;
        });
    };

    $scope.updateSwim = function(swimid, userid, newDate, newDistance, newDuration) {
        Swims.updateSwim(swimid, userid, newDate, newDistance, newDuration, function() {
           Feedback.successWithMessage("swim successfully updated");
        });
    };

    $scope.insertSwim = function(username, date, distance, duration, successCallback) {
        Users.userInfo(username, function(userInfo) {
            Swims.insertSwim(userInfo.id, date, distance, duration, function() {
                $scope.getSwims($scope.fromDate, $scope.toDate);
                successCallback();
                Feedback.successWithMessage("swim successfully inserted");
            });
        });
    };

    $scope.deleteSwim = function(swimid) {
        Swims.deleteSwim(swimid, function() {
            $scope.getSwims($scope.fromDate, $scope.toDate)
        });
    };

    $scope.getSwims();
});