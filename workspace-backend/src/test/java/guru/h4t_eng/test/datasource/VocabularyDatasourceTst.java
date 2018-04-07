package guru.h4t_eng.test.datasource;

import guru.h4t_eng.datasource.vocabulary.VocabularyDatasource;
import guru.h4t_eng.model.dictionary.DictionaryWord;
import guru.h4t_eng.rest.words.model.FormData;
import guru.h4t_eng.test.WithLogging;
import guru.h4t_eng.test.util.Utils4Tst;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.IsNot.not;

/**
 * VocabularyDatasourceTst.
 *
 * Created by aalexeenka on 12/16/2015.
 */
public class VocabularyDatasourceTst extends WithLogging {

    @Test
    public void testWordIndexAlphabetInsertDelete() {
        final FormData formData = Utils4Tst.newFormDataFromJson__1();
        final DictionaryWord word = formData.getWord();

        // insert and check
        {
            VocabularyDatasource.getInstance().insert(formData.getUser(), word.getEngVal());
            Set<String> values = VocabularyDatasource.getInstance().loadWordsByLetter(formData.getUser(), word.getEngVal().charAt(0));
            assertThat(values, hasItem(word.getEngVal()));
        }

        // delete and check
        {
            VocabularyDatasource.getInstance().delete(formData.getUser(), word.getEngVal());
            Set<String> values = VocabularyDatasource.getInstance().loadWordsByLetter(formData.getUser(), word.getEngVal().charAt(0));
            assertThat(values, not(hasItem(word.getEngVal())));
        }
    }

}
