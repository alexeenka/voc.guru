package guru.h4t_eng.test.scheduling;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * JobSchedulingExample.
 *
 * Created by aalexeenka on 14.11.2016.
 */
public class JobSchedulingExample {

    public static final long start_time = new Date().getTime();

    private static AtomicInteger job_done_index = new AtomicInteger(0);

    public static void main(String args[]) {

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final Runnable simpleWork = () -> {
            job_done_index.incrementAndGet();

            if (job_done_index.get() % 3 == 0) {
                try {
                    System.out.println("Sleep! job=" + job_done_index.get());
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Job = " + job_done_index.get() + ", time: " + ((double) (new Date().getTime() - start_time) / 1000));
        };
        final ScheduledFuture<?> simpleWorkHandler = scheduler.scheduleWithFixedDelay(simpleWork, 0, 3, SECONDS);

        scheduler.schedule(() -> {
            simpleWorkHandler.cancel(true);
            scheduler.shutdown();
        }, 25, SECONDS);

    }
}
