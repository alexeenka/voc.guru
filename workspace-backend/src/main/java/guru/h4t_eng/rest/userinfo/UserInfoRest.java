package guru.h4t_eng.rest.userinfo;

import guru.h4t_eng.datasource.UserDataSource;
import guru.h4t_eng.rest.Main4Rest;
import guru.h4t_eng.rest.json.FastJsonBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.UUID;

/**
 * Provide needed information for [client].
 * <p>
 * <p>
 * Created by Alexey Alexeenka on 05.07.2015.
 */
@Path("/user-info")
public class UserInfoRest extends Main4Rest {

    private static final String NO_SOCIAL_ID = "0";

    @GET
    public Response getUserInfo(
            @Context HttpServletRequest httpRequest
    ) {
        UUID userId = getUserId(httpRequest);
        Optional<String[]> optUserDetails = UserDataSource.getInstance().getUserDetails(userId);
        if (!optUserDetails.isPresent()) {
            Response.serverError();
        }

        String[] userDetails = optUserDetails.get();

        String result = new FastJsonBuilder()
                .append("userFN", userDetails[0]).next()
                .append("userImg", userDetails[1]).next()
                .appendBoolean("fbLink", !NO_SOCIAL_ID.equals(userDetails[2])).next()
                .appendBoolean("vkLink", !"0".equals(userDetails[3]))
                .finish();

        return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
    }

}