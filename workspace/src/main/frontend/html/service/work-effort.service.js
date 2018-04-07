(function (angular) {
    'use strict';

    angular.module('h4t-eng').service('workEffortService', WorkEffortService);

    WorkEffortService.$inject = ['$log', '$q', '$http', '$rootScope'];

    function WorkEffortService($log, $q, $http, $rootScope) {

        var spentTime = {total: 0, min: 0, sec: 0};
        updateWorkEffort();

        return {
            updateWorkEffort : updateWorkEffort,
            todayWorkFinish: todayWorkFinish,
            isWorkFinish : isWorkFinish,
            currentDayYear: currentDayYear,
            spentTime: spentTime
        };

        function todayWorkFinish() {
            return isWorkFinish(spentTime.total);
        }

        function isWorkFinish(work) {
            return work >= 1200;
        }

        function currentDayYear() {
            var current = moment();
            return {dayOfYear:current.format('DDD'), year:current.format('YYYY')}
        }
        
        function updateWorkEffort() {
            var job = $q.defer();
            
            var current = currentDayYear();
            var year = current.year;
            var dayOfYear = current.dayOfYear;

            $http.post('/rest/work-effort/time-spent-for-day', {year:year, dayOfYear:dayOfYear}).success(function(response) {
                $log.debug(currentTime() + "UpdateWorkEffort: ", response);
                
                var responseTime = response.spentTime;
                spentTime.total = responseTime;
                spentTime.min = parseInt(responseTime / 60);
                spentTime.sec = parseInt(responseTime % 60);
                
                job.resolve();
                $rootScope.$broadcast("updatedWorkEffort");
            });

            return job.promise;
        }
    }

}(window.angular));