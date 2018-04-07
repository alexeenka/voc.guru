package guru.h4t_eng.rest.user_activity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import guru.h4t_eng.config.ApplicationProperties;
import guru.h4t_eng.logs.AppLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * UnloggedUserActivity.
 *
 * Created by aalexeenka on 23.02.2017.
 */
@WebServlet("/stat/unlogged_user")
public class UnloggedUserActivity extends HttpServlet {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(UnloggedUserActivity.class);

    private static final java.nio.file.Path userActivityLogPath = ApplicationProperties.getInstance().getUserActivityLogPath();

    private static final Gson GSON = new GsonBuilder().create();


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            final String data = IOUtils.toString(req.getInputStream(), "UTF-8");
            UserActivity userActivity = GSON.fromJson(data, UserActivity.class);

            final java.nio.file.Path logFile = userActivityLogPath.resolve(userActivity.getId() + "-login-page" + ".txt");

            Files.write(logFile, userActivity.getData(), CREATE, APPEND);
        } catch (Exception e) {
            LOG.error("unloggedUserActivity", e);
        }
    }

}
