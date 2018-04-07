package guru.h4t_eng.rest.validation;

import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.model.dictionary.DictionaryWord;
import guru.h4t_eng.model.dictionary.EngDef;
import guru.h4t_eng.rest.Main4Rest;
import guru.h4t_eng.rest.training.SentenceTrainingUtils;
import guru.h4t_eng.util.gson.TrainingWordGsonUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static guru.h4t_eng.util.FormDataUtil.parseJson;

/**
 * Main4Rest.
 *
 * Created by aalexeenka on 29.09.2016.
 */
@Path("/validate")
public class ValidationRest extends Main4Rest {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(ValidationRest.class);


    /**
     * It's in separate rest URL, because when we save word, we upload also image, it's expensive operation for just simple validation.
     */
    @Path("/word")
    @POST
    public Response validateSavingWord(@Context HttpServletRequest httpRequest) {
        try {
            String jsonStr = IOUtils.toString(httpRequest.getInputStream(), "UTF-8");
            ArrayList<String> invalidSentences = getInvalidSentences(jsonStr);
            return Response.ok(TrainingWordGsonUtil.GSON.toJson(invalidSentences), MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            LOG.error("Can't validateSavingWord", e);
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    private ArrayList<String> getInvalidSentences(String jsonStr) {
        ArrayList<String> invalidSentences = new ArrayList<>();

        final DictionaryWord dictionaryWord = parseJson(jsonStr, DictionaryWord.class);
        final ArrayList<EngDef> engDefs = dictionaryWord.getEngDefs();
        for (EngDef engDef : engDefs) {
            final List<String> engSentences = engDef.getEngSentences();

            for (String engSen : engSentences) {
                final String[] trainingPair = SentenceTrainingUtils.makeTrainingPair(dictionaryWord.getEngVal(), engSen);
                if (trainingPair == null) {
                    invalidSentences.add(engSen);
                }
            }
        }

        return invalidSentences;
    }

}
