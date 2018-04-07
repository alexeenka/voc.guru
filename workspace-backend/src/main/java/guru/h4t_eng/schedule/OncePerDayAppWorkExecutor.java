package guru.h4t_eng.schedule;

import guru.h4t_eng.schedule.utils.WorkExecutorUtils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Execute {@link AppWork} once per day.
 * <p>
 * Created by aalexeenka on 29.12.2016.
 */
public class OncePerDayAppWorkExecutor extends WorkExecutor {

    private final int targetHour;
    private final int targetMin;
    private final int targetSec;

    public OncePerDayAppWorkExecutor(
            String name,
            AppWork appWork,
            int targetHour,
            int targetMin,
            int targetSec
    ) {
        super(name, appWork);

        this.targetHour = targetHour;
        this.targetMin = targetMin;
        this.targetSec = targetSec;
    }

    public void start() {
        scheduleNextTask(doTaskWork(appWork));
    }

    @Override
    protected Runnable doTaskWork(AppWork appWork) {
        return () -> {
            doTaskWorkPlain(appWork);
            scheduleNextTask(doTaskWork(appWork));
        };
    }

    private void scheduleNextTask(Runnable task) {
        getLogger().info(name + " make schedule in " + WorkExecutorUtils.minskDateTime());
        long delayNano = computeNextDelayNanos(targetHour, targetMin, targetSec);
        getLogger().info(name + " has delay "
                + "in nano: " + delayNano
                + ", in millis: " + TimeUnit.NANOSECONDS.toMillis(delayNano)
                + ", in min: " + TimeUnit.NANOSECONDS.toMinutes(delayNano)
                + ", in hour: " + TimeUnit.NANOSECONDS.toHours(delayNano)
        );
        scheduledTask = executorService.schedule(task, delayNano, TimeUnit.NANOSECONDS);
    }

    private static long computeNextDelayNanos(int targetHour, int targetMin, int targetSec) {
        ZonedDateTime zonedNow = WorkExecutorUtils.minskDateTime();
        ZonedDateTime zonedNextTarget = zonedNow
                .withHour(targetHour)
                .withMinute(targetMin)
                .withSecond(targetSec)
                .withNano(0);

        if (zonedNow.compareTo(zonedNextTarget) > 0) {
            zonedNextTarget = zonedNextTarget.plusDays(1);
        }

        Duration duration = Duration.between(zonedNow, zonedNextTarget);
        return duration.toNanos();
    }
}
