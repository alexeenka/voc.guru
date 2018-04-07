(function () {
    'use strict';

    //** training-thumbnail-bottom directive ** begin **//
    angular.module('h4t-eng.training').directive('trainingThumbnailBottom', TrainingThumbnailBottom);

    function TrainingThumbnailBottom() {
        return {
            restrict: 'E', // only matches element name
            scope: {
                counts: '=' // is two-way binding
            },
            templateUrl: '/html/training/directive/training-thumbnail-bottom.directive.html',
            controller: TrainingThumbnailBottomController
        }
    }

    TrainingThumbnailBottomController.$inject = ['$scope'];

    function TrainingThumbnailBottomController($scope) {
    }
    //** training-thumbnail-bottom directive ** end **//

})();    