package guru.h4t_eng.rest.json;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import java.util.Date;
import java.util.UUID;

/**
 * FastJsonParser.
 *
 * No extra spaces between name:value
 *
 * Created by aalexeenka on 19.05.2016.
 */
public final class FastJsonParser {

    public static int getInt(String jsonString, String propertyName) {
        String value = getString(jsonString, propertyName);
        return Integer.valueOf(value);
    }

    public static Long getLong(String jsonString, String propertyName) {
        String value = getString(jsonString, propertyName);
        return Long.valueOf(value);
    }

    public static UUID getUUID(String jsonString, String propertyName) {
        String value = getString(jsonString, propertyName);
        return UUID.fromString(value);
    }

    public static Boolean getBoolean(String jsonString, String propertyName) {
        String value = getString(jsonString, propertyName);
        return Boolean.valueOf(value);
    }

    public static String getString(String jsonString, String propertyName) {
        propertyName = "\"" + propertyName + "\":";
        int index = jsonString.indexOf(propertyName);
        if (index == -1) {
            throw new FastJsonParserException("Can't find property " + propertyName + " into string " + jsonString);
        }

        String value = null;

        int start = index + propertyName.length();

        for (int i = start, n = jsonString.length(); i<n; i++) {
            char c = jsonString.charAt(i);
            if (c == ',' || c == '}') {
                value = jsonString.substring(start, i);
                break;
            }
        }

        if (value == null) {
            throw new FastJsonParserException("Can't evaluate VALUE for property " + propertyName + " into string " + jsonString);
        }

        // for case when json string {"year":"2016","dayOfYear":"140"} and then value is "2016"
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    public static void main(String args[]) {
        // perfomance measure, my parser vs fast eclipse parser

        String json = "{\"year\":2016,\"dayOfYear\":155}";
        int iterations = 1000000;
        {
            long start = new Date().getTime();
            for (int i = 0; i< iterations; i++) {
                int year = FastJsonParser.getInt(json, "year");
                int dayOfYear = FastJsonParser.getInt(json, "dayOfYear");
                int aa = year + dayOfYear;
            }

            System.out.println("Time1: " + (new Date().getTime() - start));
        }

        {
            long start = new Date().getTime();
            for (int i = 0; i< iterations; i++) {
                JsonObject jsonValue = Json.parse(json).asObject();
                int year = jsonValue.get("year").asInt();
                int dayOfYear = jsonValue.get("dayOfYear").asInt();
                int aa = year + dayOfYear;
            }

            System.out.println("Time2: " + (new Date().getTime() - start));
        }
    }
}
