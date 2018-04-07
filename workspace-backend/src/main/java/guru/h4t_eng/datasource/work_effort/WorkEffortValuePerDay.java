package guru.h4t_eng.datasource.work_effort;

import guru.h4t_eng.datasource.voc_order_field.OneStringValues;
import guru.h4t_eng.datasource.voc_order_field.StringValueOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * WorkEffortValuePerDay.
 *
 * Created by aalexeenka on 23.09.2016.
 */
public class WorkEffortValuePerDay extends OneStringValues {

    @StringValueOrder(order = 0)
    private int workEffort;

    @StringValueOrder(order = 1)
    private int newWords;

    @StringValueOrder(order = 2)
    private int engRusCount;

    @StringValueOrder(order = 3)
    private int rusEngCount;

    @StringValueOrder(order = 4)
    private int imgEngCount;

    @StringValueOrder(order = 5)
    private int defEngCount;

    public WorkEffortValuePerDay() {
    }

    public WorkEffortValuePerDay(int workEffort, int newWords, int engRusCount, int rusEngCount, int imgEngCount, int defEngCount) {
        this.workEffort = workEffort;
        this.newWords = newWords;
        this.engRusCount = engRusCount;
        this.rusEngCount = rusEngCount;
        this.imgEngCount = imgEngCount;
        this.defEngCount = defEngCount;
    }

    public int getWorkEffort() {
        return workEffort;
    }

    public void setWorkEffort(int workEffort) {
        this.workEffort = workEffort;
    }

    public int getNewWords() {
        return newWords;
    }

    public void setNewWords(int newWords) {
        this.newWords = newWords;
    }

    public int getEngRusCount() {
        return engRusCount;
    }

    public void setEngRusCount(int engRusCount) {
        this.engRusCount = engRusCount;
    }

    public int getRusEngCount() {
        return rusEngCount;
    }

    public void setRusEngCount(int rusEngCount) {
        this.rusEngCount = rusEngCount;
    }

    public int getImgEngCount() {
        return imgEngCount;
    }

    public void setImgEngCount(int imgEngCount) {
        this.imgEngCount = imgEngCount;
    }

    public int getDefEngCount() {
        return defEngCount;
    }

    public void setDefEngCount(int defEngCount) {
        this.defEngCount = defEngCount;
    }

    public static WorkEffortValuePerDay valueOf(String databaseValue) {
        WorkEffortValuePerDay result = new WorkEffortValuePerDay();
        result.applyDatabaseValue(databaseValue);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof WorkEffortValuePerDay)) return false;

        WorkEffortValuePerDay that = (WorkEffortValuePerDay) o;

        return new EqualsBuilder()
                .append(workEffort, that.workEffort)
                .append(newWords, that.newWords)
                .append(engRusCount, that.engRusCount)
                .append(rusEngCount, that.rusEngCount)
                .append(imgEngCount, that.imgEngCount)
                .append(defEngCount, that.defEngCount)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(workEffort)
                .append(newWords)
                .append(engRusCount)
                .append(rusEngCount)
                .append(imgEngCount)
                .append(defEngCount)
                .toHashCode();
    }
}
