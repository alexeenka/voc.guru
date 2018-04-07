(function (angular) {
    'use strict';

    angular.module('h4t-eng.common.service', []);
    angular.module('h4t-eng.common.service').factory("commonService", CommonService);

    CommonService.$inject = ['$cookies', '$http', '$q', '$log'];

    function CommonService($cookies, $http, $q, $log) {

        var userJob = $q.defer();
        $http.get('/rest/user-info').success(function(response) {
            $log.debug(currentTime() + "Get user info.");
            userJob.resolve({
                userFN:response.userFN,
                userImg:response.userImg,
                fbLink:response.fbLink,
                vkLink:response.vkLink
            });
        });

        return {
            showHeader: showHeader,
            hideHeader: hideHeader,
            userInfo: userInfoImpl,

            getSpeechVolume: getSpeechVolume,
            getRandomValue: getRandomValue,

            playApplause: playApplauseImpl,

            joinValues: joinValuesImpl
        };

        function playApplauseImpl() {
            var vAudio = document.getElementById("applauseAudio");
            vAudio.volume = getSpeechVolume() * 0.2;
            vAudio.play();
            return vAudio.duration;
        }

        function joinValuesImpl(arr) {
            var result = "";
            for (var i=0,n=arr.length; i<n; i++) {
                result += arr[i] + ".";
                if (i + 1 < n) {
                    result += " ";
                }
            }
            return result;
        }

        function showHeader() {
            jQuery("#mainNav").show();
            jQuery("#copyrightSection").show();
            jQuery("body").css("padding-top", "70px")
        }

        function hideHeader() {
            jQuery("#mainNav").hide();
            jQuery("#copyrightSection").hide();
            jQuery("body").css("padding-top", "2px")
        }

        function userInfoImpl() {
            $log.debug(currentTime() + "Ask user info.");
            return userJob.promise;
        }

        // todo move to speech-synthesis.service.js
        function getSpeechVolume() {
            if (window.mobilecheck()) return 1;

            var cookiesVolume = $cookies.getObject("voiceVolume");
            if (!cookiesVolume) cookiesVolume = 0.75;
            return cookiesVolume;
        }

        function getRandomValue(values) {
            if (!values || values.length == 0) return "";
            var randomIndex = Math.floor(Math.random()*values.length);
            return values[randomIndex].trim();
        }
    }


}(window.angular));