package guru.h4t_eng.test.util;

import com.datastax.driver.core.ResultSet;
import guru.h4t_eng.datasource.CassandraDataSource;

import java.util.UUID;

import static guru.h4t_eng.datasource.WordDataSource.WORD_TABLE;

/**
 * Help method for test.
 *
 * Created by aalexeenka on 18.01.2017.
 */
public final class WordDataSource4Tst {
    private WordDataSource4Tst() {}

    public static String getFirstUserWord(UUID userUUID) {
        final CassandraDataSource cds = CassandraDataSource.getInstance();
        final ResultSet rows = cds.runQuery("select eng_val from " + WORD_TABLE + " where user_id = ? limit 1", false, userUUID);
        return rows.one().getString("eng_val");
   }
}
