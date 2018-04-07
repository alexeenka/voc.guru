(function (angular) {
    'use strict';

    angular.module('h4t-eng.counter', []);
    angular.module('h4t-eng.counter').factory("counterService", CounterService);

    CounterService.$inject = ['$http','$log'];

    function CounterService($http, $log) {
        var countsWord = {val: 0, str: ""};
        var countsDefEngWords = {val: 0, str: ""};
        var countsEngRusWords = {val: 0, str: ""};
        var countsImgEngWords = {val: 0, str: ""};
        var countsRusEngWords = {val: 0, str: ""};
        var countsSenEngWords = {val: 0, str: ""};

        updateCounts();

        return {
            countsWord: countsWord,
            countsDefEngWords: countsDefEngWords,
            countsEngRusWords: countsEngRusWords,
            countsRusEngWords: countsRusEngWords,
            countsImgEngWords: countsImgEngWords,
            countsSenEngWords: countsSenEngWords,

            updateCounts: updateCounts,
            evalSlovo: evalSlovo,
            updateDefEngCounts: updateDefEngCounts,
            updateRusEngCounts: updateRusEngCounts,
            updateEngRusCounts: updateEngRusCounts,
            updateImgEngCounts: updateImgEngCounts,
            updateSenEngCounts: updateSenEngCounts
        };

        function updateCounts() {
            $http.get('/rest/counter/count-words-and-indicators').success(function(response) {
                $log.debug(currentTime() + "UpdateCounts, response: ", response);
                setCounterVal(countsWord, response.all);
                setCounterVal(countsDefEngWords, response.def_eng);
                setCounterVal(countsEngRusWords, response.eng_rus);
                setCounterVal(countsImgEngWords, response.img_eng);
                setCounterVal(countsRusEngWords, response.rus_eng);
                setCounterVal(countsSenEngWords, response.sen_eng);
            });
        }

        function updateDefEngCounts() {
            $http.get('/rest/counter/count-remained-def-eng-words').success(function(response) {
                $log.debug(currentTime() + "UpdateDefEngCounts, response: ", response);
                setCounterVal(countsDefEngWords, response);
            });
        }
        function updateRusEngCounts() {
            $http.get('/rest/counter/count-remained-rus-eng-words').success(function(response) {
                $log.debug(currentTime() + "UpdateRusEngCounts, response: ", response);
                setCounterVal(countsRusEngWords, response);
            });
        }
        function updateEngRusCounts() {
            $http.get('/rest/counter/count-remained-eng-rus-words').success(function(response) {
                $log.debug(currentTime() + "UpdateEngRusCounts, response: ", response);
                setCounterVal(countsEngRusWords, response);
            });
        }
        function updateImgEngCounts() {
            $http.get('/rest/counter/count-remained-img-eng-words').success(function(response) {
                $log.debug(currentTime() + "UpdateEngRusCounts, response: ", response);
                setCounterVal(countsImgEngWords, response);
            });
        }
        function updateSenEngCounts() {
            $http.get('/rest/counter/count-remained-sen-eng-words').success(function(response) {
                $log.debug(currentTime() + "UpdateEngRusCounts, response: ", response);
                setCounterVal(countsSenEngWords, response);
            });
        }

        function setCounterVal(counter, newVal) {
            counter.val = newVal;
            counter.str = counter.val + " " + evalSlovo(counter.val);
        }

        function evalSlovo(value) {
            var number = Number(value);
            var lastDigit = number % 10;
            var last2Digit = number % 100;

            var slovoAppendage = "";
            if (last2Digit >= 11 && last2Digit <= 19) {
                slovoAppendage = "слов";
            } else if (lastDigit == 0) {
                slovoAppendage = "слов";
            } else if (lastDigit == 1) {
                slovoAppendage = "слово";
            } else if (lastDigit >= 2 && lastDigit <= 4) {
                slovoAppendage = "слова";
            } else if (lastDigit >= 5 && lastDigit <= 9) {
                slovoAppendage = "слов";
            } else if (lastDigit >= 11 && lastDigit <= 9) {
                slovoAppendage = "слов";
            }

            return slovoAppendage;
        }
    }
}(window.angular));