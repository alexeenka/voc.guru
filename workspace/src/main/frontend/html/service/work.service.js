(function (angular) {
    'use strict';

    angular.module('h4t-eng').service('workService', WorkService);

    WorkService.$inject = ['$log'];

    function WorkService($log) {

        var SEC_BEFORE_IDLE = 10;
        var work = 0;
        var lastTime;

        return {
            startWork : startWork,
            doWork : doWork,
            evalWork: evalWork
        };
        
        function evalWork() {
            return Math.round(work);
        }

        function startWork() {
            lastTime = new Date().getTime() / 1000;
            work = 0;
        }

        function doWork() {
            var curTime = new Date().getTime() / 1000;
            var workInterval = curTime - lastTime;
            if (workInterval > SEC_BEFORE_IDLE) {
                work += SEC_BEFORE_IDLE;
                //$log.debug(currentTime() + " doWork: +" + SEC_BEFORE_IDLE + " real: " + workInterval, work);
            } else {
                work += workInterval;
            }
            lastTime = curTime;

            //$log.debug(currentTime() + " doWork: ", work);
        }
    }

}(window.angular));