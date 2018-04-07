package guru.h4t_eng.security.auth_servlet;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import guru.h4t_eng.config.JerseyClient;
import guru.h4t_eng.config.SocialNetworkProperties;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.security.AuthenticationFilter;
import guru.h4t_eng.security.model.H4TUserInfo;
import guru.h4t_eng.security.model.UserAuthType;
import guru.h4t_eng.social_network.vk.VkApiURL;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;

import static guru.h4t_eng.security.AuthenticationFilter.redirect2LoginPage;
import static guru.h4t_eng.security.auth_servlet.VkAuthenticationServlet.VK_AUTHORIZE_PATH;

/**
 * AuthenticationServlet.
 *
 * Created by Alexey Alexeenka on 11.07.2015.
 */
@WebServlet(name = "VkAuthenticationServlet", urlPatterns = {VK_AUTHORIZE_PATH, VK_AUTHORIZE_PATH + "/*"})
public class VkAuthenticationServlet extends AbstractAuthenticationServlet {

    public static final String VK_AUTHORIZE_PATH = "/vk-auth";

    private static final long serialVersionUID = 4353022385178612323L;

    private static final Logger LOG = AppLoggerFactory.getH4TLog(VkAuthenticationServlet.class);
    public static final SocialNetworkProperties socialNetworkProperties = SocialNetworkProperties.getInstance();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String code = req.getParameter("code");

            if (StringUtils.isBlank(code)) {
                LOG.error("Error: vk code is empty");
                redirect2LoginPage(resp);
                return;
            }

            // todo
            final String authURL = String.format(
                    VkApiURL.VK_AUTH_URL,
                    socialNetworkProperties.getVkClientId(),
                    socialNetworkProperties.getVkClientSecret(),
                    code,
                    socialNetworkProperties.getVkRedirectURL()
            );

            // {"access_token":"sss22swsw","expires_in":0,"user_id":15447858}
            String vkResponseAccessToken = JerseyClient.getInstance().target(authURL).request().get(String.class);


            final JsonObject authResponseJson = Json.parse(vkResponseAccessToken).asObject();
            JsonValue accessTokenValue = authResponseJson.get("access_token");
            if (accessTokenValue == null) {
                LOG.error("Can't get access_token! For URL {}. AccessTokenValue is NULL", authURL);
                redirect2LoginPage(resp);
                return;
            }

            JsonValue userIdValue = authResponseJson.get("user_id");
            if (userIdValue == null) {
                LOG.error("Can't get access_token! For URL {}. UserIdValue is NULL", authURL);
                redirect2LoginPage(resp);
                return;
            }

//            {
//                final JsonValue emailValue = authResponseJson.get("email");
//                if (emailValue != null) email = emailValue.asString();
//            }

            final H4TUserInfo userInfo = getVkUserInfo(accessTokenValue.asString(), userIdValue.toString(), NO_EMAIL);
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

    private H4TUserInfo getVkUserInfo(String accessToken, String userId, String email) {
        // get user information
        {
            final String vkGetUserInfoURL = String.format(VkApiURL.VK_GET_USER_INFO_URL, userId, accessToken);
            // https://jersey.java.net/documentation/latest/client.html#d0e4260
            // {"response":[{"uid":15447858,"first_name":"Alexey","last_name":"Alexeenka","photo_medium":"http:\/\/cs623825.vk.me\/v623825858\/26a29\/ubOqnpfWaMY.jpg"}]}
            String vkResponseUserInfo = JerseyClient.getInstance().target(vkGetUserInfoURL).request(MediaType.APPLICATION_JSON_TYPE).get(String.class);
            if (StringUtils.isBlank(vkResponseUserInfo)) {
                LOG.error("Sent request return empty result [{}]", vkGetUserInfoURL);
                return null;
            }
            if (vkResponseUserInfo.startsWith("{\"error\"")) {
                LOG.error("Sent request return error [{}], response [{}]", vkGetUserInfoURL, vkResponseUserInfo);
                return null;
            }


            {
                JSONArray jsonArray = new JSONObject(vkResponseUserInfo).getJSONArray("response");
                if (jsonArray == null || jsonArray.length() == 0) {
                    LOG.error("Can't get userInfo! For URL [{}], response: [{}]", vkGetUserInfoURL, vkResponseUserInfo);
                    return null;
                }

                {
                    JSONObject jsonUserInfo = jsonArray.getJSONObject(0);
                    return new H4TUserInfo(UserAuthType.VK,
                            jsonUserInfo.getLong("id"),
                            jsonUserInfo.get("first_name").toString(),
                            jsonUserInfo.get("last_name").toString(),
                            jsonUserInfo.get("photo_medium").toString(),
                            email,
                            accessToken,
                            new Date());

                }
            }
        }
    }
}
