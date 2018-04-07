package guru.h4t_eng.test.rest.friends;

import guru.h4t_eng.rest.friends.FriendsInfoDto;
import guru.h4t_eng.rest.friends.FriendsRest;
import guru.h4t_eng.rest.json.FastJsonBuilder;
import guru.h4t_eng.test.rest.config.AbstractRest4Tst;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static guru.h4t_eng.test.util.Utils4Tst.GSON;
import static org.junit.Assert.assertEquals;

/**
 * Common for Fb, Vk and [email - in future, now is 23.12.2016] rest methods.
 *
 * Created by aalexeenka on 23.12.2016.
 */
public class FriendsRestTst extends AbstractRest4Tst {

    @Override
    protected Class getRestClass() {
        return FriendsRest.class;
    }

    @Test
    public void testGetFriendsInfo() throws JSONException {
        final Invocation.Builder request = target("/friends").path("/friends-info").request();
        String json = new FastJsonBuilder()
                .appendNumber("year", 2016).next()
                .appendNumber("dayOfYear", 155).next()
                .appendNumber("startDay", 123).next()
                .appendNumber("count", 21)
                .finish();
        final Response response = request.post(Entity.json(json));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        final String result = response.readEntity(String.class);
        System.out.println("json string: " + result);

        FriendsInfoDto[] friends = GSON.fromJson(result, FriendsInfoDto[].class);
        for (int i=0, n=friends.length; i<n; i++) {
            FriendsInfoDto friend = friends[i];

            Assert.assertNotNull(friend);
            Assert.assertNotNull(friend.getFirstName());
            Assert.assertNotNull(friend.getPhotoUrl());
            Assert.assertNotNull(friend.getWordCount());
            Assert.assertNotNull(friend.getTodaySpentTime());
            Assert.assertNotNull(friend.getTrainingCalendar());

            System.out.println("[" + i + "]: " + friend.toString());
        }
    }


    @Test
    public void addFriend() throws JSONException {
        // todo add implementation
    }

}
