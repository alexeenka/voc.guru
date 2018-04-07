package guru.h4t_eng.word_set;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import guru.h4t_eng.datasource.CassandraDataSource;
import guru.h4t_eng.datasource.WordDataSource;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.model.dictionary.DictionaryWord;
import guru.h4t_eng.model.dictionary.EngDef;
import guru.h4t_eng.rest.words.model.FormData;
import org.slf4j.Logger;

import java.util.*;

import static guru.h4t_eng.util.FormDataUtil.formatWord;

/**
 * Service to work with word-set.
 *
 * Created by aalexeenka on 30.05.2017.
 */
public class WordSetServiceDao {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(WordSetServiceDao.class);

    private static final WordSetServiceDao instance;

    private WordSetServiceDao() {
    }

    static {
        instance = new WordSetServiceDao();
    }

    public static WordSetServiceDao getInstance() {
        return instance;
    }

    private static final CassandraDataSource cds = CassandraDataSource.getInstance();

    private static final WordDataSource wds = WordDataSource.getInstance();

    private static final String INSERT_CQL = "insert into word_set_word" +
            "(" +
            "word_set_id, eng_val," +
            "eng_df, rus_vl, eng_sntnc," +
            "img_url,type, created_time," +
            "updated_time,owner" +
            ") values (" +
            "?,?," +
            "?,?,?," +
            "?,?,?," +
            "?,?"+
            ")";

    private static final String ADD_TO_SET_CQL = "update word_set " +
            "set words = words + ? where part_key = 0 and word_set_id = ?";

    public void saveWord(FormData formData, int wordSetId) {
        saveWord(cds, formData, wordSetId);
    }

    public void saveWord(CassandraDataSource ds, FormData formData, int wordSetId) {
        final DictionaryWord word = formatWord(formData.getWord());
        final EngDef engDef = word.getEngDefs().get(0);

        ArrayList<Object> params = new ArrayList<>();

        params.add(wordSetId);
        params.add(word.getEngVal());

        params.add(new HashSet<>(engDef.getVal()));
        params.add(new HashSet<>(engDef.getRusValues()));
        params.add(new HashSet<>(engDef.getEngSentences()));

        params.add(engDef.getImgUrl());
        params.add(word.getType());
        if (word.getCreatedTime() == null) {
            params.add(new Date());
        } else {
            params.add(word.getCreatedTime());
        }
        params.add(new Date());
        params.add(formData.getUser());

        ds.runQuery(INSERT_CQL, true, params);
        ds.runQuery(ADD_TO_SET_CQL, true, Collections.singleton(word.getEngVal()), wordSetId);
    }

    ArrayList<WordSetItem> wordSet(int listId) {
        final ResultSet rows = cds.runQuery("select eng_val, rus_vl, eng_sntnc, img_url " +
                "from word_set_word where word_set_id = ?", false, listId);

        ArrayList<WordSetItem> result = new ArrayList<>();
        for (Row iRow : rows) {
            WordSetItem word = new WordSetItem();
            result.add(word);

            word.setEngVal(iRow.getString("eng_val"));
            word.setRusValues(new ArrayList<>(iRow.getSet("rus_vl", String.class)));
            word.setEngSentence(new ArrayList<>(iRow.getSet("eng_sntnc", String.class)).get(0));
            word.setImgUrl(iRow.getString("img_url"));

        }

        return result;
    }

    ArrayList<String> add10Words(UUID currentUser, int listId) {
        final ResultSet rows = cds.runQuery("select words from word_set where part_key = 0 and word_set_id = ?", false, listId);
        final Row one = rows.one();
        if (one == null) {
            return null;
        }

        final Set<String> words = one.getSet("words", String.class);

        ArrayList<String> addedWords = new ArrayList<>();
        for (String engVal : words) {
            if (wds.isWordExist(currentUser, engVal)) {
                continue;
            }

            final DictionaryWord dictionaryWord = loadWord(listId, engVal);
            wds.saveWord(dictionaryWord, currentUser, false);

            addedWords.add(engVal);
            if (addedWords.size() >= 10) {
                break;
            }
        }

        return addedWords;
    }

    /**
     * 0 - word exist in user dictionary, 1 - word was added
     */
    int addSingleWord(UUID currentUser, int listId, String engVal) {
        if (wds.isWordExist(currentUser, engVal)) {
            return 0;
        }

        final DictionaryWord dictionaryWord = loadWord(listId, engVal);
        wds.saveWord(dictionaryWord, currentUser, false);

        return 1;
    }


    @SuppressWarnings("Duplicates")
    public DictionaryWord loadWord(final int listId, final String engVal) {
        final ResultSet rows = cds.runQuery("select eng_val, type, eng_df, rus_vl, eng_sntnc, img_url, created_time, updated_time, owner " +
                "from word_set_word where word_set_id = ? and eng_val = ?", false, listId, engVal);

        final Row row = rows.one();
        if (row == null) {
            LOG.error("Can't find wordSet word by listId {} and engVal {}.", listId, engVal);
            return null;
        }

        DictionaryWord word = new DictionaryWord();

        word.setEngVal(row.getString("eng_val"));
        word.setType(row.getInt("type"));

        {
            EngDef engDef = new EngDef();
            word.addEngDef(engDef);

            engDef.setVal(new ArrayList<>(row.getSet("eng_df", String.class)));
            engDef.setRusValues(new ArrayList<>(row.getSet("rus_vl", String.class)));
            engDef.setEngSentences(new ArrayList<>(row.getSet("eng_sntnc", String.class)));
            engDef.setImgUrl(row.getString("img_url"));
        }


        word.setUpdatedTime(row.getTimestamp("updated_time"));
        word.setCreatedTime(row.getTimestamp("created_time"));

        return word;
    }

}
