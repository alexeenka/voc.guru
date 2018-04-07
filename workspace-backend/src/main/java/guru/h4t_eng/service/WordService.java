package guru.h4t_eng.service;

import guru.h4t_eng.datasource.WordDataSource;
import guru.h4t_eng.datasource.WorkEffortDataSource;
import guru.h4t_eng.rest.words.model.FormData;

/**
 * WordService.
 * <p>
 *
 * Created by aalexeenka on 12/16/2015.
 */
public class WordService {

    private static final WordService instance;

    private WordService() {
    }

    static {
        instance = new WordService();
    }

    public static final int NEW_WORD_EFFORT_SEC = 300;

    public static WordService getInstance() {
        return instance;
    }

    protected WordDataSource wordDataSource = WordDataSource.getInstance();
    protected WorkEffortDataSource workEffortDataSource = WorkEffortDataSource.getInstance();

    public void saveWord (FormData formData) {

        wordDataSource.saveWord(formData, true);

        boolean isNewWord = WordDataSource.isNewWord(formData.getPrevEngVal());
        if (isNewWord)
        {
            workEffortDataSource.updateTime(formData.getUser(), formData.getCurrentDayYear().getYear(), formData.getCurrentDayYear().getDayOfYear(), NEW_WORD_EFFORT_SEC);
        }
    }


}
