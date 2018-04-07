package guru.h4t_eng.test.rest.friends;

import guru.h4t_eng.UsersDatabaseData4Tst;
import guru.h4t_eng.config.ApplicationProperties;
import guru.h4t_eng.datasource.UserDataSource;
import guru.h4t_eng.rest.friends.FriendsRest;
import guru.h4t_eng.service.friends.FriendsData;
import guru.h4t_eng.service.friends.FriendsPaging;
import guru.h4t_eng.service.friends.FriendsUsingApp;
import guru.h4t_eng.service.friends.SocialNetwork;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.List;

import static guru.h4t_eng.service.FriendService.getPageCount;
import static guru.h4t_eng.test.util.Utils4Tst.GSON;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * VkFriendsRestTst.
 * <p>
 * Created by aalexeenka on 18.05.2016.
 */
public class VkFriendsRestTst extends AbstractSocialNetworkTst {

    @Override
    protected Class getRestClass() {
        return FriendsRest.class;
    }

    private final static ApplicationProperties appProperties = ApplicationProperties.getInstance();

    static final private int VK_FRIENDS_TOTAL = 3;
    static final private int VK_PAGE_SIZE = 1;
    private static final int PAGE_COUNT = getPageCount(VK_FRIENDS_TOTAL, VK_PAGE_SIZE);


    @Test
    public void vkFriendsPagingPerSamanta() {
        int backupMaxFriendPerUser = appProperties.getMaxFriendPerUser();
        // for testing set paging to 1
        setPaging(VK_PAGE_SIZE);

        try {
            // (!) Get first page
            System.out.println("[Vk] getting first page");
            FriendsUsingApp iFriendsUsingApp = getVkFirstPageUsers();


            // (!) Paging
            int page = -1;
            boolean directionForward = true;
            while (true) {
                System.out.println("[Vk] Direction: " + (directionForward ? "forward" : "backward") + ", Page: " + ((page == -1) ? "init_page" : page) + ", Data: " + GSON.toJson(iFriendsUsingApp));

                String iNext = iFriendsUsingApp.getPaging().getNext();
                String iPrev = iFriendsUsingApp.getPaging().getPrev();

                // first page or init_page
                if (page == 0 || page == -1) {
                    assertPagingFields(iFriendsUsingApp, 0, null, "1");
                    // last page
                } else if (page + 1 == PAGE_COUNT) {
                    assertPagingFields(iFriendsUsingApp, page, "1", null);
                    // middle
                } else {
                    assertPagingFields(iFriendsUsingApp, page, "1", "1");
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

                // Stop cycle
                if (page < 0) {
                    break;
                }

                iFriendsUsingApp = getVkPage(page);
            }

        } finally {
            // restore paging
            setPaging(backupMaxFriendPerUser);
        }

    }

    private FriendsUsingApp getVkFirstPageUsers() {
        final Invocation.Builder request = target("/friends").path("/vk-friends-list").request();
        final Response response = request.post(null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final String responseJson = response.readEntity(String.class);
        return GSON.fromJson(responseJson, FriendsUsingApp.class);
    }

    @Test
    public void vkFriendsAddAndRemoveUserPerSamanta() {
        // initial condition
        {
            UserDataSource.getInstance().removeAllFriends(UsersDatabaseData4Tst.SAMANTA_UUID);
            Assert.assertEquals(0, UserDataSource.getInstance().getFriends(UsersDatabaseData4Tst.SAMANTA_UUID).size());
        }
        // Get VK users
        FriendsUsingApp iFriendsUsingApp = getVkFirstPageUsers();
        // Get Vk users
        final List<FriendsData> friendsDataList = iFriendsUsingApp.getFriendsData();
        assertEquals(appProperties.getFriendPagingSize(), friendsDataList.size());

        for (int i = 0; i < appProperties.getMaxFriendPerUser() + 1; i++) {
            FriendsData friendsData;
            // usual case
            if (i < appProperties.getMaxFriendPerUser()) {
                friendsData = friendsDataList.get(i);
                // error testcase, when try to add more users that it's allowed
            } else {
                friendsData = friendsDataList.get(i - 1);
            }

            final Invocation.Builder request = target("/friends").path("/add-vk-friend").request();
            final Response response = request.post(Entity.text("{\"sid\":" + friendsData.getId() + "}"));
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            if (i < appProperties.getMaxFriendPerUser()) {
                Assert.assertEquals(i + 1, UserDataSource.getInstance().getFriends(UsersDatabaseData4Tst.SAMANTA_UUID).size());
            }

            if (i == appProperties.getMaxFriendPerUser()) {
                Assert.assertEquals(appProperties.getMaxFriendPerUser(), UserDataSource.getInstance().getFriends(UsersDatabaseData4Tst.SAMANTA_UUID).size());
                final String responseJson = response.readEntity(String.class);
                JSONObject jsonObject = new JSONObject(responseJson);
                final String errorMsg = jsonObject.getString("errorMsg");
                Assert.assertNotNull(errorMsg);
            }
        }

        // remove users
        removeUserFriends(UsersDatabaseData4Tst.SAMANTA_UUID);
    }


    private FriendsUsingApp getVkPage(int currentPage) {
        final Invocation.Builder request = target("/friends").path("/vk-friends-page").request();
        final Response response = request.post(Entity.text("{\"page\":" + currentPage + "}"));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final String responseJson = response.readEntity(String.class);
        return GSON.fromJson(responseJson, FriendsUsingApp.class);
    }

    private void assertPagingFields(FriendsUsingApp friendsUsingApp,
                                    int pageNumber,
                                    String prevPage,
                                    String nextPage
    ) {
        // Summary
        assertEquals(VK_FRIENDS_TOTAL, friendsUsingApp.getSummary().getTotalCount());

        assertEquals(SocialNetwork.VK, friendsUsingApp.getType());
        // Paging: main-data
        final FriendsPaging paging = friendsUsingApp.getPaging();
        assertEquals(nextPage, paging.getNext());
        assertEquals(prevPage, paging.getPrev());
        assertEquals(pageNumber, paging.getPage());
        assertEquals(VK_PAGE_SIZE, paging.getPageSize());

        // Friends Data
        final List<FriendsData> friendsDataList = friendsUsingApp.getFriendsData();
        assertEquals(VK_PAGE_SIZE, friendsDataList.size());

        for (FriendsData friendsData : friendsDataList) {
            assertThat(friendsData.getId(), not(isEmptyString()));
            assertThat(friendsData.getProfileImg(), not(isEmptyString()));
            assertThat(friendsData.getName(), not(isEmptyString()));
        }
    }

    private void setPaging(int i) {
        try {
            final Field maxFriendPerUser = ApplicationProperties.class.getDeclaredField("friendPagingSize");
            maxFriendPerUser.setAccessible(true);
            maxFriendPerUser.setInt(appProperties, i);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
