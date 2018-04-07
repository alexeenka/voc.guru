package guru.h4t_eng.security.auth_servlet;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import guru.h4t_eng.config.JerseyClient;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.security.AuthenticationFilter;
import guru.h4t_eng.security.model.H4TUserInfo;
import guru.h4t_eng.security.model.UserAuthType;
import guru.h4t_eng.social_network.facebook.FacebookApiURL;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Date;

import static guru.h4t_eng.security.AuthenticationFilter.redirect2LoginPage;
import static guru.h4t_eng.security.auth_servlet.FbAuthenticationServlet.FACEBOOK_AUTHORIZE_PATH;
import static guru.h4t_eng.social_network.facebook.FacebookApiURL.GRAPH_FACEBOOK_URL;

/**
 * FbAuthenticationServlet
 *
 * Created by aalexeenka on 2/19/2016.
 */
@WebServlet(name = "FbAuthenticationServlet", urlPatterns = {FACEBOOK_AUTHORIZE_PATH, FACEBOOK_AUTHORIZE_PATH + "/*"})
public class FbAuthenticationServlet extends AbstractAuthenticationServlet {

    public static final String FACEBOOK_AUTHORIZE_PATH = "/fb-auth";

    private static final Logger LOG = AppLoggerFactory.getH4TLog(FbAuthenticationServlet.class);

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String code = req.getParameter("code");

            if (StringUtils.isBlank(code)) {
                LOG.error("Error: facebook code is empty");
                redirect2LoginPage(resp);
                return;
            }

            final String authURL = FacebookApiURL.buildAuthUrl(code);

            // https://developers.facebook.com/docs/facebook-login/manually-build-a-login-flow
            // {"access_token":"blablabla","token_type":"bearer","expires_in":5183476}
            // {"access_token": {access-token}, "token_type":{type}, "expires_in":{seconds-til-expiration}}
            String serverAccessToken = JerseyClient.getInstance().target(authURL).request().get(String.class);

            final JsonValue jsonAccessToken = Json.parse(serverAccessToken).asObject().get("access_token");
            if (jsonAccessToken == null) {
                LOG.error("Response doesn't contain access_token! For URL {}", authURL);
                redirect2LoginPage(resp);
                return;
            }
            String accessToken = jsonAccessToken.asString();

            final H4TUserInfo userInfo = getUserInfo(accessToken);
            if (userInfo == null) {
                redirect2LoginPage(resp);
                return;
            }

            // save/update user info
            loginUser(req, userInfo);

            createCookie(req, resp, userInfo.getUserId());
        } catch (Throwable th) {
            LOG.error("Can't authenticate user.", th);
            redirect2LoginPage(resp);
            return;
        }

        AuthenticationFilter.redirect2IndexPage(resp);
    }

    private H4TUserInfo getUserInfo(String accessToken) {
        // GRAPH API: https://developers.facebook.com/docs/graph-api/reference/user/
        //
        // GET id, firstname, lastname:
        // https://graph.facebook.com/v2.5/me?fields=id,first_name,last_name&access_token=CAACwr7JTwZAoBAKmUFTMVGmkvwDeT683CtWfDWZBv0ZAXGoJRzdpbIa8fNT3YDZCvLZALI3di9etXDsgJeEwxxowZCbz0Uy0TFTB2ZCWAZCZB5rFZAtAcIbtuXDkxLXKdrcgaOZAFzDfy32snZBOGx1sRfrQkDaRiOMZCd1yBZC5e8byX9ceCr77WO0O64CfZALmCwpTZCo8cjy7OXOZCYAZDZD
        //
        // GET picture:
        // https://graph.facebook.com/v2.5/me/picture?type=large&redirect=false&access_token=CAACwr7JTwZAoBAKmUFTMVGmkvwDeT683CtWfDWZBv0ZAXGoJRzdpbIa8fNT3YDZCvLZALI3di9etXDsgJeEwxxowZCbz0Uy0TFTB2ZCWAZCZB5rFZAtAcIbtuXDkxLXKdrcgaOZAFzDfy32snZBOGx1sRfrQkDaRiOMZCd1yBZC5e8byX9ceCr77WO0O64CfZALmCwpTZCo8cjy7OXOZCYAZDZD

        WebTarget webTarget = JerseyClient.getInstance().target(GRAPH_FACEBOOK_URL + "/");
        Response response = webTarget.request().post(
                Entity.text("batch=[{\"method\":\"GET\", \"relative_url\":\"me?fields=id,first_name,last_name\"},{\"method\":\"GET\", \"relative_url\":\"me/picture?redirect=false%26type=large\"}]" +
                        "&access_token=" + accessToken)
        );
        String responseStr = response.readEntity(String.class);
        final JsonValue jsonValue = Json.parse(responseStr);
        if (!jsonValue.isArray()) return null;

        // parse first part
        final JsonObject firstGET = ((JsonArray) jsonValue).get(0).asObject();
        if (firstGET.get("code").asInt() != 200) {
            LOG.error("GET: me?fields=id,first_name,last_name, return with error code: {}",  firstGET.get("code").asInt());
            return null;
        }
        final JsonObject firstBody = Json.parse(firstGET.get("body").asString()).asObject();
        final long id = Long.parseLong(firstBody.get("id").asString(), 10);
        final String firstName = firstBody.get("first_name").asString();
        final String lastName = firstBody.get("last_name").asString();
//        {
//            final JsonValue emailValue = firstBody.get("email");
//            email = emailValue != null ? emailValue.asString() : NO_EMAIL ;
//            email = NO_EMAIL;
//        }

        final JsonObject secondGET = ((JsonArray) jsonValue).get(1).asObject();
        if (secondGET.get("code").asInt() != 200) {
            LOG.error("GET: me/picture?redirect=false%26type=large: {}",  firstGET.get("code").asInt());
            return null;
        }
        final JsonObject secondBody = Json.parse(secondGET.get("body").asString()).asObject();
        final String url = secondBody.get("data").asObject().get("url").asString();

        return new H4TUserInfo(UserAuthType.FACEBOOK, id, firstName, lastName, url, NO_EMAIL, accessToken, new Date());
    }
}
