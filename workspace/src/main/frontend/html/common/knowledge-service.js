(function (angular) {
    'use strict';

    angular.module('h4t-eng.knowledge', []);
    angular.module('h4t-eng.knowledge').factory("knowledgeService", KnowledgeService);

    KnowledgeService.$inject = ['$http', '$q', '$log', '$rootScope', 'counterService', 'workEffortService'];

    function KnowledgeService($http, $q, $log, $rootScope, counterService, workEffortService) {

        return {
            loadReadyWords: loadReadyWordsImpl,
            validateWord: validateWord,
            saveReadyWord: saveReadyWord,
            deleteReadyWord: deleteReadyWord,
            loadReadySingleWord: loadReadySingleWord,

            getImgURL: getImgURL
        };

        //---- FOR READY
        function loadReadyWordsImpl(letter) {
            var job = $q.defer();
            $http.post('/rest/vocabulary/load', {letter : letter}).success(function(response) {
                $log.debug(currentTime() + " vocabulary, load words for '" + letter + "', response: ", response);
                job.resolve(response.wordList);
            });

            return job.promise;
        }

        function deleteWordFromList(list, engVal) {
            var index = -1;
            for (var i = 0, N = list.length; i < N; i++) {
                if (list[i].engVal == engVal) {
                    index = i;
                    break;
                }
            }
            if (index != -1) list.splice(index,1);
        }

        function saveReadyWord(word, prevEngVal) {
            return commonSave('/rest/word/save', word, prevEngVal);
        }

        function validateWord(word) {
            var job = $q.defer();
            $http.post('/rest/validate/word', word).success(function (response) {
                $log.debug(currentTime() + "Validate word: ", word);
                job.resolve(response);
            });
            return job.promise;
        }

        function deleteReadyWord(engVal, controllerScope) {
            $http.post('/rest/word/delete', {engVal: engVal}).success(function (response) {
                $log.debug(currentTime() + "Delete word: ", engVal);
                if (engVal) deleteWordFromList(controllerScope.wordList, engVal);
                // update ready word count
                counterService.updateCounts();
            });
        }

        function commonSave(url, word, prevEngVal) {
            var job = $q.defer();

            var fd = new FormData();

            // First, each image in separate form data, 1. we can't stringify blob 2. and we send byte array for image
            for (var i=0, N=word.engDefs.length; i<N; i++) {
                if (word.engDefs[i].imageBLOB) {
                    fd.append('imgData_' + i, word.engDefs[i].imageBLOB);
                }
            }

            // Second, all other data
            fd.append('jsonStr', JSON.stringify( {word : word, prevEngVal:prevEngVal, currentDayYear:workEffortService.currentDayYear()}));

            jQuery.ajax(
                {
                    type: 'POST',
                    url: url,
                    data: fd,
                    processData: false,
                    contentType: false
                }
            ).done(function (response) {
                job.resolve({response:response});
                counterService.updateCounts();
                if (!prevEngVal) workEffortService.updateWorkEffort(); // only for new words
            }).fail(function(error) {
                error['config'] = {'url':url};
                error['data'] = error.responseText;
                $rootScope.$broadcast("appH4TServerError", {rejection: error});
            });

            return job.promise;
        }

        function loadReadySingleWord(engVal, wordSet) {
            return $http.get('/rest/word/load-single-word', {params: {engVal : engVal, wordSet : wordSet}});
        }

        function getImgURL (imgURL, date) {
            if (!imgURL) {
                return "";
            }

            return imgURL + "?t=" + date;
        }
    }


}(window.angular));