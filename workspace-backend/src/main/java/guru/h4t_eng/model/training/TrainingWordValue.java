package guru.h4t_eng.model.training;

import guru.h4t_eng.datasource.voc_order_field.OneStringValues;
import guru.h4t_eng.datasource.voc_order_field.StringValueOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * TrainingWordValue.
 *
 * Created by aalexeenka on 21.09.2016.
 */
public class TrainingWordValue extends OneStringValues {

    public static final String NEW_WORD_DATABASE_VALUE = new TrainingWordValue().toDatabaseValue();

    public TrainingWordValue() {
    }

    public TrainingWordValue(int p, int a, int s) {
        this.p = p;
        this.a = a;
        this.s = s;
    }

    /**
     * Points
     */
    @StringValueOrder(order = 0)
    protected int p = 0;

    /**
     * Attempts
     */
    @StringValueOrder(order = 1)
    protected int a = 0;

    /**
     * Streak
     */
    @StringValueOrder(order = 2)
    protected int s = 0;

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getS() {
        return s;
    }

    public void setS(int strikes) {
        this.s = strikes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("p", p)
                .append("a", a)
                .append("s", s)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof TrainingWordValue)) return false;

        TrainingWordValue that = (TrainingWordValue) o;

        return new EqualsBuilder()
                .append(p, that.p)
                .append(a, that.a)
                .append(s, that.s)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(p)
                .append(a)
                .append(s)
                .toHashCode();
    }

    public static TrainingWordValue valueOf(String databaseValue) {
        TrainingWordValue result = new TrainingWordValue();
        result.applyDatabaseValue(databaseValue);
        return result;
    }

}
