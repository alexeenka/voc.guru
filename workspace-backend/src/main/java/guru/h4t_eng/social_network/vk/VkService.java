package guru.h4t_eng.social_network.vk;

import java.util.List;

/**
 * VkService.
 *
 * Created by aalexeenka on 3/28/2016.
 */
public class VkService {

    private static final VkService instance;

    static {
        instance = new VkService();
    }

    public static VkService getInstance() {
        return instance;
    }

    public List<Long> getFriends(String accessToken) {
        // String vkResponseAccessToken = JerseyClient.getInstance().target(authURL).request().get(String.class);
        return  null;
    }

}
