(function (angular) {
    'use strict';

    angular.module('h4t-eng').factory("speechSynthesisService", SpeechSynthesisService);

    SpeechSynthesisService.$inject = ['$log', '$q', '$cookies', '$timeout', 'commonService'];

    function SpeechSynthesisService($log, $q, $cookies, $timeout, commonService) {

        var TEXT_CHUNK_SIZE = 250;

        var speechJob;

        var initVoicesJob = $q.defer();

        var speakerEng = {lang: 'en-US', desc: 'English (United States)', voice: null};
        var englishSpeakers = [];
        var speakerRu = {lang: 'ru-RU', desc: 'Russian', voice: null};


        // "en_GB" , 'en_IN', 'en_US' android codes
        var languagesEnglish = {
            'en-US' : {desc: 'English (United States)', voices: []},
            'en-GB' : {desc: 'English (United Kingdom)', voices: []}
        };
        // 'ru_RU' android codes
        var languagesRussian = {
            'ru-RU' : {desc: 'Russian', voices: []}
        };

        var browserInfo = new UAParser().getBrowser();
        browserInfo['nameUpper'] = browserInfo.name.toUpperCase();
        browserInfo['isFireFoxOrEdge'] = browserInfo.nameUpper === 'FIREFOX'
            || browserInfo.nameUpper === "EDGE" || browserInfo.nameUpper === 'SAFARI';
        $log.debug(currentTime() + " browser info: ", JSON.stringify(browserInfo));

        if ('speechSynthesis' in window) {
            if (window.mobilecheck()) {
                englishSpeakers = prepareEnglishSpeakers();
                initVoicesJob.resolve();
            }

            window.speechSynthesis.onvoiceschanged = function() {
                if (initVoicesJob.promise.$$state.status) return;
                $log.debug(currentTime() + " InitVoice: onvoiceschanged");
                desktopInitializeVoices();
                englishSpeakers = prepareEnglishSpeakers();
                initVoicesJob.resolve();
            };

            // special condition for firefox and for edge
            if (browserInfo.isFireFoxOrEdge) {
                window.speechSynthesis.onvoiceschanged();
            }
        }

        return {
            cancelSpeech: cancelSpeech,

            startSpeechJob: startSpeechJob,
            stopSpeechJob: stopSpeechJob,
            getSpeechJob: getSpeechJob,

            getInitVoiceJob: getInitVoiceJob,
            getEnglishLang: getEnglishLang,
            getRussianLang: getRussianLang,

            getSpeakerEng: getSpeakerEng,
            setSpeakerEng: setSpeakerEng,
            getSpeakerRu: getSpeakerRu,
            getEnglishSpeakers: getEnglishSpeakers,

            sayAnyText: sayAnyText,
            sayRuText: sayRuText,
            sayEngText: sayEngText,
            dontSupportSpeechSynthesis: isNotSupportSpeechSynthesis,
            saveVoice: saveVoice,
            saveVolume: saveVolume,

            getBrowserInfo: getBrowserInfo
        };

        function getBrowserInfo() {
            return browserInfo;
        }

        function getEnglishLang() {
            return languagesEnglish;
        }

        function getRussianLang() {
            return languagesRussian;
        }

        function getVoices() {
            return voices;
        }

        function getEnglishSpeakers() {
            return englishSpeakers;
        }

        function getSpeakerEng() {
            return speakerEng;
        }

        function setSpeakerEng(speaker) {
            speakerEng.lang = speaker.lang;
            speakerEng.desc = speaker.desc;
            speakerEng.voice = speaker.voice;
        }

        function getSpeakerRu() {
            return speakerRu;
        }

        function saveVoice(voiceName) {
            $cookies.putObject("voiceEng", voiceName);
        }

        function saveVolume(volume) {
            $cookies.putObject("voiceVolume", volume);
        }


        function isNotSupportSpeechSynthesis() {
            return !('speechSynthesis' in window);
        }

        function getInitVoiceJob() {
            return initVoicesJob.promise;
        }

        function getSpeechJob() {
            return speechJob.promise;
        }

        function startSpeechJob() {
            speechJob = $q.defer();
        }

        function stopSpeechJob(status) {
            if (speechJob) speechJob.resolve({status:status});
            speechJob = null;
        }

        function cancelSpeech() {
            if (isNotSupportSpeechSynthesis()) return;
            speechSynthesis.cancel();
            stopSpeechJob("break");
        }

        function sayRuText (text) {
            return sayAnyText(getSpeakerRu(), 1, text);
        }

        function sayEngText(text) {
            return sayAnyText(getSpeakerEng(), 1, text);
        }

        function sayAnyText(speaker, voiceVolume, text) {
            try {
                /**
                 * Voice is not set for Android and iOS!
                 */
                if (isNotSupportSpeechSynthesis() || !speaker || (!speaker.voice && !window.mobilecheck())) {
                    var tJob = $q.defer();
                    $timeout(function () {
                        tJob.resolve();
                    });

                    if (!speaker) {
                        $log.error(currentTime() + "Speaker is NULL");
                    } else if (!speaker.voice && !window.mobilecheck()) {
                        $log.error(currentTime() + "Speaker Voice is NULL");
                    }

                    return tJob.promise;
                }

                var phrases = [];
                if (text.constructor === Array) {
                    for (var ti = 0, tn = text.length; ti < tn; ti++) {
                        var iPhrases = splitToPhrase(text[ti]);
                        // concat arrays, fast method: http://stackoverflow.com/questions/4156101/javascript-push-array-values-into-another-array
                        for (var pi = 0, pn = iPhrases.length; pi < pn; pi++) {
                            phrases.push(iPhrases[pi]);
                        }
                    }
                } else {
                    phrases = splitToPhrase(text);
                }

                speechSynthesis.cancel();
                stopSpeechJob("break");
                startSpeechJob();

                for (var i = 0, n = phrases.length; i < n; i++) {
                    var msg = new SpeechSynthesisUtterance();
                    msg.lang = speaker.lang;
                    if (speaker.voice) msg.voice = speaker.voice;
                    msg.rate = 1;
                    msg.volume = voiceVolume;
                    msg.text = phrases[i];

                    if (i + 1 == n) {
                        msg.onend = function (event) {
                            stopSpeechJob("success");
                        };
                    }

                    msg.onerror = function (event) {
                        stopSpeechJob("break");
                    };

                    speechSynthesis.speak(msg);
                }

                return getSpeechJob();
            } catch(err) {
            }
        }

        /**
         * There is a bug in Chrome do not say text more than 250 chars
         *
         * @param text - text to pronounce
         *
         * @returns {*}
         */
        function splitToPhrase(text) {
            text = text.trim();

            if (text.length < TEXT_CHUNK_SIZE) {
                var result = [];
                result.push(text);
                return result;
            }

            var pieces = text.split(' ');

            // combine
            var phrases = [];
            var iPhrase = '';
            for (var i = 0, n = pieces.length; i < n; i++) {
                pieces[i] = pieces[i].trim();
                if (pieces[i].length == 0) continue;

                if (pieces[i].length >= TEXT_CHUNK_SIZE) {
                    $log.error(currentTime() + ' skip word: ', pieces[i]);
                    pieces[i] = 'Skip! ';
                } else {
                    pieces[i] = pieces[i] + ' ';
                }

                if ((iPhrase.length + pieces[i].length) >= TEXT_CHUNK_SIZE) {
                    phrases.push(iPhrase);
                    iPhrase = pieces[i];
                } else {
                    iPhrase += pieces[i];
                }

                if (i + 1 == n) {
                    phrases.push(iPhrase);
                }
            }

            $log.debug(currentTime() + "Split text, phrases: ", phrases);
            return phrases;
        }

        function desktopInitializeVoices() {
            if (window.mobilecheck()) {
                return;
            }

            var cookiesVoiceName = $cookies.getObject("voiceEng"); // todo: correct work
            // if (cookiesVoiceName && cookiesVoiceName == voice.name && speakerEng == null) speakerEng = {lang: voice.lang, voice: voice};

            var systemVoices = speechSynthesis.getVoices();

            $log.debug(currentTime() + " InitVoice: voices, size: ", systemVoices.length);

            systemVoices.forEach(function (voice) {
                var languageEnglish = languagesEnglish[voice.lang];
                if (languageEnglish) {
                    languageEnglish.voices.push(voice);
                }

                var languageRussian = languagesRussian[voice.lang];
                if (languageRussian) {
                    languageRussian.voices.push(voice);
                    speakerRu.voice = voice;
                    speakerRu.desc = languageRussian.desc;
                }
            });
            // if we have cookie value, than we use it

            // work with Alexey Alexeenka preferences:
            if (speakerEng.voice == null) {
                var prefLang = languagesEnglish['en-GB'];
                if (prefLang) {
                    for (var pi=0, pn=prefLang.voices.length; pi<pn; pi++) {
                        if (prefLang.voices[pi].name == 'Google UK English Female') speakerEng = {lang: 'en-GB', desc: prefLang.desc, voice: prefLang.voices[pi]};
                    }
                }
            }
            // if no preference, took first what we can found
            if (speakerEng.voice == null) {
                for (var iLangKey in languagesEnglish) {
                    if (!languagesEnglish.hasOwnProperty(iLangKey)) {
                        continue;
                    }

                    var iLang = languagesEnglish[iLangKey];
                    if (iLang.voices.length > 0) {
                        speakerEng = {lang: iLangKey, voice: iLang.voices[0], desc: prefLang.desc};
                        break;
                    }
                }

            }

            $log.debug(currentTime() + "Result: languagesEnglish: " + JSON.stringify(languagesEnglish));
            $log.debug(currentTime() + "Result: languageRussian: " + JSON.stringify(languagesRussian));

            if (speakerEng) {
                $log.debug(currentTime() + "Result: ActiveVoiceEng : Lang [" + speakerEng.lang + "] voice [" + (speakerEng.voice == null ? 'NULL' : speakerEng.voice.name) + "] name");
            } else {
                $log.debug(currentTime() + "Result: ActiveVoiceEng : NULL");
            }

            if (speakerRu) {
                $log.debug(currentTime() + "Result: ActiveVoiceRu : Lang [" + speakerRu.lang + "] voice [" + (speakerRu.voice == null ? 'NULL' : speakerRu.voice.name)  + "] name");
            } else {
                $log.debug(currentTime() + "Result: ActiveVoiceRu : NULL");
            }
        }

        function prepareEnglishSpeakers() {
            var speakerList = [];
            for (var iLangKey in languagesEnglish) {
                if (!languagesEnglish.hasOwnProperty(iLangKey)) {
                    continue;
                }

                var iLang = languagesEnglish[iLangKey];
                // only for mobiles
                if ((!iLang.voices || iLang.voices.length == 0) && window.mobilecheck()) {
                    speakerList.push({lang : iLangKey, desc: iLang.desc, voice : null});
                    continue;
                }

                for (var i=0, n=iLang.voices.length; i<n; i++) {
                    speakerList.push({lang : iLangKey, desc: iLang.desc, voice : iLang.voices[i]});
                }
            }

            return speakerList;
        }
    }



}(window.angular));