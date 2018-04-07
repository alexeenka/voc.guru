package guru.h4t_eng.rest.user_activity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import guru.h4t_eng.config.ApplicationProperties;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.rest.Main4Rest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.nio.file.Files;
import java.util.UUID;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Filter to record all user actions.
 *
 * Created by aalexeenka on 22.02.2017.
 */
@Path("/stat")
public class LoggedUserActivityRest extends Main4Rest {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(LoggedUserActivityRest.class);

    private static final java.nio.file.Path userActivityLogPath = ApplicationProperties.getInstance().getUserActivityLogPath();

    private static final Gson GSON = new GsonBuilder().create();

    @Path("/logged_user")
    @POST
    public Response logLoggedUserActivity(@Context HttpServletRequest request) {
        try {
            UUID userId = getUserId(request);

            final String data = IOUtils.toString(request.getInputStream(), "UTF-8");
            UserActivity userActivity = GSON.fromJson(data, UserActivity.class);

            final java.nio.file.Path logFile = userActivityLogPath.resolve(userActivity.getId() + "-lu-" + userId.toString() + ".txt");

            Files.write(logFile, userActivity.getData(), CREATE, APPEND);
        } catch (Exception e) {
            LOG.error("logLoggedUserActivity", e);
        }

        return Response.ok().build();
    }
}
