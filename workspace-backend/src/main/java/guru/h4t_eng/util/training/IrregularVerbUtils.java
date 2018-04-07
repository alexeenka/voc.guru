package guru.h4t_eng.util.training;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import guru.h4t_eng.logs.AppLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * IrregularVerbUtils.
 * <p>
 *
 * Thinks how to use it!
 *
 * Created by aalexeenka on 28.09.2016.
 */
@Deprecated
public final class IrregularVerbUtils {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(IrregularVerbUtils.class);

    private IrregularVerbUtils() {
    }

    private static class IrregularVerbForms {

        /**
         * Base
         */
        private final String b;
        /**
         * Past Simple
         */
        private final String[] v2;

        /**
         * Past participle
         */
        private final String[] v3;

        public IrregularVerbForms(String base, String pastSimple, String pastParticiple) {
            this.b = base;

            // v2
            {
                final String[] split = pastSimple.split("/");
                if (split.length > 1) {
                    this.v2 = new String[2];
                    this.v2[0] = split[0];
                    this.v2[1] = split[1];
                } else {
                    if (split[0].length() == 0) {
                        this.v2 = new String[]{};
                    } else {
                        this.v2 = split;
                    }
                }
            }
            // v3
            {
                final String[] split = pastParticiple.split("/");
                if (split.length > 1) {
                    this.v3 = new String[2];
                    this.v3[0] = split[0];
                    this.v3[1] = split[1];
                } else {
                    if (split[0].length() == 0) {
                        this.v3 = new String[]{};
                    } else {
                        this.v3 = split;
                    }
                }
            }
        }

        public String[] toForms() {
            int size = 1 + v2.length + v3.length;
            String[] result = new String[size];

            result[0] = b;

            int index = 1;
            System.arraycopy(v2, 0, result, index, v2.length);
            index += index + v2.length - 1;
            System.arraycopy(v3, 0, result, index, v3.length);

            return result;
        }
    }

    private static final ImmutableMap<String, IrregularVerbForms> BASE_FORM_MAP;

    static {
        final String list;
        try {
            // source http://www.gingersoftware.com/content/grammar-rules/verbs/list-of-irregular-verbs/
            final InputStream resourceAsStream = IrregularVerbUtils.class.getClassLoader().getResourceAsStream("training/complete-list-of-irregular-verbs.csv");
            list = IOUtils.toString(resourceAsStream);
            IOUtils.closeQuietly(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        final String[] verbItems = list.split("\n");

        final ImmutableMap.Builder<String, IrregularVerbForms> baseVerbMap = ImmutableMap.builder();

        for (String verbItem : verbItems) {
            final String[] forms = verbItem.split(";");
            String base = getVerb(forms, 0);
            String v2 = getVerb(forms, 1);
            String v3 = getVerb(forms, 2);

            final IrregularVerbForms verbForms = new IrregularVerbForms(base, v2, v3);

            baseVerbMap.put(base, verbForms);
        }

        BASE_FORM_MAP = baseVerbMap.build();

        LOG.info("IrregularVerbUtils was initialized! Size is {}", BASE_FORM_MAP.size());
    }

    private static String getVerb(String[] forms, int index) {
        if (index + 1 > forms.length) return StringUtils.EMPTY;

        if (StringUtils.isBlank(forms[index])) return StringUtils.EMPTY;

        return forms[index].toLowerCase().trim();
    }

    /**
     * Find word forms by initial form.
     *
     * @param infinitive - infinitive
     *
     * @return forms of word
     */
    public static String[] findVerbForms(final String infinitive) {
        if (StringUtils.isBlank(infinitive)) return null;
        String key = infinitive.toLowerCase().trim();
        final IrregularVerbForms verbForms = BASE_FORM_MAP.get(key);
        if (verbForms != null) {
            return verbForms.toForms();
        }

        return null;
    }

    public static ImmutableSet<String> getFullList() {
        return BASE_FORM_MAP.keySet();
    }
}
