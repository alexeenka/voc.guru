package guru.h4t_eng.datasource;

import com.datastax.driver.core.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import guru.h4t_eng.datasource.vocabulary.VocabularyDatasource;
import guru.h4t_eng.exception.CassandraException;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.model.WordType;
import guru.h4t_eng.model.dictionary.DictionaryWord;
import guru.h4t_eng.model.dictionary.EngDef;
import guru.h4t_eng.rest.WordListDto;
import guru.h4t_eng.rest.words.model.FormData;
import guru.h4t_eng.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static guru.h4t_eng.util.CommonUtils.joinWithIndex;
import static guru.h4t_eng.util.FormDataUtil.formatText;
import static guru.h4t_eng.util.FormDataUtil.formatWord;

/**
 * WordDataSource.
 *
 * Created by Alexey Alexeenka on 07.09.2015.
 */
public class WordDataSource {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(WordDataSource.class);

    public static final String WORD_TABLE = "WORD3";

    public static final String CQL_SELECT_WORD_LIST_DTO = "SELECT " + " eng_val, img_url_0, eng_df_0, rus_vl_0, eng_sntnc_0, type, updated_time " + " FROM " + WordDataSource.WORD_TABLE;;
    public static final String CQL_SELECT_WORD_LIST_DTO_WHERE_USER = CQL_SELECT_WORD_LIST_DTO + " where user_id = ?";
    public static final String CQL_SELECT_WORD_LIST_DTO_WHERE_USER_AND_ENGVAL = CQL_SELECT_WORD_LIST_DTO_WHERE_USER + " and eng_val = ?";


    private static final WordDataSource instance = new WordDataSource();

    public static final int MAX_DEF_COUNT = 5;

    private WordDataSource() {
    }

    public static WordDataSource getInstance() {
        return instance;
    }

    public static final CassandraDataSource cds = CassandraDataSource.getInstance();

    public static boolean isNotNewWord(String prevEngVal) {
        return StringUtils.isNotBlank(prevEngVal);
    }

    public static boolean isNewWord(String prevEngVal) {
        return !isNotNewWord(prevEngVal);
    }

    public static boolean isChangeEngValue(String currentEngVal, String prevEngVal) {
        return !prevEngVal.equals(currentEngVal);
    }

    public static boolean needToDeleteWord(String currentEngVal, String prevEngVal) {
        return isNotNewWord(prevEngVal) && isChangeEngValue(currentEngVal, prevEngVal);
    }

    public void saveWord(DictionaryWord word, UUID userId, boolean authentic) {
        FormData data = new FormData();
        data.setWord(word);
        data.setUser(userId);
        data.setPrevEngVal(StringUtils.EMPTY);

        saveWord(data, authentic);
    }

    public void saveWord(FormData formData, boolean authentic) {
        String prevEngVal = formData.getPrevEngVal();
        DictionaryWord word = formatWord(formData.getWord());

        final boolean isNotNewWord = isNotNewWord(prevEngVal);
        boolean isNewWord = !isNotNewWord;
        final boolean isChangeEngValue = isChangeEngValue(word.getEngVal(), prevEngVal);

        if (needToDeleteWord(word.getEngVal(), prevEngVal)) {
            deleteWord(formData.getUser(), prevEngVal);
        }

        // PREPARATION
        final StringBuilder insertParams = new StringBuilder("user_id " +
                ",eng_val " +
                ",type " +
                ",authentic "
        );
        final StringBuilder bindParams = new StringBuilder("?,?,?,?");
        final ArrayList<Object> parameters = new ArrayList<>(Arrays.asList(
                formData.getUser(),
                word.getEngVal(),
                word.getType(),
                authentic
        ));

        {
            final ArrayList<EngDef> engDefs = word.getEngDefs();

            for (int i = 0; i< MAX_DEF_COUNT; i++) {
                final EngDef engDef = i < engDefs.size() ? engDefs.get(i) : null;

                // engDef value
                insertParams.append(",eng_df_").append(i).append(" ");
                bindParams.append(",?");
                parameters.add(engDef != null ? engDef.getVal() : null);


                // engDef.rusValues values
                insertParams.append(",rus_vl_").append(i).append(" ");
                bindParams.append(",?");
                parameters.add(engDef != null ? engDef.getRusValues() : null);

                // engDef.getEngSentences values
                insertParams.append(",eng_sntnc_").append(i).append(" ");
                bindParams.append(",?");
                parameters.add(engDef != null ? engDef.getEngSentences() : null);

                // engDef.getEngSentences values
                insertParams.append(",img_url_").append(i).append(" ");
                bindParams.append(",?");
                parameters.add(engDef != null ? engDef.getImgUrl() : null);
            }
        }

        // EXTRA CONDITION
        if (isNewWord || isChangeEngValue) {
            insertParams.append(",created_time").append(",updated_time ");
            bindParams.append(",?,?");
            parameters.add(new Date());
            parameters.add(new Date());
        } else {
            insertParams.append(",updated_time  ");
            bindParams.append(",?");
            parameters.add(new Date());
        }


        // BUILD AND RUN
        cds.runQuery("insert into " +  WordDataSource.WORD_TABLE + " (" + insertParams + ") values (" + bindParams + ")", false, parameters.toArray(new Object[parameters.size()]));

        // insert to training tables
        if ((isNewWord || isChangeEngValue)) {
            TrainingDataSource.getInstance().newWord(formData.getUser(), word.getEngVal());
            VocabularyDatasource.getInstance().insert(formData.getUser(), word.getEngVal());
        }
    }

    public static WordListDto getWordListDTO(Row row) {
        WordListDto wordListDto = new WordListDto();
        wordListDto.setEngVal(row.getString("eng_val"));
        wordListDto.setImgURL(row.getString("img_url_0"));
        wordListDto.setEngDevFav(joinWithIndex(row.getList("eng_df_0", String.class)));
        wordListDto.setRusValueFav(StringUtils.join(row.getList("rus_vl_0", String.class), ". "));
        wordListDto.setEngSentenceFav(joinWithIndex(row.getList("eng_sntnc_0", String.class)));
        wordListDto.setWordType(WordType.evalStringValue(row.getInt("type")));
        wordListDto.setUpdatedTime(row.getTimestamp("updated_time"));

        return wordListDto;
    }

    public void deleteWord(UUID userId, String engVal) {
        cds.runQuery("delete from " + WordDataSource.WORD_TABLE + " where user_id = ? and eng_val = ?", false, userId, engVal);

        // delete from training tables
        TrainingDataSource.getInstance().deleteWord(userId, engVal);
        VocabularyDatasource.getInstance().delete(userId, engVal);
    }

    public DictionaryWord loadSingleWord(UUID userId, String engVal) {
        engVal = formatText(engVal);

        final StringBuilder selectParams = new StringBuilder(
                "type, created_time, updated_time, " +
                        "eng_df_0, eng_sntnc_0, rus_vl_0, img_url_0, " +
                        "eng_df_1, eng_sntnc_1, rus_vl_1, img_url_1, " +
                        "eng_df_2, eng_sntnc_2, rus_vl_2, img_url_2, " +
                        "eng_df_3, eng_sntnc_3, rus_vl_3, img_url_3, " +
                        "eng_df_4, eng_sntnc_4, rus_vl_4, img_url_4"
        );

        //noinspection StringBufferReplaceableByString
        final StringBuilder query = new StringBuilder("SELECT ").append(selectParams).append(" FROM ").append(WordDataSource.WORD_TABLE).append(" WHERE user_id = ? AND eng_val = ? ");
        final ResultSet rows = cds.runQuery(query.toString(), false, userId, engVal);

        if (rows.isExhausted()) {
            throw new CassandraException("Can't find word");
        }
        final Row row = rows.one();

        DictionaryWord word = new DictionaryWord();

        word.setEngVal(engVal);
        word.setType(row.getInt("type"));
        word.setUpdatedTime(row.getTimestamp("updated_time"));

        for (int i=0; i<5; i++) {
            final List<String> engDefValue = row.getList("eng_df_" + i, String.class);
            if (isEmptyListVal(engDefValue)) continue;

            EngDef def = new EngDef();
            def.setVal(engDefValue);

            def.setImgUrl(row.getString("img_url_" + i));
            def.setRusValues(row.getList("rus_vl_" + i, String.class));
            def.setEngSentences(row.getList("eng_sntnc_" + i, String.class));

            word.addEngDef(def);
        }

        return word;
    }


    public HashMap<String, DictionaryWord> loadTrainingWordAsync(
            UUID userId,
            Set<String> engValues
    ) {
        final StringBuilder selectParams = new StringBuilder(
                "eng_val, type, created_time, updated_time, " +
                        "eng_df_0, eng_sntnc_0, rus_vl_0, img_url_0, " +
                        "eng_df_1, eng_sntnc_1, rus_vl_1, img_url_1, " +
                        "eng_df_2, eng_sntnc_2, rus_vl_2, img_url_2, " +
                        "eng_df_3, eng_sntnc_3, rus_vl_3, img_url_3, " +
                        "eng_df_4, eng_sntnc_4, rus_vl_4, img_url_4"
        );

        //noinspection StringBufferReplaceableByString
        final StringBuilder query = new StringBuilder("SELECT ").append(selectParams).append(" FROM ").append(WordDataSource.WORD_TABLE).append(" WHERE user_id = ? AND eng_val = ? ");

        List<ResultSetFuture> futures = Lists.newArrayListWithExpectedSize(engValues.size());
        final PreparedStatement prepared = cds.prepare(query.toString());
        for (Object engVal  : engValues) {
            BoundStatement statement = prepared.bind(userId, engVal);
            final ResultSetFuture resultSetFuture = cds.executeAsync(statement);
            futures.add(resultSetFuture);
        }

        final ImmutableList<ListenableFuture<ResultSet>> futureList = Futures.inCompletionOrder(futures);
        final HashMap<String, DictionaryWord> result = new HashMap<>();
        for (ListenableFuture<ResultSet> future : futureList) {

            final ResultSet rows;
            try {
                rows = future.get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("loadTrainingWordAsync", e);
                throw new RuntimeException("Error in loadTrainingWordAsync");
            }

            if (rows.isExhausted()) {
                LOG.error("Can't find word!");
                throw new RuntimeException("Can't find word!");
            }

            final Row row = rows.one();

            DictionaryWord word = new DictionaryWord();

            word.setEngVal(row.getString("eng_val"));
            word.setType(row.getInt("type"));
            word.setUpdatedTime(row.getTimestamp("updated_time"));

            for (int i = 0; i < 5; i++) {
                final List<String> engDefValue = row.getList("eng_df_" + i, String.class);
                if (isEmptyListVal(engDefValue)) continue;

                EngDef def = new EngDef();
                def.setVal(engDefValue);

                def.setImgUrl(row.getString("img_url_" + i));
                def.setRusValues(row.getList("rus_vl_" + i, String.class));
                def.setEngSentences(row.getList("eng_sntnc_" + i, String.class));

                word.addEngDef(def);
            }

            result.put(word.getEngVal(), word);
        }

        return result;
    }

    private boolean isEmptyListVal(List<String> values) {
        if (values == null || values.size() == 0) return true;

        for (String iVal : values) {
            if (StringUtils.isNotBlank(iVal)) {
                return false;
            }
        }
        return true;
    }

    public LoadWordsMethodReturn loadWordsByEngVals(UUID userId, Set<String> words) {
        try {
            Object[][] partitionKeysArray = new Object[words.size()][2];
            {
                int i = 0;
                for (String word : words) {
                    partitionKeysArray[i][0] = userId;
                    partitionKeysArray[i][1] = word;
                    i++;
                }
            }

            List<ListenableFuture<ResultSet>> futures = cds.queryAll(CQL_SELECT_WORD_LIST_DTO_WHERE_USER_AND_ENGVAL, partitionKeysArray);

            final ArrayList<WordListDto> result = Lists.newArrayListWithExpectedSize(partitionKeysArray.length);
            for (ListenableFuture<ResultSet> future : futures) {
                ResultSet rs = future.get();
                if (rs.isExhausted()) continue;
                final WordListDto wordListDto = getWordListDTO(rs.one());
                result.add(wordListDto);
            }

            return new LoadWordsMethodReturn(result, null);
        } catch (Throwable th) {
            LOG.error("Can't load words by words list userId: {}, words: [{}]", userId, StringUtils.join(words, ","), th);
            throw new RuntimeException(th);
        }
    }

    public List<String> loadImgURL(UUID userId, String engVal) {
        final ResultSet rows = cds.runQuery("select img_url_0, img_url_1, img_url_2, img_url_3, img_url_4 from " + WordDataSource.WORD_TABLE + " where user_id = ? and eng_val = ?", false, userId, engVal);
        if (rows.isExhausted()) return null;
        final Row one = rows.one();
        return CommonUtils.makeList(
                one.getString("img_url_0"),
                one.getString("img_url_1"),
                one.getString("img_url_2"),
                one.getString("img_url_3"),
                one.getString("img_url_4")
        );
    }

    public boolean isWordNotExist(UUID userId, String engVal) {
        final ResultSet rows = cds.runQuery("select eng_val from " + WORD_TABLE + " where user_id = ? and eng_val = ?", false, userId, engVal);
        return rows.isExhausted();
    }

    public boolean isWordExist(UUID userId, String engVal) {
        return !isWordNotExist(userId, engVal);
    }
}