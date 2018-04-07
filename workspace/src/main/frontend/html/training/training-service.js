(function (angular) {
    'use strict';

    angular.module('h4t-eng.training.service', []);
    angular.module('h4t-eng.training.service').factory("trainingService", TrainingService);

    TrainingService.$inject = ['$http','$log', '$q', 'commonService', 'workService'];

    function TrainingService($http, $log, $q, commonService, workService) {

        var engRusWorkoutLoad = function() {
            var job = $q.defer();
            $http.get('/rest/training/eng-rus-workout').success(function(response) {
                $log.debug(currentTime() + "Eng-rus-workout load.");
                job.resolve({response:response});
            });
            return job.promise;
        };

        var saveEngRusWorkout = function(result) {
            var job = $q.defer();
            $http.post('/rest/training/save-eng-rus-workout', {result : result, workEffort: workEffort()}).then(
                function () {
                    $log.debug(currentTime() + "Eng-rus-workout save.");
                    job.resolve();
                },
                function (event) {
                    $log.debug(currentTime() + "Error save.");
                    job.reject(event);
                }
            );
            return job.promise;
        };

        var defEngWorkoutLoad = function() {
            var job = $q.defer();
            $http.get('/rest/training/def-eng-workout').success(function(response) {
                $log.debug(currentTime() + "Def-eng-workout load.");
                job.resolve({response:response});
            });
            return job.promise;
        };

        var saveDefEngWorkout = function(result) {
            var job = $q.defer();
            $http.post('/rest/training/save-def-eng-workout',  {result : result, workEffort: workEffort()}).then(
                function () {
                    $log.debug(currentTime() + "Eng-rus-workout save.");
                    job.resolve();
                },
                function (event) {
                    $log.debug(currentTime() + "Error save.");
                    job.reject(event);
                }
            );
            return job.promise;
        };

        var rusEngWorkoutLoad = function() {
            var job = $q.defer();
            $http.get('/rest/training/rus-eng-workout').success(function(response) {
                $log.debug(currentTime() + "Rus-eng-workout load.");
                job.resolve({response:response});
            });
            return job.promise;
        };

        var saveRusEngWorkout = function(result) {
            var job = $q.defer();
            $http.post('/rest/training/save-rus-eng-workout',  {result : result, workEffort: workEffort()}).then(
                function () {
                    $log.debug(currentTime() + "Eng-rus-workout save.");
                    job.resolve();
                },
                function (event) {
                    $log.debug(currentTime() + "Error save.");
                    job.reject(event);
                }
            );
            return job.promise;
        };

        var senEngWorkoutLoad = function() {
            var job = $q.defer();
            $http.get('/rest/training/sen-eng-workout').success(function(response) {
                $log.debug(currentTime() + "Sen-eng-workout load.");
                job.resolve({response:response});
            });
            return job.promise;
        };

        var saveSenEngWorkout = function(result) {
            var job = $q.defer();
            $http.post('/rest/training/save-sen-eng-workout',  {result : result, workEffort: workEffort()}).then(
                function () {
                    $log.debug(currentTime() + "Sen-eng-workout save.");
                    job.resolve();
                },
                function (event) {
                    $log.debug(currentTime() + "Error save.");
                    job.reject(event);
                }
            );
            return job.promise;
        };

        var imgEngWorkoutLoad = function() {
            var job = $q.defer();
            $http.get('/rest/training/img-eng-workout').success(function(response) {
                $log.debug(currentTime() + "Img-eng-workout load.");
                job.resolve({response:response});
            });
            return job.promise;
        };

        var saveImgEngWorkout = function(result) {
            var job = $q.defer();
            $http.post('/rest/training/save-img-eng-workout', {result : result, workEffort: workEffort()}).then(
                function () {
                    $log.debug(currentTime() + "Eng-rus-workout save.");
                    job.resolve();
                },
                function (event) {
                    $log.debug(currentTime() + "Error save.");
                    job.reject(event);
                }
            );
            return job.promise;
        };

        var messages_success = ["Молодец!", "Ура!", "Умничка!", "Какая красота!", "Класс!", "Отлично!"];
        var messages_mistake = ["Упс!", "Еще разок!", "Поднажми!", "Ну!", "Давай еще!"];
        var messages_cancel = ["Ну ладно!", "Ну ок!", "Бывает"];

        function showTrainingMsg(trainingType, type) {
            var text = "ups";
            var cssClass = "";
            if (type == "success") {
                text = messages_success[Math.floor(Math.random()*messages_success.length)];
                cssClass = 'color_success';
            } else if (type == "mistake") {
                text = messages_mistake[Math.floor(Math.random()*messages_mistake.length)];
                cssClass = 'color_mistake';
            } else if (type == "cancel") {
                text = messages_cancel[Math.floor(Math.random()*messages_cancel.length)];
                cssClass = 'color_info';
            }
            var training_msg = jQuery("#" + trainingType + "_training_msg");
            training_msg.removeClass('color_success');
            training_msg.removeClass('color_mistake');
            training_msg.removeClass('color_info');
            training_msg.addClass(cssClass);
            training_msg.text(text);
            training_msg.show();
            return training_msg;
        }

        var showTrainingMsgCancel = function(trainingType) {
            var training_msg = showTrainingMsg(trainingType, "cancel");
            training_msg.fadeOut(1800);
        };
        var showTrainingMsgMistake = function(trainingType) {
            var training_msg = showTrainingMsg(trainingType, "mistake");
            training_msg.fadeOut(1800);
        };
        var showTrainingMsgSuccess = function(trainingType) {
            var training_msg = showTrainingMsg(trainingType, "success");
            training_msg.fadeOut(1800);
        };

        return {
            engRusWorkoutLoad: engRusWorkoutLoad,
            saveEngRusWorkout: saveEngRusWorkout,

            defEngWorkoutLoad: defEngWorkoutLoad,
            saveDefEngWorkout: saveDefEngWorkout,

            rusEngWorkoutLoad: rusEngWorkoutLoad,
            saveRusEngWorkout: saveRusEngWorkout,

            imgEngWorkoutLoad: imgEngWorkoutLoad,
            saveImgEngWorkout: saveImgEngWorkout,

            senEngWorkoutLoad: senEngWorkoutLoad,
            saveSenEngWorkout: saveSenEngWorkout,


            showTrainingMsgCancel:showTrainingMsgCancel,
            showTrainingMsgMistake: showTrainingMsgMistake,
            showTrainingMsgSuccess: showTrainingMsgSuccess,

            preLoadImages: preLoadImages,

            playStartTraining: playStartTraining,
            playApplause: playApplause,
            playCrowdCheers: playCrowdCheers,
            playWaterDropsAudio: playWaterDropsAudio,

            produceLetterHint: produceLetterHint,
            isShowedSymbol: isShowedSymbolImpl,
            capitalizeFirstLetter: capitalizeFirstLetter

        };

        function workEffort() {
            var current = moment();
            var year = current.format('YYYY');
            var dayOfYear = current.format('DDD');
            
            return {spent:workService.evalWork(), year:year, dayOfYear:dayOfYear};
        }

        function preLoadImages(words) {
            var job = $q.defer();

            var count = words.length;
            var loadedCount = 0;

            angular.forEach(words, function(word) {
                var downloadingImage = new Image();
                downloadingImage.onload = function(){
                    loadedCount++;
                    if (loadedCount == count) {
                        $log.debug(currentTime() + "FINISH: preLoadImages", loadedCount);
                        job.resolve();
                    }
                };
                downloadingImage.onerror = function() {
                    $log.error(currentTime() + ' Can not load image: ', this.src);
                    loadedCount++;
                    if (loadedCount == count) {
                        $log.debug(currentTime() + "FINISH: preLoadImages", loadedCount);
                        job.resolve();
                    }
                };
                downloadingImage.src = word.picURL;
            });

            return job.promise;
        }

        function playStartTraining(audioId) {
            var vAudio = document.getElementById(audioId);
            vAudio.volume = commonService.getSpeechVolume() * 0.7;
            vAudio.play();
            return vAudio.duration;
        }

        function playApplause() {
            return commonService.playApplause();
        }

        function playCrowdCheers() {
            var vAudio = document.getElementById("crowdCheersAudio");
            vAudio.volume = commonService.getSpeechVolume() * 0.7;
            vAudio.play();
            return vAudio.duration;
        }

        function playWaterDropsAudio() {
            var vAudio = document.getElementById("waterDropsAudio");
            vAudio.volume = commonService.getSpeechVolume();
            vAudio.play();
            return vAudio.duration;
        }

        function produceLetterHint(value) {
            if (!value) return [];

            var result = [];

            for (var i=0, n=value.length; i<n; i++) {
                var iChar = value.charAt(i);

                // don't make change
                if (isShowedSymbolImpl(iChar)) {
                    result.push(iChar);
                    continue;
                }

                result.push('*');
            }

            return result;
        }

        function isShowedSymbolImpl(val) {
            return '-' == val || ' ' == val || '*' == val;

        }

        function capitalizeFirstLetter(value) {
            if (!value) return "";
            return value.charAt(0).toUpperCase() + value.slice(1);
        }
    }


}(window.angular));