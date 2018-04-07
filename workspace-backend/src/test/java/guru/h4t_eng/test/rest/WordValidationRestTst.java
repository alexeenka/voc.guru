package guru.h4t_eng.test.rest;

import com.google.gson.reflect.TypeToken;
import guru.h4t_eng.rest.validation.ValidationRest;
import guru.h4t_eng.test.rest.config.AbstractRest4Tst;
import guru.h4t_eng.util.gson.TrainingWordGsonUtil;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

import static guru.h4t_eng.test.util.Utils4Tst.readResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;

/**
 * WordValidationRestTst.
 *
 * Created by aalexeenka on 29.09.2016.
 */
public class WordValidationRestTst extends AbstractRest4Tst {

    @Override
    protected Class getRestClass() {
        return ValidationRest.class;
    }

    @Test
    public void testCase_invalid_sentences() {
        final ArrayList<String> result = validateWord("/word_validation_rest/invalid_sentences.json");
        assertThat(result, hasItem("Beeeek the chocolate bar into pieces so that everyone can have some"));
    }
    @Test
    public void testCase_valid_sentences() {
        final ArrayList<String> result = validateWord("/word_validation_rest/valid_sentences.json");
        assertEquals(0, result.size());
    }

    private ArrayList<String> validateWord(String resource) {
        final Invocation.Builder validationRequest = target("/validate").path("word").request();
        final Response response = validationRequest.post(Entity.text(readResource(resource)));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        final String responseJson = response.readEntity(String.class);

        return TrainingWordGsonUtil.GSON.fromJson(responseJson, new TypeToken<ArrayList<String>>() {}.getType());
    }


}
