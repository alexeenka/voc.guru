(function() {
    'use strict';

    angular
        .module('h4t-eng.training')
        .directive('imgEngTraining',  ImgEngTraining);

    function ImgEngTraining() {
        return {
            restrict: 'E',
            scope: {
                training: '=',
                imgEngWorkout: '='
            },
            templateUrl: '/html/training/workout-template/workout-template.html',
            controller: ImgEngTrainingController
        }
    }

    ImgEngTrainingController.$inject = ['$scope', '$q', 'trainingService', 'speechSynthesisService'];

    function ImgEngTrainingController($scope, $q, trainingService, speechSynthesisService) {
        $scope.html_templates = {
            workout_key : "/html/training/img-eng/img-eng-key.html",
            answer_in_answer_state: "/html/training/img-eng/img-eng-answer_answer-state.html",
            hint_list: "/html/training/img-eng/img-eng-hint-list.html",
            include_definition_hint: true,
            include_hint_picture: false
        };

        $scope.trainingName = 'IMG-ENG';
        $scope.imgEngWorkout['innerScope'] = $scope;

        $scope.startWorkout = function() {
            var job = $q.defer();
            trainingService.imgEngWorkoutLoad().then(function (result) {
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
                    usedHintDefinition: false,
                    userAnswer : '',
                    hintAnswer: [],
                    currentDef: '' // what we need to guess

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
            $scope.workout.hintAnswer[0] = $scope.workout.currentWord.engVal.substring(0, 1).toUpperCase();
        };
        $scope.hint2ndLetter = function() {
            reducePoints(3);
            $scope.workout.usedHint2ndLetter = true;
            $scope.workout.hintAnswer[1] = $scope.workout.currentWord.engVal.substring(1, 2).toUpperCase();
        };
        function reducePoints(value) {
            $scope.workout.currentPoints -= value;
        }

        $scope.checkAnswer = $scope.training.checkAnswer;

        $scope.finishWord = function() {
            speechSynthesisService.sayEngText($scope.workout.currentWord.engVal).then(function(status) {
                if (status.status == "break") return;
                speechSynthesisService.sayRuText($scope.workout.currentWord.rusValues[0]).then(function(status) {
                    if (status.status == "break") return;
                    speechSynthesisService.sayEngText($scope.workout.currentWord.engSentences[0]);
                });
            });

            $scope.training.finishWord();

            // HintsList: Begin
            $scope.workout.usedHint1stLetter = true;
            $scope.workout.usedHint2ndLetter = true;
            $scope.workout.usedHintDefinition = true;
            $scope.workout.userAnswer = '';
            // HintsList: End
        };

        $scope.nextTrainingWord = function() {
            var imgKey = jQuery('#img-eng-training-key-img-id');
            imgKey.hide();
            $scope.training.nextWord();
            imgKey.fadeIn(1000);

            $scope.workout.hintAnswer = trainingService.produceLetterHint($scope.workout.currentWord.engVal);

            // HintsList: Begin
            $scope.workout.usedHint1stLetter = false;
            $scope.workout.usedHint2ndLetter = false;
            $scope.workout.usedHintDefinition = false;
            $scope.workout.userAnswer = '';
            // HintsList: End
        };
    }

})();