package guru.h4t_eng.test.rest.friends;

import guru.h4t_eng.UsersDatabaseData4Tst;
import guru.h4t_eng.datasource.UserDataSource;
import guru.h4t_eng.rest.friends.FriendsRest;
import guru.h4t_eng.service.friends.FriendsData;
import guru.h4t_eng.service.friends.FriendsUsingApp;
import guru.h4t_eng.service.friends.SocialNetwork;
import guru.h4t_eng.test.rest.config.DaveFbSecurity4TstFilter;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static guru.h4t_eng.service.FriendService.getPageCount;
import static guru.h4t_eng.test.util.Utils4Tst.GSON;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;

/**
 * FbFriendsRestTst.
 * <p>
 * Created by aalexeenka on 15.11.2016.
 */
public class FbFriendsRestTst extends AbstractSocialNetworkTst {

    private static final int FACEBOOK_FRIENDS_TOTAL = 11;

    private static final int PAGE_COUNT = getPageCount(FACEBOOK_FRIENDS_TOTAL, appProperties.getFriendPagingSize());

    @Override
    protected Class getRestClass() {
        return FriendsRest.class;
    }

    @Override
    protected Class getUserSecurityFilter() {
        return DaveFbSecurity4TstFilter.class;
    }


    @Test
    public void facebookFriendsPaging() {
        // initial request
        FriendsUsingApp iFriendsUsingApp = getFbFriendsList();

        // go to last record using next link
        // after go to first record using prev link
        int page = 0;
        boolean directionForward = true;
        while (true) {
            System.out.println("[FB] Direction: " + (directionForward ? "forward" : "backward") + ", Page: " + page);

            assertThat(iFriendsUsingApp.getFriendsData().size(), lessThanOrEqualTo(appProperties.getFriendPagingSize()));
            assertEquals(FACEBOOK_FRIENDS_TOTAL, iFriendsUsingApp.getSummary().getTotalCount());
            assertEquals(page, iFriendsUsingApp.getPaging().getPage());
            assertEquals(appProperties.getFriendPagingSize(), iFriendsUsingApp.getPaging().getPageSize());

            String iNext = iFriendsUsingApp.getPaging().getNext();
            String iPrev = iFriendsUsingApp.getPaging().getPrev();

            // first page
            if (page == 0) {
                assertNotNull(iNext);
                assertNull(iPrev);
                // last page
            } else if (page + 1 == PAGE_COUNT) {
                assertNull(iNext);
                assertNotNull(iPrev);
                // middle
            } else {
                assertNotNull(iNext);
                assertNotNull(iPrev);
            }

            // Change direction
            if (page + 1 == PAGE_COUNT) {
                directionForward = false;
            }

            if (directionForward) {
                page++;
            } else {
                page--;
            }
            if (directionForward && page < PAGE_COUNT) {
                iFriendsUsingApp = nextFbFriendsList(iNext, page - 1);
            }
            if (!directionForward && page >= 0) {
                iFriendsUsingApp = prevFbFriendsList(iPrev, page + 1);
            }
            // Stop cycle
            if (page < 0) {
                break;
            }
        }
    }

    private FriendsUsingApp prevFbFriendsList(String iPrev, int page) {
        final Invocation.Builder request = target("/friends").path("/facebook-friends-prev-page").request();
        final Response response = request.post(Entity.text("{\"prev\":\"" + iPrev + "\", \"page\":" + page + "}"));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final String responseJson = response.readEntity(String.class);
        return GSON.fromJson(responseJson, FriendsUsingApp.class);
    }

    private FriendsUsingApp nextFbFriendsList(String iNext, int page) {
        final Invocation.Builder request = target("/friends").path("/facebook-friends-next-page").request();
        final Response response = request.post(Entity.text("{\"next\":\"" + iNext + "\", \"page\":" + page + "}"));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final String responseJson = response.readEntity(String.class);
        return GSON.fromJson(responseJson, FriendsUsingApp.class);
    }

    @Test
    public void fbFriendsAddDelete() throws JSONException {
        // initial condition
        {
            UserDataSource.getInstance().removeAllFriends(UsersDatabaseData4Tst.DAVE_UUID);
            Assert.assertEquals(0, UserDataSource.getInstance().getFriends(UsersDatabaseData4Tst.DAVE_UUID).size());
        }

        // add users
        {
            final FriendsUsingApp friendsUsingApp = getFbFriendsList();
            assertEquals(appProperties.getFriendPagingSize(), friendsUsingApp.getFriendsData().size());

            for (int i = 0; i < appProperties.getMaxFriendPerUser() + 1; i++) {
                final FriendsData friendsData;
                // usual case
                if (i < appProperties.getMaxFriendPerUser()) {
                    friendsData = friendsUsingApp.getFriendsData().get(i);
                // error testcase, when try to add more users that it's allowed
                } else {
                    friendsData = friendsUsingApp.getFriendsData().get(i - 1);
                }

                final Invocation.Builder request = target("/friends").path("/add-facebook-friend").request();
                final Response response = request.post(Entity.text("{\"fid\":\"" + friendsData.getId() + "\"}"));
                assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

                if (i < appProperties.getMaxFriendPerUser()) {
                    Assert.assertEquals(i + 1, UserDataSource.getInstance().getFriends(UsersDatabaseData4Tst.DAVE_UUID).size());
                }

                if (i == appProperties.getMaxFriendPerUser()) {
                    Assert.assertEquals(appProperties.getMaxFriendPerUser(), UserDataSource.getInstance().getFriends(UsersDatabaseData4Tst.DAVE_UUID).size());
                    final String responseJson = response.readEntity(String.class);
                    JSONObject jsonObject = new JSONObject(responseJson);
                    final String errorMsg = jsonObject.getString("errorMsg");
                    Assert.assertNotNull(errorMsg);
                }
            }
        }
        // remove users
        removeUserFriends(UsersDatabaseData4Tst.DAVE_UUID);
    }


    private FriendsUsingApp getFbFriendsList() {
        final Invocation.Builder request = target("/friends").path("/facebook-friends-list").request();
        final Response response = request.post(null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final String responseJson = response.readEntity(String.class);

        final FriendsUsingApp friendsUsingApp = GSON.fromJson(responseJson, FriendsUsingApp.class);
        assertEquals(SocialNetwork.FACEBOOK, friendsUsingApp.getType());
        return friendsUsingApp;
    }
}
