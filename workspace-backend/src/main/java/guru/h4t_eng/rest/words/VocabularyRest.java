package guru.h4t_eng.rest.words;

import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.rest.Main4Rest;
import guru.h4t_eng.service.VocabularyService;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * WordListRest.
 * <p>
 * Created by aalexeenka on 12/18/2015.
 */
@Path("/vocabulary")
public class VocabularyRest extends Main4Rest {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(VocabularyRest.class);

    @Path("/load")
    @POST
    public Response loadWords(
            @Context HttpServletRequest httpRequest
    ) {
        try {
            String json = IOUtils.toString(httpRequest.getInputStream(), "UTF-8");
            JSONObject obj = new JSONObject(json);
            final char letter = obj.getString("letter").charAt(0);

            return VocabularyService.getInstance().loadWordsByLetter(getUserId(httpRequest), letter).response();
        } catch (Throwable th) {
            LOG.error("Can't load words by letter", th);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
