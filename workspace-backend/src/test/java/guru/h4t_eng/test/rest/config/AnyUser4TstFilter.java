package guru.h4t_eng.test.rest.config;

import guru.h4t_eng.security.model.Constants;
import guru.h4t_eng.test.util.TestContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import java.io.IOException;

/**
 * Security filter to test functionality without creating user using {@link TestContext}.
 *
 * Created by aalexeenka on 15.03.2017.
 */
public class AnyUser4TstFilter  implements ContainerRequestFilter {

    @Context
    private HttpServletRequest httpRequest;

    private TestContext testContext = TestContext.getInstance();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        httpRequest.setAttribute(Constants.USER_ID, testContext.getTestUserId());
    }

}
