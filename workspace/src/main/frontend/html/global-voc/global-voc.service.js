(function (angular) {
    'use strict';

    angular.module('h4t-eng.global-voc').factory("globalVocService", GlobalVocService);

    GlobalVocService.$inject = ['$http','$log','$q', 'modalService', 'counterService'];

    function GlobalVocService($http, $log, $q, modalService, counterService) {
        var globalVocJob = $q.defer();
        $http.get('/rest/global-voc/size').success(function(response) {
            $log.debug(currentTime() + "Get global-voc size.");
            globalVocJob.resolve({
                size:response.size
            });
        });

        return {
            getGlobalVocSize: getGlobalVocSizeImpl,
            getSlideUnknownWords: getSlideUnknownWordsImpl,
            autocompleteSearch: autocompleteSearchImpl,
            loadWord: loadWordImpl,
            copyWord: copyWordImpl
        };

        function loadWordImpl(word) {
            var job = $q.defer();
            $http.post('/rest/global-voc/load-word', {w:word}).success(function(response) {
                $log.debug(currentTime() + "loadWordImpl.", response);
                job.resolve(response);
            });
            return job.promise;
        }

        function autocompleteSearchImpl(word) {
            var job = $q.defer();
            $http.post('/rest/global-voc/autocomplete', {w:word}).success(function(response) {
                $log.debug(currentTime() + "autocompleteSearchImpl.", response);
                job.resolve(response);
            });
            return job.promise;
        }

        function getGlobalVocSizeImpl() {
            $log.debug(currentTime() + "Ask GlobalVocSize.");
            return globalVocJob.promise;
        }

        function getSlideUnknownWordsImpl() {
            var job = $q.defer();
            $http.get('/rest/global-voc/get-random-words').success(function(response) {
                $log.debug(currentTime() + "getSlideUnknownWordsImpl.", response);
                job.resolve(response);
            });
            return job.promise;
        }

        function copyWordImpl(author, word) {
            var job = $q.defer();
            $http.post('/rest/global-voc/copy-word', {author : author, word: word}).success(function(response) {
                $log.debug(currentTime() + "copyWordImpl.", response);
                if (response.added == true) {
                    counterService.updateCounts();
                }
                modalService.showModal({}, {
                    showCloseButton: false,
                    actionButtonText: 'Ок',
                    headerText: 'Уведомление',
                    bodyText: response.msg
                }).then(function (result) {
                    job.resolve(response);
                });

            });
            return job.promise;
        }
    }
}(window.angular));