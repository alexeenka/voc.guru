(function (angular) {
    'use strict';

    angular.module('h4t-eng')
        .controller('SpeechSettingsController',  SpeechSettingsController)
        .factory("speechSettingsService", SpeechSettingsService);

    SpeechSettingsService.$inject = ['$log', '$uibModal'];

    function SpeechSettingsService($log, $uibModal) {
        return {
            openModalSpeechSettings: openModalSpeechSettings
        };

        function openModalSpeechSettings() {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: '/html/speech-settings/speech-settings.html',
                controller: 'SpeechSettingsController',
                backdrop: 'static',
                resolve: {}
            });
            modalInstance.rendered.then(function() {});
            modalInstance.result.then(function (modalResult) {});
        }
    }

    SpeechSettingsController.$inject = ['$scope','$cookies','$log','$uibModalInstance','commonService','speechSynthesisService'];
    function SpeechSettingsController($scope,$cookies,$log,$uibModalInstance,commonService,speechSynthesisService) {
        $scope.closeSpeechSettings = function() {
            $uibModalInstance.close();
        };

        $scope.dontSupportSpeechSynthesis = speechSynthesisService.dontSupportSpeechSynthesis();

        $scope.isAndroid = false;
        try {
            $scope.isAndroid = new UAParser().getOS().name.toLocaleLowerCase() == 'android';
        } catch (e) {
            $log.debug(currentTime() + " isAndroid-error: " + e);
        }
        $scope.isIOS = false;
        try {
            $scope.isIOS = new UAParser().getOS().name.toLocaleLowerCase() == 'ios';
        } catch (e) {
            $log.debug(currentTime() + " isIOS-error: " + e);
        }


        $scope.isInitSpeech = false;
        speechSynthesisService.getInitVoiceJob().then(function() {
            $scope.speakerEng = speechSynthesisService.getSpeakerEng();
            $scope.speakerRu = speechSynthesisService.getSpeakerRu();
            $scope.englishSpeakers = speechSynthesisService.getEnglishSpeakers();

            $log.debug(currentTime() + "Eng voice: ", JSON.stringify($scope.speakerEng));
            $log.debug(currentTime() + "Ru voice: ", JSON.stringify($scope.speakerRu));
            $log.debug(currentTime() + "English speakers: ", JSON.stringify($scope.englishSpeakers));

            $scope.isInitSpeech = true;
        });

        $scope.isMobile = window.mobilecheck();
        $scope.browserInfo = speechSynthesisService.getBrowserInfo();
        $scope.englishSpeakers = [];

        $scope.stopPlay = function() {
            speechSynthesis.cancel();
        };

        $scope.playText = function (speaker) {
            speechSynthesisService.sayAnyText(speaker, 1, commonService.getRandomValue(quotes));
        };
        $scope.playRuText = function (speaker) {
            speechSynthesisService.sayAnyText(speaker, 1, commonService.getRandomValue(quotesRu));
        };
        $scope.chooseSpeaker = function(speaker) {
            speechSynthesisService.setSpeakerEng(speaker);
            $scope.speakerEng = speechSynthesisService.getSpeakerEng();
        };
        $scope.isActiveEngSpeaker = function(speaker) {
            if (!speaker || !$scope.speakerEng) return;
            if (speaker.lang != $scope.speakerEng.lang) return false;
            if (!$scope.speakerEng.voice && !speaker.voice) return true;

            return $scope.speakerEng.voice.name == speaker.voice.name;
        };

        // https://litemind.com/best-famous-quotes-2/
        var quotes = [
            'It’s not that I’m so smart, it’s just that I stay with problems longer. Albert Einstein', 'Eighty percent of success is showing up. Woody Allen',
            'I respect faith, but doubt is what gets you an education. Wilson Mizner',
            'The greatest obstacle to discovery is not ignorance; it is the illusion of knowledge. Daniel J. Boorstin',
            'The pessimist complains about the wind; the optimist expects it to change; the realist adjusts the sails. William Arthur Ward',
            'If you don’t make mistakes, you’re not working on hard enough problems. And that’s a big mistake. Frank Wilczek',
            'You can never get enough of what you don’t really need. Eric Hoffer',
            'Insanity: doing the same thing over and over again and expecting different results. Albert Einstein',
            'Do not confuse motion and progress. A rocking horse keeps moving but does not make any progress. Alfred A. Montapert',
            'I don’t know the key to success, but the key to failure is trying to please everybody. Bill Cosby',
            'Tomorrow is often the busiest day of the week. Spanish Proverb',
            'Be yourself; everyone else is already taken. Oscar Wilde',
            'There is a great difference between worry and concern. A worried person sees a problem, and a concerned person solves a problem. Harold Stephens',
            'It is easier to fight for one’s principles than to live up to them. Alfred Adler',
            'I hear: I forget / I see: I remember / I do: I understand. Chinese Proverb',
            'Discipline is just choosing between what you want now and what you want most. Unknown Author',
            'The very best thing you can do for the whole world is to make the most of yourself. Wallace Wattles',
            'Good judgment comes from experience, and experience comes from bad judgment. Barry LePatner',
            'When I do good, I feel good; when I do bad, I feel bad, and that is my religion. Abraham Lincoln',
            'Success consists of going from failure to failure without loss of enthusiasm. Winston Churchill'
        ];

        var quotesRu = [
            'Величайшее богатство народа – его язык! Тысячелетиями накапливаются и вечно живут в слове несметные сокровища человеческой мысли и опыта. Михаил Александрович Шолохов.',
            'Из всех наслаждений, отпущенных человеку, самое изысканное — шевелить мозгами. Борис Акунин',
            'Все в наших руках, поэтому их нельзя опускать! Коко Шанель',
            'Язык - одежда мыслей. Сэмюэл Джонсон.',
            'Мудрый человек требует всего только от себя, ничтожный же человек требует всего от других. Лев Николаевич Толстой',
            'Упавший духом гибнет раньше срока. Омар Хайям.'
        ];
    }

}(window.angular));