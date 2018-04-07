package guru.h4t_eng.test.util;

import guru.h4t_eng.datasource.voc_order_field.OneStringValuesUtil;
import guru.h4t_eng.datasource.work_effort.WorkEffortValuePerDay;
import guru.h4t_eng.model.training.TrainingWordValue;
import guru.h4t_eng.test.WithLogging;
import org.junit.Assert;
import org.junit.Test;

import static guru.h4t_eng.datasource.voc_order_field.OneStringValuesUtil.SEPARATOR;
import static org.junit.Assert.assertEquals;

/**
 * Test annotation OneStringValues.
 *
 * Created by aalexeenka on 23.09.2016.
 */
public class OneStringValuesUtilsTst extends WithLogging {

    @Test
    public void test_TrainingWordValue_entity() {
        final TrainingWordValue testedObj = new TrainingWordValue(0, 235, 2);

        final String databaseValue = TrainingWordValue.toDatabaseValue(testedObj);
        Assert.assertEquals("0;235;2", databaseValue);

        {
            final TrainingWordValue newValue = TrainingWordValue.valueOf(databaseValue);
            Assert.assertEquals(testedObj, newValue);
        }

        // test extreme situations
        {
            final TrainingWordValue newValue = TrainingWordValue.valueOf("123");
            Assert.assertEquals(new TrainingWordValue(123,0,0), newValue);
        }
        {
            final TrainingWordValue newValue = TrainingWordValue.valueOf("123;");
            Assert.assertEquals(new TrainingWordValue(123,0,0), newValue);
        }
        {
            final TrainingWordValue newValue = TrainingWordValue.valueOf("123;22;44");
            Assert.assertEquals(new TrainingWordValue(123,22,44), newValue);
        }
        {
            final TrainingWordValue newValue = TrainingWordValue.valueOf(";0");
            Assert.assertEquals(new TrainingWordValue(0,0,0), newValue);
        }
        {
            final TrainingWordValue newValue = TrainingWordValue.valueOf("asdasdads;0asdsda");
            Assert.assertEquals(new TrainingWordValue(0,0,0), newValue);
        }
        {
            final TrainingWordValue newValue = TrainingWordValue.valueOf("as");
            Assert.assertEquals(new TrainingWordValue(0,0,0), newValue);
        }
    }

    @Test
    public void test_WorkEffortValuePerDay_entity() {
        // missed one param
        {
            final WorkEffortValuePerDay workEffortValuePerDay = WorkEffortValuePerDay.valueOf("11;22;33;44;55");
            assertEquals(new WorkEffortValuePerDay(11, 22, 33, 44, 55, 0), workEffortValuePerDay);
        }
        // more than one param
        {
            final WorkEffortValuePerDay workEffortValuePerDay = WorkEffortValuePerDay.valueOf("11;22;33;44;55;66;77");
            assertEquals(new WorkEffortValuePerDay(11, 22, 33, 44, 55, 66), workEffortValuePerDay);
        }
        // equal number of params
        {
            final String databaseValue = "11;22;33;44;55;66";

            final WorkEffortValuePerDay workEffortValuePerDay = WorkEffortValuePerDay.valueOf(databaseValue);

            final WorkEffortValuePerDay expected = new WorkEffortValuePerDay(11, 22, 33, 44, 55, 66);
            assertEquals(expected, workEffortValuePerDay);
            assertEquals(expected.toDatabaseValue(), databaseValue);
        }
    }


    /**
     * Performance test.
     */
    public static void main(String[] args) throws Exception {
        Class.forName(OneStringValuesUtil.class.getName(), true, OneStringValuesUtil.class.getClassLoader());

        doReflection();
        doRegular();
    }

    private static final String performance_database_value = "11;22;33;44;55;66";

    private static void doRegular() throws Exception
    {
        long start = System.currentTimeMillis();
        for (int i=0; i<10000000; i++)
        {
            WorkEffortValuePerDay work = new WorkEffortValuePerDay();
            final String[] values = performance_database_value.split(SEPARATOR);
            work.setWorkEffort(Integer.valueOf(values[0]));
            work.setNewWords(Integer.valueOf(values[1]));
            work.setDefEngCount(Integer.valueOf(values[2]));
            work.setEngRusCount(Integer.valueOf(values[3]));
            work.setImgEngCount(Integer.valueOf(values[4]));
            work.setRusEngCount(Integer.valueOf(values[5]));
        }
        System.out.println("doRegular: " + (System.currentTimeMillis() - start));
    }

    private static void doReflection() throws Exception
    {
        long start = System.currentTimeMillis();
        for (int i=0; i<10000000; i++)
        {
            WorkEffortValuePerDay work = new WorkEffortValuePerDay();
            work.applyDatabaseValue(performance_database_value);
        }
        System.out.println("doReflection: " + (System.currentTimeMillis() - start));
    }
}