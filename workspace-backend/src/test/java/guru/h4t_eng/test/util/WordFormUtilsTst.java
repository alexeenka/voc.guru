package guru.h4t_eng.test.util;

import guru.h4t_eng.test.WithLogging;
import guru.h4t_eng.util.training.WordFormUtils;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * WordFormUtilsTst.
 *
 * Created by aalexeenka on 10.10.2016.
 */
public class WordFormUtilsTst extends WithLogging {

    @Test
    public void test_IrregularVerbForms() {
        {
            final String[] forms = WordFormUtils.findWordForms("bear");
            assertNotNull(forms);
            assertThat(Arrays.asList(forms), containsInAnyOrder("borne", "bearing", "bore", "born", "bears"));
        }
        {
            final String[] forms = WordFormUtils.findWordForms("Be");
            assertNotNull(forms);
            assertThat(Arrays.asList(forms), containsInAnyOrder("been", "are", "were", "was", "is", "being", "am"));
        }

        {
            final String[] forms = WordFormUtils.findWordForms("wind");
            assertNotNull(forms);
            assertThat(Arrays.asList(forms), containsInAnyOrder("wound", "winds", "winding"));
        }

        {
            final String[] forms = WordFormUtils.findWordForms("shall");
            assertNotNull(forms);
            assertThat(Arrays.asList(forms), containsInAnyOrder("should"));
        }

        {
            final String[] forms = WordFormUtils.findWordForms("spell");
            assertNotNull(forms);
            assertThat(Arrays.asList(forms), containsInAnyOrder("spelling", "spells", "spelt"));
        }

        {
            final String[] forms = WordFormUtils.findWordForms("swell");
            assertNotNull(forms);
            assertThat(Arrays.asList(forms), containsInAnyOrder("swelled", "swollen", "swells", "swelling"));
        }
        {
            final String[] forms = WordFormUtils.findWordForms("sb");
            assertNotNull(forms);
            assertThat(Arrays.asList(forms), containsInAnyOrder("you","him","her","his","it","me", "your", "us"));
        }
    }


}
