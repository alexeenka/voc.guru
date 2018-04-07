(function() {
    'use strict';

    angular
        .module('h4t-eng')
        .directive('engValValidator',  EngValValidator);


    function EngValValidator() {
        return {
            require: 'ngModel',
            link: EngValValidatorLink
        }
    }
    function EngValValidatorLink(scope, elem, attr, ngModel) {

        function isCorrectFstLetter(value) {
            return /^[A-Za-z]+.*$/.test(value);
        }

        //For DOM -> model validation
        ngModel.$parsers.unshift(function(value) {
            var valid = isCorrectFstLetter(value);
            ngModel.$setValidity('engValFstLetter', valid);
            return valid ? value : undefined;
        });

        //For model -> DOM validation
        ngModel.$formatters.unshift(function(value) {
            var valid = isCorrectFstLetter(value);
            ngModel.$setValidity('engValFstLetter', valid);
            return value;
        });
    }
})();