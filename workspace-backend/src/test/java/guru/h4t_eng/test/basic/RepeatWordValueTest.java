package guru.h4t_eng.test.basic;

import guru.h4t_eng.datasource.training.RepeatWordValue;
import guru.h4t_eng.test.WithLogging;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * RepeatWordValueTest.
 *
 * Created by aalexeenka on 15.03.2017.
 */
public class RepeatWordValueTest extends WithLogging {

    @Test
    public void testRepeatWordValue() {
        // convert test
        {
            final LocalDate now = LocalDate.now();
            final RepeatWordValue value1 = new RepeatWordValue(now, 2);
            final RepeatWordValue value2 = RepeatWordValue.valueOf(value1.toDatabaseValue());
            assertEquals(value1, value2);
            final LocalDate date2 = value2.getLocalDate();
            assertEquals(now, date2);
        }
        // test for isNeedToBeRepeated
        {
            assertFalse(new RepeatWordValue(LocalDate.now(), 2).isNeedToBeRepeated());
            assertFalse(RepeatWordValue.valueOf("31-12-99;8").isNeedToBeRepeated());
            assertTrue(RepeatWordValue.valueOf("31-12-16;8").isNeedToBeRepeated());
        }

    }

}
