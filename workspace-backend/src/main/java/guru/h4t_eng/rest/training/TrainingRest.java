package guru.h4t_eng.rest.training;

import guru.h4t_eng.datasource.TrainingDataSource;
import guru.h4t_eng.datasource.WordDataSource;
import guru.h4t_eng.datasource.WorkEffortDataSource;
import guru.h4t_eng.datasource.WorkoutType;
import guru.h4t_eng.datasource.training.RepeatWordMapValue;
import guru.h4t_eng.datasource.training.TrainingFinishDto;
import guru.h4t_eng.datasource.training.TrainingWordMapValue;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.model.dictionary.DictionaryWord;
import guru.h4t_eng.model.training.TrainingWord;
import guru.h4t_eng.model.training.TrainingWordValue;
import guru.h4t_eng.rest.Main4Rest;
import guru.h4t_eng.util.gson.TrainingWordGsonUtil;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * TrainingRest.
 *
 * Created by Alexey Alexeenka on 14.10.2015.
 */
@Path("/training")
public class TrainingRest extends Main4Rest {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(TrainingRest.class);

    public static final int NEEDED_WORDS_FOR_TRAINING = 10;
    public static final int POINTS_THRESHOLD = 30;
    public static final int MAX_REPEATED_WORD_COUNT = 3;

    private TrainingDataSource trainingDataSource = TrainingDataSource.getInstance();
    private WordDataSource wordDataSource = WordDataSource.getInstance();
    private WorkEffortDataSource workEffortDataSource = WorkEffortDataSource.getInstance();

    @Path("/eng-rus-workout")
    @GET
    public Response getEngRusWorkout(@Context HttpServletRequest httpRequest) {
        return workoutResponse(httpRequest, WorkoutType.ENG_RUS, null);
    }

    @Path("/def-eng-workout")
    @GET
    public Response getDefEngWorkout(@Context HttpServletRequest httpRequest) {
        return workoutResponse(httpRequest, WorkoutType.DEF_ENG, null);
    }

    @Path("/rus-eng-workout")
    @GET
    public Response getRusEngWorkout(@Context HttpServletRequest httpRequest) {
        return workoutResponse(httpRequest, WorkoutType.RUS_ENG, null);
    }

    @Path("/img-eng-workout")
    @GET
    public Response getPicEngWorkout(@Context HttpServletRequest httpRequest) {
        return workoutResponse(httpRequest, WorkoutType.IMG_ENG, null);
    }


    @Path("/sen-eng-workout")
    @GET
    public Response getSenEngWorkout(@Context HttpServletRequest httpRequest) {
        return workoutResponse(httpRequest, WorkoutType.SEN_ENG, trainingWords -> {
            for (TrainingWord trainingWord : trainingWords) {
                trainingWord.setTrainingExt(SentenceTrainingUtils.createSenEngExt(trainingWord, getUserId(httpRequest)));
            }
        });
    }


    public static Integer[] getTrainingRepeatNumbers(int newWords, int repeatedWords) {
        if (newWords + repeatedWords < NEEDED_WORDS_FOR_TRAINING) {
            return new Integer[] {0,0};
        }

        if (newWords >= NEEDED_WORDS_FOR_TRAINING) {
            if (repeatedWords <= MAX_REPEATED_WORD_COUNT) {
                return new Integer[] {NEEDED_WORDS_FOR_TRAINING - repeatedWords, repeatedWords};
            } else {
                return new Integer[] {NEEDED_WORDS_FOR_TRAINING - MAX_REPEATED_WORD_COUNT, MAX_REPEATED_WORD_COUNT};
            }
        }

        // newWords < NEEDED_WORDS_FOR_TRAINING
        if (repeatedWords <= MAX_REPEATED_WORD_COUNT) {
            return new Integer[] {NEEDED_WORDS_FOR_TRAINING - repeatedWords, repeatedWords};
        } else {
            if (newWords >= NEEDED_WORDS_FOR_TRAINING - MAX_REPEATED_WORD_COUNT) {
                return new Integer[] {NEEDED_WORDS_FOR_TRAINING - MAX_REPEATED_WORD_COUNT, MAX_REPEATED_WORD_COUNT};
            } else {
                return new Integer[] {newWords, NEEDED_WORDS_FOR_TRAINING - newWords};
            }
        }
    }

    private class LoadedWordInfo {
        private String word;
        private TrainingWordMapValue trainingWord;
        private RepeatWordMapValue repeatedWord;
    }

    private LoadedWordInfo value4TrainingWord(TrainingWordMapValue trainingWord) {
        LoadedWordInfo loadedWordInfo = new LoadedWordInfo();
        loadedWordInfo.trainingWord = trainingWord;
        loadedWordInfo.word = trainingWord.getWord();

        return loadedWordInfo;
    }

    private LoadedWordInfo value4RepeatedWord(RepeatWordMapValue repeatedWord) {
        LoadedWordInfo loadedWordInfo = new LoadedWordInfo();
        loadedWordInfo.repeatedWord = repeatedWord;
        loadedWordInfo.word = repeatedWord.getWord();

        return loadedWordInfo;
    }


    private Response workoutResponse(HttpServletRequest httpRequest, WorkoutType workoutType, Consumer<ArrayList<TrainingWord>> extensionConsumer) {
        final List<TrainingWordMapValue> trainingWords = trainingDataSource.getTrainingWords(workoutType, getUserId(httpRequest));
        final List<RepeatWordMapValue> repeatedWords = trainingDataSource.getRepeatedWords(workoutType, getUserId(httpRequest), true);

        if (trainingWords.size() + repeatedWords.size() < NEEDED_WORDS_FOR_TRAINING) {
            JSONObject json = new JSONObject();
            // Need to use ResourceBundle.getBundle(); Example: http://www.avajava.com/tutorials/lessons/how-do-i-use-locales-and-resource-bundles-to-internationalize-my-application.html
            final StringBuilder value = new StringBuilder("Нужно минимум " + NEEDED_WORDS_FOR_TRAINING + " слов для тренировки, " +
                    "сейчас у вас тренируемых слов: " + trainingWords.size());
            if (repeatedWords.size() > 0) {
                value.append(", слов на повторении: ").append(repeatedWords.size()).append(".");
            } else {
                value.append(".");
            }

            json.put(
                    "errorMsg",
                    value
            );
            return Response.ok(json.toString(), MediaType.APPLICATION_JSON_TYPE).build();
        }

        // evaluate count number
        final Integer[] trainingRepeatNumbers = getTrainingRepeatNumbers(trainingWords.size(), repeatedWords.size());
        final int trainingWordNumber = trainingRepeatNumbers[0];
        final int repeatedWordNumber = trainingRepeatNumbers[1];


        // when there are a lot of not studied word, need to shuffle first
        Collections.shuffle(trainingWords);
        Collections.shuffle(repeatedWords);
        // sort word, to show new trainingWords first
        trainingWords.sort((w1, w2) -> (Integer.compare(w1.getP(), w2.getP())));
        // trainingWords with point
        randomizeWordsAboveThreshold(trainingWords, trainingWordNumber);

        // prepare final set of word, to load full info from database
        ArrayList<LoadedWordInfo> words2Load = new ArrayList<>(NEEDED_WORDS_FOR_TRAINING);
        {
            // Create random sequence, to load trainingWords
            final ArrayList<Integer> randomSeq = getRandomSequence(trainingWordNumber);
            // training words
            for (int i = 0; i < trainingWordNumber; i++) {
                final TrainingWordMapValue trainingWordValue = trainingWords.get(randomSeq.get(i));
                words2Load.add(value4TrainingWord(trainingWordValue));
            }
            // repeated words
            for (int i=0; i<repeatedWordNumber; i++) {
                final RepeatWordMapValue repeatWordMapValue = repeatedWords.get(i);
                words2Load.add(value4RepeatedWord(repeatWordMapValue));
            }
        }

        // Load final set of words async.
        HashMap<String, DictionaryWord> dictionaryWords;
        {
            Set<String> keys = words2Load.stream()
                    .map(v -> v.word)
                    .collect(Collectors.toSet());

            dictionaryWords = wordDataSource.loadTrainingWordAsync(getUserId(httpRequest), keys);
        }

        // Produce result.
        final ArrayList<TrainingWord> result = new ArrayList<>();
        for (int i = 0; i < NEEDED_WORDS_FOR_TRAINING; i++) {
            final LoadedWordInfo info = words2Load.get(i);
            final DictionaryWord dictionaryWord = dictionaryWords.get(info.word);

            TrainingWord trainingWord = new TrainingWord(dictionaryWord);
            if (info.trainingWord != null) {
                trainingWord.setTrainingValue(info.trainingWord.toTrainingWord());
            }

            if (info.repeatedWord != null) {
                trainingWord.setTrainingValue(new TrainingWordValue(0,0,0));
                trainingWord.setRepeatWordValue(info.repeatedWord.getValue());
            }
            result.add(trainingWord);
        }

        if (extensionConsumer != null) extensionConsumer.accept(result);

        Collections.shuffle(result);

        return Response.ok(TrainingWordGsonUtil.GSON.toJson(result), MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Words already sorted, need to words that above {@link TrainingRest#POINTS_THRESHOLD} point treat them that are equal.
     *
     * @param words words
     * @param neededWordsForTraining count to randomize
     */
    private void randomizeWordsAboveThreshold(List<TrainingWordMapValue> words, int neededWordsForTraining) {
        int i=0;
        for (;i< neededWordsForTraining && words.get(i).getP() <= POINTS_THRESHOLD;) {i++;}

        ArrayList<Integer> usedIndexes = new ArrayList<>(neededWordsForTraining - i);

        int rangeFrom = i;
        int rangeToExcluded = words.size();
        for (; i< neededWordsForTraining; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(rangeFrom, rangeToExcluded);
            while (usedIndexes.contains(randomIndex)) randomIndex = ThreadLocalRandom.current().nextInt(rangeFrom, rangeToExcluded);
            usedIndexes.add(randomIndex);
        }

        for (int j = rangeFrom; j< neededWordsForTraining; j++) {
            TrainingWordMapValue temp = words.get(j);

            Integer randomIndex = usedIndexes.get(j - rangeFrom);
            words.set(j, words.get(randomIndex));
            words.set(randomIndex, temp);
        }
    }

    private ArrayList<Integer> getRandomSequence(int neededWordsForTraining) {
        Random r = ThreadLocalRandom.current();
        ArrayList<Integer> sequence = new ArrayList<>(neededWordsForTraining);
        for (int i = 0; i< neededWordsForTraining; i++) {
            int val = r.nextInt(neededWordsForTraining);
            while (sequence.contains(val)) val = r.nextInt(neededWordsForTraining);
            sequence.add(val);
        }

        return sequence;
    }

    @Path("/save-eng-rus-workout")
    @POST
    public Response saveEngRusWorkout(@Context HttpServletRequest httpRequest) {
        return workoutSave(httpRequest, WorkoutType.ENG_RUS);
    }

    @Path("/save-def-eng-workout")
    @POST
    public Response saveDefEngWorkout(@Context HttpServletRequest httpRequest) {
        return workoutSave(httpRequest, WorkoutType.DEF_ENG);
    }

    @Path("/save-rus-eng-workout")
    @POST
    public Response saveRusEngWorkout(@Context HttpServletRequest httpRequest) {
        return workoutSave(httpRequest, WorkoutType.RUS_ENG);
    }

    @Path("/save-img-eng-workout")
    @POST
    public Response saveImgEngWorkout(@Context HttpServletRequest httpRequest) {
        return workoutSave(httpRequest, WorkoutType.IMG_ENG);
    }

    @Path("/save-sen-eng-workout")
    @POST
    public Response saveSenEngWorkout(@Context HttpServletRequest httpRequest) {
        return workoutSave(httpRequest, WorkoutType.SEN_ENG);
    }

    private Response workoutSave(@Context HttpServletRequest httpRequest, WorkoutType workoutType) {
        try
        {
            String jsonStr = IOUtils.toString(httpRequest.getInputStream(), "UTF-8");
            JSONObject jsonObj = new JSONObject(jsonStr);
            // save training result
            {
                final JSONArray result = jsonObj.getJSONArray("result");
                ArrayList<TrainingFinishDto> trainingWordValues = TrainingWordGsonUtil.toTrainingFinishDto(result.toString());
                trainingDataSource.updateScore(workoutType, getUserId(httpRequest), trainingWordValues);

            }

            // update work effort
            {
                final JSONObject workEffort = jsonObj.getJSONObject("workEffort");
                final int year = workEffort.getInt("year");
                final int dayOfYear = workEffort.getInt("dayOfYear");
                final long spent = workEffort.getLong("spent");
                workEffortDataSource.updateTime(getUserId(httpRequest), year, dayOfYear, spent);
            }

        }
        catch (IOException | JSONException e)
        {
            LOG.error("Can't get json string", e);
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.ok().build();
    }
}