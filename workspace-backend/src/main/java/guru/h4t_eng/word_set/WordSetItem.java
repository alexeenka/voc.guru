package guru.h4t_eng.word_set;

import java.util.ArrayList;

/**
 * WordSetItem.
 *
 * Created by Alexey Alexeenka on 05.06.2017.
 */
@SuppressWarnings("WeakerAccess")
public class WordSetItem {

    private String engVal;
    private String engSentence;
    private String imgUrl;
    private ArrayList<String> rusValues;

    public void setEngVal(String engVal) {
        this.engVal = engVal;
    }

    public String getEngVal() {
        return engVal;
    }

    public String getEngSentence() {
        return engSentence;
    }

    public void setEngSentence(String engSentence) {
        this.engSentence = engSentence;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public ArrayList<String> getRusValues() {
        return rusValues;
    }

    public void setRusValues(ArrayList<String> rusValues) {
        this.rusValues = rusValues;
    }
}
