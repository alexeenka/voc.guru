package guru.h4t_eng.util;

import java.util.UUID;

/**
 * ErrorUtils.
 *
 * Created by aalexeenka on 23.12.2016.
 */
public final class ErrorUtils {

    private ErrorUtils() {}

    public static String produceErrorTicket() {
        return "err_" + UUID.randomUUID().toString();
    }

}
