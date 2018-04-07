package guru.h4t_eng.test.rest.config;

import guru.h4t_eng.security.model.Constants;

import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import java.io.IOException;

import static guru.h4t_eng.UsersDatabaseData4Tst.DAVE_UUID;

/**
 * DaveFbSecurity4TstFilter.
 *
 * Add user attribute to session
 *
 * Created by aalexeenka on 17.11.2016.
 */
@WebFilter(urlPatterns = "/*")
public class DaveFbSecurity4TstFilter implements ContainerRequestFilter {

    @Context
    private HttpServletRequest httpRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        httpRequest.setAttribute(Constants.USER_ID, DAVE_UUID);
    }

}

