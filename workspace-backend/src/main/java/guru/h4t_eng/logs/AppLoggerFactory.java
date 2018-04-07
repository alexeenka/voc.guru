package guru.h4t_eng.logs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AppLoggerFactory.
 *
 * Created by aalexeenka on 12/18/2015.
 */
public class AppLoggerFactory {

    public static Logger getAdminLog (java.lang.Class clazz) {
        return LoggerFactory.getLogger("admin." + clazz.toString());
    }

    public static Logger getH4TLog (java.lang.Class clazz) {
        return LoggerFactory.getLogger("H4TLog." + clazz.toString());
    }

    public static Logger getScheduleLog (java.lang.Class clazz) {
        return LoggerFactory.getLogger("ScheduleLog." + clazz.toString());
    }

    public static Logger getWordSetLog (java.lang.Class clazz) {
        return LoggerFactory.getLogger("WordSetLog." + clazz.toString());
    }

    public static Logger getCassandraLog(java.lang.Class clazz) {
        return LoggerFactory.getLogger("CassandraLog." + clazz.toString());
    }


    public static Logger getPerformanceLog (java.lang.Class clazz) {
        return LoggerFactory.getLogger("performance." + clazz.toString());
    }
}
