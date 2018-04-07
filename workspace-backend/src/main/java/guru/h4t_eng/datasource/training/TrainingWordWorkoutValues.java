package guru.h4t_eng.datasource.training;

import guru.h4t_eng.datasource.WorkoutType;
import guru.h4t_eng.model.training.TrainingWordValue;

import java.util.HashMap;
import java.util.Map;

/**
 * TrainingWordWorkoutValues.
 *
 * Contains value per each workout.
 *
 * Created by aalexeenka on 06.09.2016.
 */
public class TrainingWordWorkoutValues {

    private String engVal;

    private HashMap<WorkoutType, TrainingWordValue> workouts = new HashMap<>();

    public TrainingWordWorkoutValues(String engVal) {
        this.engVal = engVal;
    }

    public HashMap<WorkoutType, TrainingWordValue> getWorkouts() {
        return workouts;
    }

    public void setWorkouts(HashMap<WorkoutType, TrainingWordValue> workouts) {
        this.workouts = workouts;
    }

    public static TrainingWordWorkoutValues valueOf(Training training, String engVal) {
        TrainingWordWorkoutValues result = new TrainingWordWorkoutValues(engVal);

        final HashMap<WorkoutType, Map<String, String>> workouts = training.getWorkouts();

        for (WorkoutType workout : workouts.keySet()) {
            final String databaseValue = workouts.get(workout).get(engVal);
            if (databaseValue != null) {
                result.workouts.put(workout, TrainingWordValue.valueOf(databaseValue));
            }

        }

        if (result.workouts.size() == 0) {
            return null;
        }

        return result;
    }

    public TrainingWordValue getPerWorkout(WorkoutType workoutType) {
        return getWorkouts().get(workoutType);
    }
}