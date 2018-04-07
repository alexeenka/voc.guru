package guru.h4t_eng.model.training;

import guru.h4t_eng.datasource.training.RepeatWordValue;
import guru.h4t_eng.model.WordType;
import guru.h4t_eng.model.dictionary.DictionaryWord;
import guru.h4t_eng.model.dictionary.EngDef;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Java representation of Json word form!
 *
 * Created by aalexeenka on 8/4/2015.
 */
public class TrainingWord {
    // eng value
    private String engVal;

    // EngDefs, need to replace with only one value
    private List<String> engDefs;
    // eng sentences
    private List<String> engSentences;
    // rus values
    private List<String> rusValues;
    // synonyms: todo:
    private List<String> engSynonyms;
    // antonyms: todo:
    private List<String> engAntonyms;
    // picture url
    private String picURL;

    private Object trainingExt;

    /**
     * Contains training word value for current training
     */
    private TrainingWordValue trainingValue;

    /**
     * Contains information about repeated value
     */
    private RepeatWordValue repeatWordValue;

    private Date updatedTime;

    private String wordTypeDesc;

    public TrainingWord(DictionaryWord word) {
        int index = ThreadLocalRandom.current().nextInt(0, word.getEngDefs().size());
        this.engVal = word.getEngVal();
        this.wordTypeDesc = WordType.evalStringValue(word.getType())
                + (word.getEngDefs().size() > 1 ? " - Def" + (index + 1) : "" );

        final EngDef engDef = word.getEngDefs().get(index);
        this.engDefs = engDef.getVal();
        this.engSentences = engDef.getEngSentences();
        this.rusValues = engDef.getRusValues();
        this.picURL = engDef.getImgUrl();

        this.updatedTime = word.getUpdatedTime();
    }

    public RepeatWordValue getRepeatWordValue() {
        return repeatWordValue;
    }

    public void setRepeatWordValue(RepeatWordValue repeatWordValue) {
        this.repeatWordValue = repeatWordValue;
    }

    public String getEngVal() {
        return engVal;
    }

    public void setEngVal(String engVal) {
        this.engVal = engVal;
    }

    public String getWordTypeDesc() {
        return wordTypeDesc;
    }

    public void setWordTypeDesc(String wordTypeDesc) {
        this.wordTypeDesc = wordTypeDesc;
    }

    public List<String> getEngDefs() {
        return engDefs;
    }

    public void setEngDefs(List<String> engDefs) {
        this.engDefs = engDefs;
    }

    public List<String> getEngSentences() {
        return engSentences;
    }

    public void setEngSentences(List<String> engSentences) {
        this.engSentences = engSentences;
    }

    public List<String> getRusValues() {
        return rusValues;
    }

    public void setRusValues(List<String> rusValues) {
        this.rusValues = rusValues;
    }

    public List<String> getEngSynonyms() {
        return engSynonyms;
    }

    public void setEngSynonyms(List<String> engSynonyms) {
        this.engSynonyms = engSynonyms;
    }

    public List<String> getEngAntonyms() {
        return engAntonyms;
    }

    public void setEngAntonyms(List<String> engAntonyms) {
        this.engAntonyms = engAntonyms;
    }

    public String getPicURL() {
        return picURL;
    }

    public void setPicURL(String picURL) {
        this.picURL = picURL;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public TrainingWordValue getTrainingValue() {
        return trainingValue;
    }

    public void setTrainingValue(TrainingWordValue trainingValue) {
        this.trainingValue = trainingValue;
    }

    public Object getTrainingExt() {
        return trainingExt;
    }

    public void setTrainingExt(Object trainingExt) {
        this.trainingExt = trainingExt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("engVal", engVal)
                .append("engDefs", engDefs)
                .append("engSentences", engSentences)
                .append("rusValues", rusValues)
                .append("engSynonyms", engSynonyms)
                .append("engAntonyms", engAntonyms)
                .append("picURL", picURL)
                .append("trainingValue", trainingValue)
                .append("updatedTime", updatedTime)
                .append("wordTypeDesc", wordTypeDesc)
                .append("trainingExt", trainingExt)
                .toString();
    }
}
