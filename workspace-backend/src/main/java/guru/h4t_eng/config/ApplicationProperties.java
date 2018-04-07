package guru.h4t_eng.config;

import guru.h4t_eng.logs.AppLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * ApplicationProperties.
 *
 * Created by Alexey Alexeenka on 11.07.2015.
 */
public class ApplicationProperties extends AppProperties {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(ApplicationProperties.class);

    private static final ApplicationProperties instance = new ApplicationProperties("application.properties");
    public static ApplicationProperties getInstance() {return instance;}

    private ApplicationProperties(String fileName) {
        super(fileName);
    }

    @Override
    protected void reading(Properties prop) {
        buildVersion = prop.getProperty("buildVersion");
        maxUploadFileSizeBytes = Integer.valueOf(prop.getProperty("max-upload-file-size-bytes"));

        // friends related fields
        maxFriendPerUser = Integer.valueOf(prop.getProperty("friend.max-friend-per-user"));
        friendPagingSize = Integer.valueOf(prop.getProperty("friend.paging-size"));

        // calculate field
        indexPage = "index.html";
        loginPage = "/login.html";

        // user activity
        {
            String propUserActivityLogPath = prop.getProperty("user-activity.log.path");
            if (propUserActivityLogPath == null) {
                LOG.error("\"user-activity.log.path\" doesn't found");
            } else {
                final String newPath = replaceMacros(propUserActivityLogPath);
                final File userActivityLogPathFile = new File(newPath);
                if (!userActivityLogPathFile.exists()) {
                    LOG.info("Path doesn't exist: {}, create directory", propUserActivityLogPath);
                    try {
                        userActivityLogPath = Files.createDirectories(userActivityLogPathFile.toPath());
                    } catch (IOException e) {
                        LOG.error("Can't create directory: {}" , propUserActivityLogPath, e);
                    }
                } else {
                    userActivityLogPath = userActivityLogPathFile.toPath();
                }
            }

            userActivityLoggedUserEnable = Boolean.valueOf(prop.getProperty("user-activity.log.logged-user.enable"));
            userActivityUnloggedUserEnable = Boolean.valueOf(prop.getProperty("user-activity.log.unlogged-user.enable"));

            userActivityExpired = Integer.valueOf(prop.getProperty("user-activity.active-users.expired"));
            userActivityLogTime = Integer.valueOf(prop.getProperty("user-activity.active-users.log-time"));
        }

        topUserUpdateTime = Integer.valueOf(prop.getProperty("top-users.update-time-min"));
        topUserUpdateInitialDelayTime = Integer.valueOf(prop.getProperty("top-users.update-time-min-initial-delay"));
    }

    private String replaceMacros(String path) {
        {
            String catalinaBase = System.getProperty("catalina.base");
            if (StringUtils.isNoneBlank(catalinaBase)) {
                path = path.replace("${catalina.base}", catalinaBase);
            }
        }
        {
            String tmpDir = System.getProperty("java.io.tmpdir");
            if (StringUtils.isNoneBlank(tmpDir)) {
                path = path.replace("${java.io.tmpdir}", tmpDir);
            }
        }
        return path;
    }

    private String buildVersion;
    private Integer maxUploadFileSizeBytes;

    private int maxFriendPerUser;
    private int friendPagingSize;

    private int userActivityExpired;
    private int userActivityLogTime;

    private int topUserUpdateTime;
    private int topUserUpdateInitialDelayTime;

    private String indexPage;
    private String loginPage;

    private Path userActivityLogPath;
    private boolean userActivityLoggedUserEnable;
    private boolean userActivityUnloggedUserEnable;


    public String getBuildVersion() {
        return buildVersion;
    }

    public Integer getMaxUploadFileSizeBytes() {
        return maxUploadFileSizeBytes;
    }

    public String getIndexPage() {
        return indexPage;
    }

    public String getLoginPage() {
        return loginPage;
    }

    public int getMaxFriendPerUser() {
        return maxFriendPerUser;
    }

    public int getFriendPagingSize() {
        return friendPagingSize;
    }

    public Path getUserActivityLogPath() {
        return userActivityLogPath;
    }

    public boolean isUserActivityLoggedUserEnable() {
        return userActivityLoggedUserEnable;
    }

    public boolean isUserActivityUnloggedUserEnable() {
        return userActivityUnloggedUserEnable;
    }

    public int getUserActivityExpired() {
        return userActivityExpired;
    }

    public int getUserActivityLogTime() {
        return userActivityLogTime;
    }

    public int getTopUserUpdateTime() {
        return topUserUpdateTime;
    }

    public int getTopUserUpdateInitialDelayTime() {
        return topUserUpdateInitialDelayTime;
    }
}
