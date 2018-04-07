package guru.h4t_eng.test.word_set;

import com.datastax.driver.core.utils.UUIDs;
import com.google.gson.reflect.TypeToken;
import guru.h4t_eng.datasource.WordDataSource;
import guru.h4t_eng.test.rest.config.AbstractRest4Tst;
import guru.h4t_eng.test.rest.config.AnyUser4TstFilter;
import guru.h4t_eng.test.util.TestContext;
import guru.h4t_eng.word_set.WordSet;
import guru.h4t_eng.word_set.WordSetItem;
import guru.h4t_eng.word_set.WordSetRest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.UUID;

import static guru.h4t_eng.test.util.Utils4Tst.GSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * WordSetTest.
 * <p>
 * Created by Alexey Alexeenka on 08.06.2017.
 */
public class WordSetTest extends AbstractRest4Tst {
    @Override
    protected Class getRestClass() {
        return WordSetRest.class;
    }

    @Override
    protected Class getUserSecurityFilter() {
        return AnyUser4TstFilter.class;
    }

    @Test
    public void add10Words() {
        for (WordSet wordSet : WordSet.values()) {
            System.out.println("Processed WordSet: " + wordSet);

            final UUID userId = UUIDs.timeBased();
            TestContext.getInstance().setTestUserId(userId);

            // get and check list
            final ArrayList<WordSetItem> wordSetItems = getAndAssertList(wordSet.getId());

            final int single_size_butch = 13;
            for (int i = 0; i < single_size_butch; i++) {
                final WordSetItem iItem = wordSetItems.get(i);
                final String result = addSingleWord(iItem, wordSet.getId());
                assertEquals("1", result);

                final boolean wordExist = WordDataSource.getInstance().isWordExist(userId, iItem.getEngVal());
                assertTrue(wordExist);
            }


            final int size = (wordSetItems.size() - single_size_butch);
            final int itCount = size / 10 + (size % 10 > 0 ? 1 : 0);
            for (int i = 0; i < itCount; i++) {
                final Response resp = target().path("/word-set/add-10-words").request().post(Entity.text("{\"listId\"" + ":" + wordSet.getId() + "}"));
                assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
                final String responseStr = resp.readEntity(String.class);

                ArrayList<String> words = GSON.fromJson(responseStr, new TypeToken<ArrayList<String>>() {
                }.getType());

                assertThat(words, hasSize(greaterThan(0)));

                final int pos = single_size_butch + i * 10;
                for (int j = pos; j < pos + words.size(); j++) {
                    final String iWord = words.get(j - pos);
                    assertEquals(wordSetItems.get(j).getEngVal(), iWord);

                    final boolean wordExist = WordDataSource.getInstance().isWordExist(userId, iWord);
                    assertTrue(wordExist);

                }
            }

            // last send, all words are added.
            {
                final Response resp = target().path("/word-set/add-10-words").request().post(Entity.text("{\"listId\"" + ":" + wordSet.getId() + "}"));
                assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
                final String responseStr = resp.readEntity(String.class);
                ArrayList<String> words = GSON.fromJson(responseStr, new TypeToken<ArrayList<String>>() {
                }.getType());
                assertEquals(0, words.size());
            }

            // all words are added, recheck
            checkAllWordsAdded(userId, wordSetItems, wordSet);
            // todo: add check for second set.
            break;
        }

    }

    private String addSingleWord(WordSetItem iItem, int wordSetId) {
        final Response resp = target().path("/word-set/add-single-word").request().post(Entity.text("{\"listId\"" + ":" + wordSetId + ",\"word\":\"" + iItem.getEngVal() + "\"}"));
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        return resp.readEntity(String.class);
    }


    @Test
    public void addSingleWord() {
        for (WordSet wordSet : WordSet.values()) {
            System.out.println("Processed WordSet: " + wordSet);

            final UUID userId = UUIDs.timeBased();
            TestContext.getInstance().setTestUserId(userId);


            // get and check list
            final ArrayList<WordSetItem> wordSetItems = getAndAssertList(wordSet.getId());

            // add all elements, for NEW
            for (WordSetItem iItem : wordSetItems) {
                final String result = addSingleWord(iItem, wordSet.getId());
                assertEquals("1", result);

                final boolean wordExist = WordDataSource.getInstance().isWordExist(userId, iItem.getEngVal());
                assertTrue(wordExist);
            }
            // add all elements, for EXIST
            checkAllWordsAdded(userId, wordSetItems, wordSet);
            // todo: add check for second set.
            break;
        }
    }

    private void checkAllWordsAdded(UUID userId, ArrayList<WordSetItem> wordSetItems, WordSet wordSet) {
        for (WordSetItem iItem : wordSetItems) {
            final String result = addSingleWord(iItem, wordSet.getId());
            assertEquals("0", result);

            final boolean wordExist = WordDataSource.getInstance().isWordExist(userId, iItem.getEngVal());
            assertTrue(wordExist);
        }
    }

    private ArrayList<WordSetItem> getAndAssertList(int wordSetId) {
        ArrayList<WordSetItem> wordSetItems;
        final Response listResponse = target().path("/word-set/list").request().post(Entity.text("{\"listId\"" + ":" + wordSetId + "}"));
        assertEquals(Response.Status.OK.getStatusCode(), listResponse.getStatus());
        final String trainingWordsStr = listResponse.readEntity(String.class);

        wordSetItems = GSON.fromJson(trainingWordsStr, new TypeToken<ArrayList<WordSetItem>>() {
        }.getType());

        assertEquals(50, wordSetItems.size());
        for (WordSetItem iItem : wordSetItems) {
            assertThat(iItem.getEngVal(), not(isEmptyString()));
            assertThat(iItem.getEngSentence(), not(isEmptyString()));
            assertThat(iItem.getImgUrl(), not(isEmptyString()));
            assertThat(iItem.getRusValues(), hasSize(greaterThan(0)));
        }
        return wordSetItems;
    }

}
