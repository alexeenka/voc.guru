(function (angular) {
    'use strict';

    angular.module('h4t-eng')
        .factory("socialNetworkFriendsService", SocialNetworkFriendsService);

    SocialNetworkFriendsService.$inject = ['$log', '$uibModal', '$state'];

    function SocialNetworkFriendsService($log, $uibModal, $state) {
        return {
            openModalSocialNetworkFriends: openModalSocialNetworkFriendsImpl
        };

        function openModalSocialNetworkFriendsImpl(friendsInfo) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: '/html/friends/social-networks/add-friends-modal.html',
                controller: SocialNetworkFriendsController,
                backdrop: 'static'
            });
            modalInstance.friendsInfo = friendsInfo;

            modalInstance.rendered.then(function(result) {});
            modalInstance.result.then(function (modalResult) {
                if (modalResult && modalResult.reload) {
                    $state.go($state.current, {}, {reload: true});
                }
            });
        }
    }

    SocialNetworkFriendsController.$inject = ['$scope','$log','$uibModalInstance','friendsService','modalService'];
    function SocialNetworkFriendsController($scope,$log,$uibModalInstance,friendsService,modalService) {
        $scope.friendsInfo = $uibModalInstance.friendsInfo;

        $scope.closeFriendsModal = function() {
            $uibModalInstance.close();
        };

        // paging information
        function updatePagingInfo() {
            if ($scope.friendsInfo) {
                var paging = $scope.friendsInfo.paging;
                if (paging) {
                    $scope.from = paging.page * paging.pageSize + 1;
                    $scope.to = paging.page * paging.pageSize + $scope.friendsInfo.data.length;
                }
            }
        }

        updatePagingInfo();

        $scope.modalPrevFriends = function () {
            var job;
            if ($scope.friendsInfo.type == 'FACEBOOK') {
                job = friendsService.prevFacebookFriends($scope.friendsInfo.paging.previous, $scope.friendsInfo.paging.page);
            }
            if ($scope.friendsInfo.type == 'VK') {
                job = friendsService.vkFriendsPage($scope.friendsInfo.paging.page - 1);
            }

            if (job) job.then(function (friendsInfo) {
                $scope.friendsInfo = friendsInfo;
                updatePagingInfo();
            });

        };

        $scope.modalNextFriends = function () {
            var job;
            if ($scope.friendsInfo.type == 'FACEBOOK') {
                job = friendsService.nextFacebookFriends($scope.friendsInfo.paging.next, $scope.friendsInfo.paging.page);
            }
            if ($scope.friendsInfo.type == 'VK') {
                job = friendsService.vkFriendsPage($scope.friendsInfo.paging.page + 1);
            }

            if (job) job.then(
                function (friendsInfo) {
                    $scope.friendsInfo = friendsInfo;
                    updatePagingInfo();
                }
            );
        };

        $scope.modalAddFriend = function(id) {
            var job;
            if ($scope.friendsInfo.type == 'FACEBOOK') {
                job = friendsService.addFacebookFriend(id);
            }
            if ($scope.friendsInfo.type == 'VK') {
                job = friendsService.addVkFriend(id);
            }

            if (job) job.then(function(response) {
                if (response.errorMsg) {
                    var modalOptions = {
                        showCloseButton: false,
                        actionButtonText: 'Понимаю',
                        headerText: 'Уведомление',
                        bodyText: response.errorMsg
                    };
                    modalService.showModal({}, modalOptions).then(function (result) {
                    });
                    return;
                }

                $uibModalInstance.close({reload:true});
            });
        };

        $scope.isMobile = function() {
            return window.mobilecheck();
        };
    }

}(window.angular));