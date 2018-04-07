(function() {
    'use strict';

    angular
        .module('h4t-eng')
        .directive('pressEnter',  PressEnterFunction);

    function PressEnterFunction() {
        return function (scope, element, attrs) {
            element.bind("keydown keypress", function (event) {
                if(event.which === 13) {
                    scope.$apply(function (){
                        scope.$eval(attrs.pressEnter);
                    });

                    event.preventDefault();
                }
            });
        };
    }

})();
