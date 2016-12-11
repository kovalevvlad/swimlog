var app = angular.module("swimlog");

app.factory('ConfigurationRepository', function() {
    return {
        getRootUri : function() { return "http://localhost:9090" },

        minUsernameLength: 5,

        maxUsernameLength: 15,

        minPasswordLength: 7,

        maxPasswordLength: 20,

        validUsernameRegex: /^[A-Za-z0-9_]*$/,

        validPasswordRegex: /^[\x00-\x7F]*$/
    }
});