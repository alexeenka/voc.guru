package guru.h4t_eng.test.rest.training;

import com.datastax.driver.core.utils.UUIDs;
import com.google.gson.reflect.TypeToken;
import guru.h4t_eng.datasource.TrainingDataSource;
import guru.h4t_eng.datasource.WordDataSource;
import guru.h4t_eng.datasource.WorkoutType;
import guru.h4t_eng.datasource.training.RepeatWordMapValue;
import guru.h4t_eng.datasource.training.RepeatWordValue;
import guru.h4t_eng.datasource.training.TrainingFinishDto;
import guru.h4t_eng.datasource.training.TrainingWordMapValue;
import guru.h4t_eng.model.training.TrainingWord;
import guru.h4t_eng.model.training.TrainingWordValue;
import guru.h4t_eng.rest.training.TrainingRest;
import guru.h4t_eng.test.rest.config.AbstractRest4Tst;
import guru.h4t_eng.test.rest.config.AnyUser4TstFilter;
import guru.h4t_eng.test.util.TestContext;
import guru.h4t_eng.util.gson.TrainingWordGsonUtil;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static guru.h4t_eng.datasource.TrainingDataSource.*;
import static guru.h4t_eng.datasource.training.RepeatWordValue.INITIAL_PERIOD;
import static guru.h4t_eng.datasource.training.RepeatWordValue.POINTS_MULTIPLY_FACTOR;
import static guru.h4t_eng.rest.training.TrainingRest.NEEDED_WORDS_FOR_TRAINING;
import static guru.h4t_eng.rest.training.TrainingRest.getTrainingRepeatNumbers;
import static guru.h4t_eng.test.rest.training.TrainingUtils4Tst.*;
import static guru.h4t_eng.test.util.Utils4Tst.randomLetter;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.*;

/**
 * TrainingRestRepeatedWordsTest.
 *
 * Created by aalexeenka on 15.03.2017.
 */
public class TrainingRestRepeatedWordsTest extends AbstractRest4Tst {

    @Override
    protected Class getRestClass() {
        return TrainingRest.class;
    }

    @Override
    protected Class getUserSecurityFilter() {
        return AnyUser4TstFilter.class;
    }

    private static final TrainingDataSource trainingDataSource = TrainingDataSource.getInstance();
    private static final WordDataSource wordDataSource = WordDataSource.getInstance();

    @Test
    public void noTraining() {
        final UUID userId = UUIDs.timeBased();
        TestContext.getInstance().setTestUserId(userId);

        for (WorkoutType workoutType : WorkoutType.values()) {
            Response response = loadWorkoutResponse(target(), workoutType);
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            final String trainingWordsStr = response.readEntity(String.class);
            assertThat(trainingWordsStr, containsString("errorMsg"));
        }
    }

    @Test
    public void testNumbersForTrainingAndRepeat() {
        assertNumbers(Arrays.asList(2, 8), getTrainingRepeatNumbers(2, 35));
        assertNumbers(Arrays.asList(7, 3), getTrainingRepeatNumbers(8, 35));
        assertNumbers(Arrays.asList(7, 3), getTrainingRepeatNumbers(7, 3));

        assertNumbers(Arrays.asList(9, 1), getTrainingRepeatNumbers(10, 1));
        assertNumbers(Arrays.asList(8, 2), getTrainingRepeatNumbers(10, 2));
        assertNumbers(Arrays.asList(7, 3), getTrainingRepeatNumbers(10, 3));
        assertNumbers(Arrays.asList(7, 3), getTrainingRepeatNumbers(10, 4));
        assertNumbers(Arrays.asList(7, 3), getTrainingRepeatNumbers(10, 5));
        assertNumbers(Arrays.asList(10, 0), getTrainingRepeatNumbers(10, 0));
        assertNumbers(Arrays.asList(10, 0), getTrainingRepeatNumbers(11, 0));


        assertNumbers(Arrays.asList(9, 1), getTrainingRepeatNumbers(11, 1));
        assertNumbers(Arrays.asList(7, 3), getTrainingRepeatNumbers(15, 10));
        assertNumbers(Arrays.asList(7, 3), getTrainingRepeatNumbers(25, 6));
        assertNumbers(Arrays.asList(9, 1), getTrainingRepeatNumbers(35, 1));
        assertNumbers(Arrays.asList(8, 2), getTrainingRepeatNumbers(35, 2));
        assertNumbers(Arrays.asList(7, 3), getTrainingRepeatNumbers(37, 3));
        assertNumbers(Arrays.asList(10, 0), getTrainingRepeatNumbers(39, 0));

        assertNumbers(Arrays.asList(0, 0), getTrainingRepeatNumbers(1, 1));
        assertNumbers(Arrays.asList(1, 9), getTrainingRepeatNumbers(1, 9));
        assertNumbers(Arrays.asList(2, 8), getTrainingRepeatNumbers(2, 9));
        assertNumbers(Arrays.asList(3, 7), getTrainingRepeatNumbers(3, 9));
        assertNumbers(Arrays.asList(4, 6), getTrainingRepeatNumbers(4, 9));

        assertNumbers(Arrays.asList(5, 5), getTrainingRepeatNumbers(5, 9));
        assertNumbers(Arrays.asList(5, 5), getTrainingRepeatNumbers(5, 6));
        assertNumbers(Arrays.asList(5, 5), getTrainingRepeatNumbers(5, 7));
        assertNumbers(Arrays.asList(0, 0), getTrainingRepeatNumbers(5, 4));
        assertNumbers(Arrays.asList(6, 4), getTrainingRepeatNumbers(6, 4));
        assertNumbers(Arrays.asList(0, 0), getTrainingRepeatNumbers(6, 3));

        assertNumbers(Arrays.asList(7, 3), getTrainingRepeatNumbers(7, 3));
        assertNumbers(Arrays.asList(7, 3), getTrainingRepeatNumbers(8, 3));
        assertNumbers(Arrays.asList(8, 2), getTrainingRepeatNumbers(9, 2));
        assertNumbers(Arrays.asList(0, 0), getTrainingRepeatNumbers(7, 2));
        assertNumbers(Arrays.asList(9, 1), getTrainingRepeatNumbers(9, 1));
        assertNumbers(Arrays.asList(8, 2), getTrainingRepeatNumbers(8, 2));
    }

    private void assertNumbers(List<Integer> actual, Integer[] result) {
        if (!(result[0] == 0 && result[1] ==0)) {
            assertEquals(NEEDED_WORDS_FOR_TRAINING, result[0] + result[1]);
        }
        Assert.assertThat(actual, contains(result));
    }

    @Test
    public void test7new3repeated() {
        // Generate userId
        final UUID userId = UUIDs.timeBased();
        TestContext.getInstance().setTestUserId(userId);

        // Generate test data
        generateTrainingWords(userId, 10, 3);

        // Load and Save words!
        for (WorkoutType workout : WorkoutType.values()) {
            // Assert load data
            final ArrayList<TrainingWord> trainingWords = loadMainAssertion(workout, 3, 7);
            // Assert save data
            generateFinishSaveCheck(userId, workout, trainingWords);
        }
    }

    @Test
    public void test0new10repeated() {
        // Generate userId
        final UUID userId = UUIDs.timeBased();
        TestContext.getInstance().setTestUserId(userId);

        // Generate test data
        generateTrainingWords(userId, 10, 10);

        // Load and Save words!
        for (WorkoutType workout : WorkoutType.values()) {
            // Assert load data
            final ArrayList<TrainingWord> trainingWords = loadMainAssertion(workout, 10, 0);
            // Assert save data
            generateFinishSaveCheck(userId, workout, trainingWords);
        }
    }

    @Test
    public void test30new2repeated() {
        // Generate userId
        final UUID userId = UUIDs.timeBased();
        TestContext.getInstance().setTestUserId(userId);

        // Generate test data
        generateTrainingWords(userId, 30, 2);

        // Load and Save words!
        for (WorkoutType workout : WorkoutType.values()) {
            // Assert load data
            final ArrayList<TrainingWord> trainingWords = loadMainAssertion(workout, 2, 8);
            // Assert save data
            generateFinishSaveCheck(userId, workout, trainingWords);
        }
    }

    @Test
    public void test30new10repeated() {
        // Generate userId
        final UUID userId = UUIDs.timeBased();
        TestContext.getInstance().setTestUserId(userId);

        // Generate test data
        generateTrainingWords(userId, 30, 10);

        // Load and Save words!
        for (WorkoutType workout : WorkoutType.values()) {
            // Assert load data
            final ArrayList<TrainingWord> trainingWords = loadMainAssertion(workout, 3, 7);
            // Assert save data
            generateFinishSaveCheck(userId, workout, trainingWords);
        }
    }

    @Test
    public void test10new0repeated() {
        // Generate userId
        final UUID userId = UUIDs.timeBased();
        TestContext.getInstance().setTestUserId(userId);

        // Generate test data
        generateTrainingWords(userId, 10, 0);

        // Load and Save words!
        for (WorkoutType workout : WorkoutType.values()) {
            // Assert load data
            final ArrayList<TrainingWord> trainingWords = loadMainAssertion(workout, 0, 10);
            // Assert save data
            generateFinishSaveCheck(userId, workout, trainingWords);
        }
    }


    private void generateFinishSaveCheck(UUID userId, WorkoutType workout, ArrayList<TrainingWord> trainingWords) {
        final ArrayList<TrainingFinishDto> finishWords = new ArrayList<>();
        generateFinishResult(trainingWords, finishWords);
        saveTrainingWords(target(), workout, finishWords);
        checkSaveResult(userId, workout, trainingWords, finishWords);
    }

    private void checkSaveResult(UUID userId, WorkoutType workout, ArrayList<TrainingWord> trainingWords, ArrayList<TrainingFinishDto> finishWords) {
        for (int i=0, n=finishWords.size(); i<n; i++) {
            final TrainingWord iTrainingWord = trainingWords.get(i);
            final TrainingFinishDto iFinishValue = finishWords.get(i);

            if (iTrainingWord.getRepeatWordValue() == null) {
                int currentCase = i % 3;
                switch (currentCase) {
                    case 0:
                    case 1: {
                        final TrainingWordMapValue trainingWordValue = trainingDataSource.getTrainingWordValue(workout, userId, iFinishValue.getW());
                        assertNull(trainingWordValue);
                        final RepeatWordValue repeatWord = getRepeatWord(userId, workout, iTrainingWord.getEngVal());
                        assertNotNull(repeatWord);
                        assertEquals(LocalDate.now(), repeatWord.getLocalDate());
                        assertEquals(INITIAL_PERIOD, repeatWord.getPeriod());
                        break;
                    }
                    case 2: {
                        final TrainingWordMapValue trainingWordValue = trainingDataSource.getTrainingWordValue(workout, userId, iFinishValue.getW());
                        assertNotNull(trainingWordValue);
                        assertEquals(iFinishValue.getTv().getA(), trainingWordValue.getA());
                        assertEquals(iFinishValue.getTv().getS(), trainingWordValue.getS());
                        assertEquals(iFinishValue.getTv().getP(), trainingWordValue.getP());

                        final RepeatWordValue repeatWord = getRepeatWord(userId, workout, iTrainingWord.getEngVal());
                        assertNull(repeatWord);
                        break;
                    }
                }
            } else {
                int currentCase = i % 2;
                final RepeatWordValue repeatWord = getRepeatWord(userId, workout, iTrainingWord.getEngVal());
                assertNotNull(repeatWord);
                assertEquals(LocalDate.now(), repeatWord.getLocalDate());

                switch (currentCase) {
                    case 0:
                        assertEquals(iTrainingWord.getRepeatWordValue().getPeriod() * POINTS_MULTIPLY_FACTOR, repeatWord.getPeriod());
                        break;
                    case 1:
                        assertEquals(iTrainingWord.getRepeatWordValue().getPeriod(), repeatWord.getPeriod());
                        break;
                }
            }
        }
    }

    private void generateFinishResult(ArrayList<TrainingWord> trainingWords, ArrayList<TrainingFinishDto> finishWords) {
        for (int i=0, n = trainingWords.size(); i<n; i++) {
            final TrainingWord trainingWord = trainingWords.get(i);


            TrainingFinishDto trainingFinishDto = new TrainingFinishDto();
            finishWords.add(trainingFinishDto);
            trainingFinishDto.setW(trainingWord.getEngVal());

            if (trainingWord.getRepeatWordValue() == null) {
                int currentCase = i % 3;
                switch (currentCase) {
                    case 0:
                        trainingFinishDto.setTv(new TrainingWordValue(MAX_INDICATOR_POINTS,
                                trainingWord.getTrainingValue().getA() + 1,
                                0
                                )
                        );
                        break;
                    case 1:
                        trainingFinishDto.setTv(new TrainingWordValue(
                                ThreadLocalRandom.current().nextInt(60, 140),
                                trainingWord.getTrainingValue().getA() + 1,
                                NEEDED_STRIKES)
                        );
                        break;
                    case 2:
                        trainingFinishDto.setTv(new TrainingWordValue(
                                ThreadLocalRandom.current().nextInt(0, 140),
                                trainingWord.getTrainingValue().getA() + 1,
                                0
                        ));
                        break;
                }
            } else {
                int currentCase = i % 2;
                switch (currentCase) {
                    case 0:
                        trainingFinishDto.setTv(new TrainingWordValue(MAX_POINTS_PER_WORD, 0, 0));
                        trainingFinishDto.setRv(trainingWord.getRepeatWordValue());
                        break;
                    case 1:
                        trainingFinishDto.setTv(new TrainingWordValue(ThreadLocalRandom.current().nextInt(-5, 9), 0, 0));
                        trainingFinishDto.setRv(trainingWord.getRepeatWordValue());
                        break;
                }
            }
        }
    }

    private RepeatWordValue getRepeatWord(UUID userId, WorkoutType workout, String word) {
        final List<RepeatWordMapValue> repeatedWords = trainingDataSource.getRepeatedWords(workout, userId, false);
        return repeatedWords.stream()
                .filter(repeatWordMapValue -> repeatWordMapValue.getWord().equals(word))
                .findFirst()
                .map(RepeatWordMapValue::getValue)
                .orElse(null);
    }

    private ArrayList<TrainingWord> loadMainAssertion(WorkoutType workout, int expectedCountRWords, int expectedCountTWord) {
        Response response = loadWorkoutResponse(target(), workout);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        final String trainingWordsStr = response.readEntity(String.class);
        assertThat(trainingWordsStr, not(containsString("errorMsg")));

        final ArrayList<TrainingWord> trainingWordsList = TrainingWordGsonUtil.GSON.fromJson(trainingWordsStr, new TypeToken<ArrayList<TrainingWord>>() {
        }.getType());
        int countTrainingWord = 0;
        int countRepeatedWord = 0;
        for (TrainingWord trainingWord : trainingWordsList) {
            if (trainingWord.getRepeatWordValue() == null) {
                countTrainingWord++;
                assertThat(trainingWord.getTrainingValue().getP(), lessThan(MAX_INDICATOR_POINTS));
            }
            if (trainingWord.getRepeatWordValue() != null) {
                countRepeatedWord++;
                assertTrue(trainingWord.getRepeatWordValue().isNeedToBeRepeated());
            }
        }
        assertEquals(expectedCountRWords, countRepeatedWord);
        assertEquals(expectedCountTWord, countTrainingWord);

        return trainingWordsList;
    }

    private void generateTrainingWords(UUID userId, int count, int withRepeatedNumber) {
        final ArrayList<String> newWords = new ArrayList<>();
        final ArrayList<TrainingFinishDto> trainingWords = new ArrayList<>();
        final ArrayList<String> repeatedWords = new ArrayList<>();

        int unique = 0;

        // trainingNumber
        for (;unique < count - withRepeatedNumber; unique++) {
            String word = randomLetter() + "_" + getTestName() + "_" + unique;
            newWords.add(word);
            trainingWords.add(
                    new TrainingFinishDto(
                            word,
                            new TrainingWordValue(0,0,0),
                            null
                    )
            );
        }

        for (;unique < count; unique++) {
            String word = randomLetter() + "_" + getTestName() + "_" + unique;
            newWords.add(word);
            repeatedWords.add(word);
            if (unique % 2 == 0) {
                trainingWords.add(new TrainingFinishDto(word, new TrainingWordValue(MAX_INDICATOR_POINTS, 0, 0), null));
            } else {
                trainingWords.add(new TrainingFinishDto(word, new TrainingWordValue(0, 0, NEEDED_STRIKES), null));
            }

        }
        generateWords(userId, newWords);

        for (WorkoutType workout : WorkoutType.values()) {
            // update training with new values and check
            trainingDataSource.updateScore(workout, userId, trainingWords);

            // new training words, check size
            {
                final List<TrainingWordMapValue> remainingTrainingWords = trainingDataSource.getTrainingWords(workout, userId);
                assertEquals(count - withRepeatedNumber, remainingTrainingWords.size());
            }
            {
                final List<RepeatWordMapValue> newRepeatedWords = trainingDataSource.getRepeatedWords(workout, userId, false);
                assertEquals(repeatedWords.size(), withRepeatedNumber);
                assertEquals(repeatedWords.size(), newRepeatedWords.size());
                for (RepeatWordMapValue iMapValue : newRepeatedWords) {
                    final RepeatWordValue repeatWordValue = iMapValue.getValue();
                    assertThat(repeatedWords, hasItems(iMapValue.getWord()));
                    assertFalse(repeatWordValue.isNeedToBeRepeated());
                    assertEquals(LocalDate.now(), repeatWordValue.getLocalDate());
                    assertEquals(INITIAL_PERIOD, repeatWordValue.getPeriod());
                }
            }

            // Make repeated words visible for test
            {
                ArrayList<RepeatWordMapValue> repeatWordMapValues = new ArrayList<>();
                final LocalDate mustRepeatDate = LocalDate.now().minusDays(2);
                final int oneDayPeriod = 1;
                for (String repeatedWord : repeatedWords) {

                    repeatWordMapValues.add(new RepeatWordMapValue(repeatedWord, new RepeatWordValue(mustRepeatDate, oneDayPeriod)));
                }
                trainingDataSource.updateRepeatedValues(workout, userId, repeatWordMapValues);
                {
                    final List<RepeatWordMapValue> newRepeatedWords = trainingDataSource.getRepeatedWords(workout, userId, true);
                    assertEquals(repeatedWords.size(), withRepeatedNumber);
                    assertEquals(repeatedWords.size(), newRepeatedWords.size());

                    for (RepeatWordMapValue iMapValue : newRepeatedWords) {
                        final RepeatWordValue repeatWordValue = iMapValue.getValue();
                        assertThat(repeatedWords, hasItems(iMapValue.getWord()));
                        assertTrue(repeatWordValue.isNeedToBeRepeated());
                        assertEquals(mustRepeatDate, repeatWordValue.getLocalDate());
                        assertEquals(oneDayPeriod, repeatWordValue.getPeriod());
                    }
                }
            }
        }
    }

}