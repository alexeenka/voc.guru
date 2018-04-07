package guru.h4t_eng.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.model.dictionary.DictionaryWord;
import guru.h4t_eng.model.dictionary.EngDef;
import guru.h4t_eng.rest.words.model.FormData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * FormDataUtil.
 *
 * Created by aalexeenka on 01.08.2016.
 */
public final class FormDataUtil {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(FormDataUtil.class);

    private static final Gson GSON = new Gson();

    private static final String UPPER_ENG = "ETOAKCB";
    private static final String UPPER_RUS = "ЕТОАКСВ";

    private static final String SMALL_ENG = "eoac";
    private static final String SMALL_RUS = "еоас";


    public static FormData parseJson(String json) {
        try {
            return GSON.fromJson(json, FormData.class);
        } catch (Exception ex) {
            LOG.error("FormData: Can't parse json:  " + json, ex);
            throw new RuntimeException("Can't parse json");
        }
    }

    public static String toJson(DictionaryWord word) {
        try {
            return GSON.toJson(word);
        } catch (Exception ex) {
            LOG.error("DictionaryWord: Can't get json:  " + word.toString(), ex);
            throw new RuntimeException("Can't get json");
        }
    }

    public static String toJson(List<DictionaryWord> words) {
        try {
            return GSON.toJson(words);
        } catch (Exception ex) {
            LOG.error("toJson,list: Can't get json:  " + words, ex);
            throw new RuntimeException("Can't get json");
        }
    }

    public static List<DictionaryWord> parseJsonDictionaryWordList(String json) {
        try {
            Type datasetListType = new TypeToken<Collection<DictionaryWord>>() {}.getType();
            return FormDataUtil.GSON.fromJson(json, datasetListType);
        } catch (Exception ex) {
            LOG.error("parseJson,list: Can't get objects: from " + json, ex);
            throw new RuntimeException("parseJson,list: Can't get objects");
        }
    }

    public static <T> T parseJson(String json, Class<T> clazz) {
        try {
            return GSON.fromJson(json, clazz);
        } catch (Exception ex) {
            LOG.error("Can't parse json:  " + json + ", class: " + clazz, ex);
            throw new RuntimeException("Can't parse json");
        }
    }

    public static String formatText(String value) {
        if (StringUtils.isBlank(value)) {
            return StringUtils.EMPTY;
        }
        String s = value.trim();
        s = StringUtils.strip(s);
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String formatTextWithoutUppercase(String value) {
        if (StringUtils.isBlank(value)) {
            return StringUtils.EMPTY;
        }

        String s = value.trim();
        s = StringUtils.strip(s);
        return s;
    }

    private static void formatStringList(List<String> values) {
        if (values == null) return;

        for (int i=0, n=values.size(); i<n; i++) {
            values.set(i, formatText(values.get(i)));
        }
    }

    public static DictionaryWord formatWord(DictionaryWord word) {
        word.setEngVal(formatText(word.getEngVal()));

        if (CollectionUtils.isNotEmpty(word.getEngDefs())) {
            for (EngDef engDef : word.getEngDefs()) {
                // 1. format engVal
                formatStringList(engDef.getVal());

                // 2. format rus values
                formatStringList(engDef.getRusValues());
                // replace english chars with russian
                for (int i=0; i<engDef.getRusValues().size(); i++) {
                    StrBuilder str = new StrBuilder(engDef.getRusValues().get(i));
                    for (int j=0; j<UPPER_ENG.length(); j++) {
                        str.replaceAll(UPPER_ENG.charAt(j), UPPER_RUS.charAt(j));
                    }
                    for (int j=0; j<SMALL_ENG.length(); j++) {
                        str.replaceAll(SMALL_ENG.charAt(j), SMALL_RUS.charAt(j));
                    }
                    engDef.getRusValues().set(i, str.toString());
                }

                // 3. format sentences
                if (engDef.getEngSentences() != null) {
                    final List<String> values = engDef.getEngSentences();
                    for (int i=0, n=values.size(); i<n; i++) {
                        values.set(i, formatTextWithoutUppercase(values.get(i)));
                    }
                }
            }
        }
        return word;
    }

    public static String extractFirstLetter(String engVal) {
        if (StringUtils.isEmpty(engVal) || engVal.length() < 1) {
            return StringUtils.EMPTY;
        }
        return engVal.substring(0,1).toUpperCase();
    }
}
