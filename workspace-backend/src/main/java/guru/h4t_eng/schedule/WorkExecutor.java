package guru.h4t_eng.schedule;

import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.schedule.utils.WorkExecutorUtils;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Common Executor Parent.
 *
 * Created by aalexeenka on 19.01.2017.
 */
public abstract class WorkExecutor {

    private static final Logger LOG = AppLoggerFactory.getScheduleLog(WorkExecutor.class);

    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private volatile boolean isBusy = false;

    volatile ScheduledFuture<?> scheduledTask = null;

    private AtomicInteger completedTasks = new AtomicInteger(0);

    protected final String name;
    protected final AppWork appWork;

    WorkExecutor(String name, AppWork appWork) {
        this.name = "Executor [" + name + "]";
        this.appWork = appWork;

    }

    Logger getLogger() {
        return LOG;
    }

    abstract public void start();

    protected Runnable doTaskWork(AppWork appWork) {
        return () -> {
            doTaskWorkPlain(appWork);
        };
    }

    void doTaskWorkPlain(AppWork appWork) {
        doTaskWorkPlain(appWork, true);
    }

    void doTaskWorkPlain(AppWork appWork, boolean printInfo) {
        if (printInfo) {
            getLogger().info(name + " [" + completedTasks.get() + "] start: " + WorkExecutorUtils.minskDateTime());
        }

        try {
            isBusy = true;
            appWork.doWork();
            if (printInfo) {
                getLogger().info(name + " [" + completedTasks.get() + "]" + " finish work in " + WorkExecutorUtils.minskDateTime());
            }
        } catch (Exception ex) {
            getLogger().error(name + " throw exception in " + WorkExecutorUtils.minskDateTime(), ex);
        } finally {
            isBusy = false;
        }
        if (printInfo) {
            getLogger().info(name + " completed tasks: " + completedTasks.incrementAndGet());
        }
    }

    public void stop() {
        getLogger().info(name + " is stopping.");
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
        executorService.shutdown();
        getLogger().info(name + " stopped.");
        try {
            getLogger().info(name + " awaitTermination, start: isBusy [ " + isBusy + "]");
            // wait one minute to termination if busy
            if (isBusy) {
                getLogger().info(name + " start waiting");
                executorService.awaitTermination(1, TimeUnit.MINUTES);
                getLogger().info(name + " stop waiting");
            }
        } catch (InterruptedException ex) {
            getLogger().error(name + " awaitTermination exception", ex);
        } finally {
            getLogger().info(name + " awaitTermination, finish");
        }
    }



}
