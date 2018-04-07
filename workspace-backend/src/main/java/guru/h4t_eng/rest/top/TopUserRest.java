package guru.h4t_eng.rest.top;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.rest.Main4Rest;
import guru.h4t_eng.top_users.TopUserStorage;
import org.slf4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Rest for top user functionality.
 *
 * Created by aalexeenka on 12.05.2017.
 */
@Path("/top-user")
public class TopUserRest extends Main4Rest {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(TopUserRest.class);

    public static final Gson GSON = new GsonBuilder().create();
    public static final TopUserStorage topUserStorage = TopUserStorage.getInstance();

    @Path("/get")
    @GET
    public Response getTopUser() {
        final String json = GSON.toJson(topUserStorage.getTop());
        return Response.ok(json, MediaType.APPLICATION_JSON_TYPE).build();
    }
}
