package guru.h4t_eng.model.dictionary;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Date;

/**
 * DictionaryWord.
 *
 * Created by aalexeenka on 29.07.2016.
 */
public class DictionaryWord {

    private String engVal;

    private int type;

    private Date createdTime;
    private Date updatedTime;

    private ArrayList<EngDef> engDefs = new ArrayList<>();

    public String getEngVal() {
        return engVal;
    }

    public void setEngVal(String engVal) {
        this.engVal = engVal;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ArrayList<EngDef> getEngDefs() {
        return engDefs;
    }

    public void setEngDefs(ArrayList<EngDef> engDefs) {
        this.engDefs = engDefs;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("engVal", engVal)
                .append("type", type)
                .append("createdTime", createdTime)
                .append("updatedTime", updatedTime)
                .append("engDefs", engDefs)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof DictionaryWord)) return false;

        DictionaryWord that = (DictionaryWord) o;

        return new EqualsBuilder()
                .append(type, that.type)
                .append(engVal, that.engVal)
                .append(createdTime, that.createdTime)
                .append(updatedTime, that.updatedTime)
                .append(engDefs, that.engDefs)
                .isEquals();
    }

    public boolean equalsExcludeDispensableFields(Object o) {
        if (this == o) return true;

        if (!(o instanceof DictionaryWord)) return false;

        DictionaryWord that = (DictionaryWord) o;

        return new EqualsBuilder()
                .append(type, that.type)
                .append(engVal, that.engVal)
                .append(engDefs, that.engDefs)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(engVal)
                .append(type)
                .append(createdTime)
                .append(updatedTime)
                .append(engDefs)
                .toHashCode();
    }

    public void addEngDef(EngDef def) {
        if (def == null) return;
        engDefs.add(def);
    }
}
