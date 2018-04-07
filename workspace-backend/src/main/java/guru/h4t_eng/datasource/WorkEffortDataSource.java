package guru.h4t_eng.datasource;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.util.concurrent.ListenableFuture;
import guru.h4t_eng.common.PeriodLocalDate;
import guru.h4t_eng.logs.AppLoggerFactory;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * WorkEffortDataSource.
 * <p>
 * Created by aalexeenka on 3/21/2016.
 */
public class WorkEffortDataSource {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(WorkEffortDataSource.class);

    private static final WorkEffortDataSource instance = new WorkEffortDataSource();

    private WorkEffortDataSource() {
    }

    public static WorkEffortDataSource getInstance() {
        return instance;
    }

    private static CassandraDataSource mds = CassandraDataSource.getInstance();

    public void updateTime(UUID userId, int year, int dayOfYear, long spent) {
        mds.runQuery("update work_effort set time = time + ? where user_id = ? and year = ? and day_of_year = ?", false, spent, userId, year, dayOfYear);
    }


    public long getTime(UUID userId, Integer year, Integer dayOfYear) {
        final ResultSet rows = mds.runQuery("select time from work_effort where user_id = ? and year = ? and day_of_year = ?", false, userId, year, dayOfYear);

        if (rows.isExhausted()) {
            return 0L;
        }

        return rows.one().getLong("time");
    }

    public Long[] getTrainingCalendar(UUID userId, Integer year, Integer startDay, Integer count) {
        int yearBefore = year - 1;
        int yearBefore_days = Year.of(yearBefore).length();

        try {
            String cql = "select day_of_year, time from work_effort where user_id = ? and year = ? and day_of_year = ?";

            Object[][] partitionKeysArray = new Object[count][3];
            for (int i = 0; i < count; i++) {
                partitionKeysArray[i][0] = userId;
                // for first month of new year
                final int dayOfYear = startDay + i;
                if (dayOfYear <= 0) {
                    partitionKeysArray[i][1] = yearBefore;
                    partitionKeysArray[i][2] = yearBefore_days - Math.abs(dayOfYear);
                    continue;
                }

                partitionKeysArray[i][1] = year;
                partitionKeysArray[i][2] = dayOfYear;
            }

            final Long[] result = new Long[count];

            List<ListenableFuture<ResultSet>> futures = CassandraDataSource.getInstance().queryAll(cql, partitionKeysArray);

            for (ListenableFuture<ResultSet> future : futures) {
                ResultSet rs = future.get();
                if (rs.isExhausted()) continue;

                final Row row = rs.one();

                final int dayOfYear = row.getInt("day_of_year");

                int index;
                // for first month of new year
                if (startDay < 0) {
                    // numbers like 1, 2, 3
                    if (dayOfYear < count) {
                        index = Math.abs(startDay) + dayOfYear;
                    // number like 347, 348, etc
                    } else {
                        index = Math.abs(startDay) - (yearBefore_days - dayOfYear);
                    }
                    // common case
                } else {
                    index = dayOfYear - startDay;
                }
                result[index] = row.getLong("time");
            }

            return result;
        } catch (Throwable th) {
            LOG.error("Can't load words by words list userId: {}, year: [{}], startDay: [{}], count: [{}] ", userId, year, startDay, count, th);
            throw new RuntimeException(th);
        }
    }


    public Long[] evalUserEffort(UUID userId, PeriodLocalDate... periods) {
        Long[] efforts = new Long[periods.length];
        Arrays.fill(efforts, 0L);

        final ResultSet workResultSet = mds.runQuery("select time, day_of_year, year from work_effort where user_id = ?", true, userId);


        for (Row workEffortRow : workResultSet) {
            long currentTime = workEffortRow.getLong("time");

            final LocalDate localDate = LocalDate.ofYearDay(workEffortRow.getInt("year"), workEffortRow.getInt("day_of_year"));
            for (int i=0; i<periods.length; i++) {
                if (periods[i].isDateIn(localDate)) {
                    efforts[i] += currentTime;
                }
            }
        }

        return efforts;
    }
}
