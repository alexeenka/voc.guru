(function() {
    'use strict';

    angular.module('h4t-eng').directive('trainingCalendar',  TrainingCalendar);


    function TrainingCalendar() {
        return {
            restrict: 'E',
            scope: {
                trainingCalendar:'=',
                withoutHeader:'='
            },
            templateUrl: '/html/directive/training-calendar.directive.html',
            controller: TrainingCalendarController
        }
    }

    TrainingCalendarController.$inject = ['$scope', 'trainingCalendarService'];

    function TrainingCalendarController($scope, trainingCalendarService) {
        $scope.UNKNOWN = trainingCalendarService.UNKNOWN;
        $scope.BAD = trainingCalendarService.BAD;
        $scope.NOT_BAD = trainingCalendarService.NOT_BAD;
        $scope.GOOD = trainingCalendarService.GOOD;

        $scope.showHeader = $scope.withoutHeader == undefined || !$scope.withoutHeader; 

        // start from 0
        $scope.getCssClass = function (day) {
            var dayHue = $scope.trainingCalendar.daysWork[day];
            if (dayHue == $scope.GOOD) {
                return 'training_calendar_cell_good';
            }
            
            if (dayHue == $scope.NOT_BAD) {
                return 'training_calendar_cell_not_bad';
            }

            if (dayHue == $scope.BAD) {
                return 'training_calendar_cell_bad';
            }

            if (dayHue == $scope.UNKNOWN) {
                return 'training_calendar_cell_empty';
            }
        };
    
    }
})();