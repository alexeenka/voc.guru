package guru.h4t_eng.model.dictionary;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * EngDef.
 *
 * Created by aalexeenka on 29.07.2016.
 */
public class EngDef {

    private List<String> val;

    private List<String> rusValues = new ArrayList<>();

    private List<String> engSentences = new ArrayList<>();

    private String imgUrl;

    public List<String> getVal() {
        return val;
    }

    public void setVal(List<String> val) {
        this.val = val;
    }

    public List<String> getRusValues() {
        return rusValues;
    }

    public void setRusValues(List<String> rusValues) {
        this.rusValues = rusValues;
    }

    public List<String> getEngSentences() {
        return engSentences;
    }

    public void setEngSentences(List<String> engSentences) {
        this.engSentences = engSentences;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("val", val)
                .append("rusValues", rusValues)
                .append("engSentences", engSentences)
                .append("imgUrl", imgUrl)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof EngDef)) return false;

        EngDef engDef = (EngDef) o;

        return new EqualsBuilder()
                .append(val, engDef.val)
                .append(rusValues, engDef.rusValues)
                .append(engSentences, engDef.engSentences)
                .append(imgUrl, engDef.imgUrl)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(val)
                .append(rusValues)
                .append(engSentences)
                .append(imgUrl)
                .toHashCode();
    }
}
