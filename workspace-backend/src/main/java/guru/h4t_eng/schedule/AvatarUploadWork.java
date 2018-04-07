package guru.h4t_eng.schedule;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import guru.h4t_eng.amazon_s3.ImageStorageAmazonS3;
import guru.h4t_eng.datasource.CassandraDataSource;
import guru.h4t_eng.logs.AppLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static guru.h4t_eng.amazon_s3.ImageStorageAmazonS3.CLOUD_FRONT_PREFIX;
import static guru.h4t_eng.amazon_s3.ImageStorageAmazonS3.S3_PREFIX;
import static guru.h4t_eng.schedule.utils.WorkExecutorUtils.minskDateTime;

/**
 * Upload user avatars to amazon.
 *
 * Created by aalexeenka on 28.12.2016.
 */
public class AvatarUploadWork implements AppWork {

    private static final Logger LOG = AppLoggerFactory.getScheduleLog(AvatarUploadWork.class);;

    public static final String DEFAULT_AVATAR_URL = "https://d2ce9r2khtuixp.cloudfront.net/img/friend/avatar-error-v3.png";

    public void doWork() {
        final Instant startTime = Instant.now();
        LOG.info("Start work at server-time:" + startTime + ", minsk-time: " + minskDateTime());
        final CassandraDataSource cds = CassandraDataSource.getInstance();

        Statement stmt = new SimpleStatement("select user_id, photo_url from user");
        stmt.setFetchSize(1000);
        ResultSet rs = cds.getSession().execute(stmt);

        int records = 0;
        int errors = 0;
        for (Row row : rs) {

            final String photo_url = row.getString("photo_url");
            final UUID userId = row.getUUID("user_id");

            String newUrl;
            if (StringUtils.isBlank(photo_url)) {
                newUrl = DEFAULT_AVATAR_URL;
            } else {
                if (photo_url.startsWith(S3_PREFIX) || photo_url.startsWith(CLOUD_FRONT_PREFIX)) {
                    continue;
                }

                try {
                    final InputStream inputStream = new URL(photo_url).openStream();
                    LOG.info("Try to get photo for [" + userId + "], photo_url " + photo_url);
                    newUrl = ImageStorageAmazonS3.getInstance().uploadToAmazonS3(
                            inputStream, -1, "image/jpeg", "jpg",
                            userId.toString() + "/user-info",
                            "avatar_" + Instant.now().toEpochMilli()
                    );
                    IOUtils.closeQuietly(inputStream);
                } catch (IOException e) {
                    LOG.error("Can't upload avatar for photo_url = [" + photo_url + "], user_id = [" + userId + "]", e);
                    newUrl = DEFAULT_AVATAR_URL;
                    errors++;
                }
            }
            records++;

            cds.runQuery("update user set photo_url = ? where user_id = ?", false, newUrl, userId);
            LOG.info("Update [" + userId + "] with photo_url: " + newUrl);
        }

        LOG.info("Processed records: " + records + ", errors: " + errors);
        final Instant finishTime = Instant.now();

        LOG.info("Finish work at server-time: " + finishTime + ", minsk-time: " + minskDateTime() + ", duration: " + Duration.between(startTime, finishTime).toMillis());
    }
}
