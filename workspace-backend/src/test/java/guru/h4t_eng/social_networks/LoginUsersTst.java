package guru.h4t_eng.social_networks;

import guru.h4t_eng.datasource.UserDataSource;
import guru.h4t_eng.test.WithLogging;
import guru.h4t_eng.ui_tests.UIUtils;
import guru.h4t_eng.ui_tests.selenium.BaseSelenium;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

/**
 * LoginUsersTst.
 * <p>
 * Created by aalexeenka on 16.11.2016.
 */
public class LoginUsersTst extends WithLogging {

    @Test
    public void loginMainVkUser() throws Exception {
        long start = new Date().getTime();
        try {
            BaseSelenium.setUp();
            UIUtils.loginVkSamanta();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            BaseSelenium.tearDown();
        }
        System.out.println("Total time: " + (new Date().getTime() - start));
    }

    @Test
    public void loginMainFbUser() throws Exception {
        long start = new Date().getTime();
        try {
            BaseSelenium.setUp();
            UIUtils.loginDave();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            BaseSelenium.tearDown();
        }
        System.out.println("Total time: " + (new Date().getTime() - start));
    }

    @Test
    public void loginMainFbFriends() throws Exception {
        long start = new Date().getTime();

        Object[] friendsData = new Object[]{
                "sandra_uekrfuy_sadanstein@tfbnw.net", 11L,
                "james_hhujaco_carrieroson@tfbnw.net", 11L,
                "jennifer_pezjxuc_valtchanovescu@tfbnw.net", 11L,
                "elizabeth_qdnyalj_shepardson@tfbnw.net", 11L,
                "harry_yqsmiiw_greeneberg@tfbnw.net", 11L,
                "margaret_fgvicgu_fallerberg@tfbnw.net", 11L,
                "lisa_kqlodxz_lauman@tfbnw.net", 11L,
                "mike_hsbxbwr_liberg@tfbnw.net", 11L,
                "linda_pdoicpm_lisen@tfbnw.net", 11L,
                "john_hwigsif_sharpeson@tfbnw.net", 11L,
                "nancy_nbowumv_qinstein@tfbnw.net", 11L
        };

        final String facebookPsw = "VocGuruZX12345";
        for (int i=0, n=friendsData.length; i<n; i+=2) {
            // 1. check is user already exist
            long facebookId = (long) friendsData[i+1];
            UUID userId = UserDataSource.getInstance().getUserIdByFbID(facebookId);
            if (userId != null) {
                final String fbAccessToken = UserDataSource.getInstance().getFbAccessToken(userId);
                if (StringUtils.isNoneBlank(fbAccessToken)) {
                    continue;
                }
            }
            // 2. so than login facebook user
            try {
                String email = (String) friendsData[i];
                BaseSelenium.setUp();

                BaseSelenium.loginAsFbUser(email, facebookPsw);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                BaseSelenium.tearDown();
            }
        }

        System.out.println("Total time: " + (new Date().getTime() - start));
    }
}
