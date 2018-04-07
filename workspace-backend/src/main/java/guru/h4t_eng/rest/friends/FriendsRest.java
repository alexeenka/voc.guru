package guru.h4t_eng.rest.friends;

import com.google.common.collect.Sets;
import guru.h4t_eng.config.ApplicationProperties;
import guru.h4t_eng.datasource.UserDataSource;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.rest.Main4Rest;
import guru.h4t_eng.rest.json.FastJsonBuilder;
import guru.h4t_eng.rest.json.FastJsonParser;
import guru.h4t_eng.service.FriendService;
import guru.h4t_eng.service.friends.FriendsUsingApp;
import guru.h4t_eng.service.friends.SocialNetwork;
import guru.h4t_eng.util.SimpleEncryptUtil;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * FriendsRest.
 * <p>
 * Created by aalexeenka on 3/28/2016.
 */
@Path("/friends")
public class FriendsRest extends Main4Rest {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(FriendsRest.class);

    public static final FriendService friendsService = FriendService.getInstance();

    public static final UserDataSource userDataSource = UserDataSource.getInstance();


    @Path("/facebook-friends-list")
    @POST
    public Response getFacebookFriendsList(@Context HttpServletRequest request) {
        UUID userId = getUserId(request);
        Optional<String> userAccessToken = Optional.ofNullable(userDataSource.getFbAccessToken(userId));
        if (!userAccessToken.isPresent()) {
            throw new RuntimeException("No token information");
        }

        final FriendsUsingApp facebookFriendsList = friendsService.getFacebookFriendsList(userAccessToken.get());
        facebookFriendsList.setType(SocialNetwork.FACEBOOK);
        facebookFriendsList.pagingNewIfNull().setPage(0);

        return Response.ok(FriendService.GSON.toJson(facebookFriendsList), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Path("/vk-friends-list")
    @POST
    public Response getVkFrindsList(@Context HttpServletRequest request) {
        UUID userId = getUserId(request);
        Optional<String> userAccessToken = Optional.ofNullable(userDataSource.getVkAccessToken(userId));
        if (!userAccessToken.isPresent()) {
            throw new RuntimeException("No token information");
        }

        FriendsUsingApp vkFriendsList = friendsService.getVkFriendsList(userId, userAccessToken.get());
        vkFriendsList.setType(SocialNetwork.VK);
        return Response.ok(FriendService.GSON.toJson(vkFriendsList), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Path("/vk-friends-page")
    @POST
    public Response getVkFriendsListPage(@Context HttpServletRequest request) throws IOException {
        UUID userId = getUserId(request);

        String jsonString = IOUtils.toString(request.getInputStream(), "UTF-8");
        int currentPage = FastJsonParser.getInt(jsonString, "page");

        final FriendsUsingApp facebookFriendsList = friendsService.getVkFriendsListByPage(userId, currentPage);
        facebookFriendsList.setType(SocialNetwork.VK);

        return Response.ok(FriendService.GSON.toJson(facebookFriendsList), MediaType.APPLICATION_JSON_TYPE).build();
    }


    @Path("/facebook-friends-next-page")
    @POST
    public Response getFacebookFriendsListNextPage(@Context HttpServletRequest request) {
        try {
            UUID userId = getUserId(request);
            String jsonString = IOUtils.toString(request.getInputStream(), "UTF-8");
            String next = FastJsonParser.getString(jsonString, "next");
            int page = FastJsonParser.getInt(jsonString, "page");

            final FriendsUsingApp facebookFriendsList = friendsService.getFacebookFriendsListByPagingUrl(userId, next);
            facebookFriendsList.setType(SocialNetwork.FACEBOOK);
            facebookFriendsList.pagingNewIfNull().setPage(page + 1);

            return Response.ok(FriendService.GSON.toJson(facebookFriendsList), MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            LOG.error("getFacebookFriendsListNextPage", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("/facebook-friends-prev-page")
    @POST
    public Response getFacebookFriendsListPrevPage(@Context HttpServletRequest request) {
        try {
            UUID userId = getUserId(request);
            String jsonString = IOUtils.toString(request.getInputStream(), "UTF-8");
            String prev = FastJsonParser.getString(jsonString, "prev");
            int page = FastJsonParser.getInt(jsonString, "page");

            final FriendsUsingApp facebookFriendsList = friendsService.getFacebookFriendsListByPagingUrl(userId, prev);
            facebookFriendsList.setType(SocialNetwork.FACEBOOK);
            facebookFriendsList.pagingNewIfNull().setPage(page - 1);

            return Response.ok(FriendService.GSON.toJson(facebookFriendsList), MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            LOG.error("getFacebookFriendsListPrevPage", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("/add-vk-friend")
    @POST
    public Response addVkFriend(@Context HttpServletRequest request) throws IOException {
        return addFriendByRequest(request, true);
    }

    @Path("/add-friend")
    @POST
    public Response addFriend(@Context HttpServletRequest request) throws IOException {
        return addFriendByRequest(request, false);
    }

    private Response addFriendByRequest(@Context HttpServletRequest request, boolean secure) throws IOException {
        UUID userId = getUserId(request);
        String jsonString = IOUtils.toString(request.getInputStream(), "UTF-8");
        String sid = FastJsonParser.getString(jsonString, "sid");
        if (secure) {
            sid = SimpleEncryptUtil.decode(sid);
        }
        final UUID friendId = UUID.fromString(sid);

        Response validationResponse = checkFriend(userId, friendId);
        if (validationResponse != null) return validationResponse;

        userDataSource.addFriends(userId, Sets.newHashSet(friendId));

        return Response.ok().build();
    }


    @Path("/add-facebook-friend")
    @POST
    public Response addFacebookFriend(@Context HttpServletRequest request) throws IOException {
        UUID userId = getUserId(request);

        String jsonString = IOUtils.toString(request.getInputStream(), "UTF-8");
        Long fid = FastJsonParser.getLong(jsonString, "fid");

        final UUID friendId = userDataSource.getUserIdByFbID(fid);
        if (friendId == null) {
            JSONObject json = new JSONObject();
            // Need to use ResourceBundle.getBundle(); Example: http://www.avajava.com/tutorials/lessons/how-do-i-use-locales-and-resource-bundles-to-internationalize-my-application.html
            json.put(
                    "errorMsg",
                    "Пользователь использовал старую версию приложения и его нету в новой, " +
                            "можно постучать ему в личку и попросить перезайти на сайт :)"
            );
            return Response.ok(json.toString(), MediaType.APPLICATION_JSON_TYPE).build();
        }

        Response validationResponse = checkFriend(userId, friendId);
        if (validationResponse != null) return validationResponse;

        userDataSource.addFriends(userId, Sets.newHashSet(friendId));

        return Response.ok().build();
    }

    private Response checkFriend(UUID userId, UUID friendId) {
        if (userId.equals(friendId)) {
            JSONObject json = new JSONObject();
            // Need to use ResourceBundle.getBundle(); Example: http://www.avajava.com/tutorials/lessons/how-do-i-use-locales-and-resource-bundles-to-internationalize-my-application.html
            json.put(
                    "errorMsg",
                    "Нельзя добавить самого себя :)"
            );
            return Response.ok(json.toString(), MediaType.APPLICATION_JSON_TYPE).build();
        }

        final Set<UUID> friends = userDataSource.getFriends(userId);

        if (friends.contains(friendId)) {
            JSONObject json = new JSONObject();
            // Need to use ResourceBundle.getBundle(); Example: http://www.avajava.com/tutorials/lessons/how-do-i-use-locales-and-resource-bundles-to-internationalize-my-application.html
            json.put(
                    "errorMsg",
                    "Пользователь уже у вас в друзьях."
            );
            return Response.ok(json.toString(), MediaType.APPLICATION_JSON_TYPE).build();
        }

        if (friends.size() >= ApplicationProperties.getInstance().getMaxFriendPerUser()) {
            JSONObject json = new JSONObject();
            // Need to use ResourceBundle.getBundle(); Example: http://www.avajava.com/tutorials/lessons/how-do-i-use-locales-and-resource-bundles-to-internationalize-my-application.html
            json.put(
                    "errorMsg",
                    "Нельзя добавить больше " + ApplicationProperties.getInstance().getMaxFriendPerUser() + " друзей."
            );
            return Response.ok(json.toString(), MediaType.APPLICATION_JSON_TYPE).build();
        }

        return null;
    }

    @Path("/remove-friend")
    @POST
    public Response removeFriend(@Context HttpServletRequest request) {
        try {
            UUID userId = getUserId(request);

            String jsonString = IOUtils.toString(request.getInputStream(), "UTF-8");
            String encodedUid = FastJsonParser.getString(jsonString, "uid");
            final UUID uid = UUID.fromString(SimpleEncryptUtil.decode(encodedUid));

            userDataSource.deleteFriends(userId, Sets.newHashSet(uid));

            return Response.ok().build();
        } catch (Exception e) {
            LOG.error("removeFriend", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("/friends-info")
    @POST
    public Response getFriends(@Context HttpServletRequest request) {
        UUID userId = getUserId(request);
        try {
            String jsonString = IOUtils.toString(request.getInputStream(), "UTF-8");

            int year = FastJsonParser.getInt(jsonString, "year");
            int dayOfYear = FastJsonParser.getInt(jsonString, "dayOfYear");
            int trainingCalendarStartDay = FastJsonParser.getInt(jsonString, "startDay");
            int trainingCalendarCount = FastJsonParser.getInt(jsonString, "count");

            ArrayList<FriendsInfoDto> friends = FriendService.getInstance().getFriendsData(
                    userId, year, dayOfYear, trainingCalendarStartDay, trainingCalendarCount
            );

            return Response.ok(FastJsonBuilder.toJsonArray(friends), MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            LOG.error("getFriendsData", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
