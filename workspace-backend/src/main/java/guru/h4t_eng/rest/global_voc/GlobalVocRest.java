package guru.h4t_eng.rest.global_voc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import guru.h4t_eng.datasource.WordDataSource;
import guru.h4t_eng.global_voc.GlobalVoc;
import guru.h4t_eng.global_voc.GlobalVocWord;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.model.dictionary.DictionaryWord;
import guru.h4t_eng.rest.Main4Rest;
import guru.h4t_eng.rest.json.FastJsonBuilder;
import guru.h4t_eng.rest.json.FastJsonParser;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static guru.h4t_eng.global_voc.GlobalVoc.MAX_FIND_WORD;
import static guru.h4t_eng.rest.json.FastJsonBuilder.oneJsonValue;

/**
 * Rest to work with GlobalVoc.
 *
 * Created by aalexeenka on 16.01.2017.
 */
@Path("/global-voc")
public class GlobalVocRest extends Main4Rest {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(GlobalVocRest.class);

    private static final GlobalVoc globalVoc = GlobalVoc.getInstance();
    public static final WordDataSource wds = WordDataSource.getInstance();
    private static final int maxFindTimeMs = 200;

    public static final Gson GSON = new GsonBuilder().create();

    public static final String COPY_WORD_CURRENT_USER_AUTHOR = "Вы автор текущего слова :)";
    public static final String COPY_WORD_ALREADY_EXIST = "У вас уже было данное слово в словаре";
    public static final String COPY_WORD_SUCCESS = "Слово успешно добавлено!";

    @Path("/size")
    @GET
    public Response globalVocSize() {
        final String json = new FastJsonBuilder().appendNumber("size", globalVoc.getSize()).finish();
        return Response.ok(json, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Path("/autocomplete")
    @POST
    public Response autocomplete(@Context HttpServletRequest request) throws IOException {
        String jsonStr = IOUtils.toString(request.getInputStream(), "UTF-8");
        final String w = FastJsonParser.getString(jsonStr,"w");
        final List<String> words = globalVoc.findWords(w);
        return Response.ok(GSON.toJson(words), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Path("/load-word")
    @POST
    public Response loadWord(@Context HttpServletRequest request) throws IOException {
        String jsonStr = IOUtils.toString(request.getInputStream(), "UTF-8");
        final String w = FastJsonParser.getString(jsonStr,"w");

        final LinkedList<GlobalVocWord> words = globalVoc.getWordValues(w);
        return Response.ok(GSON.toJson(words), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Path("/copy-word")
    @POST
    public Response addNewWord(@Context HttpServletRequest request) throws IOException {
        UUID userId = getUserId(request);

        String jsonStr = IOUtils.toString(request.getInputStream(), "UTF-8");

        UUID authorUID = UUID.fromString(FastJsonParser.getString(jsonStr,"author"));
        String word = FastJsonParser.getString(jsonStr,"word");

        if (userId.equals(authorUID)) {
            return Response.ok(oneJsonValue("msg", COPY_WORD_CURRENT_USER_AUTHOR), MediaType.APPLICATION_JSON_TYPE).build();
        }

        if (wds.isWordExist(userId, word)) {
            return Response.ok(oneJsonValue("msg", COPY_WORD_ALREADY_EXIST), MediaType.APPLICATION_JSON_TYPE).build();
        }

        final DictionaryWord dictionaryWord = wds.loadSingleWord(authorUID, word);
        wds.saveWord(dictionaryWord, userId, false);

        final String result = new FastJsonBuilder().append("msg", COPY_WORD_SUCCESS).next().appendBoolean("added", true).finish();
        return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Path("/get-random-words")
    @GET
    public Response getRandomWords(@Context HttpServletRequest request) {
        UUID userId = getUserId(request);

        ArrayList<GlobalVocWord> result = new ArrayList<>();

        // try to find unknown result for 200ms
        Instant startDate = Instant.now();
        Random r = ThreadLocalRandom.current();
        int debugIteration = 0;

        while (result.size() < MAX_FIND_WORD && Duration.between(startDate, Instant.now()).toMillis() < maxFindTimeMs) {
            final int index = r.nextInt(globalVoc.getSize());
            final String possibleNewWord = globalVoc.getWords().get(index);
            final LinkedList<GlobalVocWord> globalVocWords = globalVoc.getWordValues(possibleNewWord);
            if (isUserNotAuthor(userId, globalVocWords) && wds.isWordNotExist(userId, possibleNewWord) && !result.contains(globalVocWords.getFirst())) {
                result.add(globalVocWords.getFirst());
            }
            debugIteration++;
        }

        LOG.info("1 - Search time: " + Duration.between(startDate, Instant.now()).toMillis() + ", word size: " + result.size() + ", iteration: " + debugIteration);

        while (result.size() < MAX_FIND_WORD && Duration.between(startDate, Instant.now()).toMillis() < maxFindTimeMs * 1.2) {
            final int index = r.nextInt(globalVoc.getSize());
            final String possibleNewWord = globalVoc.getWords().get(index);
            final LinkedList<GlobalVocWord> globalVocWords = globalVoc.getWordValues(possibleNewWord);
            if (!result.contains(globalVocWords.getFirst())) {
                result.add(globalVocWords.getFirst());
            }
        }

        LOG.info("2 - Search time: " + Duration.between(startDate, Instant.now()).toMillis() + ", word size: " + result.size());

        return Response.ok(GSON.toJson(result), MediaType.APPLICATION_JSON_TYPE).build();
    }

    private boolean isUserNotAuthor(UUID userId, LinkedList<GlobalVocWord> globalVocWords) {
        return globalVocWords.stream().noneMatch(word -> word.getAuthor().getUid().equals(userId));
    }
}
