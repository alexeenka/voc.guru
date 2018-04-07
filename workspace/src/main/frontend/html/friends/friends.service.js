(function (angular) {
    'use strict';

    angular.module('h4t-eng.friends').factory("friendsService", FriendsService);

    FriendsService.$inject = ['$http','$log','$q', 'trainingCalendarService'];

    function FriendsService($http, $log, $q, trainingCalendarService) {
        return {
            getFriendsInfo: getFriendsInfoImpl,

            getFacebookFriends: getFacebookFriendsImpl,
            prevFacebookFriends: prevFacebookFriendsImpl,
            nextFacebookFriends: nextFacebookFriendsImpl,

            getVkFriends: getVkFriendsImpl,
            vkFriendsPage: vkFriendsPageImpl,

            addFacebookFriend: addFacebookFriendImpl,
            addVkFriend: addVkFriendImpl,
            unsubscribeFriend: unsubscribeFriendImpl,

            // implementation for top users
            addFriend: addFriendImpl
        };

        function unsubscribeFriendImpl(friendUid) {
            var job = $q.defer();
            $http.post('/rest/friends/remove-friend', {uid:friendUid}).success(function(response) {
                $log.debug(currentTime() + "unsubscribeFriendImpl", response);
                job.resolve(response);
            });
            return job.promise;
        }

        function addFacebookFriendImpl(fid) {
            var job = $q.defer();
            $http.post('/rest/friends/add-facebook-friend', {fid:fid}).success(function(response) {
                $log.debug(currentTime() + "addFacebookFriendImpl", response);
                job.resolve(response);
            });
            return job.promise;
        }

        function addVkFriendImpl(sid) {
            var job = $q.defer();
            $http.post('/rest/friends/add-vk-friend', {sid:sid}).success(function(response) {
                $log.debug(currentTime() + "addVkFriendImpl", response);
                job.resolve(response);
            });
            return job.promise;
        }

        function addFriendImpl(sid) {
            var job = $q.defer();
            $http.post('/rest/friends/add-friend', {sid:sid}).success(function(response) {
                $log.debug(currentTime() + "addFriendImpl", response);
                job.resolve(response);
            });
            return job.promise;
        }

        function getFacebookFriendsImpl() {
            var job = $q.defer();
            $http.post('/rest/friends/facebook-friends-list').success(function(response) {
                $log.debug(currentTime() + "getFacebookFriendsImpl", response);
                job.resolve(response);
            });
            return job.promise;
        }

        function prevFacebookFriendsImpl(linkInfo, page) {
            var job = $q.defer();
            $http.post(
                '/rest/friends/facebook-friends-prev-page', {
                    prev: linkInfo,
                    page: page
                }
            ).success(function(response) {
                $log.debug(currentTime() + "prevFacebookFriendsImpl", response);
                job.resolve(response);
            });
            return job.promise;
        }

        function nextFacebookFriendsImpl(linkInfo, page) {
            var job = $q.defer();
            $http.post('/rest/friends/facebook-friends-next-page',
                {
                    next: linkInfo,
                    page: page
                }
            ).success(function(response) {
                $log.debug(currentTime() + "nextFacebookFriendsImpl", response);
                job.resolve(response);
            });
            return job.promise;
        }

        function getVkFriendsImpl() {
            var job = $q.defer();
            $http.post('/rest/friends/vk-friends-list').success(function(response) {
                $log.debug(currentTime() + "getVkFriendsImpl", response);
                job.resolve(response);
            });
            return job.promise;
        }

        function vkFriendsPageImpl(page) {
            var job = $q.defer();
            $http.post(
                '/rest/friends/vk-friends-page', {
                    page: page
                }
            ).success(function(response) {
                $log.debug(currentTime() + "vkFriendsPageImpl", response);
                job.resolve(response);
            });
            return job.promise;
        }

        function getFriendsInfoImpl() {
            var job = $q.defer();
            
            var calendarParameters = trainingCalendarService.calendarParameters();
            
            $http.post('/rest/friends/friends-info',
                {
                    year:calendarParameters.year,
                    dayOfYear:calendarParameters.dayOfYear,
                    startDay:calendarParameters.startDay,
                    count:calendarParameters.countPlusToday
                }
            ).success(function(response) {
                $log.debug(currentTime() + "friends-info.", response);
                job.resolve({"friends":response});
            });
            return job.promise;
        }

    }
}(window.angular));