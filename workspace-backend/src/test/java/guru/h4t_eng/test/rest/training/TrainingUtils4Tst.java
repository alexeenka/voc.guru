package guru.h4t_eng.test.rest.training;

import com.google.common.collect.ImmutableMap;
import guru.h4t_eng.datasource.TrainingDataSource;
import guru.h4t_eng.datasource.WordDataSource;
import guru.h4t_eng.datasource.WorkoutType;
import guru.h4t_eng.datasource.training.RepeatWordMapValue;
import guru.h4t_eng.datasource.training.Training;
import guru.h4t_eng.datasource.training.TrainingFinishDto;
import guru.h4t_eng.rest.WordListDto;
import guru.h4t_eng.rest.words.model.FormData;
import guru.h4t_eng.test.util.Utils4Tst;
import guru.h4t_eng.util.gson.TrainingWordGsonUtil;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.*;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Utils for training rest test.
 *
 * Created by aalexeenka on 16.03.2017.
 */
public final class TrainingUtils4Tst {

    private static final WordDataSource wordDataSource = WordDataSource.getInstance();
    private static final TrainingDataSource trainingDataSource = TrainingDataSource.getInstance();

    public static final ImmutableMap<WorkoutType, LoadSaveUrls> trainingURLMap = ImmutableMap.<WorkoutType, LoadSaveUrls> builder()
            .put(WorkoutType.ENG_RUS, new LoadSaveUrls("/eng-rus-workout", "/save-eng-rus-workout"))
            .put(WorkoutType.DEF_ENG,  new LoadSaveUrls("/def-eng-workout", "/save-def-eng-workout"))
            .put(WorkoutType.RUS_ENG,  new LoadSaveUrls("/rus-eng-workout", "/save-rus-eng-workout"))
            .put(WorkoutType.IMG_ENG,  new LoadSaveUrls("/img-eng-workout", "/save-img-eng-workout"))
            .put(WorkoutType.SEN_ENG,  new LoadSaveUrls("/sen-eng-workout", "/save-sen-eng-workout"))
            .build();

    public static void generateWords(UUID userId, ArrayList<String> newWords) {
        final FormData formData = Utils4Tst.newDataForSimpleForm();
        formData.setUser(userId);
        for (String word : newWords) {
            formData.getWord().setEngVal(word);
            formData.getWord().getEngDefs().get(0).getEngSentences().set(0, word + " sentence");
            wordDataSource.saveWord(formData, true);
        }

        // check word table
        final ArrayList<WordListDto> savedWords = wordDataSource.loadWordsByEngVals(userId, new HashSet<>(newWords)).getWordList();
        for (WordListDto iWord : savedWords) {
            assertThat(newWords, hasItems(iWord.getEngVal()));
        }

        // check training table: training values
        final Training training = trainingDataSource.getTraining(userId);
        final HashMap<WorkoutType, Map<String, String>> workouts = training.getWorkouts();
        for (Map<String, String> workout : workouts.values()) {
            assertThat(newWords, hasItems(workout.keySet().toArray(new String[newWords.size()])));
        }
        // check training table: repeated values
        for (WorkoutType workout : WorkoutType.values()) {
            final List<RepeatWordMapValue> repeatedWords = trainingDataSource.getRepeatedWords(workout, userId, false);
            assertEquals(0, repeatedWords.size());
        }


    }

    public static Response loadWorkoutResponse(WebTarget target, WorkoutType workoutType) {
        final Invocation.Builder loadRequest = target.path("/training" + trainingURLMap.get(workoutType).loadUrl).request();
        return loadRequest.get();
    }

    public static void saveTrainingWords(WebTarget target, WorkoutType workoutType, ArrayList<TrainingFinishDto> wordsResult) {
        final Invocation.Builder saveRequest = target.path("/training" + trainingURLMap.get(workoutType).saveUrl).request();
        final Response saveResponse = saveRequest.post(Entity.text("{\"result\":" + TrainingWordGsonUtil.GSON.toJson(wordsResult) + ", \"workEffort\": {\"spent\":20, \"year\":2020,\"dayOfYear\":81}}"));
        assertEquals(Response.Status.OK.getStatusCode(), saveResponse.getStatus());
    }

    public static class LoadSaveUrls {

        public LoadSaveUrls(String loadUrl, String saveUrl) {
            this.loadUrl = loadUrl;
            this.saveUrl = saveUrl;
        }

        public String loadUrl;
        public String saveUrl;

    }
}
