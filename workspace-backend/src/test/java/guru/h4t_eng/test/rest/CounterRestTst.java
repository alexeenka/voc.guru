package guru.h4t_eng.test.rest;

import guru.h4t_eng.rest.words.CounterRest;
import guru.h4t_eng.test.rest.config.AbstractRest4Tst;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * CounterRestTst.
 *
 * Created by aalexeenka on 1/20/2016.
 */
public class CounterRestTst extends AbstractRest4Tst {

    @Override
    protected Class getRestClass() {
        return CounterRest.class;
    }

    @Test
    public void testCounter() throws JSONException {
        // 1. Call for Rest and Check
        final Invocation.Builder request = target("/counter").path("/count-words-and-indicators").request();

        final Response response = request.get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        final String result = response.readEntity(String.class);

        System.out.println(result);

        JSONObject jsonObject = new JSONObject(result);
        checkLongValue(jsonObject, "all");
        checkLongValue(jsonObject, "def_eng");
        checkLongValue(jsonObject, "eng_rus");
        checkLongValue(jsonObject, "rus_eng");
        checkLongValue(jsonObject, "img_eng");
        checkLongValue(jsonObject, "sen_eng");

        // Call for each counter
        checkCounter("/count-user-words");
        // Check count for DEF-ENG
        checkCounter("/count-remained-def-eng-words");
        // Check count for ENG-RUS
        checkCounter("/count-remained-eng-rus-words");
        // Check count for RUS-ENG
        checkCounter("/count-remained-rus-eng-words");
        // Check count for IMG-ENG
        checkCounter("/count-remained-img-eng-words");
        // Check count for SEN-ENG
        checkCounter("/count-remained-sen-eng-words");

    }

    private void checkLongValue(JSONObject result, String jsonName) throws JSONException {
        final long value = result.getLong(jsonName);
        assertCounter(value);
    }

    private void assertCounter(long value) {
        assertNotNull("Count must be not NULL value", value);
        assertThat(value, greaterThanOrEqualTo(0L));
    }

    private void checkCounter(String path) {
        final Invocation.Builder request = target("/counter").path(path).request();
        final Response response = request.get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        final String countStr = response.readEntity(String.class);
        final Long countLong = Long.valueOf(countStr);
        assertCounter(countLong);
    }

}