/**
 * h4t-eng.
 *
 * Created by aalexeenka on 20/07/2015.
 */
(function () {
    'use strict';

    angular.module('h4t-eng.training', ["h4t-eng.training.service", "ngAnimate"]).config(TrainingStateProvider);

    function TrainingStateProvider($stateProvider) {
        $stateProvider.state('training', {
            url: "/training",
            templateUrl: '/html/training/training.html',
            data: {tabName: "training"},
            controller: TrainingController
        })
    }

    function TrainingController($scope, $http, $log, $timeout,
                                $rootScope, trainingService, commonService, modalService, counterService, speechSynthesisService,
                                workService, workEffortService, trainingCalendarService
    ) {
        $scope.training = {
            state: "none",
            type: "none",
            isNone: function () {
                return $scope.training.state == "none" && $scope.training.type == "none";
            },
            toNone: function () {
                speechSynthesisService.cancelSpeech();

                if ($scope.training.type == "ENG-RUS") {
                    counterService.updateEngRusCounts();
                } else if ($scope.training.type == "DEF-ENG") {
                    counterService.updateDefEngCounts();
                } else if ($scope.training.type == "RUS-ENG") {
                    counterService.updateRusEngCounts();
                } else if ($scope.training.type == "IMG-ENG") {
                    counterService.updateImgEngCounts();
                } else if ($scope.training.type == "SEN-ENG") {
                    counterService.updateSenEngCounts();
                }
                $scope.training.state = "none";
                $scope.training.type = "none";
                commonService.showHeader();
            },
            isRepeatedWord: function() {
                var workoutInnerScope = getWorkoutInnerScope($scope.training.type);
                return workoutInnerScope.workout.currentWord.hasOwnProperty('repeatWordValue');
            },
            showTraining: function(trainingType) {
                return $scope.training.type == trainingType && ($scope.training.state == 'started' || $scope.training.state == 'finish' || $scope.training.state == 'saving');
            },
            serverError: function (msg) {
                var modalOptions = {
                    showCloseButton: false,
                    actionButtonText: 'Понимаю',
                    headerText: 'Уведомление',
                    bodyText: msg
                };
                modalService.showModal({}, modalOptions).then(function (result) {
                });
            },
            // common methods section. begin
            finishWorkout: function () {
                var workoutInnerScope = getWorkoutInnerScope($scope.training.type);

                function saveTraining() {
                    var jobPromise;
                    $log.debug(currentTime() + " Workout Result:", workoutInnerScope.workout.wordsResult);
                    $log.debug(currentTime() + " Work effort: " + workService.evalWork() + "sec.");

                    var words = workoutInnerScope.workout.words;
                    var wordsResult = workoutInnerScope.workout.wordsResult;
                    if ($scope.training.type == "ENG-RUS") {
                        jobPromise = trainingService.saveEngRusWorkout(workoutInnerScope.workout.wordsResult);
                    } else if ($scope.training.type == "DEF-ENG") {
                        jobPromise = trainingService.saveDefEngWorkout(workoutInnerScope.workout.wordsResult);
                    } else if ($scope.training.type == "RUS-ENG") {
                        jobPromise = trainingService.saveRusEngWorkout(workoutInnerScope.workout.wordsResult);
                    } else if ($scope.training.type == "IMG-ENG") {
                        jobPromise = trainingService.saveImgEngWorkout(workoutInnerScope.workout.wordsResult);
                    } else if ($scope.training.type == "SEN-ENG") {
                        jobPromise = trainingService.saveSenEngWorkout(workoutInnerScope.workout.wordsResult);
                    }
                    return jobPromise;
                }

                function rotateBrain() {
                    var rotatedBrain = jQuery("#" + workoutInnerScope.trainingName + "_saving_brain");
                    jQuery({deg: 0}).animate({deg: 360},  {duration: 3000, easing : 'linear', queue: false, step: function(now, fx){
                        rotatedBrain.css({
                            '-moz-transform':'rotate('+now+'deg)',
                            '-webkit-transform':'rotate('+now+'deg)',
                            '-o-transform':'rotate('+now+'deg)',
                            '-ms-transform':'rotate('+now+'deg)',
                            'transform':'rotate('+now+'deg)'
                        });
                    }
                    });
                }

                var timerInterval;
                $scope.training.saveAttempt = 0;
                $scope.training.state = 'saving';

                function finishLogic() {
                    saveTraining().then(
                        function () {
                            clearInterval(timerInterval);

                            speechSynthesisService.cancelSpeech();
                            trainingService.playCrowdCheers();
                            workEffortService.updateWorkEffort();
                            $scope.training.state = 'finish';

                            $timeout(function() {jQuery("#" + workoutInnerScope.trainingName + "_repeatWorkoutButton").focus();}, 500);
                        },
                        function (event) {
                            $log.error(currentTime() + ' Can\'t save training result', event);
                            if (event && (-1 === event.status || 0 === event.status)) {
                                finishLogic();
                                $scope.training.saveAttempt++;
                            } else {
                                clearInterval(timerInterval);
                                speechSynthesisService.cancelSpeech();
                                $scope.training.state = 'finish';
                            }
                        }
                    );
                }

                $timeout(function(){
                    rotateBrain();
                    finishLogic();
                    timerInterval = setInterval(function() {rotateBrain()}, 3000);
                });
            },
            restartTraining: function() {
                $scope.startWorkout($scope.training.type);
            },
            giveUp: function() {
                trainingService.playWaterDropsAudio();
                trainingService.showTrainingMsgCancel($scope.training.type);

                var workoutInnerScope = getWorkoutInnerScope($scope.training.type);
                workoutInnerScope.workout.currentPoints = -5;
                workoutInnerScope.finishWord();
            },
            hintDefinition: function() {
                var workoutInnerScope = getWorkoutInnerScope($scope.training.type);
                reducePoints(workoutInnerScope.workout, 3);
                workoutInnerScope.workout.usedHintDefinition = true;
                speechSynthesisService.sayEngText(workoutInnerScope.workout.currentWord.engDefs[0]);
            },
            hintPicture: function() {
                var workoutInnerScope = getWorkoutInnerScope($scope.training.type);
                reducePoints(workoutInnerScope.workout, 3);
                workoutInnerScope.workout.usedHintPicture = true;
            },
            finishWord: function() {
                var workout = getWorkoutInnerScope($scope.training.type).workout;
                $timeout(function () {workout.show_next_button = true;}, 1800);
                $timeout(function () {
                    var nextButton = jQuery("#" + $scope.training.type + "_workoutButtonNext");
                    if (nextButton.length != 0 && nextButton.is(":visible")) {
                        nextButton.focus();
                        return;
                    }
                    var finishButton = jQuery("#" + $scope.training.type + "_workoutButtonFinish");
                    if (finishButton.length != 0 && finishButton.is(":visible")) {
                        finishButton.focus();
                    }
                }, 2000);

                // inside training datasource, to exclude extra query to database, with sum points on client side
                var iR = {
                    w:workout.currentWord.engVal,
                    tv:{
                        p:workout.currentPoints + workout.currentWord.trainingValue.p,
                        a:workout.currentWord.trainingValue.a + 1,
                        s:0
                    },
                    rv:workout.currentWord.repeatWordValue
                };
                if (workout.currentPoints == 10 && workout.currentWord.trainingValue.p >= 0) {
                    iR.tv.s = workout.currentWord.trainingValue.s + 1
                }
                workout.wordsResult.push(iR);
                workout.show_answer_state = true;
            },
            nextWord: function() {
                speechSynthesisService.cancelSpeech();

                var workout = getWorkoutInnerScope($scope.training.type).workout;
                workout.wordIndex++;
                if (workout.wordIndex >= workout.words.length) return;

                workout.show_answer_state = false;
                workout.currentPoints = 10;
                workout.show_next_button = false;
                workout.currentWord = workout.words[workout.wordIndex];

                if (!window.mobilecheck()) {
                    $timeout(function() {jQuery("#" + $scope.training.type + "_userAnswer").focus();});
                }
            },
            checkAnswer: function(checkFunction) {
                var innerScope = getWorkoutInnerScope($scope.training.type);

                if (!checkFunction) {
                    checkFunction = function() {
                        return innerScope.workout.userAnswer.trim().toUpperCase() == innerScope.workout.currentWord.engVal.trim().toUpperCase();
                    }
                }

                if (checkFunction()) {
                    trainingService.playApplause();
                    trainingService.showTrainingMsgSuccess($scope.training.type);
                    innerScope.finishWord();
                    return
                }
                trainingService.showTrainingMsgMistake($scope.training.type);
            },
            isMobileTraining: function() {
                return window.mobilecheck();
            },
            lastWordResult: function () {
                if ($scope.training.isNone()) return;
                var workout = getWorkoutInnerScope($scope.training.type).workout;
                return workout.wordsResult[workout.wordsResult.length - 1];
            },
            nextRepeatDay: function (val) {
                if (!val) return "";
                if (!val.tv) return "";
                if (!val.rv) return "";
                var period = val.tv.p  == 10 ? val.rv.period * 2 : val.rv.period;
                return moment().add(period, 'days').format('DD-MM-YY');
            },
            // is word completely studied.
            isWordStudied: function (wordValue) {
                return wordValue.tv.p >= 150 || wordValue.tv.s >=5;
            },
            isShowedSymbol4Hint : function (val) {
                return trainingService.isShowedSymbol(val);
            }
            // common methods section. end
        };

        function reducePoints(workout, value) {
            workout.currentPoints -= value;
        }

        function getWorkoutInnerScope(trainingType) {
            if (trainingType == "ENG-RUS") {
                return $scope.engRusWorkout.innerScope;
            } else if (trainingType == "DEF-ENG") {
                return $scope.defEngWorkout.innerScope;
            } else if (trainingType == "RUS-ENG") {
                return $scope.rusEngWorkout.innerScope;
            } else if (trainingType == "IMG-ENG") {
                return $scope.imgEngWorkout.innerScope;
            } else if (trainingType == "SEN-ENG") {
                return $scope.senEngWorkout.innerScope;
            }

            $log.error(currentTime() + ' Incorrect training type', trainingType);
            throw 'Incorrect training type';
        }

        // that object extends into directive, for example it use to call start method
        $scope.engRusWorkout = {};
        // that object extends into directive, for example it use to call start method
        $scope.defEngWorkout = {};
        // that object extends into directive, for example it use to call start method
        $scope.rusEngWorkout = {};
        // that object extends into directive, for example it use to call start method
        $scope.imgEngWorkout = {};
        // that object extends into directive, for example it use to call start method
        $scope.senEngWorkout = {};

        $scope.startWorkout = function (trainingType) {
            trainingService.playStartTraining(trainingType + '-start-audio');
            var brain = jQuery("#" + trainingType + '-start-pic');
            var brainOnPage = brain.length !== 0 && brain.is(":visible");
            if (brainOnPage) brain.effect('shake', {times:48}, 60000); // 48 times for 60 sec duration

            var innerScope = getWorkoutInnerScope(trainingType);

            innerScope.startWorkout().then(function() {
                trainingService.preLoadImages(innerScope.workout.words).then(function() {
                    commonService.hideHeader();
                    $scope.training.state = "started";
                    $scope.training.type = trainingType;
                    innerScope.nextTrainingWord();
                    $log.debug(currentTime() + "[" + trainingType + "] was started ", innerScope.workout.words);
                    if (brainOnPage) brain.finish();

                    // shuffle values
                    for (var i=0, n=innerScope.workout.words.length; i<n; i++) {
                        // shuffle rus values
                        shuffleArray(innerScope.workout.words[i].rusValues);
                        // shuffle engDefs
                        shuffleArray(innerScope.workout.words[i].engDefs);
                        // shuffle engSentences
                        shuffleArray(innerScope.workout.words[i].engSentences);
                    }

                    // just for test final view
                    // {
                    //     var doOneRvPassed = false;
                    //     var iR;
                    //     for (var ii = 0, nn = innerScope.workout.words.length - 1; ii < nn; ii++) {
                    //         if (innerScope.workout.words[ii].repeatWordValue != null && !doOneRvPassed) {
                    //             doOneRvPassed = true;
                    //             iR = {
                    //                 w: innerScope.workout.words[ii].engVal,
                    //                 tv: {p: 10, a: 0, s: 0},
                    //                 rv: innerScope.workout.words[ii].repeatWordValue
                    //             };
                    //             innerScope.workout.wordsResult.push(iR);
                    //             console.log("Good repeated word: ", iR.w, iR.rv.period);
                    //             continue;
                    //         }
                    //         iR = {
                    //             w: innerScope.workout.words[ii].engVal,
                    //             tv: {
                    //                 p: innerScope.workout.words[ii].trainingValue.p + randomIntFromInterval(0, 10) - 10,
                    //                 a: innerScope.workout.words[ii].trainingValue.a + 1,
                    //                 s: 0
                    //             },
                    //             rv: innerScope.workout.words[ii].repeatWordValue
                    //         };
                    //         if (innerScope.workout.words[ii].repeatWordValue != null) {
                    //             console.log("Just repeated word: ", iR.w, iR.rv.period);
                    //         }
                    //         if (innerScope.workout.words[ii].repeatWordValue == null) {
                    //             console.log("Just training word: ", iR.w);
                    //         }
                    //         innerScope.workout.wordsResult.push(iR);
                    //     }
                    //     iR = {
                    //         w: innerScope.workout.words[ii].engVal,
                    //         tv: {p: 10, a: 0, s: 5},
                    //         rv: null
                    //     };
                    //     innerScope.workout.wordsResult.push(iR);
                    // }
                    // $scope.training.finishWorkout();

                    // start work
                    workService.startWork();
                });
            }, function (error) {
                $log.debug(currentTime() + "[" + trainingType + "] was NOT started. " );
                if (brainOnPage) brain.finish();
                $scope.training.serverError(error);
            });
        };

        $scope.countsDefEngWords = counterService.countsDefEngWords;
        $scope.countsEngRusWords = counterService.countsEngRusWords;
        $scope.countsImgEngWords = counterService.countsImgEngWords;
        $scope.countsRusEngWords = counterService.countsRusEngWords;
        $scope.countsSenEngWords = counterService.countsSenEngWords;

        $scope.spentTime = workEffortService.spentTime;
        $scope.trainingPageTodayWorkFinish = workEffortService.todayWorkFinish;

        commonService.userInfo().then(function(userInfo) {
            $scope.training['userName'] = userInfo.userFN;
        });

        $scope.trainingStatistics = [
            {},
            {}
        ];

        // *** Training Calendar. Begin. *** //
        var calendarParameters = trainingCalendarService.calendarParameters();
        $scope.trainingCalendar = trainingCalendarService.emptyTrainingCalendar();

        $http.post('/rest/work-effort/training-calendar',
            {year:calendarParameters.year, startDay:calendarParameters.startDay, count:calendarParameters.count}
        ).success(function(response) {
            $log.debug(currentTime() + "Training-calendar: ", response);
            trainingCalendarService.initTrainingCalendarCurrentUser($scope.trainingCalendar, response.trainingCalendar);
        });
        $rootScope.$on('updatedWorkEffort', function () {
            // for today
            $scope.trainingCalendar.daysWork[calendarParameters.count] = workEffortService.isWorkFinish(workEffortService.spentTime.total) ? trainingCalendarService.GOOD : trainingCalendarService.NOT_BAD;
        });
        // *** Training Calendar. End. *** //
    }
})();