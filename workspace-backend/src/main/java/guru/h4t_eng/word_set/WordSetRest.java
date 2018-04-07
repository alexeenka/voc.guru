package guru.h4t_eng.word_set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import guru.h4t_eng.amazon_s3.ImageStorageAmazonS3;
import guru.h4t_eng.common.LimitedSizeInputStream;
import guru.h4t_eng.config.ApplicationProperties;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.model.dictionary.DictionaryWord;
import guru.h4t_eng.model.dictionary.EngDef;
import guru.h4t_eng.rest.Main4Rest;
import guru.h4t_eng.rest.json.FastJsonParser;
import guru.h4t_eng.rest.words.CrudWordRest;
import guru.h4t_eng.rest.words.model.FormData;
import guru.h4t_eng.util.FormDataUtil;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * Rest to work with word rest.
 *
 * Created by aalexeenka on 30.05.2017.
 */
@Path("/word-set")
public class WordSetRest extends Main4Rest {

    public static final Gson GSON = new GsonBuilder().create();

    private static final Logger LOG = AppLoggerFactory.getWordSetLog(CrudWordRest.class);

    public static final String ADMIN_ALEXEENKA = "220761a0-6b46-11e5-ba60-a1efb4cdc0bf";

    private final ImageStorageAmazonS3 imageStorageAmazonS3 = ImageStorageAmazonS3.getInstance();

    private final ApplicationProperties appProperties = ApplicationProperties.getInstance();

    private final WordSetServiceDao wordSetDao = WordSetServiceDao.getInstance();

    @Path("/allow")
    @POST
    public Response can(@Context HttpServletRequest request) {
        UUID userId = getUserId(request);

        boolean result = canSaveToWordSet(userId);

        return Response.ok(GSON.toJson(result), MediaType.APPLICATION_JSON_TYPE).build();
    }

    public static boolean canSaveToWordSet(UUID userId) {
        boolean result = false;
        if (ADMIN_ALEXEENKA.equals(userId.toString())) {
            result = true;
        }
        return result;
    }

    @Path("/add-single-word")
    @POST
    public Response addSingleWord(
            @Context HttpServletRequest request
    ) throws IOException {
        String json = IOUtils.toString(request.getInputStream(), "UTF-8");
        final int listId = FastJsonParser.getInt(json, "listId");
        final String word = FastJsonParser.getString(json, "word");
        UUID currentUser = getUserId(request);

        final int result = wordSetDao.addSingleWord(currentUser, listId, word);
        LOG.info("addSingleWord: User [{}] add word [{}] from list [{}]. Word is new [{}].", currentUser, word, listId, result == 1 ? Boolean.TRUE : Boolean.FALSE);

        return Response.ok(GSON.toJson(result), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Path("/add-10-words")
    @POST
    public Response add10Words(
            @Context HttpServletRequest request
    ) throws IOException {
        String json = IOUtils.toString(request.getInputStream(), "UTF-8");
        final int listId = FastJsonParser.getInt(json, "listId");
        UUID currentUser = getUserId(request);

        final ArrayList<String> addedWords = wordSetDao.add10Words(currentUser, listId);

        LOG.info("add10Words: User [{}] add word [{}] from list [{}].", currentUser, Arrays.toString(addedWords.toArray(new String[addedWords.size()])), listId);

        return Response.ok(GSON.toJson(addedWords), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Path("/list")
    @POST
    public Response list(
            @Context HttpServletRequest httpRequest
    ) {
        try {
            String json = IOUtils.toString(httpRequest.getInputStream(), "UTF-8");
            final int listId = FastJsonParser.getInt(json, "listId");
            final ArrayList<WordSetItem> result = wordSetDao.wordSet(listId);
            return Response.ok(GSON.toJson(result), MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Throwable th) {
            LOG.error("Can't load words by letter", th);
            throw new RuntimeException(th);
        }
    }

    @Path("/save")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response saveWordToSet(
            @Context HttpServletRequest httpRequest,

            @FormDataParam("jsonStr") String wordDetails,
            @FormDataParam("imgData_0") InputStream img,
            @FormDataParam("imgData_0") FormDataBodyPart imgDetails,
            @FormDataParam("wordSetId") int wordSetId

    ) throws IOException {
        final UUID userId = getUserId(httpRequest);

        final String amazonS3Prefix = "word-set-" + wordSetId;

        // 1. check privilege
        {
            boolean enoughPrivilege = canSaveToWordSet(userId);

            if (!enoughPrivilege) {
                LOG.error("Hacker detected! user_id {}", userId);
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        }
        // 2. save data
        // 1. get word from request
        final FormData formData = FormDataUtil.parseJson(wordDetails);
        formData.setUser(userId);

        // 2. save img if have (example: https://docs.oracle.com/javaee/6/tutorial/doc/glraq.html )
        {
            final Optional<InputStream> optImage = Optional.ofNullable(img);
            final Optional<FormDataBodyPart> optImgDetails = Optional.ofNullable(imgDetails);

            final DictionaryWord word = formData.getWord();
            final EngDef engDef = word.getEngDefs().get(0);

            if (optImage.isPresent() && optImgDetails.isPresent()) {
                final String imgUrl = imageStorageAmazonS3.uploadToAmazonS3(
                        new LimitedSizeInputStream(optImage.get(), appProperties.getMaxUploadFileSizeBytes()),
                        Optional.ofNullable(optImgDetails.get().getMediaType()).map(MediaType::toString).orElse(""),
                        amazonS3Prefix,
                        word.getEngVal() + "_" + System.currentTimeMillis()
                );
                engDef.setImgUrl(imgUrl);
            }
            // TODO: NEED TO THINK ABOUT HOW TO DELETE IMAGE FOR EDIT CASE!!!
        }

        // 3. save word into cassandra
        wordSetDao.saveWord(formData, wordSetId);

        return Response.ok().build();
    }
}
