var app = angular.module('swimlog');

app.factory('Swims', function(AuthorizedHttp, DateUtil, Users, ConfigurationRepository, Feedback) {

    var swimResponseToSwimArray = function(response) {
        return response.data.map(function(swim) {
            return {
                id: swim.id,
                date: DateUtil.arrayDateToIsoString(swim.date),
                durationSeconds: swim.durationSeconds,
                distanceKm: swim.distanceKm,
                userid: swim.userId
            };
        });
    };

    return {
        allSwims: function(fromDate, toDate, swimsConsumer) {
            var getParams = {};
            if (!isNaN(fromDate.getTime())) {
                getParams.fromDate = DateUtil.dateToIsoString(fromDate);
            }
            if (!isNaN(toDate.getTime())) {
                getParams.toDate = DateUtil.dateToIsoString(toDate);
            }

            AuthorizedHttp
                .get('{0}/api/swims'.format(ConfigurationRepository.getRootUri()), getParams)
                .then(
                    function successCallback(response) {
                        var swims = swimResponseToSwimArray(response);
                        Users.allUsers(function(users) {
                            var userMap = {};
                            users.forEach(function(user) {
                               userMap[user.id] = user;
                            });
                            swims.forEach(function(swim){
                               swim.user = userMap[swim.userid];
                            });
                            swimsConsumer(swims);
                        });
                    },
                    Feedback.errorWithResponse)
        },

        userSwims: function(userid, fromDate, toDate, swimsConsumer) {
            var getParams = {};
            if (!isNaN(fromDate.getTime())) {
                getParams.fromDate = DateUtil.dateToIsoString(fromDate);
            }
            if (!isNaN(toDate.getTime())) {
                getParams.toDate = DateUtil.dateToIsoString(toDate);
            }

            AuthorizedHttp
                .get('{0}/api/users/{1}/swims'.format(ConfigurationRepository.getRootUri(), userid), getParams)
                .then(
                    function successCallback(response) {
                        var swims = swimResponseToSwimArray(response);
                        swimsConsumer(swims);
                    },
                    Feedback.errorWithResponse);
        },

        updateSwim: function(swimid, userid, newDate, newDistance, newDuration, successCallback) {
            AuthorizedHttp.put(
                '{0}/api/swims/{1}'.format(ConfigurationRepository.getRootUri(), swimid),
                {
                    date: DateUtil.isoStringToArrayDate(newDate),
                    durationSeconds: newDuration,
                    distanceKm: newDistance,
                    userId: userid
                })
                .then( function(response) { successCallback(); }, Feedback.errorWithResponse)
        },

        insertSwim: function(userid, newDate, newDistanceKm, newDurationSeconds, successCallback) {
            AuthorizedHttp.post(
                '{0}/api/swims'.format(ConfigurationRepository.getRootUri()),
                {
                    date: DateUtil.isoStringToArrayDate(newDate),
                    durationSeconds: newDurationSeconds,
                    distanceKm: newDistanceKm,
                    userId: userid
                })
                .then(function(response) { successCallback(); }, Feedback.errorWithResponse)
        },

        deleteSwim: function(swimid, successCallback) {
            AuthorizedHttp
                .delete('{0}/api/swims/{1}'.format(ConfigurationRepository.getRootUri(), swimid))
                .then(function(response) { successCallback() }, Feedback.errorWithResponse)
        }
    };
});