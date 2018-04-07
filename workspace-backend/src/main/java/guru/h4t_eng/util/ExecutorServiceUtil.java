package guru.h4t_eng.util;

import org.slf4j.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ExecutorServiceUtil.
 *
 * Created by aalexeenka on 12.05.2017.
 */
public final class ExecutorServiceUtil {

    public static void stopExecutorService(ScheduledExecutorService executorService, Logger logger, String logName) {
        executorService.shutdown();
        logger.info(logName + " stopped.");
        try {
            logger.info(logName + " awaitTermination, start");
            // wait one minute to termination if busy
            logger.info(logName + " start waiting");
            executorService.awaitTermination(1, TimeUnit.MINUTES);
            logger.info(logName + " stop waiting");
        } catch (InterruptedException ex) {
            logger.error(logName + " awaitTermination exception", ex);
        } finally {
            logger.info(logName + " awaitTermination, finish");
        }
    }

}
