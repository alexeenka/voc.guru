/**
 * VOC.GURU
 * Controller to create new word!
 *
 * Created by aalexeenka on 22/07/2016.
 *
 */
angular.module('h4t-eng.create-word', []).config(CreateWordProvider);

function CreateWordProvider($stateProvider) {
    $stateProvider.state('create-word-state', {
        url: "/create-word?:engVal,:wordSet",
        params: {
            // engVal: null
        },
        templateUrl: '/html/create-knowledge/create-word/create-word.html',
        data: {tabName: "create-knowledge"},
        controller: CreateWordController
    });
}

function CreateWordController($scope, $state, $stateParams, $log, knowledgeService, trainingService, workEffortService, modalService, globalVocService, wordSetService) {
    console.log($stateParams.engVal);

    // work with word set, begin
    $scope.isWordSetUser = false;
    wordSetService.isWordSetUser().then(function (result) {
        $scope.isWordSetUser = result;
    });
    $scope.wordSetId = $stateParams.wordSet;
    // noinspection EqualityComparisonWithCoercionJS
    if ($scope.wordSetId == null) {
        $scope.wordSetId = 1; // current word set
    }
    // work with word set, end

    // noinspection EqualityComparisonWithCoercionJS
    if ($stateParams.engVal == null) {
        $log.debug(currentTime() + "NEW word");

        $scope.prevEngVal = "";

        // new word
        $scope.word = {
            engVal: "",
            engDefs: [{
                val: [""],
                rusValues: [""],
                engSentences: [""]
            }]

        };
    } else {
        $log.debug(currentTime() + "EDIT word", $stateParams.engVal);

        knowledgeService.loadReadySingleWord($stateParams.engVal, $stateParams.wordSet).success(function (response) {
            $log.debug(currentTime() + "Load single word, response: ", response);
            $scope.prevEngVal = response.engVal;
            $scope.word = response;
        });
    }

    var autocompleteWords = $scope.autocompleteWords = [];
    if ($stateParams.engVal == null) {
        $scope.$watch("word.engVal",
            function (newValue, oldValue) {
                if (newValue == oldValue) return;

                if (newValue == undefined || newValue.length < 3) {
                    // clear array
                    autocompleteWords.splice(0, autocompleteWords.length);
                    return;
                }

                globalVocService.autocompleteSearch(newValue).then(function (foundWords) {
                    // clear array
                    autocompleteWords.splice(0, autocompleteWords.length);
                    // add new values
                    foundWords.forEach(function (iWord) {
                        autocompleteWords.push(iWord);
                    });
                });
            }
        );
    }


    $scope.imageBLOB = null;

    $scope.addEngDef = function () {
        $scope.word.engDefs.push({
            val: [""],
            rusValues: [""],
            engSentences: [""]
        });
    };

    $scope.removeEngDef = function (engDef) {
        var index = $scope.word.engDefs.indexOf(engDef);
        $scope.word.engDefs.splice(index, 1);
    };

    $scope.showExtLink = function () {
        //noinspection RedundantIfStatementJS
        if ($scope.word.engVal && $scope.word.engVal.length > 2) return true;
        return false;
    };

    $scope.showYandexPictureLink = function (engDef) {
        if (!engDef) return false;
        if (!engDef.rusValues[0]) return false;

        return true;
    };

    $scope.engVal = function () {
        if ($scope.word.engVal) return $scope.word.engVal;
        return "";
    };

    $scope.engValCollins = function () {
        if ($scope.word.engVal) return $scope.word.engVal.replace(/ /g, '-');
        return "";
    };

    $scope.rusValueFav = function() {
        if (!$scope.word.rusValues) return "";

        var fav;
        for (var i=0, n=$scope.word.rusValues.length; i<n; i++) {
            if ($scope.word.rusValues[i].favorite) {
                fav = $scope.word.rusValues[i];
                break;
            }
        }

        return fav.val;
    };

    $scope.engSentencesSearch = function () {
        if ($scope.word.engVal) return "\"" + $scope.word.engVal + "\"";
        return "";
    };

    // ** Work with dynamic list. Begin
    $scope.dynamic_list_add_val = function (uiValues) {
        uiValues.push("");
    };

    $scope.dynamic_list_delete_val = function (uiValues, uiValue) {
        var index = uiValues.indexOf(uiValue);
        uiValues.splice(index, 1);
    };
    // ** Work with dynamic list. End

    function validationFocusWordForm() {
        focus('wordForm');
    }
    function focus(formId) {
        // find the invalid elements
        var visibleInvalids = jQuery('#' + formId + ' .ng-invalid:visible');
        if (angular.isDefined(visibleInvalids)){
            for (var i=0, N=visibleInvalids.length; i<N; i++) {
                var nodeName = visibleInvalids[i].nodeName;
                if (nodeName.toUpperCase() == 'FORM') continue;
                if (nodeName.toUpperCase() == 'NG-FORM') continue;
                // if we find one, set focus
                visibleInvalids[i].focus();
                break;
            }
        }
    }

    $scope.isNotValidForNonStandardFields = function() {
        // 1. image validation for each engDefs
        for (var i=0, N=$scope.word.engDefs.length; i<N; i++) {
            if (!$scope.word.engDefs[i].imageBLOB && !$scope.word.engDefs[i].imgUrl) {
                return true;
            }
        }

        // 2. word.type validation
        if ($scope.word.type == undefined) {
            return true;
        }
    };

    $scope.saveWord = function (params) {
        var wordForm = params.data;

        wordForm.$submitted = true;
        if (wordForm.engVal.$invalid) {
            validationFocusWordForm();
            return;
        }


        if (wordForm.$invalid || $scope.isNotValidForNonStandardFields()) {
            validationFocusWordForm();
            return;
        }

        // server validation, if ok, then save word
        knowledgeService.validateWord($scope.word).then(function(validationResult) {
            if (validationResult.length > 0)  {
                $scope.serverErrors = validationResult;
                return;
            }
            if (true === params.toWordSet) {
                saveWordToSet(params.wordSetId);
            } else {
                saveWord();
            }

        });
    };

    function saveWordToSet(wordSetId) {
        var saveJob;
        $log.debug(currentTime() + "SaveWordToSet: [" + $scope.word.engVal + "].");
        saveJob = wordSetService.saveWordToSet($scope.word, $scope.prevEngVal, wordSetId);

        saveJob.then(function () {
            $log.debug(currentTime() + "SaveWordToSet. End!");
            $state.go('create-knowledge');
        });

    }

    function saveWord() {
        var saveJob;
        $log.debug(currentTime() + "Save word: [" + $scope.word.engVal + "]. Type: [READY]. Begin.");
        saveJob = knowledgeService.saveReadyWord($scope.word, $scope.prevEngVal);

        saveJob.then(function () {
            $log.debug(currentTime() + "Save word. End!");
            // Applaud only for new words
            if (!$scope.prevEngVal) {
                trainingService.playCrowdCheers();
            }
            workEffortService.updateWorkEffort();
            $state.go('create-knowledge');
        });
    }

    function scaleImage(index, dataURL) {
        var img = new Image();
        img.src = dataURL;
        img.addEventListener("load", function() {
            var MAX_WIDTH = 600;
            var MAX_HEIGHT = 300;
            var width = img.width;
            var height = img.height;
            if (width > MAX_WIDTH) {
                height *= MAX_WIDTH / width;
                width = MAX_WIDTH;
            }
            if (height > MAX_HEIGHT) {
                width *= MAX_HEIGHT / height;
                height = MAX_HEIGHT;
            }

            var canvas = document.getElementById("imgCanvas_" + index);
            canvas.width = width;
            canvas.height = height;
            ctx = canvas.getContext("2d");
            ctx.fillStyle = "#ffffff";
            ctx.fillRect(0,0,width,height);
            ctx.drawImage(img, 0, 0, width, height);

            //var imageType = dataURL.substring(5, dataURL.indexOf(';'));
            var imageType = "image/jpeg"; // always jpeg, It has optimum compression!
            $log.debug(currentTime() + "ImageProcessing: ImageType: " + imageType);
            var scaleDataURL = canvas.toDataURL(imageType, 0.8);

            $log.debug(currentTime() + "ImageProcessing: Modify: " + img.width + "->" + width + " : " + img.height + "->" + height);
            $log.debug(currentTime() + "ImageProcessing: success: original: dataURL: " + dataURL.length);
            $log.debug(currentTime() + "ImageProcessing: success: new: dataURL: " + scaleDataURL.length);

            // if (scaleDataURL.length > dataURL.length) {
            //     $log.debug(currentTime() + "New DataURL is greater than original, so stay with original");
            //     scaleDataURL = dataURL;
            // }


            var imageEl = document.getElementById('wordImg_' + index);
            imageEl.src = scaleDataURL;
            $scope.$apply(function() {
                var engDef = staticGetEngDef(imageEl, index);
                engDef.imageBLOB = dataURItoBlob(scaleDataURL);
            });
        }, false);
    }

    $scope.openImage = function (element, event) {
        var input = event.target;
        if (!input || !input.files[0]) return;

        var reader = new FileReader(); // http://www.javascripture.com/FileReader
        reader.onload = function () {
            var dataURL = reader.result;
            var index = parseInt(element.getAttribute("keyDef"));
            scaleImage(index, dataURL);
        };
        reader.readAsDataURL(input.files[0]);
    };

    function staticGetEngDef(element, index) {
        return angular.element(element).scope().word.engDefs[index];
    }

    // http://stackoverflow.com/questions/6850276/how-to-convert-dataurl-to-file-object-in-javascript
    function dataURItoBlob(dataURI) {
        // convert base64/URLEncoded data component to raw binary data held in a string
        var byteString;
        if (dataURI.split(',')[0].indexOf('base64') >= 0)
            byteString = atob(dataURI.split(',')[1]);
        else
            byteString = unescape(dataURI.split(',')[1]);

        // separate out the mime component
        var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];

        // write the bytes of the string to a typed array
        var ia = new Uint8Array(byteString.length);
        for (var i = 0; i < byteString.length; i++) {
            ia[i] = byteString.charCodeAt(i);
        }

        $log.debug(currentTime() + "Final size: " + ia.length);
        return new Blob([ia], {type:mimeString});
    }

    $scope.cancelWord = function () {
        var modalOptions = {
            closeButtonText: 'Cancel',
            actionButtonText: 'Yes',
            headerText: 'Вы уверены?',
            bodyText: 'Все введенные данные будут потеряны?'
        };

        modalService.showModal({}, modalOptions).then(function (result) {
            $state.go('create-knowledge');
        });
    };

    $scope.getImgURL = knowledgeService.getImgURL;
}