var app = angular.module("swimlog");

app.controller("MySwimsController", function ($scope, UserState, Swims, Feedback) {

    $scope.currentUserName = UserState.getState().username;

    $scope.currentUserId = UserState.getState().userid;

    $scope.getMySwims = function(fromDateString, toDateString) {
        var fromDate = new Date(fromDateString);
        var toDate = new Date(toDateString);

        Swims.userSwims(UserState.getState().userid, fromDate, toDate, function (swims) {
            $scope.mySwims = swims;

            var totalDistance = swims.map(function(swim){ return swim.distanceKm }).reduce(function(a, b) { return a + b }, 0);
            var totalTime = swims.map(function(swim){ return swim.durationSeconds }).reduce(function(a, b) { return a + b }, 0);

            // average speed in KM/H
            $scope.averageSpeed = totalDistance / totalTime * 3600;

            if (swims.length > 1) {
                var minMillis = Math.min.apply(Math, swims.map(function(swim){ return new Date(swim.date).getTime() }));
                var maxMillis = Math.max.apply(Math, swims.map(function(swim){ return new Date(swim.date).getTime() }));
                var millisInWeek = 1000 * 60 * 60 * 24 * 7;
                var totalWeeks = Math.ceil((maxMillis - minMillis) / millisInWeek);
                $scope.distancePerWeek = totalDistance / totalWeeks;
            }
            else {
                $scope.distancePerWeek = totalDistance;
            }
        });
    };

    $scope.updateSwim = function(swimid, newDate, newDistance, newDuration) {
        Swims.updateSwim(swimid, UserState.getState().userid, newDate, newDistance, newDuration, function() {
            Feedback.successWithMessage("swim successfully updated");
        })
    };

    $scope.insertSwim = function(username, date, distance, duration, successCallback) {
        Swims.insertSwim(UserState.getState().userid, date, distance, duration, function() {
            $scope.getMySwims($scope.fromDate, $scope.toDate);
            successCallback();
            Feedback.successWithMessage("swim successfully inserted");
        });
    };

    $scope.deleteSwim = function(swimid) {
        Swims.deleteSwim(swimid, function() {
            $scope.getMySwims($scope.fromDateString, $scope.toDateString);
        });
    };

    $scope.getMySwims();
});