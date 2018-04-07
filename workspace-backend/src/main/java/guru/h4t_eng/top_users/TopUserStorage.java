package guru.h4t_eng.top_users;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import guru.h4t_eng.common.PeriodLocalDate;
import guru.h4t_eng.common.WorkWithExecutorServices;
import guru.h4t_eng.config.ApplicationProperties;
import guru.h4t_eng.datasource.CassandraDataSource;
import guru.h4t_eng.datasource.WorkEffortDataSource;
import guru.h4t_eng.logs.AppLoggerFactory;
import org.slf4j.Logger;

import java.time.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static guru.h4t_eng.schedule.utils.WorkExecutorUtils.minskDateTime;
import static guru.h4t_eng.util.ExecutorServiceUtil.stopExecutorService;

/**
 * Contains information about Top Users.
 *
 * Created by aalexeenka on 11.05.2017.
 */
public class TopUserStorage implements WorkWithExecutorServices {

    private static final Logger LOG = AppLoggerFactory.getScheduleLog(TopUserStorage.class);

    private static final int MAX_USER_DAY = 5;
    private static final int MAX_USER_WEEK = 5;
    private static final int MAX_USER_MONTH = 5;

    private static final int UPDATE_RATING_TIME_MIN = ApplicationProperties.getInstance().getTopUserUpdateTime();
    private static final int UPDATE_RATING_TIME_DELAY_MIN = ApplicationProperties.getInstance().getTopUserUpdateInitialDelayTime();

    private static TopUserStorage instance = new TopUserStorage();
    private TopUserStorage() {
    }

    public static TopUserStorage getInstance() {
        return instance;
    }

    private static CassandraDataSource mds = CassandraDataSource.getInstance();
    private static WorkEffortDataSource wds = WorkEffortDataSource.getInstance();

    private PeriodLocalDate getCurrentDayPeriod() {
        final LocalDate now = LocalDate.now(ZoneId.of("Europe/Minsk"));
        return new PeriodLocalDate(now, now);
    }

    private PeriodLocalDate getCurrentWeekPeriod() {
        final LocalDate now = LocalDate.now(ZoneId.of("Europe/Minsk"));
        final LocalDate begin = now.with(DayOfWeek.MONDAY);
        final LocalDate end = now.with(DayOfWeek.SUNDAY);
        return new PeriodLocalDate(begin, end);
    }

    private PeriodLocalDate getCurrentMonthPeriod() {
        final LocalDate now = LocalDate.now(ZoneId.of("Europe/Minsk"));
        final LocalDate begin = now.withDayOfMonth(1);
        final LocalDate end = now.withDayOfMonth(now.lengthOfMonth());
        return new PeriodLocalDate(begin, end);
    }

    private volatile TopUser[] currentDayRating = new TopUser[]{};

    private volatile TopUser[] currentWeekRating = new TopUser[]{};

    private volatile TopUser[] currentMonthRating = new TopUser[]{};

    private final ScheduledExecutorService updateService = Executors.newSingleThreadScheduledExecutor();

    public CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            initialize();
            updateService.scheduleAtFixedRate(this::initialize,
                    UPDATE_RATING_TIME_DELAY_MIN,
                    UPDATE_RATING_TIME_MIN,
                    TimeUnit.MINUTES
            );
        });
    }

    public void stop() {
        stopExecutorService(updateService, LOG, "updateService");
    }

    private void initialize() {
        TopUser[] currentDayRating = new TopUser[MAX_USER_DAY];
        TopUser[] currentWeekRating = new TopUser[MAX_USER_WEEK];
        TopUser[] currentMonthRating = new TopUser[MAX_USER_MONTH];

        int logUserCount = 0;

        try {
            final Instant startTime = Instant.now();
            LOG.info("TopUserStorage: Start load at server-time:" + startTime + ", minsk-time: " + minskDateTime());
            final CassandraDataSource cds = CassandraDataSource.getInstance();

            // GET TOP USERS
            {
                Statement stmt = new SimpleStatement("select user_id from user");
                stmt.setFetchSize(1000);
                ResultSet userRs = cds.getSession().execute(stmt);

                for (Row userRow : userRs) {
                    logUserCount++;
                    final UUID userId = userRow.getUUID("user_id");
                    final Long[] userEffort = wds.evalUserEffort(userId, getCurrentDayPeriod(), getCurrentWeekPeriod(), getCurrentMonthPeriod());

                    rebuildTop(currentDayRating, userId, userEffort[0]);
                    rebuildTop(currentWeekRating, userId, userEffort[1]);
                    rebuildTop(currentMonthRating, userId, userEffort[2]);
                }
            }

            // Filter and sort
            currentDayRating = Arrays.stream(currentDayRating).filter(Objects::nonNull).filter(o->o.getWork() >= 90).toArray(TopUser[]::new);
            currentWeekRating = Arrays.stream(currentWeekRating).filter(Objects::nonNull).filter(o->o.getWork() >= 1200).toArray(TopUser[]::new);
            currentMonthRating = Arrays.stream(currentMonthRating).filter(Objects::nonNull).filter(o->o.getWork() >= 1200).toArray(TopUser[]::new);
            Arrays.sort(currentDayRating, (o1, o2) -> (- Long.compare(o1.getWork(), o2.getWork())));
            Arrays.sort(currentWeekRating, (o1, o2) -> (- Long.compare(o1.getWork(), o2.getWork())));
            Arrays.sort(currentMonthRating, (o1, o2) -> (- Long.compare(o1.getWork(), o2.getWork())));


            // LOAD INFO
            fulfillTopUser(currentDayRating, currentWeekRating, currentMonthRating);

            final Instant finishTime = Instant.now();
            LOG.info("TopUserStorage: UserCount {}. Finish work at server-time: {}, minsk-time: {}, duration: {}.", logUserCount, finishTime, minskDateTime(), Duration.between(startTime, finishTime).toMillis());
        } catch (Exception e) {
            LOG.error("TopUserStorage: ERROR: Processed users: {}", logUserCount, e);
            return;
        }

        this.currentDayRating = currentDayRating;
        this.currentWeekRating = currentWeekRating;
        this.currentMonthRating = currentMonthRating;
    }

    private void fulfillTopUser(TopUser[]... topUsersArray) {
        for (TopUser[] topUsers : topUsersArray) {
            for (TopUser user : topUsers) {
                final Row userInfo = mds.runQuery("select first_name, last_name, vkuid, fbuid, photo_url from user where user_id = ?",
                        true,
                        user.getUserId()
                ).one();
                user.setFirstName(userInfo.getString("first_name"));
                user.setLastName(userInfo.getString("last_name"));
                user.setPhotoUrl(userInfo.getString("photo_url"));
                user.setVkuid(userInfo.getLong("vkuid"));
                user.setFbuid(userInfo.getLong("fbuid"));
            }
        }
    }

    private void rebuildTop(TopUser[] rating, UUID userId, Long userEffort) {
        if (userEffort > 0) {
            int minIndex = -1;
            long min = -1;

            for (int i=0; i<rating.length; i++) {
                TopUser topUser = rating[i];
                if (topUser == null) {
                    minIndex = i;
                    break;
                }

                if (min == -1 || topUser.getWork() < min) {
                    min = topUser.getWork();
                    minIndex = i;
                }
            }

            if (minIndex >= 0 && rating[minIndex] == null && minIndex < rating.length) {
                rating[minIndex] = new TopUser();
                rating[minIndex].setUserId(userId);
                rating[minIndex].setWork(userEffort);
            }

            if (minIndex >= 0 && userEffort > min && min != -1) {
                rating[minIndex].setUserId(userId);
                rating[minIndex].setWork(userEffort);
            }
        }
    }

    public TopUser[][] getTop() {
        return new TopUser[][] {currentDayRating, currentWeekRating, currentMonthRating};
    }

}
