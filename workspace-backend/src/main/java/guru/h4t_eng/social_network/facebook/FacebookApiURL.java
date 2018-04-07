package guru.h4t_eng.social_network.facebook;

import guru.h4t_eng.config.ApplicationProperties;
import guru.h4t_eng.config.SocialNetworkProperties;

/**
 * FacebookApiURL.
 *
 * Created by aalexeenka on 2/19/2016.
 */
public final class FacebookApiURL {

    private FacebookApiURL() {}

    public static final String GRAPH_FACEBOOK_URL = "https://graph.facebook.com/v2.12";

    private static final String clientId = SocialNetworkProperties.getInstance().getFbClientId();
    private static final String clientSecret = SocialNetworkProperties.getInstance().getFbClientSecret();
    private static final String redirectURL = SocialNetworkProperties.getInstance().getFbRedirectURL();

    public static String buildAuthUrl(String code) {
        return GRAPH_FACEBOOK_URL + "/oauth/access_token?" +
                "client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&code=" + code +
                "&redirect_uri=" + redirectURL;
    }

    public static String buildUserFriendsURL(String accessToken) {
        // Example:
        // https://graph.facebook.com/v2.8/194268657598874?fields=context.fields(friends_using_app.limit(10))
        // &access_token=EAACwr7JTwZAoBAD1TiIZAo8H4ZBP5U5dEoAjtPrkPCJ7eSeAaAGZCwRv2zdkqcyFatt3uCZC2FLOZBkM2nLyGme9OERSR38jy0hIQorXSnZCu7ioEY6j4lGWIZBfeHDMxga0hrQmkegPzTZC3UXUVBdN63sEp4v6iLIoHyvJ77oKoUAZDZD

        return GRAPH_FACEBOOK_URL + "/" + clientId + "?fields=context.fields(friends_using_app.limit("
                + ApplicationProperties.getInstance().getFriendPagingSize() + "))&access_token=" + accessToken;
    }

    /**
     * It can be next or prev url
     *
     * @return facebook full url
     */
    public static String buildUserFriendsURLPagingUrl(String pagingUrl, String accessToken) {
        // Example of nextPageUrl:
        // https://graph.facebook.com/v2.8/YXBwbGljYXRpb25fY29udGV4dDoxOTQyNjg2NTc1OTg4NzQZD/friends_using_app?&limit=10&after=MTA4OTA5NzIyOTI2NTg4
        return pagingUrl + "&access_token=" + accessToken;
    }
}
