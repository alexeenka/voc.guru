package guru.h4t_eng.datasource.training;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Value for map inside cassandra table.
 *
 * Created by aalexeenka on 14.03.2017.
 */
public class RepeatWordMapValue {

    private String word;

    private RepeatWordValue value;

    /**
     * when user don't use any hint
     */
    private boolean memorized = true;

    public RepeatWordMapValue(String word, RepeatWordValue value) {
        this.word = word;
        this.value = value;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public RepeatWordValue getValue() {
        return value;
    }

    public void setValue(RepeatWordValue value) {
        this.value = value;
    }

    public boolean isMemorized() {
        return memorized;
    }

    public void setMemorized(boolean memorized) {
        this.memorized = memorized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof RepeatWordMapValue)) return false;

        RepeatWordMapValue that = (RepeatWordMapValue) o;

        return new EqualsBuilder()
                .append(memorized, that.memorized)
                .append(word, that.word)
                .append(value, that.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(word)
                .append(value)
                .append(memorized)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("word", word)
                .append("value", value)
                .append("memorized", memorized)
                .toString();
    }
}
