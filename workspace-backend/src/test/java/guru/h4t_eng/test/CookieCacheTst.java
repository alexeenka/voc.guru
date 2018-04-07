package guru.h4t_eng.test;

import guru.h4t_eng.security.model.UserSessionCookie;
import guru.h4t_eng.util.SecurityEncodeUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

/**
 * Test that we code and decode secret cookie value.
 *
 * Created by aalexeenka on 4/5/2016.
 */
public class CookieCacheTst extends WithLogging {

    @Test
    public void encodeDecodeTest() {
        UserSessionCookie userCookie = new UserSessionCookie(
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.75 Safari/537.36",
                "10.99.55.90:9080",
                "220761a0-6b46-11e5-ba60-a1efb4cdc0bf"
        );

        String json = UserSessionCookie.toJson(userCookie);

        long time1 = new Date().getTime();
        String encodedCookie = SecurityEncodeUtil.cipherEncode(json);
        System.out.println("encodedCookie: " + encodedCookie);

        // check length, max size all json value for host 4096 bytes
        assertThat("json length", encodedCookie.length(), lessThan(400));

        String decodedCookie = SecurityEncodeUtil.cipherDecode(encodedCookie);
        System.out.println("Time:" + (new Date().getTime() - time1));

        UserSessionCookie actual = UserSessionCookie.fromJson(decodedCookie);
        Assert.assertEquals(userCookie, actual);
    }

    @Test(expected=RuntimeException.class)
    public void decodeFailed() {
        SecurityEncodeUtil.cipherDecode("xyz");
    }

}