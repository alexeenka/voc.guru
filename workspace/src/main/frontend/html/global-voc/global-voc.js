/**
 * GlobalVoc main state+controller
 */
(function (angular) {
    'use strict';

    angular.module('h4t-eng.global-voc', []).config(GlobalVocStateProvider);

    function GlobalVocStateProvider($stateProvider) {
        $stateProvider.state('global-voc', {
                url: "/global-voc",
                templateUrl: '/html/global-voc/global-voc-main.html',
                data: {tabName: "global-voc"},
                controller: GlobalVocController,
                params: {
                    paramGVocSearchWord:''
                }
            }
        )
    }

    GlobalVocController.$inject = [
        '$scope', '$state', '$stateParams', '$timeout', '$log', '$uibModal', 'globalVocService'
    ];

    function GlobalVocController(
        $scope, $state, $stateParams, $timeout, $log, $uibModal, globalVocService
    ) {

        $scope.searchWord = '';
        $scope.vocSize = "";
        globalVocService.getGlobalVocSize().then(function (result) {
            $scope.vocSize = result.size;
        });

        var autocompleteWords = $scope.autocompleteWords = [];
        $scope.$watch("searchWord",
            function(newValue, oldValue) {
                if (newValue == undefined) return;
                if (newValue == oldValue) return;

                if (newValue.length == 0) {
                    // clear array
                    autocompleteWords.splice(0, autocompleteWords.length);
                    return;
                }

                globalVocService.autocompleteSearch(newValue).then(function (foundWords) {
                    // clear array
                    autocompleteWords.splice(0, autocompleteWords.length);
                    // add new values
                    foundWords.forEach(function (iWord) {
                        autocompleteWords.push(iWord);
                    });
                });
            }
        );
        if ($stateParams.paramGVocSearchWord != undefined && $stateParams.paramGVocSearchWord.length > 0) {
            $timeout(function () {$scope.searchWord = $stateParams.paramGVocSearchWord;}, 400);
        }

        $scope.addAutocompleteWord = function (chosenWord) {
            $log.debug(currentTime() + "AddAutocompleteWord:", chosenWord);
            globalVocService.loadWord(chosenWord).then(function (wordsByAuthor) {
                if (wordsByAuthor.length > 0) {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: '/html/global-voc/add-autocomplete-word.html',
                        controller: AddAutoCompleteWordController,
                        backdrop: 'static',
                        resolve: {
                            chosenWord: function() {return chosenWord},
                            wordsByAuthor: function() {return wordsByAuthor}
                        }
                    });
                    modalInstance.rendered.then(function() {

                    });
                    modalInstance.result.then(function (modalResult) {
                        $log.debug(currentTime() + 'Modal-result, autocompleteWord', modalResult);
                        $scope.searchWord = '';
                        autocompleteWords.splice(0, autocompleteWords.length);
                        $scope.copyWord(modalResult.author, modalResult.word);
                    });
                }
            }
            );
        };

        $scope.active = 0;
        var words = $scope.words = [];

        function loadSlideUnknownWords() {
            words.splice(0,words.length);
            globalVocService.getSlideUnknownWords().then(function (rWords) {
                words.splice(0,words.length);
                $scope.active = 0;

                rWords.forEach(function (iWord) {
                    words.push(iWord);
                });
            });
        }

        loadSlideUnknownWords();

        $scope.nextSlideWords = function () {
            loadSlideUnknownWords();
        };

        $scope.copyWord = function (author, word) {
            globalVocService.copyWord(author, word).then(function() {
                loadSlideUnknownWords();
            });
        }
    }

    AddAutoCompleteWordController.$inject = ['$scope','$log','$uibModalInstance', 'wordsByAuthor','chosenWord'];
    function AddAutoCompleteWordController($scope,$log,$uibModalInstance,wordsByAuthor,chosenWord) {
        $scope.wordsByAuthor = wordsByAuthor;
        $scope.chosenWord = chosenWord;

        $scope.closePopupWindow = function() {$uibModalInstance.dismiss()};
        $scope.copyPopupWord = function(author, word) {$uibModalInstance.close({
            author:author, word:word
        })};
    }


    }(window.angular));