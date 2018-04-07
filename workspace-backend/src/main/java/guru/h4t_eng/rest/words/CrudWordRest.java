package guru.h4t_eng.rest.words;

import guru.h4t_eng.amazon_s3.ImageStorageAmazonS3;
import guru.h4t_eng.common.LimitedSizeInputStream;
import guru.h4t_eng.config.ApplicationProperties;
import guru.h4t_eng.datasource.WordDataSource;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.model.dictionary.DictionaryWord;
import guru.h4t_eng.model.dictionary.EngDef;
import guru.h4t_eng.rest.Main4Rest;
import guru.h4t_eng.rest.words.model.FormData;
import guru.h4t_eng.service.WordService;
import guru.h4t_eng.util.FormDataUtil;
import guru.h4t_eng.word_set.WordSetRest;
import guru.h4t_eng.word_set.WordSetServiceDao;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * WordRestService.
 *
 * Created by Alexey Alexeenka on 03.08.2015.
 */
@Path("/word")
public class CrudWordRest extends Main4Rest {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(CrudWordRest.class);

    private final WordDataSource wordDataSource = WordDataSource.getInstance();
    private final WordSetServiceDao wordSetDao = WordSetServiceDao.getInstance();


    private WordService wordService = WordService.getInstance();

    private ImageStorageAmazonS3 imageStorageAmazonS3 = ImageStorageAmazonS3.getInstance();

    private ApplicationProperties appProperties = ApplicationProperties.getInstance();


    @Path("/save")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response saveRest(
            @Context HttpServletRequest httpRequest,
            @FormDataParam("jsonStr") String wordDetails,

            @FormDataParam("imgData_0") InputStream img0,
            @FormDataParam("imgData_1") InputStream img1,
            @FormDataParam("imgData_2") InputStream img2,
            @FormDataParam("imgData_3") InputStream img3,
            @FormDataParam("imgData_4") InputStream img4,

            @FormDataParam("imgData_0") FormDataBodyPart img0Details,
            @FormDataParam("imgData_1") FormDataBodyPart img1Details,
            @FormDataParam("imgData_2") FormDataBodyPart img2Details,
            @FormDataParam("imgData_3") FormDataBodyPart img3Details,
            @FormDataParam("imgData_4") FormDataBodyPart img4Details

            ) throws IOException {
        // 1. get word from request
        final FormData formData = FormDataUtil.parseJson(wordDetails);
        formData.setUser(getUserId(httpRequest));

        // 2. save img if have (example: https://docs.oracle.com/javaee/6/tutorial/doc/glraq.html )
        {
            final List<Optional<InputStream>> images = Arrays.asList(
                    Optional.ofNullable(img0),
                    Optional.ofNullable(img1),
                    Optional.ofNullable(img2),
                    Optional.ofNullable(img3),
                    Optional.ofNullable(img4)
            );

            final List<Optional<FormDataBodyPart>> imgDetails = Arrays.asList(
                    Optional.ofNullable(img0Details),
                    Optional.ofNullable(img1Details),
                    Optional.ofNullable(img2Details),
                    Optional.ofNullable(img3Details),
                    Optional.ofNullable(img4Details)
            );

            final DictionaryWord word = formData.getWord();
            final ArrayList<EngDef> engDefs = word.getEngDefs();
            for (int i = 0, n = engDefs.size(); i < n; i++) {
                final EngDef engDef = engDefs.get(i);

                final Optional<InputStream> img = images.get(i);
                final Optional<FormDataBodyPart> imgDetail = imgDetails.get(i);

                if (img.isPresent() && imgDetail.isPresent()) {
                    final String imgUrl = imageStorageAmazonS3.uploadToAmazonS3(
                            new LimitedSizeInputStream(img.get(), appProperties.getMaxUploadFileSizeBytes()),
                            Optional.ofNullable(imgDetail.get().getMediaType()).map(MediaType::toString).orElse(""),
                            getUserId(httpRequest).toString(),
                            word.getEngVal() + "_" + i + "_" + System.currentTimeMillis()
                    );
                    engDef.setImgUrl(imgUrl);
                }
                // TODO: WRITE SPECIAL JOB, CAUSE SAME IMG CAN BE SHARE ACROSS USER
            }
        }

        // 3. save word into cassandra
        wordService.saveWord(formData);

        return Response.ok().build();
    }

    @Path("/delete")
    @POST
    public Response deleteRest(
            @Context HttpServletRequest httpRequest
    ) throws IOException {
        String jsonStr = IOUtils.toString(httpRequest.getInputStream(), "UTF-8");
        JSONObject jsonObj = new JSONObject(jsonStr);
        String engVal = jsonObj.getString("engVal");
        if (StringUtils.isNoneEmpty(engVal)) {
            // todo: WRITE SPECIAL JOB, CAUSE SAME IMG CAN BE SHARE ACROSS USER
            //imageStorageAmazonS3.deleteImg(getUserId(httpRequest), engVal);
            wordDataSource.deleteWord(getUserId(httpRequest), engVal);
        }
        return Response.ok().build();
    }

    @Path("/load-single-word")
    @GET
    public Response loadOneWordRest(@Context HttpServletRequest httpRequest)
    {
        String engVal = httpRequest.getParameter("engVal");
        String wordSet = httpRequest.getParameter("wordSet");
        if (StringUtils.isEmpty(engVal)) {
            throw new RuntimeException("eng_val is empty");
        }

        UUID userId = getUserId(httpRequest);
        DictionaryWord word;

        // load word for word set
        if (StringUtils.isNotEmpty(wordSet) && Integer.valueOf(wordSet) != -1) {
            if (!WordSetRest.canSaveToWordSet(userId)) {
                LOG.error("Hacker detected! user_id {}", userId);
                throw new RuntimeException("Not enough permissions");
            }
            word = wordSetDao.loadWord(Integer.valueOf(wordSet), engVal);
        } else {
            word = wordDataSource.loadSingleWord(userId, engVal);
        }

        final String json = FormDataUtil.toJson(word);
        return Response.ok(json, MediaType.APPLICATION_JSON_TYPE).build();
    }
}
