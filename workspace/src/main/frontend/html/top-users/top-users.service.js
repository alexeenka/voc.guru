(function (angular) {
    'use strict';

    angular.module('h4t-eng.top-users').factory("topUsersService", TopUsersService);

    TopUsersService.$inject = ['$http','$log','$q'];

    function TopUsersService($http, $log, $q) {
        return {
            getTopUsers: getTopUsersImpl
        };

        function getTopUsersImpl() {
            var job = $q.defer();
            $http.get('/rest/top-user/get').success(function(response) {
                $log.debug(currentTime() + "top-user.", response);
                job.resolve(response);
            });
            return job.promise;
        }

    }
}(window.angular));