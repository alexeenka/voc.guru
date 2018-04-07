package guru.h4t_eng.rest;

import java.util.Date;

/**
 * WordListDto.
 * <p/>
 * Created by Alexey Alexeenka on 24.09.2015.
 */
public class WordListDto {
    private String engVal;
    private String imgURL;
    private String rusValueFav;
    private String engSentenceFav;
    private String wordType;
    private String engDevFav;
    private int engRusPoints;
    private int defEngPoints;
    private int rusEngPoints;
    private Date updatedTime;
    private int imgEngPoints;

    public String getEngVal() {
        return engVal;
    }

    public void setEngVal(String engVal) {
        this.engVal = engVal;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public String getRusValueFav() {
        return rusValueFav;
    }

    public void setRusValueFav(String rusValueFav) {
        this.rusValueFav = rusValueFav;
    }

    public void setEngDevFav(String engDevFav) {
        this.engDevFav = engDevFav;
    }

    public String getEngDevFav() {
        return engDevFav;
    }

    public void setEngRusPoints(int engRusPoints) {
        this.engRusPoints = engRusPoints;
    }

    public int getEngRusPoints() {
        return engRusPoints;
    }

    public void setDefEngPoints(int defEngPoints) {
        this.defEngPoints = defEngPoints;
    }

    public int getDefEngPoints() {
        return defEngPoints;
    }

    public int getRusEngPoints() {
        return rusEngPoints;
    }

    public void setRusEngPoints(int rusEngPoints) {
        this.rusEngPoints = rusEngPoints;
    }

    public void setImgEngPoints(int imgEngPoints) {
        this.imgEngPoints = imgEngPoints;
    }

    public int getImgEngPoints() {
        return imgEngPoints;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public String getEngSentenceFav() {
        return engSentenceFav;
    }

    public void setEngSentenceFav(String engSentenceFav) {
        this.engSentenceFav = engSentenceFav;
    }

    public String getWordType() {
        return wordType;
    }

    public void setWordType(String wordType) {
        this.wordType = wordType;
    }
}
