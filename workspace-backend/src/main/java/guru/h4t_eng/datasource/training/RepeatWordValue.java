package guru.h4t_eng.datasource.training;

import guru.h4t_eng.datasource.voc_order_field.OneStringValues;
import guru.h4t_eng.datasource.voc_order_field.StringValueOrder;
import guru.h4t_eng.logs.AppLoggerFactory;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Value of repeated word.
 *
 * Created by aalexeenka on 14.03.2017.
 */
public class RepeatWordValue extends OneStringValues {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(RepeatWordValue.class);

    /**
     * Initial word period is 2 days
     */
    public static final int INITIAL_PERIOD = 2;

    public static final int MAX_PERIOD_VALUE = 128;

    public static final int POINTS_MULTIPLY_FACTOR = 2;

    private static final DateTimeFormatter date_format = DateTimeFormatter.ofPattern("dd-MM-yy");

    public RepeatWordValue() {
        date = date_format.format(LocalDate.now());
        period = INITIAL_PERIOD;
    }

    /**
     * String representation of date:
     * format dd-MM-yy, example 23-10-16
     */
    @StringValueOrder(order = 0, type = String.class)
    private String date;

    /**
     * Period in days in which word must be repeated,
     * once in 2 days, start
     * once in 4 days,
     * once in 8 days,
     * once in 16 days
     * once in 32 days
     * once in 64 days
     * once in 128 days, finish
     *
     */
    @StringValueOrder(order = 1)
    private long period = INITIAL_PERIOD;

    public RepeatWordValue(LocalDate date, long period) {
        this.date = date_format.format(date);
        this.period = period;
    }

    public String getDate() {
        return date;
    }

    public LocalDate getLocalDate() {
        if (date == null) {
            LOG.error("date is null, for period {}", period);
            return null;
        }
        return LocalDate.parse(date, date_format);
    }

    public boolean isNeedToBeRepeated() {
        final LocalDate localDate = getLocalDate();
        if (localDate == null) {
            return false;
        }
        return localDate.plusDays(period).isBefore(LocalDate.now());

    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof RepeatWordValue)) return false;

        RepeatWordValue that = (RepeatWordValue) o;

        return new EqualsBuilder()
                .append(period, that.period)
                .append(date, that.date)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(date)
                .append(period)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("date", date)
                .append("period", period)
                .toString();
    }

    public static RepeatWordValue valueOf(String databaseValue) {
        RepeatWordValue result = new RepeatWordValue();
        result.applyDatabaseValue(databaseValue);
        return result;
    }

    public RepeatWordValue nextRepeat(boolean memorized) {
        if (!memorized || period > MAX_PERIOD_VALUE) {
            return new RepeatWordValue(LocalDate.now(), period);
        }

        return new RepeatWordValue(LocalDate.now(), period * POINTS_MULTIPLY_FACTOR);
    }
}
