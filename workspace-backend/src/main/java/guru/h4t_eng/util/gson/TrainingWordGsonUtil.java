package guru.h4t_eng.util.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import guru.h4t_eng.datasource.training.TrainingFinishDto;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

/**
 * Util to work with TrainingWord JSON data.
 * Created by aalexeenka on 21.09.2016.
 */
public final class TrainingWordGsonUtil {

    private TrainingWordGsonUtil() {
    }

    public static final Gson GSON;

    static {
        GSON = new GsonBuilder().registerTypeAdapter(Date.class, new DateGsonAdapter()).create();
    }

    public static ArrayList<TrainingFinishDto> toTrainingFinishDto(String resultJson) {
        final Type type = new TypeToken<ArrayList<TrainingFinishDto>>() {}.getType();
        return GSON.fromJson(resultJson, type);
    }
}
