package guru.h4t_eng.security.login;

/**
 * EmailUserLogin.
 *
 * Created by aalexeenka on 02.12.2016.
 */
public class EmailUserLogin {

    private static final EmailUserLogin instance;

    static {
        instance = new EmailUserLogin();
    }

    public static EmailUserLogin getInstance() {
        return instance;
    }

    public static void newEmailUser(String email) {

    }
}
