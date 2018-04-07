package guru.h4t_eng.common;

import java.time.LocalDate;

/**
 * PeriodLocalDate.
 *
 * Created by aalexeenka on 11.05.2017.
 */
public class PeriodLocalDate {

    private LocalDate begin;
    private LocalDate end;

    public PeriodLocalDate(LocalDate begin, LocalDate end) {
        this.begin = begin;
        this.end = end;
    }

    public LocalDate getBegin() {
        return begin;
    }

    public LocalDate getEnd() {
        return end;
    }

    public boolean isDateIn(LocalDate date) {
        return date.isEqual(begin) || date.isEqual(end) || (date.isBefore(end) && date.isAfter(begin));
    }
}
