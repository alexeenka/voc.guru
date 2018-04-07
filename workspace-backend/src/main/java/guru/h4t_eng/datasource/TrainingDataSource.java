package guru.h4t_eng.datasource;

import com.datastax.driver.core.Row;
import guru.h4t_eng.datasource.training.*;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.model.training.TrainingWordValue;
import org.slf4j.Logger;

import java.util.*;

/**
 * TrainingDataSource.
 * <p>
 * Created by aalexeenka on 10/16/2015.
 */
public class TrainingDataSource {

    public static final int MAX_INDICATOR_POINTS = 150;
    public static final int MAX_POINTS_PER_WORD = 10;
    public static final int NEEDED_STRIKES = 5;
    public static final String USER_TRAINING_TABLE = "user_training";

    private static CassandraDataSource mds = CassandraDataSource.getInstance();

    private static final Logger LOG = AppLoggerFactory.getH4TLog(TrainingDataSource.class);

    private TrainingDataSource() {
    }

    public static TrainingDataSource getInstance() {
        return new TrainingDataSource();
    }

    private static final String NEW_VALUES_STATEMENT_PART;
    private static final String DELETE_VALUES_STATEMENT_PART;
    private static final String DELETE_REPEATED_VALUES_PART;
    private static final String SELECT_TRAINING_WORD_VALUE_PART;

    static {
        // NEW_VALUES_STATEMENT_PART initialization
        {
            StringBuilder statement = new StringBuilder();

            final WorkoutType[] workoutTypes = WorkoutType.values();
            for (int i = 0, n = workoutTypes.length; i < n; i++) {
                statement.append(workoutTypes[i].columnName).append(" = ").append(workoutTypes[i].columnName).append(" + ?");
                if (i + 1 != n) {
                    statement.append(", ");
                }
            }

            NEW_VALUES_STATEMENT_PART = statement.toString();
        }

        // DELETE_VALUES_STATEMENT_PART initialization
        {
            StringBuilder statement = new StringBuilder();

            final WorkoutType[] workoutTypes = WorkoutType.values();
            for (int i = 0, n = workoutTypes.length; i < n; i++) {
                statement.append(workoutTypes[i].columnName).append(" = ").append(workoutTypes[i].columnName).append(" - ?");
                if (i + 1 != n) {
                    statement.append(", ");
                }
            }

            DELETE_VALUES_STATEMENT_PART = statement.toString();
        }

        // DELETE_REPEATED_VALUES_PART initialization
        {
            StringBuilder statement = new StringBuilder();

            final WorkoutType[] workoutTypes = WorkoutType.values();
            for (int i = 0, n = workoutTypes.length; i < n; i++) {
                statement.append(workoutTypes[i].repeatColumnName).append(" = ").append(workoutTypes[i].repeatColumnName).append(" - ?");
                if (i + 1 != n) {
                    statement.append(", ");
                }
            }

            DELETE_REPEATED_VALUES_PART = statement.toString();
        }

        // SELECT_TRAINING_WORD_VALUE_PART initialization
        {
            StringBuilder statement = new StringBuilder();
            final WorkoutType[] workoutTypes = WorkoutType.values();
            for (int i = 0, n = workoutTypes.length; i < n; i++) {
                statement.append(workoutTypes[i].columnName);
                if (i + 1 != n) {
                    statement.append(", ");
                }
            }

            SELECT_TRAINING_WORD_VALUE_PART = statement.toString();
        }
    }

    public void newWord(UUID uuid, String engVal) {
        // UPDATE user_training SET eng_rus = eng_rus + ?, def_eng = def_eng + ?, rus_eng = rus_eng + ?, img_eng = img_eng + ? WHERE user_id = ?
        final ArrayList<Object> params = new ArrayList<>();
        for (int i = 0, n = WorkoutType.values().length; i < n; i++) {
            params.add(Collections.singletonMap(engVal, TrainingWordValue.NEW_WORD_DATABASE_VALUE));
        }
        params.add(uuid);

        mds.runQuery("UPDATE " + USER_TRAINING_TABLE + " SET " + NEW_VALUES_STATEMENT_PART + " WHERE user_id = ? ", false, params);
    }

    public void deleteWord(UUID uuid, String engVal) {
        // UPDATE user_training SET eng_rus = eng_rus - ?, def_eng = def_eng - ?, rus_eng = rus_eng - ?, img_eng = img_eng - ? WHERE user_id = ?
        final ArrayList<Object> params = new ArrayList<>();
        // For DELETE_VALUES_STATEMENT_PART
        for (int i = 0, n = WorkoutType.values().length; i < n; i++) {
            params.add(new HashSet<>(Collections.singletonList(engVal)));
        }
        // For DELETE_REPEATED_VALUES_PART
        for (int i = 0, n = WorkoutType.values().length; i < n; i++) {
            params.add(new HashSet<>(Collections.singletonList(engVal)));
        }
        params.add(uuid);

        mds.runQuery("UPDATE " + USER_TRAINING_TABLE + " SET " + DELETE_VALUES_STATEMENT_PART + "," + DELETE_REPEATED_VALUES_PART + " WHERE user_id = ? ", false, params);
    }

    public Training getTraining(UUID uuid) {
        final Row one = mds.runQuery("SELECT " + SELECT_TRAINING_WORD_VALUE_PART + " FROM " + USER_TRAINING_TABLE + " WHERE user_id = ?", false, uuid).one();

        Training training = new Training();
        if (one == null) {
            for (WorkoutType workoutType : WorkoutType.values()) {
                training.add(workoutType, new HashMap<>());
            }
            return training;
        }

        for (WorkoutType workoutType : WorkoutType.values()) {
            final Map<String, String> values = one.getMap(workoutType.columnName, String.class, String.class);
            training.add(workoutType, values);
        }

        return training;
    }

    public int countRemainedTrainingWords(UUID userId, WorkoutType workoutType) {
        final Row workout = mds.runQuery("SELECT " +  workoutType.columnName + " from " + USER_TRAINING_TABLE + " where user_id = ?", false, userId).one();
        if (workout == null) return 0;

        final Map<String, String> workoutValue = workout.getMap(workoutType.columnName, String.class, String.class);
        return workoutValue.size();
    }

    public List<TrainingWordMapValue> getTrainingWords(WorkoutType workoutType, UUID userId)
    {
        final Row row = mds.runQuery("select " + workoutType.columnName + " from " + USER_TRAINING_TABLE + " where user_id = ?", false, userId).one();

        ArrayList<TrainingWordMapValue> result = new ArrayList<>();
        if (row == null) {
            return new ArrayList<>();
        }

        final Map<String, String> trainingWords = row.getMap(workoutType.columnName, String.class, String.class);
        for (Map.Entry<String, String> entry : trainingWords.entrySet())
        {
            result.add(TrainingWordMapValue.toValue(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    public List<RepeatWordMapValue> getRepeatedWords(WorkoutType workoutType, UUID userId, boolean forTraining)
    {
        final Row row = mds.runQuery("select " + workoutType.repeatColumnName + " from " + USER_TRAINING_TABLE + " where user_id = ?", false, userId).one();

        ArrayList<RepeatWordMapValue> result = new ArrayList<>();
        if (row == null) {
            return new ArrayList<>();
        }

        final Map<String, String> trainingWords = row.getMap(workoutType.repeatColumnName, String.class, String.class);
        for (Map.Entry<String, String> entry : trainingWords.entrySet())
        {
            final RepeatWordValue repeatWordValue = RepeatWordValue.valueOf(entry.getValue());
            if (forTraining) {
                if (repeatWordValue.isNeedToBeRepeated()) {
                    result.add(new RepeatWordMapValue(entry.getKey(), repeatWordValue));
                }
            } else {
                result.add(new RepeatWordMapValue(entry.getKey(), repeatWordValue));
            }
        }

        return result;
    }

    public TrainingWordMapValue getTrainingWordValue(WorkoutType workoutType, UUID userId, String word) {
        final List<TrainingWordMapValue> trainingWords = getTrainingWords(workoutType, userId);
        if (trainingWords == null) return null;
        for (TrainingWordMapValue wordValue : trainingWords) {
            if (wordValue.getWord().equals(word)) return wordValue;
        }

        return null;
    }

    public void updateScore(WorkoutType workoutType, UUID userId, ArrayList<TrainingFinishDto> trainingWordValues)
    {
        updateScore(mds, workoutType, userId, trainingWordValues);
    }

    public void updateScore(CassandraDataSource dataSource, WorkoutType workoutType, UUID userId, ArrayList<TrainingFinishDto> trainingWordValues)
    {
        final RunnableQuery runnableQuery = updateScoreQuery(workoutType, userId, trainingWordValues);
        if (runnableQuery == null) return;
        dataSource.runQuery(runnableQuery.statement, true, runnableQuery.params);
    }

    public void updateRepeatedValues(WorkoutType workoutType, UUID userId, ArrayList<RepeatWordMapValue> repeatedValues) {
        updateRepeatedValues(mds, workoutType, userId, repeatedValues);
    }

    public void updateRepeatedValues(CassandraDataSource dataSource, WorkoutType workoutType, UUID userId, ArrayList<RepeatWordMapValue> repeatedValues) {
        final Optional<RunnableQuery> runnableQuery = updateRepeatedValuesQuery(workoutType, userId, repeatedValues);
        if (!runnableQuery.isPresent()) return;
        dataSource.runQuery(runnableQuery.get().statement, true, runnableQuery.get().params);
    }

    private Optional<RunnableQuery> updateRepeatedValuesQuery(WorkoutType workoutType, UUID userId, ArrayList<RepeatWordMapValue> repeatedValues) {
        HashMap<String, String> updatedResult = new HashMap<>();
        for (RepeatWordMapValue repeatWordMapValue : repeatedValues) {
            // todo: move it to rest, here just save values
            // final RepeatWordValue nextRepeat = repeatWordMapValue.getValue().nextRepeat(repeatWordMapValue.isMemorized());
            updatedResult.put(repeatWordMapValue.getWord(), repeatWordMapValue.getValue().toDatabaseValue());
        }

        if (updatedResult.size() == 0) return Optional.empty();

        final ArrayList<Object> params = new ArrayList<>();

        StringBuilder statement = new StringBuilder();
        statement.append("UPDATE " + USER_TRAINING_TABLE + " SET ");

        if (updatedResult.size() > 0) {
            statement.append(workoutType.repeatColumnName).append(" = ").append(workoutType.repeatColumnName).append(" + ? ");
            params.add(updatedResult);
        }

        statement.append(" where user_id=? ");
        params.add(userId);

        return Optional.of(new RunnableQuery(statement.toString(), params));
    }


    public static RunnableQuery updateScoreQuery(WorkoutType workoutType, UUID userId, ArrayList<TrainingFinishDto> trainingWordValues) {
        HashMap<String, String> updateTrainingWordsResult = new HashMap<>();
        HashSet<String> deleteWords = new HashSet<>();
        HashMap<String, String> repeatedWordsResult = new HashMap<>();

        for (TrainingFinishDto finishDto : trainingWordValues) {
            TrainingWordValue twVal = finishDto.getTv();
            if (twVal.getP() >= MAX_INDICATOR_POINTS) {
                // add words to repeated values
                repeatedWordsResult.put(finishDto.getW(), new RepeatWordValue().toDatabaseValue());

                // delete word from training list
                deleteWords.add(finishDto.getW());
                continue;
            }

            if (twVal.getS() >= NEEDED_STRIKES) {
                // add words to repeated values
                repeatedWordsResult.put(finishDto.getW(), new RepeatWordValue().toDatabaseValue());

                // delete word from training list
                deleteWords.add(finishDto.getW());
                continue;
            }

            // updated repeated values
            if (finishDto.getRv() != null) {
                final boolean memorized = twVal.getP() >= MAX_POINTS_PER_WORD;
                repeatedWordsResult.put(finishDto.getW(), finishDto.getRv().nextRepeat(memorized).toDatabaseValue());
                continue;
            }

            // update training values
            updateTrainingWordsResult.put(finishDto.getW(), finishDto.getTv().toDatabaseValue());
        }


        if (updateTrainingWordsResult.size() == 0 && deleteWords.size() == 0 && repeatedWordsResult.size() == 0) return null;

        final ArrayList<Object> params = new ArrayList<>();

        StringBuilder statement = new StringBuilder();
        statement.append("UPDATE " + USER_TRAINING_TABLE + " SET ");

        if (updateTrainingWordsResult.size() > 0) {
            statement.append(workoutType.columnName).append(" = ").append(workoutType.columnName).append(" + ? ");
            params.add(updateTrainingWordsResult);
        }

        if (deleteWords.size() > 0) {
            if (params.size() > 0) statement.append(", ");
            statement.append(workoutType.columnName).append(" = ").append(workoutType.columnName).append(" - ? ");
            params.add(deleteWords);
        }

        if (repeatedWordsResult.size() > 0) {
            if (params.size() > 0) statement.append(", ");
            statement.append(workoutType.repeatColumnName).append(" = ").append(workoutType.repeatColumnName).append(" + ? ");
            params.add(repeatedWordsResult);
        }

        statement.append(" where user_id=? ");
        params.add(userId);

        return new RunnableQuery(statement.toString(), params);

    }

    public static class RunnableQuery {
        public RunnableQuery(String statement, ArrayList<Object> params) {
            this.statement = statement;
            this.params = params;
        }

        private String statement;
        private ArrayList<Object> params;

        public String getStatement() {
            return statement;
        }

        public void setStatement(String statement) {
            this.statement = statement;
        }

        public ArrayList<Object> getParams() {
            return params;
        }

        public void setParams(ArrayList<Object> params) {
            this.params = params;
        }
    }
}