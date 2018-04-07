package guru.h4t_eng.test.rest.training;

import com.datastax.driver.core.utils.UUIDs;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import guru.h4t_eng.datasource.TrainingDataSource;
import guru.h4t_eng.datasource.WorkoutType;
import guru.h4t_eng.datasource.training.TrainingFinishDto;
import guru.h4t_eng.datasource.training.TrainingWordMapValue;
import guru.h4t_eng.model.training.TrainingWord;
import guru.h4t_eng.model.training.TrainingWordValue;
import guru.h4t_eng.rest.training.TrainingRest;
import guru.h4t_eng.test.rest.config.AbstractRest4Tst;
import guru.h4t_eng.test.rest.config.AnyUser4TstFilter;
import guru.h4t_eng.test.util.TestContext;
import guru.h4t_eng.util.gson.TrainingWordGsonUtil;
import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static guru.h4t_eng.datasource.TrainingDataSource.MAX_INDICATOR_POINTS;
import static guru.h4t_eng.datasource.TrainingDataSource.NEEDED_STRIKES;
import static guru.h4t_eng.rest.training.SentenceTrainingUtils.REPLACE_STR;
import static guru.h4t_eng.rest.training.SentenceTrainingUtils.SPEAK_PART;
import static guru.h4t_eng.test.rest.training.TrainingUtils4Tst.loadWorkoutResponse;
import static guru.h4t_eng.test.rest.training.TrainingUtils4Tst.saveTrainingWords;
import static guru.h4t_eng.test.util.Utils4Tst.randomLetter;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.*;

/**
 * TrainingRestTst.
 * <p>
 * Created by aalexeenka on 1/13/2016.
 */
public class TrainingRestTest extends AbstractRest4Tst {

    public static final TrainingDataSource trainingDataSource = TrainingDataSource.getInstance();
    public static final TestContext testContext = TestContext.getInstance();

    @Override
    protected Class getRestClass() {
        return TrainingRest.class;
    }

    protected Class getUserSecurityFilter() {
        return AnyUser4TstFilter.class;
    }

    private UUID generateWords() {
        final UUID userId = UUIDs.timeBased();
        System.out.println("Current UserId: " + userId);
        TestContext.getInstance().setTestUserId(userId);

        final ArrayList<String> newWords = IntStream
                .range(0, 10)
                .mapToObj(j -> randomLetter() + "_" + getTestName() + "_" + j)
                .collect(Collectors.toCollection(ArrayList::new));
        TrainingUtils4Tst.generateWords(userId, newWords);

        System.out.println("Current UserId: " + userId);
        testContext.setTestUserId(userId);

        return userId;
    }

    @Test
    public void testAllWorkouts() throws JSONException {
        UUID userId = generateWords();

        for (WorkoutType workoutType : WorkoutType.values()) {
            testTraining(userId, workoutType);
        }
    }

    @Test
    public void testSenEngExt() {
        UUID userId = generateWords();

        Response response = loadWorkoutResponse(target(), WorkoutType.SEN_ENG);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        final String trainingWordsStr = response.readEntity(String.class);
        assertThat(trainingWordsStr, not(containsString("errorMsg")));

        final ArrayList<TrainingWord> trainingWordsList = TrainingWordGsonUtil.GSON.fromJson(trainingWordsStr, new TypeToken<ArrayList<TrainingWord>>() {}.getType());

        for (TrainingWord trainingWord : trainingWordsList) {

            @SuppressWarnings("unchecked")
            final LinkedTreeMap<String, String> trainingExt = (LinkedTreeMap) trainingWord.getTrainingExt();

            Assert.assertNotNull(trainingExt);

            Assert.assertNotNull(trainingExt.get("sA"));
            assertThat(trainingWord.getEngSentences(), hasItem(trainingExt.get("sA")));
            Assert.assertNotNull(trainingExt.get("sQ"));
            Assert.assertThat(trainingExt.get("sQ"), CoreMatchers.containsString(REPLACE_STR));
            Assert.assertNotNull(trainingExt.get("sH"));
            Assert.assertThat(trainingExt.get("sH"), CoreMatchers.containsString(SPEAK_PART));
            Assert.assertNotNull(trainingExt.get("answer"));
        }
    }

    private void testTraining(UUID userId, WorkoutType workoutType) {
        // 1(!): Load words for user
        Response response = loadWorkoutResponse(target(), workoutType);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        final String trainingWordsStr = response.readEntity(String.class);
        assertThat(trainingWordsStr, not(containsString("errorMsg")));

        final ArrayList<TrainingWord> trainingWordsList = TrainingWordGsonUtil.GSON.fromJson(trainingWordsStr, new TypeToken<ArrayList<TrainingWord>>() {}.getType());
        for (TrainingWord word : trainingWordsList) {
            assertThat(word.getTrainingValue().getP(), lessThan(MAX_INDICATOR_POINTS));
        }

        // 2(!): Emulate training
        ArrayList<TrainingFinishDto> wordsResult = new ArrayList<>();
        for (int i=0, n=trainingWordsList.size(); i<n; i++) {
            final TrainingWord iWord = trainingWordsList.get(i);
            final TrainingWordValue trainingValue = iWord.getTrainingValue();

            final String engVal = iWord.getEngVal();
            if (i + 2 < n) {
                wordsResult.add(new TrainingFinishDto(
                                engVal,
                                new TrainingWordValue(ThreadLocalRandom.current().nextInt(0, 140), trainingValue.getA() + 1, trainingValue.getS()),
                                null
                        )
                );
                continue;
            }

            if (i + 1 < n) {
                wordsResult.add(new TrainingFinishDto(
                        engVal,
                        new TrainingWordValue(77, trainingValue.getA() + 1, NEEDED_STRIKES),
                        null
                ));
                continue;
            }

            if (i + 1 == n) {
                wordsResult.add(new TrainingFinishDto(
                        engVal,
                        new TrainingWordValue(MAX_INDICATOR_POINTS, trainingValue.getA() + 1, trainingValue.getS()),
                        null
                ));
            }
        }

        // 3. save training data
        saveTrainingWords(target(), workoutType, wordsResult);

        // 4. Make check
        for (int i=0, n=wordsResult.size(); i<n; i++) {
            final TrainingFinishDto value = wordsResult.get(i);
            if (i + 2 < n) {
                checkPoints(userId, workoutType, value);
                continue;
            }

            // 2 completed words, by strike and max points
            if (i + 1 <= n) {
                assertNull(trainingDataSource.getTrainingWordValue(workoutType, userId, value.getW()));
                // Check word table: todo
            }
        }
    }

    private void checkPoints(UUID userId, WorkoutType workoutType, TrainingFinishDto expected) {
        final TrainingWordMapValue value = trainingDataSource.getTrainingWordValue(workoutType, userId, expected.getW());

        final String assertMsg = "Word: " + expected.getW() + ", Workout: " + workoutType + ", UserId: " + userId;

        assertNotNull(assertMsg, value);
        assertEquals(assertMsg, expected.getTv(), value);
    }
}