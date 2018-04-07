/**
 * WordSet main state+controller
 */
(function (angular) {
    'use strict';

    angular.module('h4t-eng.word-set', []).config(WordSetStateProvider);

    function WordSetStateProvider($stateProvider) {
        $stateProvider.state('word-set', {
                url: "/word-set",
                templateUrl: '/html/word-set/word-set-main.html',
                data: {tabName: "word-set"},
                controller: WordSetController,
                params: {}
            }
        )
    }

    WordSetController.$inject = [
        '$scope', '$state', '$stateParams', '$timeout', '$log', '$uibModal', 'wordSetService', 'modalService', 'counterService', 'commonService'
    ];

    function WordSetController(
        $scope, $state, $stateParams, $timeout, $log, $uibModal, wordSetService, modalService, counterService, commonService
    ) {
        $scope.ogdenHint = function() {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: '/html/word-set/ogden-hint.html',
                controller: OgdenHintController,
                backdrop: 'static',
                resolve: {}
            });
            modalInstance.rendered.then(function() {
            });
            modalInstance.result.then(function (modalResult) {
            });
        };

        $scope.add10word = function (listId) {
            wordSetService.add10Words(listId).then(function (result) {
                if (result.length > 0) {
                    commonService.playApplause();
                    counterService.updateCounts();
                }

                var modalInstance = $uibModal.open({
                    animation: true,
                    templateUrl: '/html/word-set/added-words.html',
                    controller: AddedWordController,
                    backdrop: 'static',
                    resolve: {wordList: function () {return result;}}
                });
                modalInstance.rendered.then(function() {
                });
                modalInstance.result.then(function (modalResult) {
                });
            });
        }
    }

    OgdenHintController.$inject = ['$scope','$uibModalInstance'];
    function OgdenHintController($scope,$uibModalInstance) {
        $scope.closePopupWindow = function() {$uibModalInstance.dismiss()};
    }

    AddedWordController.$inject = ['$scope','$uibModalInstance', 'wordList', 'commonService'];
    function AddedWordController($scope,$uibModalInstance,wordList,commonService) {
        $scope.wordList = wordList;
        $scope.wordListStr = commonService.joinValues(wordList);
        $scope.closePopupWindow = function() {$uibModalInstance.dismiss()};
    }

}(window.angular));