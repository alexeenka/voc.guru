(function (angular) {
    'use strict';

    angular.module('h4t-eng.word-set').factory("wordSetService", WordSetService);

    WordSetService.$inject = ['$http','$log','$q', '$rootScope'];

    function WordSetService($http, $log, $q, $rootScope) {

        var vIsWordSetUser = null;

        return {
            isWordSetUser: isWordSetUserImpl,
            saveWordToSet: saveWordToSetImpl,
            add10Words : add10WordsImpl
        };

        function add10WordsImpl(listId) {
            var job = $q.defer();
            $http.post('/rest/word-set/add-10-words', {listId:listId}).success(function (response) {
                $log.debug(currentTime() + "add-10-words.", response);
                job.resolve(response)
            });
            return job.promise;
        }

        function isWordSetUserImpl() {
            var job = $q.defer();
            if (vIsWordSetUser !== null) {
                job.resolve(vIsWordSetUser);
            } else {
                $http.post('/rest/word-set/allow', {}).success(function(response) {
                    $log.debug(currentTime() + "isWordSetUserImpl.", response);
                    vIsWordSetUser = response;
                    job.resolve(vIsWordSetUser)
                });
            }
            return job.promise;
        }


        function saveWordToSetImpl(word, prevEngVal, wordSetId) {
            var job = $q.defer();

            var fd = new FormData();

            // First, each image in separate form data, 1. we can't stringify blob 2. and we send byte array for image
            for (var i=0, N=word.engDefs.length; i<N; i++) {
                if (word.engDefs[i].imageBLOB) {
                    fd.append('imgData_' + i, word.engDefs[i].imageBLOB);
                }
            }

            // Second, all other data
            fd.append('jsonStr', JSON.stringify( {word : word, prevEngVal:prevEngVal}));
            fd.append('wordSetId', wordSetId);

            // todo: add loader
            var url = '/rest/word-set/save';
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
            }).fail(function (error) {
                error['config'] = {'url': url};
                error['data'] = error.responseText;
                $rootScope.$broadcast("appH4TServerError", {rejection: error});
            });

            return job.promise;
        }

    }
}(window.angular));