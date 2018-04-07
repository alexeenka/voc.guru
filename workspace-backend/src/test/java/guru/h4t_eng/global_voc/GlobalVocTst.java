package guru.h4t_eng.global_voc;

import com.google.common.collect.Ordering;
import guru.h4t_eng.test.WithLogging;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * GlobalVocTest.
 *
 * Created by aalexeenka on 12.01.2017.
 */
public class GlobalVocTst extends WithLogging {

    @Test
    public void checkBaseGlobalVocFunctions() throws ExecutionException, InterruptedException {

        final CompletableFuture<Void> initializeAsync = GlobalVocService.getInstance().initializeAsync();
        initializeAsync.get();

        final GlobalVoc instance = GlobalVoc.getInstance();
        assertNotNull(instance.getAuthors());
        final List<String> words = instance.getWords();
        assertNotNull(words);

        // check work of search algorithm, for all words
        {
            final Instant startTime = Instant.now();
            for (int i=0; i<words.size(); i++) {
                final String searchWord = words.get((words.size() - 1) / 2);
                assertVocWord(instance, searchWord);
            }
            final Instant finishTime = Instant.now();
            System.out.println("Iterate all words, size: " + words.size() + ". Duration: " + Duration.between(startTime, finishTime).toMillis() + "ms");
        }

        // check alphabet
        {
            final Instant startTime = Instant.now();
            for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
                assertVocWord(instance, Character.toString(alphabet));
            }
            final Instant finishTime = Instant.now();
            System.out.println("Iterate alphabet: " + ('Z' - 'A' + 1) + ". Duration: " + Duration.between(startTime, finishTime).toMillis() + "ms");
        }

        // check 3 letter word
        {
            final String searchWord = "All";
            List<String> search = instance.findWords(searchWord);
            assertVocWord(instance, searchWord);
        }
    }

    private void assertVocWord(GlobalVoc instance, String searchWord) {
        final List<String> search = instance.findWords(searchWord);
        for (String iWord : search) {
            assertThat(iWord, startsWith(searchWord));
            assertTrue(Ordering.natural().isOrdered(search));
        }
    }
}
