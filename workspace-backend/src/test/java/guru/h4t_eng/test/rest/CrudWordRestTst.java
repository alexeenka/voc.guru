package guru.h4t_eng.test.rest;

import guru.h4t_eng.rest.words.CrudWordRest;
import guru.h4t_eng.test.rest.config.AbstractRest4Tst;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static guru.h4t_eng.test.util.Utils4Tst.getFileResource;
import static guru.h4t_eng.test.util.Utils4Tst.readResource;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.junit.Assert.assertEquals;

/**
 * Test CRUD for Words!
 *
 * Created by aalexeenka on 21.12.2016.
 */
public class CrudWordRestTst extends AbstractRest4Tst {

    @Override
    protected Class getRestClass() {
        return CrudWordRest.class;
    }

    @Test
    public void createUsualWord() {
        // todo extend test, instead of datasource test
        // todo: add test for img exceed max size
        // todo: add selenium test
        // todo: add test for all 5 imgs
        final FormDataBodyPart wordData = new FormDataBodyPart(
                "jsonStr", readResource("/rest_resource/crud-word-rest-tst/form-data.json")
        );
        final FileDataBodyPart wordImg1 = new FileDataBodyPart("imgData_0", getFileResource("/rest_resource/crud-word-rest-tst/img-01.jpg"));
        final FileDataBodyPart wordImg2 = new FileDataBodyPart("imgData_1", getFileResource("/rest_resource/crud-word-rest-tst/img-01.jpg"));

        final MultiPart multiPartEntity = new MultiPart()
                .bodyPart(wordData)
                .bodyPart(wordImg1)
                .bodyPart(wordImg2);

        final Response saveResponse = target("/word").path("/save").request().post(
                Entity.entity(multiPartEntity, MULTIPART_FORM_DATA)
        );

        assertEquals(Response.Status.OK.getStatusCode(), saveResponse.getStatus());
    }
}
