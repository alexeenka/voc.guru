package guru.h4t_eng.rest.json;

import java.util.Iterator;
import java.util.List;

/**
 * FastJsonBuilder.
 * Fast way to create Json.
 *
 * Example:
 * <pre>
 * new FastJsonBuilder().append("friendUid", friendUid).next().append("photoUrl", photoUrl).finish();
 * </pre>
 *
 * Created by aalexeenka on 18.05.2016.
 */
public class FastJsonBuilder {

    public static String oneJsonValue(String name, String value) {
        return new FastJsonBuilder().append(name, value).finish();
    }

    public FastJsonBuilder() {
        result.append("{");
    }

    private StringBuilder result = new StringBuilder();

    public String finish() {
        result.append("}");
        return result.toString();
    }

    public FastJsonBuilder append(String name, Object value) {
        result.append("\"").append(name).append("\":").append("\"").append(value).append("\"");
        return this;
    }

    public FastJsonBuilder appendNumber(String name, Number value) {
        result.append("\"").append(name).append("\":").append("").append(value).append("");
        return this;
    }

    public FastJsonBuilder appendBoolean(String name, Boolean value) {
        result.append("\"").append(name).append("\":").append("").append(value).append("");
        return this;
    }

    public FastJsonBuilder appendLongArray(String name, Long[] values) {
        //{"trainingCalendar":[1224,1238,1218,629,null,null,null,null,null,null,351,null,584,893,1210,1344,1265,767,null,null,null]}
        result.append("\"").append(name).append("\":").append("[");

        for (int i = 0, n = values.length; i < n; i++) {
            result.append(String.valueOf(values[i]));
            if (i + 1 < n) {
                result.append(",");
            }
        }

        result.append("]");
        return this;
    }


    public FastJsonBuilder append(String value) {
        result.append(value);
        return this;
    }

    public FastJsonBuilder next() {
        result.append(",");
        return this;
    }

    public static <T extends JsonDto> String toJsonArray(T[] dtos) {
        StringBuilder result = new StringBuilder();
        result.append("[");

        for (int i=0, n=dtos.length; i<n; i++) {
            T dto = dtos[i];
            result.append(dto.toJson());

            if (i + 1 < n) {
                result.append(",");
            }
        }

        return result.append("]").toString();
    }

    public static <T extends JsonDto>  String toJsonArray(List<T> dtos) {
        StringBuilder result = new StringBuilder();
        result.append("[");

        for (Iterator<T> it = dtos.iterator();it.hasNext();) {
            T dto = it.next();
            result.append(dto.toJson());

            if (it.hasNext()) {
                result.append(",");
            }
        }

        return result.append("]").toString();
    }

}