/**
 * WordSet, list of word
 */
(function (angular) {
    'use strict';


    angular.module('h4t-eng.word-set.list', []).config(WordSetListProvider);

    function WordSetListProvider($stateProvider) {
        $stateProvider.state('word-set-list-state', {
            url: "/word-set-list?listId",
            params: {},
            templateUrl: '/html/word-set/list/word-set.list.html',
            data: {tabName: "word-set"},
            controller: WordSetListController
        });
    }


    WordSetListController.$inject = [
        '$scope', '$state', '$stateParams', '$http', '$timeout', '$log', '$uibModal', 'wordSetService', 'counterService', 'commonService'
    ];

    function WordSetListController($scope, $state, $stateParams, $http, $timeout, $log, $uibModal, wordSetService, counterService, commonService) {
        $scope.isWordSetUser = false;
        wordSetService.isWordSetUser().then(function (result) {
            $scope.isWordSetUser = result;
        });

        $scope.wordsModel = [];
        $scope.listId = $stateParams.listId;

        $scope.addSingleWord = function (singleWord) {
            $http.post('/rest/word-set/add-single-word', {listId:$stateParams.listId, word:singleWord}).success(function (result) {
                if (result === 1) {
                    commonService.playApplause();
                    counterService.updateCounts();
                }

                var modalInstance = $uibModal.open({
                    animation: true,
                    templateUrl: '/html/word-set/list/added-single-word.html',
                    controller: AddedSingleWordController,
                    backdrop: 'static',
                    resolve: {
                        singleWord: function () {return singleWord;},
                        added: function() {return result;}
                    }
                });
                modalInstance.rendered.then(function() {
                });
                modalInstance.result.then(function (modalResult) {
                });
            });
        };

        $scope.loading = true;

        // Paging. Begin
        function initPaging() {
            $scope.filteredWords = [];
            $scope.totalItems = 0;
            $scope.maxSize = 5;
            $scope.pagingSize = 12;
            $scope.currentPage = 1;
            $scope.allWords = [];
        }

        $scope.updatePage = function () {
            $scope.totalItems = $scope.allWords.length;

            var begin = $scope.pagingSize * ($scope.currentPage  - 1);
            var end = $scope.pagingSize * $scope.currentPage;

            if (begin > $scope.totalItems) {
                $scope.filteredWords = [];
            } else if (begin < $scope.totalItems && end > $scope.totalItems) {
                $scope.filteredWords = $scope.allWords.slice(begin, $scope.totalItems);
            } else {
                $scope.filteredWords = $scope.allWords.slice(begin, end);
            }


            $scope.wordsModel = [];
            var step = 3;
            for (var i=0,n=$scope.filteredWords.length; i<n; i+=step) {
                var iArr = [];
                for (var j=0; j<step; j++) {
                    if (i + j < n) {
                        iArr.push(prepareValues($scope.filteredWords[i + j]));
                    }
                }
                $scope.wordsModel.push(iArr);
            }
        };
        initPaging();
        // Paging. End.

        $http.post('/rest/word-set/list', {listId:$stateParams.listId}).success(function(response) {
            $log.debug(currentTime() + "load-list-word", response);
            $scope.allWords = response;
            $log.debug(currentTime() + "words-model", $scope.wordsModel);
            $scope.updatePage();
            $scope.loading = false;
        });

        function prepareValues(word) {
            word['formatRus'] = commonService.joinValues(word.rusValues);
            return word;
        }
    }

    AddedSingleWordController.$inject = ['$scope','$uibModalInstance', 'singleWord', 'added', 'commonService'];
    function AddedSingleWordController($scope,$uibModalInstance,singleWord,added,commonService) {
        $scope.singleWord = singleWord;
        $scope.added = added;
        $scope.closePopupWindow = function() {$uibModalInstance.dismiss()};
    }


}(window.angular));
