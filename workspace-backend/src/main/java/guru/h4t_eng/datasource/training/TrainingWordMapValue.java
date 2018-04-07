package guru.h4t_eng.datasource.training;

import guru.h4t_eng.model.training.TrainingWordValue;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * TrainingWordMapValue.
 * <p>
 * <p>
 * Created by aalexeenka on 06.09.2016.
 */
public class TrainingWordMapValue extends TrainingWordValue {

    public TrainingWordMapValue(String word, int points, int attempts) {
        this.w = word;
        this.p = points;
        this.a = attempts;
    }

    public TrainingWordMapValue(String word, int points, int attempts, int streak) {
        this.w = word;
        this.p = points;
        this.a = attempts;
        this.s = streak;
    }

    public TrainingWordMapValue(TrainingWordValue value) {
        this.p = value.getP();
        this.a = value.getA();
        this.s = value.getS();
    }

    /**
     * Word
     */
    private String w;


    public String getWord() {
        return w;
    }

    public void setWord(String word) {
        this.w = word;
    }

    public static TrainingWordMapValue toValue(String word, String databaseValue) {
        final TrainingWordMapValue result = new TrainingWordMapValue(
                valueOf(databaseValue)
        );

        result.setWord(word);

        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("w", w)
                .toString();
    }

    /**
     * To avoid extra fields when do GSON serialization
     *
     * @return TrainingWordValue
     */
    public TrainingWordValue toTrainingWord() {
        return new TrainingWordValue(this.p, this.a, this.s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof TrainingWordMapValue)) return false;

        TrainingWordMapValue that = (TrainingWordMapValue) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(w, that.w)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(w)
                .toHashCode();
    }
}
