package guru.h4t_eng.service.friends;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * FriendsUsingApp.
 *
 * https://graph.facebook.com/v2.8/194268657598874?fields=context.fields(friends_using_app.limit(5))&access_token=EAACwr7JTwZAoBAD1TiIZAo8H4ZBP5U5dEoAjtPrkPCJ7eSeAaAGZCwRv2zdkqcyFatt3uCZC2FLOZBkM2nLyGme9OERSR38jy0hIQorXSnZCu7ioEY6j4lGWIZBfeHDMxga0hrQmkegPzTZC3UXUVBdN63sEp4v6iLIoHyvJ77oKoUAZDZD
 *
 * Created by aalexeenka on 21.11.2016.
 */
public class FriendsUsingApp {

    private SocialNetwork type;

    @SerializedName("data")
    private List<FriendsData> friendsData;

    private FriendsPaging paging;

    private FriendsSummary summary;

    public List<FriendsData> getFriendsData() {
        return friendsData;
    }

    public void setFriendsData(List<FriendsData> friendsData) {
        this.friendsData = friendsData;
    }

    public FriendsPaging getPaging() {
        return paging;
    }

    public FriendsPaging pagingNewIfNull() {
        if (paging == null) {
            return new FriendsPaging();
        }

        return paging;
    }

    public void setPaging(FriendsPaging paging) {
        this.paging = paging;
    }

    public FriendsSummary getSummary() {
        return summary;
    }

    public void setSummary(FriendsSummary summary) {
        this.summary = summary;
    }

    public SocialNetwork getType() {
        return type;
    }

    public void setType(SocialNetwork type) {
        this.type = type;
    }

    public static FriendsUsingApp empty() {
        final FriendsUsingApp friendsUsingApp = new FriendsUsingApp();

        friendsUsingApp.setFriendsData(new ArrayList<>());
        {
            final FriendsPaging paging = new FriendsPaging();
            paging.setPage(0);
            paging.setPrev("0");
            paging.setNext("0");
            friendsUsingApp.setPaging(paging);
        }

        {
            final FriendsSummary summary = new FriendsSummary();
            summary.setTotalCount(0);
            friendsUsingApp.setSummary(summary);
        }

        return friendsUsingApp;
    }

}
