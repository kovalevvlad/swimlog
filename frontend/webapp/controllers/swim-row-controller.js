var app = angular.module("swimlog");

app.controller("SwimRowController", function ($scope, Swims, DateUtil) {
    $scope.editing = false;
    $scope.dateIsValid = DateUtil.isoStringDateIsValid;

    $scope.initial = {
        duration: $scope.slDuration,
        distance: $scope.slDistance,
        date: $scope.slDate
    };

    $scope.resetSwimState = function() {
        $scope.slDuration = $scope.initial.duration;
        $scope.slDistance = $scope.initial.distance;
        $scope.slDate = $scope.initial.date;
    };

    $scope.deleteSwim = function() {
        Swims.deleteSwim($scope.slSwimId, function() {
            $scope.slOnUpdate();
        });
    };

    $scope.updateSwim = function() {
        Swims.updateSwim($scope.slSwimId, $scope.slUserId, $scope.slDate, $scope.slDistance, $scope.slDuration, $scope.slOnUpdate);
    };
});