package guru.h4t_eng.ui_tests;

import guru.h4t_eng.ui_tests.selenium.BaseSelenium;

import java.util.Date;

/**
 * UIUtils.
 * <p>
 * Created by aalexeenka on 16.11.2016.
 */
public final class UIUtils {
    private UIUtils() {
    }

    public static void loginDave() {
        final long start = new Date().getTime();
        try {
            BaseSelenium.loginAsDavid();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Can't login as Dave FB USER");
        }
        final long end = new Date().getTime();
        System.out.println("Login Dave, total time: " + (end - start));
    }

    public static void loginVkSamanta() {
        final long start = new Date().getTime();
        try {
            BaseSelenium.loginAsSamanta();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Can't login as Samanta VK USER");
        }
        final long end = new Date().getTime();
        System.out.println("Login Samanta, total time: " + (end - start));
    }
}
