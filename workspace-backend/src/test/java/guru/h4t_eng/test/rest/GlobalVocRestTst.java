package guru.h4t_eng.test.rest;

import com.google.gson.reflect.TypeToken;
import guru.h4t_eng.global_voc.GlobalVoc;
import guru.h4t_eng.global_voc.GlobalVocService;
import guru.h4t_eng.global_voc.GlobalVocWord;
import guru.h4t_eng.model.dictionary.DictionaryWord;
import guru.h4t_eng.rest.global_voc.GlobalVocRest;
import guru.h4t_eng.rest.json.FastJsonBuilder;
import guru.h4t_eng.rest.json.FastJsonParser;
import guru.h4t_eng.test.rest.config.AbstractRest4Tst;
import guru.h4t_eng.test.util.WordDataSource4Tst;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import static guru.h4t_eng.UsersDatabaseData4Tst.SAMANTA_UUID;
import static guru.h4t_eng.rest.global_voc.GlobalVocRest.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.*;

/**
 * Test for GlobalVocRest
 * <p>
 * Created by aalexeenka on 17.01.2017.
 */
public class GlobalVocRestTst extends AbstractRest4Tst {

    @Override
    protected Class getRestClass() {
        return GlobalVocRest.class;
    }

    @BeforeClass
    public static void initGlobalVoc() throws ExecutionException, InterruptedException {
        final CompletableFuture<Void> initializeAsync = GlobalVocService.getInstance().initializeAsync();
        initializeAsync.get();

        duplicateWords = findDuplicateWord();
    }

    private static List<String> duplicateWords;

    public static List<String> findDuplicateWord() {
        List<String> duplicateWords = new ArrayList<>();

        final List<String> words = GlobalVoc.getInstance().getWords();
        for (String word : words) {
            final LinkedList<GlobalVocWord> wordValues = GlobalVoc.getInstance().getWordValues(word);
            if (wordValues.size() > 1) {
                duplicateWords.add(word);
            }
        }

        System.out.println("Duplicate words: " + Arrays.toString(duplicateWords.toArray()));

        return duplicateWords;
    }

    @Test
    public void autocompleteFunctionality() {
        // load autocomplete word for letter 'A'
        List<String> autocompleteWords;
        {
            Response response = target("/global-voc").path("autocomplete").request().post(
                    Entity.text(FastJsonBuilder.oneJsonValue("w", "A"))
            );
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            final String jsonStr = response.readEntity(String.class);
            autocompleteWords = Arrays.asList(GSON.fromJson(jsonStr, String[].class));
            assertNotNull(autocompleteWords);
            assertThat(autocompleteWords.size(), greaterThan(0));
            assertThat(autocompleteWords, everyItem(startsWith("A")));
        }

        // load first word with letter A
        {
            String loadWord = autocompleteWords.get(0);
            loadRandomWord(loadWord);
        }

        // load words from duplicate list
        {
            final String word = duplicateWords.get(ThreadLocalRandom.current().nextInt(duplicateWords.size()));
            loadRandomWord(word);
        }
    }

    private void loadRandomWord(String word) {
        Response response = target("/global-voc").path("load-word").request().post(
                Entity.text(FastJsonBuilder.oneJsonValue("w", word))
        );
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        final String jsonStr = response.readEntity(String.class);
        final List<GlobalVocWord> vocWords = Arrays.asList(GSON.fromJson(jsonStr, GlobalVocWord[].class));
        assertNotNull(vocWords);
        assertThat(vocWords.size(), greaterThan(0));
    }

    @Test
    public void randomSlideFunctionality() {
        // load random slide words
        final List<GlobalVocWord> vocWords;
        {
            Response response = target("/global-voc").path("get-random-words").request().get();
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            final String gson = response.readEntity(String.class);

            Type datasetListType = new TypeToken<Collection<GlobalVocWord>>() {
            }.getType();
            vocWords = GlobalVocRest.GSON.fromJson(gson, datasetListType);

            for (GlobalVocWord item : vocWords) {
                assertNotEquals(SAMANTA_UUID, item.getAuthor());
            }
        }

        // copy word
        {
            final GlobalVocWord word = vocWords.get(0);
            // call without copy, same author
            copyWord(SAMANTA_UUID, word.getEngVal(), COPY_WORD_CURRENT_USER_AUTHOR);
            // call without copy, word already exist
            copyWord(word.getAuthor().getUid(), WordDataSource4Tst.getFirstUserWord(SAMANTA_UUID), COPY_WORD_ALREADY_EXIST);
            // call with copy
            {
                final String result = copyWord(word.getAuthor().getUid(), word.getEngVal(), COPY_WORD_SUCCESS);
                final Boolean added = FastJsonParser.getBoolean(result, "added");
                assertTrue(added);
            }
            // check word in database
            {
                final DictionaryWord copiedWord = wds.loadSingleWord(SAMANTA_UUID, word.getEngVal());
                assertNotNull(copiedWord);
                final DictionaryWord expectedWord = wds.loadSingleWord(word.getAuthor().getUid(), word.getEngVal());
                // updated time must be different
                assertNotEquals(expectedWord.getUpdatedTime(), copiedWord.getUpdatedTime());
                // reset updated time
                copiedWord.setUpdatedTime(null);
                expectedWord.setUpdatedTime(null);
                assertEquals(expectedWord, copiedWord);
            }
        }
    }

    private String copyWord(UUID uid, String engVal, String checkedMsg) {
        Response response = target("/global-voc").path("copy-word").request().post(
                Entity.text(new FastJsonBuilder()
                        .append("author", uid)
                        .next()
                        .append("word", engVal)
                        .finish()
                )
        );
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        final String jsonStr = response.readEntity(String.class);
        final String msg = FastJsonParser.getString(jsonStr, "msg");
        assertEquals(msg, checkedMsg);
        return jsonStr;
    }

}
