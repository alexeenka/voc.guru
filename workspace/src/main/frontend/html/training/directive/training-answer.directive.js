(function () {
    'use strict';

    //** training-answer directive ** begin **//
    angular.module('h4t-eng.training').directive('trainingAnswer', TrainingAnswer);

    function TrainingAnswer() {
        return {
            restrict: 'E', // only matches element name
            scope: {
                values: '=', // is two-way binding
                value: '=', // is two-way binding
                lang: '@', // is two-way binding
                wordType: '=' // is two-way binding
            },
            templateUrl: '/html/training/directive/training-answer.directive.html',
            controller: TrainingAnswerController
        }
    }

    TrainingAnswerController.$inject = ['$scope'];

    function TrainingAnswerController($scope) {
        $scope.isSingleWordAnswer = function() {
            return $scope.value;
        };
        if (!$scope.lang) $scope.lang = 'EN';

        // work with 'show more' link
        $scope.showMoreWordsFlags = [];
        $scope.showMoreWords = function (key) {
            return $scope.showMoreWordsFlags[key] && $scope.showMoreWordsFlags[key] == 1;
        };
        $scope.setFlagForShowMoreWords = function(key) {
            $scope.showMoreWordsFlags[key] = 1;
        }
    }
    //** training-answer directive ** end **//
})();