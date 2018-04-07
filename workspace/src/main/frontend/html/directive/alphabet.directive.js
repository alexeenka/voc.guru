(function() {
    'use strict';

    angular
        .module('h4t-eng')
        .directive('alphabet',  Alphabet);


    function Alphabet() {
        return {
            restrict: 'E',
            scope: {
                alphabet: '='
            },
            templateUrl: '/html/directive/alphabet.directive.html',
            controller: AlphabetController
        }
    }

    AlphabetController.$inject = ['$scope', '$timeout', 'commonService'];

    function AlphabetController($scope, $timeout, commonService) {

        // alphabet
        var alphabet = [
            'A','B','C','D','E',
            'F','G','H','I','J',
            'K','L','M','N','O',
            'P','Q','R','S','T',
            'U','V','W','X','Y',
            'Z'];

        var times = [
            1.37, 2.13, 3.09, 4.02, 4.95,
            5.65, 6.49, 7.33, 8.11, 8.99,
            9.87, 10.73, 11.5, 12.32, 13.12,
            13.8, 14.61, 15.5, 16.25, 17.08,
            17.86, 18.66, 19.54, 20.53, 21.37,
            22.21, 23.5];

        var audioPause;

        $scope.play = function(letter) {
            var index = alphabet.indexOf(letter);
            if (index == -1) return;
            $scope.alphabet.letter = letter;
            $scope.alphabet.callbackLoadWords();
            $scope.alphabet.clearFilter();

            var alphabetAudio = document.getElementById("alphabetAudio");
            alphabetAudio.currentTime = times[index];
            alphabetAudio.pause();
            if (audioPause) $timeout.cancel(audioPause);
            audioPause = $timeout(function() {alphabetAudio.pause();}, (times[index + 1] - times[index]) * 1000);

            alphabetAudio.volume = commonService.getSpeechVolume() * 0.5;
            alphabetAudio.play();
        };
    }
})();