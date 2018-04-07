package guru.h4t_eng.schedule.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class for WorkExecutor
 *
 * Created by aalexeenka on 20.01.2017.
 */
public final class WorkExecutorUtils {
    private WorkExecutorUtils() {
    }

    public static ZonedDateTime minskDateTime() {
        return ZonedDateTime.now(ZoneId.of("Europe/Minsk"));
    }
}
