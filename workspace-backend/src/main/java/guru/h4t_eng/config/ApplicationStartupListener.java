package guru.h4t_eng.config;

import guru.h4t_eng.admin.ActiveUsersLogger;
import guru.h4t_eng.amazon_s3.ImageStorageAmazonS3;
import guru.h4t_eng.datasource.CassandraDataSource;
import guru.h4t_eng.global_voc.GlobalVocService;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.schedule.AvatarUploadWork;
import guru.h4t_eng.schedule.CassandraMonitoringWorkExecutor;
import guru.h4t_eng.schedule.OncePerDayAppWorkExecutor;
import guru.h4t_eng.schedule.UpdateGlobalVocWorkExecutor;
import guru.h4t_eng.top_users.TopUserStorage;
import org.slf4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * ApplicationStartupListener.
 *
 * Created by Alexey Alexeenka on 11.07.2015.
 */
@WebListener
public class ApplicationStartupListener implements ServletContextListener {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(ApplicationStartupListener.class);

    private OncePerDayAppWorkExecutor uploadAvatarExecutor;
    private UpdateGlobalVocWorkExecutor updateGlobalVocWorkExecutor;
    private CassandraMonitoringWorkExecutor cassandraMonitoringWorkExecutor;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void contextInitialized(ServletContextEvent sce) {
        // initialize properties
        EmailProperties.getInstance();
        SocialNetworkProperties.getInstance();
        ApplicationProperties.getInstance();

        // initialize JerseyClient
        JerseyClient.getInstance();

        // initialize CassandraDataSource
        CassandraDataSource.getInstance();

        // initialize ImageStorageAmazonS3
        ImageStorageAmazonS3.getInstance();

        // upload avatar photo to S3, once per day
        uploadAvatarExecutor = new OncePerDayAppWorkExecutor("upload-avatars", new AvatarUploadWork(), 4, 0, 0);
        uploadAvatarExecutor.start();

        // *** GLOBAL VOC. BEGIN ***
        // Update global voc periodically
        {
            // start update job
            updateGlobalVocWorkExecutor = new UpdateGlobalVocWorkExecutor();
            updateGlobalVocWorkExecutor.start();
            // immediately initialize GlobalVoc
            GlobalVocService.getInstance().initializeAsync();
        }
        // *** GLOBAL VOC. END ***

        // *** ACTIVE_USERS. BEGIN ***
        ActiveUsersLogger.getInstance().start();
        // *** ACTIVE_USERS. END ***

        // *** TopUserStorage. BEGIN ***
        TopUserStorage.getInstance().start();
        // *** TopUserStorage. END ***

        // *** Cassandra Monitoring. Begin ***
        cassandraMonitoringWorkExecutor = new CassandraMonitoringWorkExecutor();
        cassandraMonitoringWorkExecutor.start();
        // *** Cassnadra Monitoring. End ***
    }

    public void contextDestroyed(ServletContextEvent sce) {
        try {
            uploadAvatarExecutor.stop();
        } catch (Exception e) {
            LOG.error("uploadAvatarExecutor error", e);
        }

        try {
            updateGlobalVocWorkExecutor.stop();
        } catch (Exception e) {
            LOG.error("updateGlobalVocWorkExecutor error", e);
        }

        try {
            cassandraMonitoringWorkExecutor.stop();
        } catch (Exception e) {
            LOG.error("cassandraMonitoringWorkExecutor error", e);
        }

        try {
            ActiveUsersLogger.getInstance().stop();
        } catch (Exception e) {
            LOG.error("ActiveUsersLogger error", e);
        }

        // *** TopUserStorage. BEGIN ***
        try {
            TopUserStorage.getInstance().stop();
        } catch (Exception e) {
            LOG.error("TopUserStorage error", e);
        }
        // *** TopUserStorage. END ***

        try {
            JerseyClient.getInstance().close();
        } catch (Exception e) {
            LOG.error("Jersey close error", e);
        }

        try {
            CassandraDataSource.getInstance().shutdown();
        } catch (Exception e) {
            LOG.error("Cassandra shutdown error", e);
        }


        // wait 5 sec to release all thread
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            LOG.error("contextDestroyed", e);
        }
    }
}
