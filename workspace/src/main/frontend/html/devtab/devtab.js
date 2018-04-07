/**
 * h4t-eng.
 *
 * Created by aalexeenka on 20/07/2015.
 */
angular
    .module('h4t-eng.devtab', [])
    .config
    (function ($stateProvider) {
        $stateProvider.state('devtab', {
                url: "/devtab",
                templateUrl: '/html/devtab/devtab.html',
                data: {tabName: "devtab"},
                controller: function ($scope, speechSynthesisService) {
                    $scope.devSpeechSupport = 'HMM...';
                    $scope.devSpeechCheck = function() {
                        $scope.devSpeechSupport = 'speechSynthesis' in window;
                    };
                    $scope.devVoices = [];
                    $scope.getVoices = function() {
                        $scope.devVoices = speechSynthesis.getVoices();
                    };
                    $scope.engNumber = 34;
                    $scope.speak = function () {
                        speechSynthesis.cancel();
                        var voice = speechSynthesis.getVoices()[$scope.engNumber];

                        var msg = new SpeechSynthesisUtterance();
                        msg.voice = voice;
                        msg.rate = 0.3;
                        msg.volume = 1;
                        msg.text = 'Write it on your heart that every day is the best day in the year.';
                        msg.lang = voice.lang;

                        speechSynthesis.speak(msg);
                    };
                    $scope.ruNumber = 33;
                    $scope.speakRu = function () {
                        speechSynthesis.cancel();
                        var voice = speechSynthesis.getVoices()[$scope.ruNumber];

                        var msg = new SpeechSynthesisUtterance();
                        msg.voice = voice;
                        msg.rate = 0.3;
                        msg.volume = 1;
                        msg.text = 'Путешествие — передвижение по какой-либо территории или акватории с целью их изучения, а также с общеобразовательными, познавательными, спортивными и другими целями.';
                        msg.lang = voice.lang;

                        speechSynthesis.speak(msg);
                    }
                }
            }
        )
    });

