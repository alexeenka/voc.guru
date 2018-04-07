/**
 * Root of application.
 *
 * Created by aalexeenka on 4/20/2015.
 */
(function (angular) {
    'use strict';

    var appH4T = angular.module('h4t-eng', [
        'ui.bootstrap',
        'ui.router',
        'ngCookies',
        'h4t-eng.templates',
        'h4t-eng.knowledge',
        'h4t-eng.create-knowledge',
        'h4t-eng.create-word',
        'h4t-eng.training',
        'h4t-eng.counter',
        'h4t-eng.common.service',
        'h4t-eng.friends',
        'h4t-eng.top-users',
        'h4t-eng.word-set',
        'h4t-eng.word-set.list',
        'h4t-eng.global-voc',
        'h4t-eng.devtab'
    ]);

    appH4T.config(function ($stateProvider, $urlRouterProvider, $logProvider, $httpProvider, $uibTooltipProvider) {
        $logProvider.debugEnabled(true);
        // For any unmatched url, send to /create-knowledge
        $urlRouterProvider.otherwise("/training");
        $httpProvider.interceptors.push('errorHttpInterceptor');

        $httpProvider.defaults.headers.common = {
            'voc-version': appGlobals.vocVersion()
        };

        // disable tooltip for mobile and tablets, description: https://github.com/angular-ui/bootstrap/issues/2525
        disableTooltipForMobileAndTablets($uibTooltipProvider);
    });

    function disableTooltipForMobileAndTablets(tooltipProvider) {
        var parser = new UAParser();
        var result = parser.getResult();
        var touch = result.device && (result.device.type === 'tablet' || result.device.type === 'mobile');
        if ( touch ){
            tooltipProvider.options({trigger: 'dontTrigger'});
        } else {
            tooltipProvider.options({trigger: 'mouseenter'});
        }
    }

    /**
     * Menu controller
     */
    appH4T.controller('PageHeaderCtrl', function ($scope, $http, commonService, speechSettingsService) {
        commonService.userInfo().then(function (userInfo) {
            $scope.userImg = userInfo.userImg;
            $scope.userFN = userInfo.userFN;
            jQuery("#userInfoMenu").show();
        });

        $scope.activeTab = 'training';
        $scope.isActiveTab = function(tab){return $scope.activeTab == tab};
        $scope.setActiveTab = function(tab){return $scope.activeTab = tab};

        $scope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
            if (angular.isDefined(toState.data.tabName)) {
                $scope.activeTab = toState.data.tabName;
            }
        });

        $scope.speechSettings = function() {
            speechSettingsService.openModalSpeechSettings();
        }
    });


    appH4T.run(function($rootScope, $timeout, $document, $log, $window, $http, modalService, workService, speechSettingsService) {
        $log.debug(currentTime() + 'Run application!');

        if (!('speechSynthesis' in window)) {
            speechSettingsService.openModalSpeechSettings();
        }

        initialize_WorkFunctionality($document, $log, workService);
        initialize_HttpErrorHandler($rootScope, $log, $window, $http, modalService);
    });

    function initialize_WorkFunctionality($document, $log, workService) {
        var bodyElement = angular.element($document);
        
        workService.startWork();
        angular.forEach(['keydown', 'keyup', 'click', 'mousemove', 'DOMMouseScroll', 'mousewheel', 'mousedown', 'touchstart', 'touchmove', 'scroll', 'focus'],
            function (EventName) {
                bodyElement.bind(EventName, function (e) {
                    workService.doWork();
                });
            }
        );
    }

    // *** ERROR_HTTP_INTERCEPTOR: begin *** //
    appH4T.factory('errorHttpInterceptor', ErrorHttpInterceptor);
    ErrorHttpInterceptor.$inject = ['$q', '$rootScope', '$log' , '$window'];

    function ErrorHttpInterceptor($q, $rootScope, $log, $window) {
        return {
            response : function (response) {
                return response;
            },
            responseError : function (rejection) {
                $rootScope.$broadcast("appH4TServerError", {rejection: rejection});
                return $q.reject(rejection);
            }
        };
    }

    function initialize_HttpErrorHandler($rootScope, $log, $window, $http, modalService) {
        // we use it to avoid multiply call for modal about session_timeout
        var session_timeout_not_in_progress = true;
        var no_internet_connection_not_in_progress = true;

        // Global server error processing: BEGIN
        $rootScope.$on('appH4TServerError', function (event, args) {
            var rejection = args.rejection;

            if (!rejection) {
                return;
            }

            if (406 === rejection.status) {
                window.location.href = "/";
                return;
            }

            if (403 === rejection.status) {
                if (session_timeout_not_in_progress) {
                    session_timeout_not_in_progress = false;
                    modalService.showModal({}, {
                        showCloseButton: false,
                        actionButtonText: 'Ок',
                        headerText: 'Уведомление',
                        bodyText: 'К сожалению, ваша сессия закончилась. Зайдите еще раз на сайт.'
                    }).then(function (result) {
                        session_timeout_not_in_progress = true;
                        window.location.href = "/";
                    });
                }

                return;
            }

            if (-1 === rejection.status || 0 === rejection.status) {
                if (needToRepeat(rejection.config.url)) {
                    $log.error(currentTime() + "No internet connection! But need to save result!");
                    return;
                }

                $log.error(currentTime() + "No internet connection!");
                no_internet_connection_not_in_progress = false;
                modalService.showModal({}, {
                    showCloseButton: false, actionButtonText: 'Ок',
                    headerText: 'Уведомление',
                    bodyText: 'Похоже на то, что у вас нету подключения к интернету.'
                }).then(function (result) {
                    no_internet_connection_not_in_progress = true;
                    window.location.href = "/";
                });
                return;
            }

            $log.error(currentTime() + "Server response with error! Begin");
            $log.error(currentTime() + "Error details: ", rejection);
            $log.error(currentTime() + "Server response with error! End");

            var url = rejection.config ? rejection.config.url : 'unknown-url';
            var status = rejection.status ? rejection.status : 'unknown-status';
            var statusText = rejection.statusText ? rejection.statusText : 'unknown-status-text';

            jQuery('#serverError').show();
            jQuery('#errorSummary').text('URL: ' + url + ', status: ' + status + ', status-text: ' + statusText);
            jQuery('#errorContent').contents().find('html').html(rejection.data);
        });
        // Global server error processing: END
    }

    function needToRepeat(checkedURL) {
        var repeatUrls = [
            '/rest/training/save-eng-rus-workout',
            '/rest/training/save-def-eng-workout',
            '/rest/training/save-rus-eng-workout',
            '/rest/training/save-img-eng-workout',
            '/rest/training/save-sen-eng-workout'
        ];
        var needToRepeat = false;
        for (var repeatUrlsIndex = 0, repeatUrlsN = repeatUrls.length; repeatUrlsIndex<repeatUrlsN; repeatUrlsIndex++) {
            // startsWith not support for some browser, check list: http://www.w3schools.com/jsref/jsref_startswith.asp
            if (checkedURL.lastIndexOf(repeatUrls[repeatUrlsIndex], 0) === 0) {
                return true;
            }
        }
        return false;
    }
    // *** ERROR_HTTP_INTERCEPTOR: end *** //


}(window.angular));