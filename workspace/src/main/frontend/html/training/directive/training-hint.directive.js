(function () {
    'use strict';

    //** training-hint directive ** begin **//

    angular.module('h4t-eng.training').directive('trainingHint', TrainingHint);

    function TrainingHint() {
        return {
            restrict: 'E', // only matches element name
            scope: {
                label: '@', // simply reads the value (one-way binding)
                lang: '@', // simply reads the value (one-way binding)
                values: '=', // is two-way binding
                fontSize: '@' // simply reads the value (one-way binding)
            },
            templateUrl: '/html/training/directive/training-hint.directive.html',
            controller: TrainingHintController
        }
    }

    TrainingHintController.$inject = ['$scope', '$log'];

    function TrainingHintController($scope, $log) {
        if (!$scope.fontSize) $scope.fontSize = '14px';
        if (!$scope.lang) $scope.lang = 'EN';

        $scope.showMoreWordsFlags = [];
        $scope.showMoreWords = function (key) {
            return $scope.showMoreWordsFlags[key] && $scope.showMoreWordsFlags[key] == 1;
        };
        $scope.setFlagForShowMoreWords = function(key) {
            $scope.showMoreWordsFlags[key] = 1;
        }
    }
    //** training-hint directive ** end **//
})();