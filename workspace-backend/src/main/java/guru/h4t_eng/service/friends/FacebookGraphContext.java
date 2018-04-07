package guru.h4t_eng.service.friends;

import com.google.gson.annotations.SerializedName;

/**
 * FacebookGraphContext.
 *
 * Created by aalexeenka on 21.11.2016.
 */
public class FacebookGraphContext {

    @SerializedName("friends_using_app")
    private FriendsUsingApp friendsUsingApp;

    public FriendsUsingApp getFriendsUsingApp() {
        return friendsUsingApp;
    }

    public void setFriendsUsingApp(FriendsUsingApp friendsUsingApp) {
        this.friendsUsingApp = friendsUsingApp;
    }
}
