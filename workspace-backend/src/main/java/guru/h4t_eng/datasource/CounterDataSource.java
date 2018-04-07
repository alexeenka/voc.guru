package guru.h4t_eng.datasource;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import guru.h4t_eng.logs.AppLoggerFactory;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * CounterDataSource.
 *
 * Created by aalexeenka on 1/20/2016.
 */
public class CounterDataSource {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(CounterDataSource.class);

    private static final CounterDataSource instance = new CounterDataSource();

    private CounterDataSource() {
    }

    public static CounterDataSource getInstance() {
        return instance;
    }

    private static CassandraDataSource mds = CassandraDataSource.getInstance();

    public Long countWord(UUID userId) {
        final ResultSet rows = mds.runQuery("select count(*) from " + WordDataSource.WORD_TABLE + " where user_id = ?", false, userId);
        final Row one = rows.one();
        return one.getLong("count");
    }

}
