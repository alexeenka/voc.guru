/**
 * h4t-eng.
 *
 * Created by aalexeenka on 20/07/2015.
 */

angular.module('h4t-eng.create-knowledge', []).config(CreateKnowledgeProvider);

function CreateKnowledgeProvider($stateProvider) {
    $stateProvider.state('create-knowledge', {
        url: "/create-knowledge",
        templateUrl: '/html/create-knowledge/create-knowledge.html',
        data: {tabName: "create-knowledge"},
        controller: CreateKnowledgeController
    });
}

function CreateKnowledgeController($scope, $log, knowledgeService, modalService, counterService) {
    $scope.countsWord = counterService.countsWord;
    $scope.evalSlovo = counterService.evalSlovo;

    // Paging. Begin
    function initPaging() {
        $scope.filteredWords = [];
        $scope.totalItems = 0;
        $scope.maxSize = 5;
        $scope.pagingSize = 10;
        $scope.currentPage = {val:1};
        $scope.allWords = [];
    }

    $scope.updatePage = function () {
        $scope.totalItems = $scope.workWordList.length;

        var begin = $scope.pagingSize * ($scope.currentPage.val  - 1);
        var end = $scope.pagingSize * $scope.currentPage.val;

        if (begin > $scope.totalItems) {
            $scope.filteredWords = [];
        } else if (begin < $scope.totalItems && end > $scope.totalItems) {
            $scope.filteredWords = $scope.workWordList.slice(begin, $scope.totalItems);
        } else {
            $scope.filteredWords = $scope.workWordList.slice(begin, end);
        }
    };
    initPaging();
    // Paging. End.


    // Filter List
    $scope.filterEngVal = {val:""};
    $scope.changeEngVal = function () {
        $scope.workWordList = $scope.wordList.filter(function (word) {
            return word.engVal.toUpperCase().lastIndexOf($scope.filterEngVal.val.toUpperCase(), 0) === 0;
        });
        $scope.currentPage = {val:1};
        $scope.updatePage();
    };

    $scope.alphabet = {
        letter : 'alphabet_choice',
        callbackLoadWords: function() {
            knowledgeService.loadReadyWords($scope.alphabet.letter).then(function (result) {
                $scope.wordList = result;

                $scope.wordList.sort(function(w1, w2){
                    var val1 = w1.engVal.toUpperCase(), val2 = w2.engVal.toUpperCase();
                    if (val1 < val2) //sort string ascending
                        return -1;
                    if (val1 > val2)
                        return 1;
                    return 0; //default return value (no sorting)
                });

                for (var i=0; i<$scope.wordList.length; i++) {
                    $scope.wordList[i]['guiOrder'] = i;
                }

                $scope.workWordList = $scope.wordList;
                $scope.currentPage = {val:1};
                $scope.updatePage();
            });
        },
        clearFilter: function() {
            $scope.filterEngVal.val = "";
        }
    };

    $scope.prevEngVal = "";

    // NEW-WORDS: begin
    $scope.wordList = [];
    $scope.pagingState = '';
    // delete word action
    $scope.delete_word = function(engVal) {
        var modalOptions = {
            closeButtonText: 'Cancel',
            actionButtonText: 'Yes',
            headerText: 'Вы действительно хотите удалить слово?',
            bodyText: 'Все введенные данные будут потеряны?'
        };

        modalService.showModal({}, modalOptions).then(function (result) {
            knowledgeService.deleteReadyWord(engVal, $scope);
        });
    };
    // edit word action
    // NEW-WORDS: end

    $scope.getImgURL = knowledgeService.getImgURL;
}