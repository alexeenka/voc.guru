package guru.h4t_eng.test.service;

import guru.h4t_eng.datasource.TrainingDataSource;
import guru.h4t_eng.datasource.WordDataSource;
import guru.h4t_eng.datasource.WorkEffortDataSource;
import guru.h4t_eng.datasource.WorkoutType;
import guru.h4t_eng.datasource.training.TrainingWordMapValue;
import guru.h4t_eng.datasource.vocabulary.VocabularyDatasource;
import guru.h4t_eng.exception.CassandraException;
import guru.h4t_eng.model.dictionary.DictionaryWord;
import guru.h4t_eng.rest.words.model.FormData;
import guru.h4t_eng.service.WordService;
import guru.h4t_eng.test.WithLogging;
import guru.h4t_eng.test.util.Utils4Tst;
import guru.h4t_eng.util.FormDataUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

/**
 * CrudWordDatasourceTst.
 *
 * Created by aalexeenka on 12/17/2015.
 */
public class CrudWordDatasourceTst extends WithLogging {

    private WordService wordService = WordService.getInstance();
    private WordDataSource wordDataSource = WordDataSource.getInstance();
    private static final TrainingDataSource trainingDataSource = TrainingDataSource.getInstance();


    @Test
    public void word2Parsing () {
        final FormData formData = Utils4Tst.newFormDataFromJson__1();

        assertEquals("", formData.getPrevEngVal());
        Assert.assertNotNull(formData.getWord());
    }

    public long getWorkEffortTime(FormData formData) {
        final long time = WorkEffortDataSource.getInstance().getTime(
                formData.getUser(),
                formData.getCurrentDayYear().getYear(),
                formData.getCurrentDayYear().getDayOfYear()
        );
        return time;
    }

    private void checkWorkEffortTable(FormData formData, long beforeEffortTime) {
        assertEquals(getWorkEffortTime(formData) - beforeEffortTime, WordService.NEW_WORD_EFFORT_SEC);
    }


    @Test
    public void crudTest() {
        // CREATE CHECK
        {
            final FormData formData = Utils4Tst.newFormDataFromJson__1();
            formData.setUser(Utils4Tst.getUserInfo().getUserId());
            final DictionaryWord word = formData.getWord();

            long beforeEffortTime = getWorkEffortTime(formData);
            wordService.saveWord(formData);
            final DictionaryWord loadedWord = wordDataSource.loadSingleWord(formData.getUser(), word.getEngVal());

            // training list
            checkTrainingTablesNotEmpty(formData.getUser(), word.getEngVal());
            checkAlphabetIndexWithValue(formData.getUser(), word.getEngVal());
            checkWorkEffortTable(formData, beforeEffortTime);

            assertTrue(FormDataUtil.formatWord(Utils4Tst.newFormDataFromJson__1().getWord()).equalsExcludeDispensableFields(loadedWord));
        }

        // EDIT WORD
        {
            final FormData formData = Utils4Tst.newFormDataFromJson__2();
            formData.setUser(Utils4Tst.getUserInfo().getUserId());
            final DictionaryWord word = formData.getWord();

            long beforeEffortTime = getWorkEffortTime(formData);
            wordService.saveWord(formData);
            final DictionaryWord loadedWord = wordDataSource.loadSingleWord(formData.getUser(), word.getEngVal());

            // training list
            checkTrainingTablesNotEmpty(Utils4Tst.getUserInfo().getUserId(), word.getEngVal());
            checkAlphabetIndexWithValue(Utils4Tst.getUserInfo().getUserId(), word.getEngVal());
            assertEquals(beforeEffortTime, getWorkEffortTime(formData)); // when edit effort the same

            assertTrue(FormDataUtil.formatWord(Utils4Tst.newFormDataFromJson__2().getWord()).equalsExcludeDispensableFields(loadedWord));
        }

        // DELETE WORD
        {
            final FormData formData = Utils4Tst.newFormDataFromJson__2();
            formData.setUser(Utils4Tst.getUserInfo().getUserId());
            final DictionaryWord word = formData.getWord();

            wordDataSource.deleteWord(formData.getUser(), word.getEngVal());
            try {
                wordDataSource.loadSingleWord(formData.getUser(), word.getEngVal());
            } catch (Throwable e) {
                assertEquals(e.getClass(), CassandraException.class);
            }
            try {
                wordDataSource.loadSingleWord(formData.getUser(), word.getEngVal());
            } catch (Throwable e) {
                assertEquals(e.getClass(), CassandraException.class);
            }
            // training list
            checkAlphabetIndexWithoutValue(formData.getUser(), word.getEngVal());
            checkTrainingTablesEmpty(formData.getUser(), word.getEngVal());
        }
    }


    private void checkAlphabetIndexWithoutValue(UUID user, String engVal) {
        Set<String> values = VocabularyDatasource.getInstance().loadWordsByLetter(user, engVal.charAt(0));
        assertThat(values, not(hasItem(engVal)));
    }

    private void checkAlphabetIndexWithValue(UUID user, String engVal) {
        Set<String> values = VocabularyDatasource.getInstance().loadWordsByLetter(user, engVal.charAt(0));
        assertThat(values, hasItem(engVal));
    }


    private void checkTrainingTablesEmpty(UUID userID, String engVal) {
        for (WorkoutType workoutType : WorkoutType.values()) {
            final TrainingWordMapValue trainingWordValue = trainingDataSource.getTrainingWordValue(workoutType, userID, engVal);
            assertNull(trainingWordValue);
        }
    }

    private void checkTrainingTablesNotEmpty(UUID userID, String engVal) {
        for (WorkoutType workoutType : WorkoutType.values()) {
            final TrainingWordMapValue trainingWordValue = trainingDataSource.getTrainingWordValue(workoutType, userID, engVal);
            assertNotNull(trainingWordValue);
            assertEquals(engVal, trainingWordValue.getWord());
            assertEquals(0, trainingWordValue.getP());
            assertEquals(0, trainingWordValue.getA());
        }
    }
}
