package guru.h4t_eng.test.service;

import guru.h4t_eng.datasource.TrainingDataSource;
import guru.h4t_eng.datasource.WorkoutType;
import guru.h4t_eng.datasource.training.TrainingFinishDto;
import guru.h4t_eng.test.WithLogging;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static guru.h4t_eng.test.util.Utils4Tst.readResource;
import static guru.h4t_eng.util.gson.TrainingWordGsonUtil.toTrainingFinishDto;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * TrainingResultTst.
 * <p>
 * Created by aalexeenka on 14.09.2016.
 */
public class TrainingResultTest extends WithLogging {

    @Test
    public void testExtractTrainingWordValues() {
        final String trainingResult = readResource("/training/training-result.json");
        final ArrayList<TrainingFinishDto> trainingWordValues = toTrainingFinishDto(trainingResult);

        {
            final UUID userId = UUID.randomUUID();
            for (WorkoutType iType : WorkoutType.values()) {
                final TrainingDataSource.RunnableQuery runnableQuery = TrainingDataSource.updateScoreQuery(iType, userId, trainingWordValues);
                assertNotNull(runnableQuery);

                final String expected = "UPDATE user_training SET "
                        + iType.columnName + " = " + iType.columnName + " + ? , "
                        + iType.columnName + " = " + iType.columnName + " - ? , "
                        + iType.repeatColumnName + " = " + iType.repeatColumnName + " + ?  " +
                        "where user_id=? ";
                assertEquals(expected, runnableQuery.getStatement());
                {
                    final HashMap newWords = (HashMap) runnableQuery.getParams().get(0);
                    assertEquals(6, newWords.size());
                }
                {
                    final HashSet deleteWords = (HashSet) runnableQuery.getParams().get(1);
                    assertEquals(1, deleteWords.size());
                }
                {
                    final HashMap repeatedWords = (HashMap) runnableQuery.getParams().get(2);
                    assertEquals(4, repeatedWords.size());
                }
                assertEquals(userId, runnableQuery.getParams().get(3));

            }
        }
    }

}
