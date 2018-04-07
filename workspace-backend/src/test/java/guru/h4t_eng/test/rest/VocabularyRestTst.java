package guru.h4t_eng.test.rest;

import guru.h4t_eng.datasource.WordDataSource;
import guru.h4t_eng.rest.words.VocabularyRest;
import guru.h4t_eng.rest.words.model.FormData;
import guru.h4t_eng.service.WordService;
import guru.h4t_eng.test.rest.config.AbstractRest4Tst;
import guru.h4t_eng.test.util.Utils4Tst;
import guru.h4t_eng.util.FormDataUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static guru.h4t_eng.UsersDatabaseData4Tst.SAMANTA_UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * VocabularyRestTst.
 *
 * Created by aalexeenka on 12/18/2015.
 */
public class VocabularyRestTst extends AbstractRest4Tst {

    protected WordService wordService = WordService.getInstance();
    protected WordDataSource wordDataSource = WordDataSource.getInstance();

    @Override
    protected Class getRestClass() {
        return VocabularyRest.class;
    }

    @Test
    public void test() {
        makeVocabulary();

        final Invocation.Builder request = target("/vocabulary").path("/load").request();

        for (char i = 'A'; i<'Z'; i++) {
            checkLetter(request, Character.toString(i));
        }

        clearVocabulary();
    }

    private void clearVocabulary() {
        final FormData formData = Utils4Tst.newFormDataFromJson__1();
        formData.setUser(SAMANTA_UUID);
        for (char i = 'A'; i<'Z'; i++) {
            for (int j=0; j<5; j++){
                formData.getWord().setEngVal(Character.toString(i) + "_VocabularyRestTest_ test" + j);
                wordDataSource.deleteWord(formData.getUser(), formData.getWord().getEngVal());
            }
        }
    }

    public void makeVocabulary() {
        final FormData formData = Utils4Tst.newFormDataFromJson__1();
        formData.setUser(SAMANTA_UUID);
        for (char i = 'A'; i<'Z'; i++) {
            for (int j=0; j<5; j++){
                formData.getWord().setEngVal(Character.toString(i) + "_VocabularyRestTest_ test" + j);
                wordService.saveWord(formData);
            }
        }
    }

    private void checkLetter(Invocation.Builder request, String pagingLetter)
    {
        Response response = request.post(Entity.entity("{\"letter\" : \"" + pagingLetter + "\"}", "application/json"));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String jsonStr = response.readEntity(String.class);
        JSONObject obj = new JSONObject(jsonStr);
        assertTrue(obj.isNull("pagingState"));
        final JSONArray wordList = obj.getJSONArray("wordList");
        assertTrue(wordList.length() > 0);
        for (int i=0, N = wordList.length(); i<N; i++) {
            JSONObject iWord = wordList.getJSONObject(i);
            assertEquals(pagingLetter, FormDataUtil.extractFirstLetter(iWord.getString("engVal")));
        }
    }


}
