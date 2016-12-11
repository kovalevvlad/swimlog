var app = angular.module('swimlog');

app.factory('Users', function(AuthorizedHttp, $http, Feedback, ConfigurationRepository) {
    return {
        allUsers: function(usersConsumer) {
            AuthorizedHttp
                .get('{0}/api/users'.format(ConfigurationRepository.getRootUri()))
                .then(function successCallback(response) {
                        usersConsumer(response.data);
                    },
                    Feedback.errorWithResponse)
        },

        login: function(username, password, userInfoConsumer) {
            $http
                .get(
                    '{0}/api/users/{1}'.format(ConfigurationRepository.getRootUri(), username),
                    { headers: AuthorizedHttp.basicAuthHeader(username, password) })
                .then(
                    function(response) {
                        userInfoConsumer(response.data)
                    },
                    Feedback.errorWithResponse)
        },

        userInfo: function(username, userInfoConsumer) {
            AuthorizedHttp
                .get('{0}/api/users/{1}'.format(ConfigurationRepository.getRootUri(), username))
                .then(
                    function(response) {
                        userInfoConsumer(response.data)
                    },
                    Feedback.errorWithResponse)
        },

        insertUser: function(username, password, successCallback) {
            AuthorizedHttp
                .post('{0}/api/users'.format(ConfigurationRepository.getRootUri()), {username: username, password: password})
                .then(function(response) { successCallback() }, Feedback.errorWithResponse)
        },

        deleteUser: function(userid, successCallback) {
            AuthorizedHttp
                .delete('{0}/api/users/{1}'.format(ConfigurationRepository.getRootUri(), userid))
                .then(function(response) { successCallback() }, Feedback.errorWithResponse)
        },

        updateUser: function(userid, newUsername, newPassword, newRole, successCallback) {
            AuthorizedHttp
                .put('{0}/api/users/{1}'.format(ConfigurationRepository.getRootUri(), userid), {username: newUsername, password: newPassword, role: newRole})
                .then(function(response) { successCallback() }, Feedback.errorWithResponse)
        }
    };
});