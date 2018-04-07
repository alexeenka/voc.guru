package guru.h4t_eng.security;

import guru.h4t_eng.security.model.UserSessionCookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * CookieUtils.
 *
 * Created by aalexeenka on 01.11.2016.
 */
public class CookieUtils {

    private CookieUtils() {
    }

    public static void removeCookie(HttpServletResponse resp) {
        // http://stackoverflow.com/questions/2255814/can-i-turn-off-the-httpsession-in-web-xml
        // http://stackoverflow.com/questions/890935/how-do-you-remove-a-cookie-in-a-java-servlet
        // REMOVE cookie
        Cookie cookie = new Cookie(UserSessionCookie.COOKIE_NAME, "");
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
    }
}
