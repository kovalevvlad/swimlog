var app = angular.module('swimlog');

app.factory('UserState', function() {
    var userState = {
        username: null,
        password: null,
        userid: null,
        role: null
    };

    return {
        setState: function(username, password, userid, role) {
            userState.password = password;
            userState.username = username;
            userState.userid = userid;
            userState.role = role;
        },

        getState: function() { return userState }
    };
});