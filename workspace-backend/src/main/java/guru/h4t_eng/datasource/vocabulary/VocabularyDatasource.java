package guru.h4t_eng.datasource.vocabulary;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import guru.h4t_eng.datasource.CassandraDataSource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * VocabularyDatasource.
 *
 * Created by aalexeenka on 12/16/2015.
 */
public class VocabularyDatasource {

    private static VocabularyDatasource instance = new VocabularyDatasource();

    private static CassandraDataSource cDS = CassandraDataSource.getInstance();

    public static VocabularyDatasource getInstance() {
        return instance;
    }

    private VocabularyDatasource() {
    }

    public void insert(UUID userId, String engVal) {
        char set_prefix = Character.toLowerCase(engVal.charAt(0));
        String set_name = set_prefix + "_set";
        String setParam = set_name + "=" + set_name + " + ?";

        cDS.runQuery("update user_voc set " + setParam + " where user_id = ?", false, Collections.singleton(engVal), userId);
    }

    public void delete(UUID userId, String engVal) {
        char set_prefix = Character.toLowerCase(engVal.charAt(0));
        String set_name = set_prefix + "_set";
        String setParam = set_name + "=" + set_name + " - ?";

        cDS.runQuery("update user_voc set " + setParam + " where user_id = ?", false, Collections.singleton(engVal), userId);
    }

    public Set<String> loadWordsByLetter(UUID userId, char letter) {
        char set_prefix = Character.toLowerCase(letter);
        String set_name = set_prefix + "_set";
        ResultSet rows = cDS.runQuery("select " + set_name + " from  user_voc  where user_id = ?", false, userId);

        if (rows.isExhausted()) {
            return new HashSet<>();
        }

        Row one = rows.one();
        return one.getSet(set_name, String.class);
    }
}
