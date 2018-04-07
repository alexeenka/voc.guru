package guru.h4t_eng.datasource.voc_order_field;

/**
 * OneStringValues.
 * <p>
 * Help to produce values in one string.
 * <p>
 * Created by aalexeenka on 23.09.2016.
 */
public class OneStringValues {

    public static String toDatabaseValue(OneStringValues instance) {
        return OneStringValuesUtil.toDatabaseValue(instance);
    }

    public String toDatabaseValue() {
        return OneStringValuesUtil.toDatabaseValue(this);
    }

    public void applyDatabaseValue(String databaseValue) {
        OneStringValuesUtil.applyDatabaseValue(this, databaseValue);
    }

}
