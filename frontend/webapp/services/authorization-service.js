var app = angular.module('swimlog');

app.factory('AuthorizedHttp', function ($http, UserState, $base64) {

    var authorizedHttpService = {
        get: function (url, params) {
            return $http.get(url, Object.assign({headers: userStateHeader()}, {params: params}));
        },
        post: function (url, data, config) {
            return $http.post(url, data, Object.assign({headers: userStateHeader()}, config));
        },
        put: function (url, data, config) {
            return $http.put(url, data, Object.assign({headers: userStateHeader()}, config));
        },
        delete: function (url, config) {
            return $http.delete(url, Object.assign({headers: userStateHeader()}, config));
        },
        basicAuthHeader: function (username, password) {
            var auth = $base64.encode("{0}:{1}".format(username, password));
            return {"Authorization": "Basic " + auth};
        }
    };

    var userStateHeader = function() { return authorizedHttpService.basicAuthHeader(UserState.getState().username, UserState.getState().password) };

    return authorizedHttpService

});