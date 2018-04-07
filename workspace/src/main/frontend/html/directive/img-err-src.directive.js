(function() {
    'use strict';

    angular
        .module('h4t-eng')
        .directive('imgErrSrc',  ImgErrSrcFunction);

    function ImgErrSrcFunction() {
        return {
            link: function (scope, elm, attrs, ctrl) {
                elm
                    .bind('load', function () {
                        if (attrs.src != attrs.imgErrSrc) {
                            scope.$apply(function () {
                                if (scope && scope.wordForm) scope.wordForm.pic.$setValidity("brokenimg", true);
                            });
                        }
                    })
                    .bind('error', function () {
                        if (attrs.src != attrs.imgErrSrc) {
                            attrs.$set('src', attrs.imgErrSrc);
                            scope.$apply(function () {
                                if (scope && scope.wordForm) scope.wordForm.pic.$setValidity("brokenimg", false);
                            });
                        }
                    });
            }
        };
    }
})();