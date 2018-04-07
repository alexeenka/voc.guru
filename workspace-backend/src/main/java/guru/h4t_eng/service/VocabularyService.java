package guru.h4t_eng.service;

import guru.h4t_eng.datasource.LoadWordsMethodReturn;
import guru.h4t_eng.datasource.WordDataSource;
import guru.h4t_eng.datasource.vocabulary.VocabularyDatasource;

import java.util.Set;
import java.util.UUID;

/**
 * VocabularyService.
 *
 * Created by aalexeenka on 12/18/2015.
 */
public class VocabularyService {

    private static VocabularyService instance = new VocabularyService();

    public static VocabularyService getInstance() {
        return instance;
    }

    private VocabularyService() {
    }

    public LoadWordsMethodReturn loadWordsByLetter(UUID userId, char letter) {
        Set<String> words = VocabularyDatasource.getInstance().loadWordsByLetter(userId, letter);
        return WordDataSource.getInstance().loadWordsByEngVals(userId, words);
    }
}
