package guru.h4t_eng.social_networks;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.temporal.ChronoUnit;

/**
 * ExpiredInFacebookTst.
 *
 * Created by aalexeenka on 30.11.2016.
 */
public class ExpiredInFacebookTst {

    @Test
    public void a() {
        final Long days;
        LocalDate today = LocalDate.now();
        LocalDate june21 = LocalDate.now().with(Month.JUNE).withDayOfMonth(21);
        if (today.isAfter(june21)) {
            june21.plusYears(1);
            days = ChronoUnit.DAYS.between(june21, today);
        } else {
            days = ChronoUnit.DAYS.between(today, june21);
        }

        System.out.println("Days: " + days);

        LocalDate birthday = LocalDate.of(1985, Month.FEBRUARY, 3);

        Period p = Period.between(birthday, today);
        long p2 = ChronoUnit.DAYS.between(birthday, today);
        System.out.println("You are " + p.getYears() + " years, " + p.getMonths() +
                " months, and " + p.getDays() +
                " days old. (" + p2 + " days total)");

        Duration.between(LocalDate.now(), LocalDate.now());

    }
}
