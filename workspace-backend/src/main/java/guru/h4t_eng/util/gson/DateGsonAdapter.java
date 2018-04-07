package guru.h4t_eng.util.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * DateGsonAdapter.
 *
 * Created by aalexeenka on 21.09.2016.
 */
public class DateGsonAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return json == null ? null : new Date(json.getAsLong());
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ? null : new JsonPrimitive(src.getTime());
    }
}
