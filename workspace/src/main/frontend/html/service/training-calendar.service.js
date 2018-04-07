(function (angular) {
    'use strict';

    angular.module('h4t-eng').service('trainingCalendarService', TrainingCalendarService);

    TrainingCalendarService.$inject = ['$log', 'workEffortService'];

    function TrainingCalendarService($log, workEffortService) {

        var UNKNOWN = -1;
        var BAD = 0;
        var NOT_BAD = 1;
        var GOOD = 2;


        return {
            UNKNOWN: UNKNOWN,
            BAD: BAD,
            NOT_BAD: NOT_BAD,
            GOOD: GOOD,

            calendarParameters : getCalendarParameters,
            initTrainingCalendarCurrentUser: doInitTrainingCalendarCurrentUser,
            initTrainingCalendar: doInitTrainingCalendar,
            emptyTrainingCalendar : getEmptyTrainingCalendar
        };

        function getEmptyTrainingCalendar() {
            var trainingCalendar = {
                daysWork:[],
                totalWorkSec: 0,
                dayOfWeek: moment().isoWeekday()
            };

            for (var i=0; i<28; i++) trainingCalendar.daysWork.push(UNKNOWN);

            return trainingCalendar;
        }

        function doInitTrainingCalendar(trainingCalendar, serverTrainingCalendar) {
            for (var i=0, n=serverTrainingCalendar.length; i<n; i++) {
                var dayWork = serverTrainingCalendar[i];
                if (dayWork == null) {
                    trainingCalendar.daysWork[i] = BAD;
                    continue;
                }

                trainingCalendar.daysWork[i] = workEffortService.isWorkFinish(dayWork) ? GOOD : NOT_BAD;
                trainingCalendar.totalWorkSec += dayWork;
            }
        }

        function doInitTrainingCalendarCurrentUser(trainingCalendar, serverTrainingCalendar) {
            doInitTrainingCalendar(trainingCalendar, serverTrainingCalendar);

            // for today
            if (workEffortService.spentTime.total == 0) {
                trainingCalendar.daysWork[getDaysCount()] = BAD;
            } else {
                trainingCalendar.daysWork[getDaysCount()] = workEffortService.todayWorkFinish() ? GOOD : NOT_BAD;
            }
        }

        function getCalendarParameters() {
            var current = moment();
            var dayOfYear = current.format('DDD');
            var year = current.format('YYYY');

            var dayOfWeek = current.isoWeekday(); // 1-7

            var count = getDaysCount(); // exclude today
            var countPlusToday = count + 1;

            var startDay = dayOfYear - count;
            
            return {year:year, startDay:startDay, count:count, countPlusToday:countPlusToday, dayOfWeek:dayOfWeek, dayOfYear:dayOfYear};
        }

        function getDaysCount() {
            var dayOfWeek = moment().isoWeekday();
            return 28 - (7 - dayOfWeek + 1);

        }
    }

}(window.angular));