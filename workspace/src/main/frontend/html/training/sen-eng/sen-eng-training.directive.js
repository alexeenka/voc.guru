(function() {
    'use strict';

    angular
        .module('h4t-eng.training')
        .directive('senEngTraining',  SenEngTraining);

    function SenEngTraining() {
        return {
            restrict: 'E',
            scope: {
                training: '=',
                senEngWorkout: '='
            },
            templateUrl: '/html/training/workout-template/workout-template.html',
            controller: SenEngTrainingController
        }
    }

    SenEngTrainingController.$inject = ['$scope', '$timeout', '$log', '$q', 'trainingService', 'commonService', 'speechSynthesisService'];

    function SenEngTrainingController($scope, $timeout, $log, $q, trainingService, commonService, speechSynthesisService) {
        $scope.html_templates = {
            workout_key : "/html/training/sen-eng/sen-eng-key.html",
            answer_in_answer_state: "/html/training/sen-eng/sen-eng-answer_answer-state.html",
            hint_list: "/html/training/sen-eng/sen-eng-hint-list.html",
            include_definition_hint: true,
            include_hint_picture: true
        };

        $scope.trainingName = 'SEN-ENG';
        $scope.senEngWorkout['innerScope'] = $scope;

        $scope.startWorkout = function() {
            var job = $q.defer();
            trainingService.senEngWorkoutLoad().then(function(result) {
                if (result.response.errorMsg) {
                    job.reject(result.response.errorMsg);
                    return;
                }

                $scope.workout = {
                    words : [],
                    wordsResult:[],
                    wordIndex : -1,
                    currentWord : null,

                    show_answer_state : false,
                    show_next_button : false,

                    currentPoints: 10,

                    usedHint1stLetter: false,
                    usedHint2ndLetter: false,
                    usedHintPicture: false,
                    userAnswer : '',
                    hintAnswer: [],
                    currentRus: '' // what we need to guess
                };
                $scope.workout.lastWord = function() {
                    return ($scope.workout.wordIndex  + 1) >= $scope.workout.words.length;
                };
                $scope.workout.words = result.response;

                job.resolve();
            });
            return job.promise;
        };

        $scope.hint1stLetter = function() {
            reducePoints(5);
            $scope.workout.usedHint1stLetter = true;
            $scope.workout.hintAnswer[0] = $scope.workout.currentWord.trainingExt.answer.substring(0, 1).toUpperCase();
        };
        $scope.hint2ndLetter = function() {
            reducePoints(3);
            $scope.workout.usedHint2ndLetter = true;
            $scope.workout.hintAnswer[1] = $scope.workout.currentWord.trainingExt.answer.substring(1, 2).toUpperCase();
        };

        function reducePoints(value) {
            $scope.workout.currentPoints -= value;
        }

        $scope.checkAnswer = function () {
            $scope.training.checkAnswer(fnCheckAnswer);

            function fnCheckAnswer() {
                var userAnswer = $scope.workout.userAnswer.trim().toUpperCase();
                var rightAnswer = $scope.workout.currentWord.trainingExt.answer.trim().toUpperCase();

                return userAnswer == rightAnswer;
            }
        };

        $scope.finishWord = function() {
            speechSynthesisService.sayEngText([$scope.workout.currentWord.trainingExt.answer, $scope.workout.currentWord.trainingExt.sA]).then(function(status) {
                if (status.status == "break") return;
                speechSynthesisService.sayRuText($scope.workout.currentWord.rusValues[0]);
            });
            $scope.training.finishWord();

            // HintsList: Begin
            $scope.workout.usedHint1stLetter = true;
            $scope.workout.usedHint2ndLetter = true;
            $scope.workout.usedHintDefinition = true;
            $scope.workout.usedHintPicture = true;
            $scope.workout.userAnswer = '';
            // HintsList: End
        };

        $scope.nextTrainingWord = function() {
            $scope.training.nextWord();
            $scope.workout.hintAnswer = trainingService.produceLetterHint($scope.workout.currentWord.trainingExt.answer);

            // HintsList: Begin
            $scope.workout.usedHint1stLetter = false;
            $scope.workout.usedHint2ndLetter = false;
            $scope.workout.usedHintPicture = false;
            $scope.workout.usedHintDefinition = false;
            $scope.workout.userAnswer = '';
            // HintsList: End

            $timeout(function() {speechSynthesisService.sayEngText($scope.workout.currentWord.trainingExt.sH);});
        };
    }
})();