package guru.h4t_eng.test.datasource;

import com.datastax.driver.core.utils.UUIDs;
import guru.h4t_eng.datasource.TrainingDataSource;
import guru.h4t_eng.datasource.WorkoutType;
import guru.h4t_eng.datasource.training.*;
import guru.h4t_eng.model.training.TrainingWordValue;
import guru.h4t_eng.test.WithLogging;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static guru.h4t_eng.datasource.TrainingDataSource.MAX_INDICATOR_POINTS;
import static guru.h4t_eng.test.util.Utils4Tst.randomLetter;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * TrainingDataSourceTst.
 * <p>
 * Created by aalexeenka on 06.09.2016.
 */
public class TrainingDataSourceTst extends WithLogging {

    public static final TrainingDataSource trainingDataSource = TrainingDataSource.getInstance();

    @Test
    public void trainingRepeatedWords() {
        // No record test
        for (WorkoutType workout : WorkoutType.values()) {
            System.out.println("test: " + getTestName() + ", workout: " + workout);
            {
                final List<RepeatWordMapValue> repeatedWords = trainingDataSource.getRepeatedWords(workout, UUIDs.timeBased(), true);
                assertEquals(0, repeatedWords.size());
            }

            // there are record test
            {
                final UUID userId = UUIDs.timeBased();
                final ArrayList<RepeatWordMapValue> repeatedWords = new ArrayList<>();
                for (int j = 0; j < 10; j++) {
                    String word = randomLetter() + "_" + getTestName() + "_" + j;
                    if (j % 2 == 0) {
                        repeatedWords.add(new RepeatWordMapValue(word, new RepeatWordValue(LocalDate.now().minusDays(2), 1)));
                    } else {
                        repeatedWords.add(new RepeatWordMapValue(word, new RepeatWordValue(LocalDate.now(), 1)));
                    }

                }
                trainingDataSource.updateRepeatedValues(workout, userId, repeatedWords);

                final List<RepeatWordMapValue> needToBeRepeated = trainingDataSource.getRepeatedWords(workout, userId, true);
                assertEquals(5, needToBeRepeated.size());
                assertThat(repeatedWords, hasItems(needToBeRepeated.toArray(new RepeatWordMapValue[needToBeRepeated.size()])));
            }

            // delete test
            {
                final UUID userId = UUIDs.timeBased();
                final ArrayList<RepeatWordMapValue> repeatedWords = new ArrayList<>();
                String word = "A_delete_" + getTestName();
                repeatedWords.add(new RepeatWordMapValue(word, new RepeatWordValue(LocalDate.now().minusDays(2), 1)));
                trainingDataSource.updateRepeatedValues(workout, userId, repeatedWords);
                {
                    final List<RepeatWordMapValue> needToBeRepeated = trainingDataSource.getRepeatedWords(workout, userId, true);
                    assertEquals(1, needToBeRepeated.size());
                }

                trainingDataSource.deleteWord(userId, word);
                {
                    final List<RepeatWordMapValue> needToBeRepeated = trainingDataSource.getRepeatedWords(workout, userId, true);
                    assertEquals(0, needToBeRepeated.size());
                }

            }
        }
    }

    @Test
    public void testNewWordDeleteWordOneWord() {
        // 1. Add new word
        final UUID user = UUIDs.timeBased();
        final String engVal = "testNewWordDeleteWord_01";
        trainingDataSource.newWord(user, engVal);

        // Test that new values inside table
        {
            final Training training = trainingDataSource.getTraining(user);
            Assert.assertNotNull(training);

            final TrainingWordWorkoutValues trainingWordValue = TrainingWordWorkoutValues.valueOf(training, engVal);
            Assert.assertNotNull(trainingWordValue);
            for (WorkoutType workoutType : WorkoutType.values()) {
                Assert.assertEquals(new TrainingWordValue(0, 0, 0), trainingWordValue.getPerWorkout(workoutType));
            }
        }

        // 2. Delete word
        trainingDataSource.deleteWord(user, engVal);

        // Test that new values inside table
        {
            final Training training = trainingDataSource.getTraining(user);
            Assert.assertNotNull(training);
            for (WorkoutType workoutType : WorkoutType.values()) {
                assertEquals(training.getWorkouts().get(workoutType).size(), 0);
            }
        }
    }

    @Test
    public void testNewWordDeleteWordTwoWord() {
        // 1. Add new word
        final UUID user = UUIDs.timeBased();
        final String word1 = "testNewWordDeleteWord_01";
        final String word2 = "testNewWordDeleteWord_02";
        final String word3 = "testNewWordDeleteWord_03";
        trainingDataSource.newWord(user, word1);
        trainingDataSource.newWord(user, word2);

        // Test that new values inside table
        {
            final Training training = trainingDataSource.getTraining(user);
            Assert.assertNotNull(training);

            {
                final TrainingWordWorkoutValues trainingWordValue = TrainingWordWorkoutValues.valueOf(training, word1);
                Assert.assertNotNull(trainingWordValue);
                for (WorkoutType workoutType : WorkoutType.values()) {
                    Assert.assertEquals(new TrainingWordValue(0, 0, 0), trainingWordValue.getPerWorkout(workoutType));
                }
            }

            {
                final TrainingWordWorkoutValues trainingWordValue = TrainingWordWorkoutValues.valueOf(training, word2);
                Assert.assertNotNull(trainingWordValue);
                for (WorkoutType workoutType : WorkoutType.values()) {
                    Assert.assertEquals(new TrainingWordValue(0, 0, 0), trainingWordValue.getPerWorkout(workoutType));
                }
            }

            {
                final TrainingWordWorkoutValues trainingWordValue = TrainingWordWorkoutValues.valueOf(training, word3);
                Assert.assertNull(trainingWordValue);
            }
        }

        // 2. Delete word
        trainingDataSource.deleteWord(user, word1);

        // Test that new values inside table
        {
            final Training training = trainingDataSource.getTraining(user);
            Assert.assertNotNull(training);

            {
                final TrainingWordWorkoutValues wordValue1 = TrainingWordWorkoutValues.valueOf(training, word1);
                Assert.assertNull(wordValue1);
                final TrainingWordWorkoutValues wordValue2 = TrainingWordWorkoutValues.valueOf(training, word2);
                Assert.assertNotNull(wordValue2);
                for (WorkoutType workoutType : WorkoutType.values()) {
                    Assert.assertEquals(new TrainingWordValue(0, 0, 0), wordValue2.getPerWorkout(workoutType));
                }
            }
        }

        trainingDataSource.deleteWord(user, word2);
        {
            final Training training = trainingDataSource.getTraining(user);
            Assert.assertNotNull(training);
            for (WorkoutType workoutType : WorkoutType.values()) {
                assertEquals(training.getWorkouts().get(workoutType).size(), 0);
            }
        }
    }

    @Test
    public void testUpdateCountFunctionality() {
        final UUID userId = UUIDs.timeBased();
        {
            final int count = trainingDataSource.countRemainedTrainingWords(userId, WorkoutType.DEF_ENG);
            Assert.assertEquals(0, count);

            trainingDataSource.newWord(userId, "aaa_1");
            trainingDataSource.newWord(userId, "aaa_2");
            trainingDataSource.newWord(userId, "aaa_3");
        }

        {
            final Training training = trainingDataSource.getTraining(userId);
            for (WorkoutType workoutType : WorkoutType.values()) {
                Assert.assertEquals(3, training.getCountRemainedTrainingWords(workoutType));
                Assert.assertEquals(3, trainingDataSource.countRemainedTrainingWords(userId, workoutType));
            }
        }

        {
            {
                final List<TrainingWordMapValue> trainingWords = trainingDataSource.getTrainingWords(WorkoutType.ENG_RUS, userId);
                Assert.assertEquals(trainingWords.size(), 3);
                assertThat(trainingWords, containsInAnyOrder(
                        new TrainingWordMapValue("aaa_1", 0, 0),
                        new TrainingWordMapValue("aaa_2", 0, 0),
                        new TrainingWordMapValue("aaa_3", 0, 0)
                ));
            }


            ArrayList<TrainingFinishDto> values = new ArrayList<>();
            values.add(new TrainingFinishDto(
                    "aaa_2",
                    new TrainingWordValue(130, 2, 0),
                    null
            ));
            values.add(new TrainingFinishDto(
                    "aaa_3",
                    new TrainingWordValue(130, 0, 0),
                    null
            ));
            trainingDataSource.updateScore(WorkoutType.ENG_RUS, userId, values);

            {
                final List<TrainingWordMapValue> trainingWords = trainingDataSource.getTrainingWords(WorkoutType.ENG_RUS, userId);
                Assert.assertEquals(trainingWords.size(), 3);
                assertThat(trainingWords, containsInAnyOrder(
                        new TrainingWordMapValue("aaa_1", 0, 0),
                        new TrainingWordMapValue("aaa_2", 130, 2),
                        new TrainingWordMapValue("aaa_3", 130, 0)
                ));

                Assert.assertEquals(3, trainingDataSource.countRemainedTrainingWords(userId, WorkoutType.ENG_RUS));
            }
        }

        {
            ArrayList<TrainingFinishDto> values = new ArrayList<>();
            values.add(new TrainingFinishDto(
                    "aaa_1",
                    new TrainingWordValue(MAX_INDICATOR_POINTS, 0, 0),
                    null
            ));
            values.add(new TrainingFinishDto(
                    "aaa_2",
                    new TrainingWordValue(80, 0, 0),
                    null
            ));
            values.add(new TrainingFinishDto(
                    "aaa_3",
                    new TrainingWordValue(155, 0, 0),
                    null
            ));
            trainingDataSource.updateScore(WorkoutType.ENG_RUS, userId, values);

            {
                final List<TrainingWordMapValue> trainingWords = trainingDataSource.getTrainingWords(WorkoutType.ENG_RUS, userId);
                Assert.assertEquals(trainingWords.size(), 1);
                assertThat(trainingWords, containsInAnyOrder(
                        new TrainingWordMapValue("aaa_2", 80, 0)
                ));

                Assert.assertEquals(1, trainingDataSource.countRemainedTrainingWords(userId, WorkoutType.ENG_RUS));
            }
        }

        {
            ArrayList<TrainingFinishDto> values = new ArrayList<>();
            values.add(new TrainingFinishDto(
                    "aaa_1",
                    new TrainingWordValue(MAX_INDICATOR_POINTS, 0, 0),
                    null
            ));
            values.add(new TrainingFinishDto(
                    "aaa_2",
                    new TrainingWordValue(MAX_INDICATOR_POINTS, 0, 0),
                    null
            ));
            values.add(new TrainingFinishDto(
                    "aaa_3",
                    new TrainingWordValue(155, 0, 0),
                    null
            ));
            trainingDataSource.updateScore(WorkoutType.ENG_RUS, userId, values);

            {
                final List<TrainingWordMapValue> trainingWords = trainingDataSource.getTrainingWords(WorkoutType.ENG_RUS, userId);
                Assert.assertEquals(trainingWords.size(), 0);
                Assert.assertEquals(0, trainingDataSource.countRemainedTrainingWords(userId, WorkoutType.ENG_RUS));
            }


            for (WorkoutType workoutType : WorkoutType.values()) {
                if (WorkoutType.ENG_RUS == workoutType) {
                    Assert.assertEquals(0, trainingDataSource.countRemainedTrainingWords(userId, WorkoutType.ENG_RUS));
                    continue;
                }
                Assert.assertEquals(3, trainingDataSource.countRemainedTrainingWords(userId, workoutType));
            }
        }
    }
}
