package guru.h4t_eng.test.security;

import guru.h4t_eng.test.WithLogging;
import guru.h4t_eng.util.SimpleEncryptUtil;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static guru.h4t_eng.util.SecurityEncodeUtil.decodeBase64;
import static guru.h4t_eng.util.SecurityEncodeUtil.encodeBase64;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;

/**
 * SimpleEncryptUtilTest.
 * <p>
 * Created by aalexeenka on 22.11.2016.
 */
public class SimpleEncryptUtilTest extends WithLogging {

    @Test
    public void constantEncryptTest() {
        String[] uuidList = new String[]{"c1d51ad0-3e1a-11e7-8653-bfe24363e725",
                "bdd8d380-4e0b-11e7-90f7-4d36e3e75985",
                "2c4d53b0-e1a8-11e7-8cf0-6d5b57e39d44"};

        for (String uuid : uuidList) {
            String constEncode = SimpleEncryptUtil.constEncode(uuid);
            assertEquals(uuid, SimpleEncryptUtil.constDecode(constEncode));
        }

        String[] constEncodedList = new String[] {
                "sdLBiNDRoNj9pdvjpq7nke_j2_35tPmJqcz-3c7xpM3gi_n4puTglOjQr_LBlMSqrsnOrNHcNgAAAA",
                "scXViNHCt8ropdvjpdvnnu_Z2_35tPmJqczymNrPis33ofLupePklPzugq_AuuqqrsnOrNHcNgAAAA",
                "pcXK08TBs8r-i9vjscj0gfnz2_35tPmJqczxhdrPqM33i_KlsuTk3fzurKvVqtyrrsnOrNHcNgAAAA"
        };

        int i=0;
        for (String constEncode : constEncodedList) {
            assertEquals(uuidList[i], SimpleEncryptUtil.constDecode(constEncode));
            i++;
        }
    }

    @Test
    public void simpleTest() {
        {
            String text = "Привет Ванек!";
            String encode = encodeBase64(text);
            System.out.println("encodeBase64, initial: " + text + ", Encode: " + encode);
            String decode = decodeBase64(encode);
            assertEquals(text, decode);
        }
        {
            String text = "Привет Ванек!";
            String encode = SimpleEncryptUtil.encode(text);
            System.out.println("Initial: " + text + ", Encode: " + encode);
            String decode = SimpleEncryptUtil.decode(encode);
            assertEquals(text, decode);
        }
        {
            String text = "Привет Ванек и John!";
            String encode = SimpleEncryptUtil.encode(text);
            System.out.println("Initial: " + text + ", Encode: " + encode);
            String decode = SimpleEncryptUtil.decode(encode);
            assertEquals(text, decode);
        }

        {
            String text = "Hello John!";
            String encode = SimpleEncryptUtil.encode(text);
            System.out.println("Initial: " + text + ", Encode: " + encode);
            String decode = SimpleEncryptUtil.decode(encode);
            assertEquals(text, decode);
        }

        for (int i = 0; i < 5; i++) {
            String text = UUID.randomUUID().toString();
            String encode = SimpleEncryptUtil.encode(text);
            System.out.println("Initial: " + text + ", Encode: " + encode);
            String decode = SimpleEncryptUtil.decode(encode);
            assertEquals(text, decode);
        }
    }

    @Test
    public void performanceTest() {
        final int COUNT = 100000;
        long startTime = new Date().getTime();
        for (int i=0; i<COUNT; i++) {
            String text = UUID.randomUUID().toString();
            String encode = SimpleEncryptUtil.encode(text);
            String decode = SimpleEncryptUtil.decode(encode);
            assertEquals(text, decode);
        }
        final long totalTime = new Date().getTime() - startTime;
        final double perOperation = (double) totalTime / COUNT;
        System.out.println("Total time: " + totalTime + ", per operation: " + perOperation);
        assertThat("Per operation time must be less than 0.03ms", perOperation, lessThan(Double.valueOf("0.03")));

    }

}