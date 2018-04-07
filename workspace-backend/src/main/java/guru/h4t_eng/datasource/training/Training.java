package guru.h4t_eng.datasource.training;

import guru.h4t_eng.datasource.WorkoutType;

import java.util.HashMap;
import java.util.Map;

/**
 * Training.
 *
 * Created by aalexeenka on 06.09.2016.
 */
public class Training {

    private HashMap<WorkoutType, Map<String, String>> workouts = new HashMap<>();

    public HashMap<WorkoutType, Map<String, String>> getWorkouts() {
        return workouts;
    }

    public void setWorkouts(HashMap<WorkoutType, Map<String, String>> workouts) {
        this.workouts = workouts;
    }

    public void add(WorkoutType workoutType, Map<String, String> values) {
        workouts.put(workoutType, values);
    }

    public int getCountRemainedTrainingWords(WorkoutType workoutType) {
        final Map<String, String> words = workouts.get(workoutType);
        if (words == null) return 0;

        return words.size();
    }
}
