(function() {
    'use strict';

    angular
        .module('h4t-eng')
        .directive('sayText',  SayText);


    function SayText() {
        return {
            restrict: 'E',
            scope: {
                text: '=',
                lang: '@'
            },
            templateUrl: '/html/directive/say-text.directive.html',
            controller: SayTextController
        }
    }

    SayTextController.$inject = ['$scope','speechSynthesisService'];

    function SayTextController($scope,speechSynthesisService) {
        if (!$scope.lang) $scope.lang = 'EN';

        $scope.speak = function() {
            if ('EN' == $scope.lang) {
                speechSynthesisService.sayEngText($scope.text);
            }
            if ('RU' == $scope.lang) {
                speechSynthesisService.sayRuText($scope.text);
            }
        };

        $scope.isInitSpeech = false;
        speechSynthesisService.getInitVoiceJob().then(function() {
            $scope.isInitSpeech = true;
        });
    }
})();