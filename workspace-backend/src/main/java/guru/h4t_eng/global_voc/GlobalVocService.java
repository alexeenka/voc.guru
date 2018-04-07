package guru.h4t_eng.global_voc;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import guru.h4t_eng.datasource.CassandraDataSource;
import guru.h4t_eng.datasource.UserDataSource;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.model.WordType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static guru.h4t_eng.datasource.WordDataSource.WORD_TABLE;
import static guru.h4t_eng.schedule.utils.WorkExecutorUtils.minskDateTime;
import static guru.h4t_eng.util.CommonUtils.joinWithIndex;

/**
 * Service to work with {@link GlobalVoc}
 * <p>
 * Created by aalexeenka on 12.01.2017.
 */
public class GlobalVocService {

    private static final Logger LOG = AppLoggerFactory.getScheduleLog(GlobalVocService.class);

    private static GlobalVocService instance = new GlobalVocService();
    private GlobalVocService() {
    }

    public static GlobalVocService getInstance() {
        return instance;
    }

    private static final UserDataSource userDataSource = UserDataSource.getInstance();

    private static final GlobalVoc globalVoc = GlobalVoc.getInstance();

    public CompletableFuture<Void> initializeAsync() {
        return CompletableFuture.runAsync(this::initialize);
    }

    public void initialize() {
        int records = 0;
        int duplicateWords = 0;

        HashMap<String, LinkedList<GlobalVocWord>> words = new HashMap<>();
        HashMap<UUID, GlobalVocAuthor> authors = new HashMap<>();

        try {
            final Instant startTime = Instant.now();
            LOG.info("GLOBAL-VOC: Start load at server-time:" + startTime + ", minsk-time: " + minskDateTime());
            final CassandraDataSource cds = CassandraDataSource.getInstance();

            Statement stmt = new SimpleStatement("select user_id, eng_val, img_url_0, eng_df_0, rus_vl_0, eng_sntnc_0, type, authentic from " + WORD_TABLE);
            stmt.setFetchSize(1000);
            ResultSet rs = cds.getSession().execute(stmt);

            for (Row row : rs) {
                if (Boolean.FALSE.equals(row.getBool("authentic"))) {
                    continue;
                }

                GlobalVocWord word = new GlobalVocWord();
                word.setEngVal(row.getString("eng_val"));
                word.setImgURL(row.getString("img_url_0"));
                word.setEngDevFav(joinWithIndex(row.getList("eng_df_0", String.class)));
                word.setRusValueFav(StringUtils.join(row.getList("rus_vl_0", String.class), ". "));
                word.setEngSentenceFav(joinWithIndex(row.getList("eng_sntnc_0", String.class)));
                word.setWordType(WordType.evalStringValue(row.getInt("type")));

                UUID authorUUID = row.getUUID("user_id");
                GlobalVocAuthor author = authors.get(authorUUID);
                if (author == null) {
                    final Optional<String[]> optUserDetails = userDataSource.getUserDetails(authorUUID);
                    if (!optUserDetails.isPresent()) {
                        continue;
                    }
                    String[] userDetails = optUserDetails.get();
                    author = new GlobalVocAuthor(authorUUID, userDetails[0], userDetails[1], userDetails[2], userDetails[3]);
                    authors.put(authorUUID, author);
                }

                word.setAuthor(author);
                final String key = word.getEngVal();
                final LinkedList<GlobalVocWord> wordList = words.get(key);
                if (wordList == null) {
                    LinkedList<GlobalVocWord> newList = new LinkedList<>();
                    newList.add(word);
                    words.put(key, newList);
                } else {
                    wordList.add(word);
                    duplicateWords++;
                }
                records++;
            }

            LOG.info("GLOBAL-VOC: Processed records: " + records +
                    ", voc-size: " + words.size() +
                    ", author-size: " + authors.size() +
                    ", duplicate-words: " + duplicateWords
            );
            final Instant finishTime = Instant.now();
            LOG.info("GLOBAL-VOC: Finish work at server-time: " + finishTime + ", minsk-time: " + minskDateTime() + ", duration: " + Duration.between(startTime, finishTime).toMillis());
        } catch (Exception e) {
            LOG.error("GLOBAL-VOC: ERROR: Processed records: " + records, e);
        } finally {
            globalVoc.newValues(words, authors);
        }
    }
}