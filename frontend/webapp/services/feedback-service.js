var app = angular.module('swimlog');

app.factory('Feedback', function(toastr) {
    return {
        errorWithResponse: function(response) {
            if(response.status == -1) {
                toastr.error("Server seems to be down");
            }
            else {
                if (response.data == null) {
                    toastr.error("{0} ({1})".format(response.statusText, response.status), response.data.error);
                }
                else {
                    toastr.error(response.data.error);
                }
            }
        },

        errorWithMessage: function(message) {
            toastr.error(message);
        },

        successWithMessage: function(message) {
            toastr.success(message);
        }
    };
});