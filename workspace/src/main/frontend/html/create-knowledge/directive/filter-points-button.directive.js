(function() {
    'use strict';

    angular
        .module('h4t-eng')
        .directive('filterPointsButton',  FilterPointsButton);


    function FilterPointsButton() {
        return {
            restrict: 'E',
            scope: {
                pointsOption: '=',
                label: '@'
            },
            templateUrl: '/html/create-knowledge/directive/filter-points-button.directive.html',
            controller: FilterPointsButtonController
        }
    }

    FilterPointsButtonController.$inject = ['$scope'];

    function FilterPointsButtonController($scope) {
        $scope.is_open = false;
    }
})();