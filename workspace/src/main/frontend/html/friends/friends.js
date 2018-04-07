/**
 * User Settings
 */
(function (angular) {
    'use strict';

    angular.module('h4t-eng.friends', []).config(FriendsStateProvider);

    function FriendsStateProvider($stateProvider) {
        $stateProvider.state('friends', {
                url: "/friends",
                templateUrl: '/html/friends/friends-main.html',
                data: {tabName: "friends"},
                controller: FriendsController
            }
        )
    }

    FriendsController.$inject = [
        '$scope', '$state', '$log',
        'commonService', 'friendsService', 'counterService', 'trainingCalendarService', 'socialNetworkFriendsService'
    ];

    function FriendsController(
        $scope, $state, $log,
        commonService, friendsService, counterService, trainingCalendarService, socialNetworkFriendsService
    ) {
        $scope.showFacebookFriends = function () {
          friendsService.getFacebookFriends().then(
              function (friends) {
                  socialNetworkFriendsService.openModalSocialNetworkFriends(friends);
              }
          );
        };

        $scope.showVkFriends = function () {
          friendsService.getVkFriends().then(
              function (friends) {
                  socialNetworkFriendsService.openModalSocialNetworkFriends(friends);
              }
          );
        };

        $scope.evalSlovo = counterService.evalSlovo;
        $scope.friends = [];
        getFriends();
        
        function getFriends() {
            friendsService.getFriendsInfo().then(function(response) {
                $scope.friends = response.friends;

                $scope.friends.forEach(function (friend) {
                    friend['trainingCalendarUi'] = trainingCalendarService.emptyTrainingCalendar();
                    trainingCalendarService.initTrainingCalendar(friend['trainingCalendarUi'], friend.trainingCalendar);

                });
                $scope.pageInit = true;
            });
        }
        
        $scope.min = function(time) {
            return parseInt(time / 60);
        };

        $scope.sec = function(time) {
            return parseInt(time % 60);
        };
        
        $scope.percent = function(time) {
            return parseInt(time * 100 / 1200);
        };

        $scope.refreshInfo = function() {
            getFriends();
        };

        $scope.unsubscribeFriend = function (friendUid) {
            friendsService.unsubscribeFriend(friendUid).then(function(response) {
                $state.go($state.current, {}, {reload: true});
            });
        };

        commonService.userInfo().then(function (userInfo) {
            $scope.userFbLink = userInfo.fbLink;
            $scope.userVkLink = userInfo.vkLink;
        });

    }

}(window.angular));