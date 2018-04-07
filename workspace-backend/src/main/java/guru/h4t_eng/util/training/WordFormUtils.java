package guru.h4t_eng.util.training;

import com.google.common.collect.ImmutableMap;
import guru.h4t_eng.logs.AppLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;

/**
 * WordFormUtils. It's alternative for IrregularVerbUtils.
 *
 * Created by aalexeenka on 10.10.2016.
 */
public class WordFormUtils {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(WordFormUtils.class);

    private WordFormUtils() {
    }

    private static class WordForms {
        private final String[] forms;

        public WordForms(String[] forms) {
            this.forms = forms;
        }
    }

    private static final ImmutableMap<String, WordFormUtils.WordForms> WORD_FORM_MAP;

    static {

        final ImmutableMap.Builder<String, WordFormUtils.WordForms> wordFormMapBuilder = ImmutableMap.builder();
        // http://www.worldclasslearning.com/english/five-verb-forms.html
        produceWordForm(wordFormMapBuilder, "training/1000-verbs.csv");
        produceWordForm(wordFormMapBuilder, "training/replacable-words.csv");
        WORD_FORM_MAP = wordFormMapBuilder.build();
        LOG.info("WordFormUtils was initialized! Size of WordForms is {}", WORD_FORM_MAP.size());
    }

    private static void produceWordForm(ImmutableMap.Builder<String, WordForms> wordFormMapBuilder, String resource) {
        final String list;
        try {

            final InputStream resourceAsStream = IrregularVerbUtils.class.getClassLoader().getResourceAsStream(resource);
            list = IOUtils.toString(resourceAsStream);
            IOUtils.closeQuietly(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        final String[] wordForms = list.split("\n");


        for (String line : wordForms) {
            if (line.trim().length() == 0) continue;
            final String[] parts = line.split(",");

            // validation
            for (int i=1; i<parts.length; i++)
            {
                if (parts[i].startsWith("co-operat")) continue;
                if (parts[i].startsWith("ill-treat")) continue;
                if (parts[i].startsWith("been able")) continue;
                if (!parts[i].matches("[a-zA-Z]+")) {
                    throw new RuntimeException("Invalid word form: " + parts[i]);
                }
            }
            String b = parts[1].toLowerCase();

            HashSet<String> forms = new HashSet<>();
            forms.addAll(Arrays.asList(parts).subList(2, parts.length));

            final WordForms verbForms = new WordForms(forms.toArray(new String[forms.size()]));
            wordFormMapBuilder.put(b, verbForms);
        }
    }

    /**
     * Find word forms by initial form.
     *
     * @return forms of word
     */
    public static String[] findWordForms(final String word) {
        if (StringUtils.isBlank(word)) return null;
        String key = word.toLowerCase().trim();

        WordForms verbForms = WORD_FORM_MAP.get(key);
        if (verbForms != null) {
            return verbForms.forms;
        }

        return null;
    }

}
