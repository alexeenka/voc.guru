package guru.h4t_eng.service;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import guru.h4t_eng.config.ApplicationProperties;
import guru.h4t_eng.config.JerseyClient;
import guru.h4t_eng.datasource.CounterDataSource;
import guru.h4t_eng.datasource.UserDataSource;
import guru.h4t_eng.datasource.WorkEffortDataSource;
import guru.h4t_eng.rest.friends.FriendsInfoDto;
import guru.h4t_eng.service.friends.*;
import guru.h4t_eng.social_network.facebook.FacebookApiURL;
import guru.h4t_eng.social_network.vk.VkApiURL;
import guru.h4t_eng.util.SecurityEncodeUtil;
import guru.h4t_eng.util.SimpleEncryptUtil;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * FriendService.
 *
 * Created by aalexeenka on 4/5/2016.
 */
public class FriendService {

    private static final FriendService instance;
    public static final ApplicationProperties appProp = ApplicationProperties.getInstance();

    private UserDataSource userDataSource = UserDataSource.getInstance();

    private static CounterDataSource counterDataSource = CounterDataSource.getInstance();
    private static WorkEffortDataSource workEffortDataSource = WorkEffortDataSource.getInstance();

    static {
        instance = new FriendService();
    }

    public static FriendService getInstance() {
        return instance;
    }

    public ArrayList<FriendsInfoDto> getFriendsData(UUID userId, int year, int dayOfYear, int trainingCalendarStartDay, int trainingCalendarCount) {
        ArrayList<FriendsInfoDto> result = new ArrayList<>();

        Set<UUID> friends = userDataSource.getFriends(userId);

        for (UUID friendId : friends) {
            FriendsInfoDto dto = new FriendsInfoDto();

            Object[] shortFriendInfo = userDataSource.getShortFriendInfo(friendId);
            dto.setFriendUid(SimpleEncryptUtil.encode(friendId.toString()));
            dto.setPhotoUrl((String) shortFriendInfo[0]);
            dto.setFirstName((String) shortFriendInfo[1]);
            dto.setVkUid((long) shortFriendInfo[2]);
            dto.setFbUid((long) shortFriendInfo[3]);

            long todaySpentTime = workEffortDataSource.getTime(friendId, year, dayOfYear);
            dto.setTodaySpentTime(todaySpentTime);

            Long[] trainingCalendar = workEffortDataSource.getTrainingCalendar(
                    friendId,
                    year,
                    trainingCalendarStartDay,
                    trainingCalendarCount
            );
            dto.setTrainingCalendar(trainingCalendar);

            Long wordCount = counterDataSource.countWord(friendId);
            dto.setWordCount(wordCount);

            result.add(dto);
        }

        return result;
    }

    public FriendsUsingApp getVkFriendsList(UUID userId, String accessToken) {

        String appUserResponse = JerseyClient.getInstance()
                .target(VkApiURL.VK_FRIENDS_APP_USER + accessToken)
                .request(MediaType.APPLICATION_JSON_TYPE).get(String.class);

        final JsonObject appUserResponseJson = Json.parse(appUserResponse).asObject();
        final JsonValue response = appUserResponseJson.get("response");

        if (!response.isArray()) {
            return FriendsUsingApp.empty();
        }

        final String jsonValue = response.asArray().toString();
        if (jsonValue.length() <= 2) {
            return FriendsUsingApp.empty();
        }

        final String vkFriends = jsonValue.substring(1, jsonValue.length() - 1);

        userDataSource.updateCachedVkFriends(userId, vkFriends);

        final String[] arrVkFriends = vkFriends.split(",");
        ArrayList<Long> firstPage = getPage(arrVkFriends, 0);

        return getVkFriendsUsingApp(firstPage,
                isNext(0, arrVkFriends.length),
                isPrev(0),
                arrVkFriends.length,
                0
        );
    }

    public FriendsUsingApp getVkFriendsListByPage(UUID userId, int pageNumber) {
        String vkFriends = userDataSource.getCachedVkFriends(userId);
        final String[] arrVkFriends = vkFriends.split(",");
        ArrayList<Long> page = getPage(arrVkFriends, pageNumber);

        return getVkFriendsUsingApp(page,
                isNext(pageNumber, arrVkFriends.length),
                isPrev(pageNumber),
                arrVkFriends.length,
                pageNumber
        );
    }

    private FriendsUsingApp getVkFriendsUsingApp(
            ArrayList<Long> page,
            String isNext,
            String isPrev,
            int totalCount,
            int currentPage
    ) {
        final FriendsUsingApp friendsUsingApp = new FriendsUsingApp();
        // Load data for page
        {
            final List<FriendsData> appFriends = userDataSource.getAppUsersByVkUsers(page);
            friendsUsingApp.setFriendsData(appFriends);
        }

        // Paging.
        {
            final FriendsPaging friendsPaging = new FriendsPaging();
            friendsPaging.setNext(isNext);
            friendsPaging.setPrev(isPrev);
            friendsPaging.setPage(currentPage);
            friendsUsingApp.setPaging(friendsPaging);
        }

        // Summary
        {
            final FriendsSummary friendsSummary = new FriendsSummary();
            friendsSummary.setTotalCount(totalCount);
            friendsUsingApp.setSummary(friendsSummary);
        }
        return friendsUsingApp;
    }

    public String isNext(int pageNumber, int recordCount) {
        int pages = getPageCount(recordCount);
        return (pageNumber + 1) < pages ? "1" : null;
    }

    private int getPageCount(int recordCount) {
        return getPageCount(recordCount, appProp.getFriendPagingSize());
    }

    public static int getPageCount(int recordCount, int pageSize) {
        return recordCount/ pageSize + ((recordCount % pageSize == 0) ? 0 : 1);
    }

    public String isPrev(int pageNumber) {
        return pageNumber > 0 ? "1" : null;
    }

    /**
     * Return page data.
     *
     * @param vkFriends array of ids
     * @param pageNumber start from zero
     *
     */
    public ArrayList<Long> getPage(String[] vkFriends, int pageNumber) {
        int startIndex = pageNumber* appProp.getFriendPagingSize();
        int endIndex = (pageNumber + 1)* appProp.getFriendPagingSize();

        ArrayList<Long> page = new ArrayList<>();
        for (int i=startIndex; i<vkFriends.length && i<endIndex; i++) {
            page.add(Long.valueOf(vkFriends[i]));
        }

        return page;
    }

    public static final Gson GSON = new GsonBuilder().create();

    public FriendsUsingApp getFacebookFriendsList(String userAccessToken) {
        final String facebookURL = FacebookApiURL.buildUserFriendsURL(userAccessToken);

        final String jsonResponse = JerseyClient.getInstance().target(facebookURL).request().get(String.class);
        final FacebookGraph facebookGraph = GSON.fromJson(jsonResponse, FacebookGraph.class);

        if (facebookGraph == null
                || facebookGraph.getGraphContext() == null
                || facebookGraph.getGraphContext().getFriendsUsingApp() == null) {
            return new FriendsUsingApp();
        }

        final FriendsUsingApp friendsUsingApp = facebookGraph.getGraphContext().getFriendsUsingApp();
        securePaging(friendsUsingApp.getPaging(), userAccessToken);
        return friendsUsingApp;
    }

    public FriendsUsingApp getFacebookFriendsListByPagingUrl(UUID userId, String pageEncodedUrl) {
        String userAccessToken = userDataSource.getFbAccessToken(userId);
        final String encodedUrl = SecurityEncodeUtil.decodeBase64(pageEncodedUrl);
        String facebookURL = FacebookApiURL.buildUserFriendsURLPagingUrl(encodedUrl, userAccessToken);

        final String jsonResponse = JerseyClient.getInstance().target(facebookURL).request().get(String.class);
        final FriendsUsingApp friendsUsingApp = GSON.fromJson(jsonResponse, FriendsUsingApp.class);
        if (friendsUsingApp == null) {
            return new FriendsUsingApp();
        }

        securePaging(friendsUsingApp.getPaging(), userAccessToken);
        return friendsUsingApp;
    }

    private void securePaging(FriendsPaging paging, String userAccessToken) {
        // Remove access_token from next link
        if (paging != null) {
            paging.setNext(
                    getSecureUrl(paging.getNext(), userAccessToken)
            );
            paging.setPrev(
                    getSecureUrl(paging.getPrev(), userAccessToken)
            );
        }
    }

    private String getSecureUrl(String url, String userAccessToken) {
        if (StringUtils.isNoneBlank(url)) {
            // Remove access_token parameter
            String iResult = url.replace("access_token=" + userAccessToken, StringUtils.EMPTY);
            // Encode url
            return SecurityEncodeUtil.encodeBase64(iResult);
        }
        return null;
    }

}
