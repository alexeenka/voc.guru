package guru.h4t_eng.test.util;

import guru.h4t_eng.test.WithLogging;
import guru.h4t_eng.util.training.IrregularVerbUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

/**
 * Test irregular verb utility.
 *
 * Created by aalexeenka on 28.09.2016.
 */
public class IrregularVerbUtilsTst extends WithLogging  {

    @Test
    public void test_IrregularVerbForms() {
        {
            final String[] forms = IrregularVerbUtils.findVerbForms("bear");

            Assert.assertNotNull(forms);
            Assert.assertEquals(4, forms.length);
            Assert.assertEquals("bear", forms[0]);
            Assert.assertEquals("bore", forms[1]);
            Assert.assertEquals("born", forms[2]);
            Assert.assertEquals("borne", forms[3]);
        }
        {
            final String[] forms = IrregularVerbUtils.findVerbForms("Be");

            Assert.assertNotNull(forms);
            Assert.assertEquals(4, forms.length);
            Assert.assertEquals("be", forms[0]);
            Assert.assertEquals("was", forms[1]);
            Assert.assertEquals("were", forms[2]);
            Assert.assertEquals("been", forms[3]);
        }
        {
            final String[] forms = IrregularVerbUtils.findVerbForms("wind");

            Assert.assertNotNull(forms);
            Assert.assertEquals(3, forms.length);
            Assert.assertEquals("wind", forms[0]);
            Assert.assertEquals("wound", forms[1]);
            Assert.assertEquals("wound", forms[2]);
        }
        {
            final String[] forms = IrregularVerbUtils.findVerbForms("shall");

            Assert.assertNotNull(forms);
            Assert.assertEquals(2, forms.length);
            Assert.assertEquals("shall", forms[0]);
            Assert.assertEquals("should", forms[1]);
        }
        {
            final String[] forms = IrregularVerbUtils.findVerbForms("spell");

            Assert.assertNotNull(forms);
            Assert.assertEquals(5, forms.length);
            Assert.assertEquals("spell", forms[0]);
            Assert.assertEquals("spelt", forms[1]);
            Assert.assertEquals("spelled", forms[2]);
            Assert.assertEquals("spelt", forms[3]);
            Assert.assertEquals("spelled", forms[4]);
        }
        {
            final String[] forms = IrregularVerbUtils.findVerbForms("swell");

            Assert.assertNotNull(forms);
            Assert.assertEquals(4, forms.length);
            Assert.assertEquals("swell", forms[0]);
            Assert.assertEquals("swelled", forms[1]);
            Assert.assertEquals("swollen", forms[2]);
            Assert.assertEquals("swelled", forms[3]);
        }
    }

    /**
     * Performance test.
     */
    public static void main(String[] args) throws Exception {
        Class.forName(IrregularVerbUtils.class.getName(), true, IrregularVerbUtils.class.getClassLoader());


        final InputStream resourceAsStream = IrregularVerbUtils.class.getClassLoader().getResourceAsStream("training/complete-list-of-irregular-verbs.csv");
        final String list = IOUtils.toString(resourceAsStream);
        IOUtils.closeQuietly(resourceAsStream);

        final String[] verbForms = list.split("\n");
        final String[] verbs = new String[verbForms.length];
        for (int i=0, n=verbForms.length; i<n; i++) {
            verbs[i] = verbForms[i].split(";")[0];

        }

        final int iterationCount = 10000000;

        doMethod2_map(verbs, iterationCount);
    }

    private static void doMethod2_map(String[] verbs, int iterationCount) throws Exception
    {
        long start = System.currentTimeMillis();
        for (int i = 0; i< iterationCount; i++)
        {
            final String[] verbForm = IrregularVerbUtils.findVerbForms(verbs[i % verbs.length]);
            if (verbForm == null) throw new RuntimeException("verbForm == null");
        }
        System.out.println("doMethod2_map: " + (System.currentTimeMillis() - start));
    }
}
