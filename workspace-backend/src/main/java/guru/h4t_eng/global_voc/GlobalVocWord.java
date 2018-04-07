package guru.h4t_eng.global_voc;

/**
 * Data about GlobalVocWord.
 *
 * Created by aalexeenka on 12.01.2017.
 */
public class GlobalVocWord {

    private GlobalVocAuthor author;
    private String engVal;
    private String imgURL;
    private String rusValueFav;
    private String engSentenceFav;
    private String wordType;
    private String engDevFav;

    public GlobalVocAuthor getAuthor() {
        return author;
    }

    public void setAuthor(GlobalVocAuthor author) {
        this.author = author;
    }

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

    public String getEngDevFav() {
        return engDevFav;
    }

    public void setEngDevFav(String engDevFav) {
        this.engDevFav = engDevFav;
    }
}
