var app = angular.module('swimlog');

app.factory('DateUtil', function() {
    return {
        arrayDateToIsoString: function(arrayDate) {
            return new Date(Date.UTC(arrayDate[0], arrayDate[1] - 1, arrayDate[2])).toISOString().slice(0, 10);
        },

        isoStringToArrayDate: function(isoString) {
            var date = new Date(isoString);
            return [date.getUTCFullYear(), date.getUTCMonth() + 1, date.getUTCDate()];
        },

        dateToIsoString: function(date) {
            return date.toISOString().slice(0, 10);
        },

        isoStringDateIsValid: function(stringDate) {
            return !isNaN(new Date(stringDate).getTime());
        }
    };
});