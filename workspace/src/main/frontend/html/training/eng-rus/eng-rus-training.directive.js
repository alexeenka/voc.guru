(function() {
    'use strict';

    angular
        .module('h4t-eng.training')
        .directive('engRusTraining',  EngRusTraining);

    function EngRusTraining() {
        return {
            restrict: 'E',
            scope: {
                training: '=',
                engRusWorkout: '='
            },
            templateUrl: '/html/training/workout-template/workout-template.html',
            controller: EngRusTrainingController
        }
    }

    EngRusTrainingController.$inject = ['$scope', '$timeout', '$q', 'trainingService', 'speechSynthesisService'];

    function EngRusTrainingController($scope, $timeout, $q, trainingService, speechSynthesisService) {
        $scope.html_templates = {
            workout_key : "/html/training/eng-rus/eng-rus-key.html",
            answer_in_answer_state: "/html/training/eng-rus/eng-rus-answer_answer-state.html",
            hint_list: "/html/training/eng-rus/eng-rus-hint-list.html",
            include_definition_hint: true,
            include_hint_picture: true
        };

        $scope.trainingName = 'ENG-RUS';
        $scope.engRusWorkout['innerScope'] = $scope;

        $scope.startWorkout = function() {
            var job = $q.defer();
            trainingService.engRusWorkoutLoad().then(function(result) {
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
                    userAnswer : '',
                    hintAnswer: [],
                    currentPoints: 10,
                    usedHint1stLetter: false,
                    usedHint2ndLetter: false,
                    usedHintDefinition: false,
                    usedHintPicture: false
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
            $scope.workout.hintAnswer[0] = $scope.workout.currentWord.rusValues[0].substring(0, 1).toUpperCase();
        };
        $scope.hint2ndLetter = function() {
            reducePoints(3);
            $scope.workout.usedHint2ndLetter = true;
            $scope.workout.hintAnswer[1] = $scope.workout.currentWord.rusValues[0].substring(1, 2).toUpperCase();
        };

        function reducePoints(value) {
            $scope.workout.currentPoints -= value;
        }

        $scope.checkAnswer = function () {
            $scope.training.checkAnswer(fnCheckAnswer);

            function fnCheckAnswer() {
                for (var i=0, n=$scope.workout.currentWord.rusValues.length;i<n;i++) {
                    var userAnswer = $scope.workout.userAnswer.trim().toUpperCase();
                    var rightAnswer = $scope.workout.currentWord.rusValues[i].trim().toUpperCase();

                    // E == Ё
                    userAnswer = userAnswer.replace(new RegExp('Ё', 'g'), 'Е');
                    rightAnswer = rightAnswer.replace(new RegExp('Ё', 'g'), 'Е');

                    if (userAnswer == rightAnswer) return true;
                }

                return false;
            }
        };

        $scope.finishWord = function() {
            speechSynthesisService.sayRuText($scope.workout.currentWord.rusValues[0]).then(function(status) {
                if (status.status == "break") return;
                speechSynthesisService.sayEngText($scope.workout.currentWord.engDefs[0]);
            });

            $scope.training.finishWord();

            // Hint List: Begin
            $scope.workout.usedHint1stLetter = true;
            $scope.workout.usedHint2ndLetter = true;
            $scope.workout.usedHintDefinition = true;
            $scope.workout.usedHintPicture = true;
            $scope.workout.userAnswer = '';
            // Hint List: End
        };

        $scope.nextTrainingWord = function() {
            $scope.training.nextWord();
            $scope.workout.hintAnswer = trainingService.produceLetterHint($scope.workout.currentWord.rusValues[0]);

            // HintsList: Begin
            $scope.workout.usedHint1stLetter = false;
            $scope.workout.usedHint2ndLetter = false;
            $scope.workout.usedHintDefinition = false;
            $scope.workout.usedHintPicture = false;
            $scope.workout.userAnswer = '';
            // HintsList: End


            $timeout(function() {speechSynthesisService.sayEngText($scope.workout.currentWord.engVal);});
        };
    }

})();