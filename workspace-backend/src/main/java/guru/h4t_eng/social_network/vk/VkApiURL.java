package guru.h4t_eng.social_network.vk;

/**
 * Created by Alexey Alexeenka on 08.07.2015.
 *
 * http://vk.com/dev/friends.getAppUsers
 *
 */
public final class VkApiURL {

    /**
     * Details on page: https://vk.com/dev/auth_sites
     */
    public static final String VK_AUTH_URL =
            "https://oauth.vk.com/access_token?" +
                    "client_id=%s" +
                    "&client_secret=%s" +
                    "&code=%s" +
                    "&redirect_uri=%s&v=5.12";
    //**=====**//

    //** Get FirstName, Photo for user of VK
    // http://vk.com/dev/users.get
    public static final String VK_GET_USER_INFO_URL =
            "https://api.vk.com/method/users.get?user_ids=%s&fields=first_name,last_name,photo_medium&access_token=%s&v=5.12";
    // *** USING URI_TEMPLATE *** //

    //**=====**//

    // Get Friends of current user who install application
    // http://vk.com/dev/friends.getAppUsers
    public static final String VK_FRIENDS_APP_USER =
            "https://api.vk.com/method/friends.getAppUsers?v=5.12&access_token=";
    //**=====**//

    // Are UserFriend
    // Docs: http://vk.com/dev/friends.areFriends
    // Example: https://api.vk.com/method/friends.areFriends?user_ids=64727605&need_sign=0&access_token=b8e8cfded58b6a6742d5b1b8c397015eaea3032e6cb0b69cb212c49db1453c1b3d7805bd190c2ea335e03
    //**=====**//
    public static final String VK_ARE_FRIENDS =
            "https://api.vk.com/method/friends.areFriends?user_ids=%s&need_sign=0&access_token=%s&v=5.12";
    //**=====**//

}
