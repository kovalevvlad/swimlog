var app = angular.module("swimlog");

app.controller("MainController", function (UserState, $scope) {
    $scope.role = UserState.getState().role;
    $scope.currentPage = 'views/my-swims.html';
    $scope.goTo = function(page) {
      $scope.currentPage = page;
    };
});