package guru.h4t_eng.security;

import guru.h4t_eng.config.ApplicationProperties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * LogoutServlet.
 *
 * Created by Alexey Alexeenka on 12.07.2015.
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 6857057121597572998L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CookieUtils.removeCookie(resp);
        resp.sendRedirect(ApplicationProperties.getInstance().getLoginPage());
    }
}
