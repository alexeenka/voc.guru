/**
 * TopUsers main state+controller
 */
(function (angular) {
    'use strict';

    angular.module('h4t-eng.top-users', []).config(TopUsersStateProvider);

    function TopUsersStateProvider($stateProvider) {
        $stateProvider.state('top-users', {
                url: "/top-users",
                templateUrl: '/html/top-users/top-users-main.html',
                data: {tabName: "top-users"},
                controller: TopUsersController,
                params: {}
            }
        )
    }

    TopUsersController.$inject = [
        '$scope', '$state', '$stateParams', '$timeout', '$log', '$uibModal', 'topUsersService'
    ];

    function TopUsersController(
        $scope, $state, $stateParams, $timeout, $log, $uibModal, topUsersService
    ) {
        topUsersService.getTopUsers().then(function (topUsers) {
            $scope.topUsers = topUsers;
        });

        var months = ["ЯНВАРЬ", "ФЕВРАЛЬ", "МАРТ", "АПРЕЛЬ", "МАЙ", "ИЮНЬ", "ИЮЛЬ", "АВГУСТ", "СЕНТЯБРЬ", "ОКТЯБРЬ", "НОЯБРЬ", "ДЕКАБРЬ"];
        $scope.currentMonth = months[parseInt(moment().format('MM')) - 1];
    }

    //** top-user directive ** begin **//
    angular.module('h4t-eng.top-users').directive('topUser', TopUserDirective);

    function TopUserDirective() {
        return {
            restrict: 'E', // only matches element name
            scope: {
                topUser: '=' // is two-way binding
            },
            templateUrl: '/html/top-users/top-user.directive.html',
            controller: TopUserDirectiveController
        }
    }

    TopUserDirectiveController.$inject = ['$scope', '$state', 'friendsService', 'modalService'];

    function TopUserDirectiveController($scope, $state, friendsService, modalService) {
        $scope.transformLastName = function(lastName) {
            if (lastName) {
                return lastName.substr(0,1).toUpperCase() + ".";
            }
            return "";
        };
        $scope.workHint = function (work) {
          var hour = parseInt(work / 3600);
          var min = parseInt((work % 3600) / 60);
          var sec = (work % 60);

          var text = "";
          if (hour > 0) {
              text = hour + "ч" + " " + min + "мин" + " " + sec + "сек";
          } else if (min > 0) {
              text = min + "мин" + " " + sec + "сек";
          } else {
              text = sec + "сек";
          }

          return text;
        };
        $scope.addUserAsFriend = function (userId) {
            var job = friendsService.addFriend(userId);

            if (job)
                job.then(function(response) {
                if (response.errorMsg) {
                    var modalOptions = {
                        showCloseButton: false,
                        actionButtonText: 'Понимаю',
                        headerText: 'Уведомление',
                        bodyText: response.errorMsg
                    };
                    modalService.showModal({}, modalOptions).then(function (result) {});
                } else {
                    $state.go("friends")
                }
            });
        }
    }
    //** top-user directive ** end **//

    //** top-user header directive ** begin **//
    angular.module('h4t-eng.top-users').directive('topUserHeader', TopUserHeaderDirective);

    function TopUserHeaderDirective() {
        return {
            restrict: 'E', // only matches element name
            scope: {
                title: '@' // is two-way binding
            },
            templateUrl: '/html/top-users/top-user-header.directive.html',
            controller: TopUserHeaderDirectiveController
        }
    }

    TopUserHeaderDirectiveController.$inject = ['$scope'];

    function TopUserHeaderDirectiveController($scope) {
    }
    //** top-user directive ** end **//



}(window.angular));