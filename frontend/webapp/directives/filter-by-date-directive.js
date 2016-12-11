var app = angular.module('swimlog');

app.directive('filterByDate', function() {
    return {
        restrict: 'AE',
        scope: {
            slOnClick: '='
        },
        templateUrl: 'templates/filter-by-date-template.html'
    };
});