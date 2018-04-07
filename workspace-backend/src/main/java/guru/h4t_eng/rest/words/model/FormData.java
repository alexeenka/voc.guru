package guru.h4t_eng.rest.words.model;

import guru.h4t_eng.model.dictionary.DictionaryWord;
import guru.h4t_eng.model.training.CurrentDayYear;

import java.util.UUID;

/**
 * FormData.
 *
 * Created by aalexeenka on 01.08.2016.
 */
public class FormData {

    private String prevEngVal;

    private UUID user;

    private DictionaryWord word;

    private CurrentDayYear currentDayYear;

    public String getPrevEngVal() {
        return prevEngVal;
    }

    public void setPrevEngVal(String prevEngVal) {
        this.prevEngVal = prevEngVal;
    }

    public DictionaryWord getWord() {
        return word;
    }

    public void setWord(DictionaryWord word) {
        this.word = word;
    }

    public UUID getUser() {
        return user;
    }

    public void setUser(UUID user) {
        this.user = user;
    }

    public CurrentDayYear getCurrentDayYear() {
        return currentDayYear;
    }

    public void setCurrentDayYear(CurrentDayYear currentDayYear) {
        this.currentDayYear = currentDayYear;
    }
}
