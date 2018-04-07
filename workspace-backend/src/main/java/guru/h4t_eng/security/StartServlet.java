package guru.h4t_eng.security;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * StartServlet.
 *
 * Created by Alexey Alexeenka on 12.07.2015.
 */
@WebServlet("/start")
public class StartServlet extends HttpServlet {
    private static final long serialVersionUID = -3508974042386282039L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (AuthenticationFilter.authFilter(req, resp) && !resp.isCommitted()) {
            AuthenticationFilter.redirect2IndexPage(resp);
        }
    }
}
