package guru.h4t_eng.datasource.training;

import guru.h4t_eng.model.training.TrainingWordValue;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Contains information when user save the result.
 *
 * Created by aalexeenka on 20.03.2017.
 */
public class TrainingFinishDto {

    public TrainingFinishDto() {
    }

    public TrainingFinishDto(String w, TrainingWordValue tv, RepeatWordValue rv) {
        this.w = w;
        this.tv = tv;
        this.rv = rv;
    }

    /**
     * English word.
     */
    private String w;

    /**
     * Training value, small name to reduce transfer data size
     */
    private TrainingWordValue tv;

    /**
     * Repeat value, small name to reduce transfer data size
     */
    private RepeatWordValue rv;


    public String getW() {
        return w;
    }

    public void setW(String w) {
        this.w = w;
    }

    public TrainingWordValue getTv() {
        return tv;
    }

    public void setTv(TrainingWordValue tv) {
        this.tv = tv;
    }

    public RepeatWordValue getRv() {
        return rv;
    }

    public void setRv(RepeatWordValue rv) {
        this.rv = rv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof TrainingFinishDto)) return false;

        TrainingFinishDto finishDto = (TrainingFinishDto) o;

        return new EqualsBuilder()
                .append(w, finishDto.w)
                .append(tv, finishDto.tv)
                .append(rv, finishDto.rv)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(w)
                .append(tv)
                .append(rv)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("w", w)
                .append("tv", tv)
                .append("rv", rv)
                .toString();
    }
}
