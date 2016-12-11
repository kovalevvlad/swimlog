var app = angular.module("swimlog");

app.controller("FilterByDateController", function ($scope, DateUtil) {
    $scope.dateRegex = /^(|\d{4}-\d{2}-\d{2})$/;

    $scope.dateIsValid = function(dateString) {
        return (dateString || '') == '' || DateUtil.isoStringDateIsValid(dateString);
    };

    $scope.fieldNotValid = function(field) {
        return field.$invalid && field.$touched;
    };
});