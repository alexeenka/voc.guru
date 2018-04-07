package guru.h4t_eng.admin;

import guru.h4t_eng.common.WorkWithExecutorServices;
import guru.h4t_eng.config.ApplicationProperties;
import guru.h4t_eng.logs.AppLoggerFactory;
import org.slf4j.Logger;

import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.*;

import static guru.h4t_eng.util.ExecutorServiceUtil.stopExecutorService;

/**
 * Contains map of current active user in the system. <br>
 * <p>
 * Maybe it need to be revise when it will be a lot of users.<br>
 *
 * Created by aalexeenka on 10.05.2017.
 */
public class ActiveUsersLogger implements WorkWithExecutorServices {

    private static final Logger LOG = AppLoggerFactory.getAdminLog(ActiveUsersLogger.class);

    private static final int EXPIRED_TIME_SEC = ApplicationProperties.getInstance().getUserActivityExpired();
    private static final int LOG_TIME_MILLISECOND = ApplicationProperties.getInstance().getUserActivityLogTime();

    private static final ActiveUsersLogger INSTANCE = new ActiveUsersLogger();

    private ActiveUsersLogger() {
    }

    public static ActiveUsersLogger getInstance() {
        return INSTANCE;
    }


    private final ConcurrentHashMap<UUID, ScheduledFuture> users = new ConcurrentHashMap<>();
    private final ScheduledExecutorService expiredService = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService logService = Executors.newSingleThreadScheduledExecutor();


    public void activate(final UUID uuid) {
        try {
            users.compute(uuid, (uuid_v, future_v) -> {
                if (future_v == null) {
                    //System.out.println("A-1:" + uuid_v + " : " + Thread.currentThread().getId());
                    return newExpiredTask(uuid_v);
                } else {
                    //System.out.println("A-2:" + uuid_v + " : " + Thread.currentThread().getId());
                    future_v.cancel(false);
                    return newExpiredTask(uuid_v);
                }
            });
        } catch (Throwable th) {
            LOG.error("activate", th);
        }
    }

    private ScheduledFuture<?> newExpiredTask(final UUID uuid) {
        return expiredService.schedule(() -> users.remove(uuid), EXPIRED_TIME_SEC, TimeUnit.SECONDS);
    }

    public int size() {
        return users.size();
    }

    public void log() {
        try {
            StringBuilder keys = new StringBuilder();
            int index = 100;
            final Enumeration<UUID> it = users.keys();
            for (; it.hasMoreElements(); ) {
                index--;
                if (index < 0) {
                    keys.append("...");
                    break;
                }

                UUID uuid = it.nextElement();
                keys.append(uuid);
                if (it.hasMoreElements()) {
                    keys.append(",");
                }
            }

            LOG.info("size: [{}], keys: [{}].", size(), keys);
        } catch (Throwable th) {
            LOG.error("log", th);
        }
    }

    @Override
    public CompletableFuture<Void> start() {
        logService.scheduleAtFixedRate(this::log,
                0,
                LOG_TIME_MILLISECOND,
                TimeUnit.MILLISECONDS
        );

        return CompletableFuture.runAsync(() -> {});
    }

    public void stop() {
        // stop all related scheduled task.
        try {
            for (ScheduledFuture task : users.values()) {
                try {
                    task.cancel(false);
                } catch (Exception e) {
                    LOG.error("task.cancel", e);
                }
            }
        } catch (Exception e) {
            LOG.error("all task.cancel", e);
        } finally {
            users.clear();
        }

        stopExecutorService(expiredService, LOG, "expiredService");
        stopExecutorService(logService, LOG,"logService");
    }
}