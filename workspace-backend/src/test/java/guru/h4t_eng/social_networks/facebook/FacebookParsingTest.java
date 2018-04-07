package guru.h4t_eng.social_networks.facebook;

import guru.h4t_eng.service.friends.FacebookGraph;
import guru.h4t_eng.service.friends.FriendsUsingApp;
import guru.h4t_eng.test.WithLogging;
import guru.h4t_eng.util.SecurityEncodeUtil;
import org.junit.Test;

import static guru.h4t_eng.service.FriendService.GSON;
import static guru.h4t_eng.test.util.Utils4Tst.readResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * FacebookParsingTst.
 *
 * Created by aalexeenka on 21.11.2016.
 */
public class FacebookParsingTest extends WithLogging {

    @Test
    public void baseTest() {
        final String facebookJson = readResource("/social_networks/facebook_friends_response.json");
        final FacebookGraph facebookGraphContext = GSON.fromJson(facebookJson, FacebookGraph.class);
        assertNotNull(facebookGraphContext);
        assertNotNull(facebookGraphContext.getGraphContext());
        final FriendsUsingApp friendsUsingApp = facebookGraphContext.getGraphContext().getFriendsUsingApp();
        assertNotNull(friendsUsingApp);
        assertEquals(5, friendsUsingApp.getFriendsData().size());
        assertEquals(11, friendsUsingApp.getSummary().getTotalCount());
        assertNotNull(friendsUsingApp.getPaging().getNext());

        // check encode logic for facebook next url
        assertEquals(friendsUsingApp.getPaging().getNext(), SecurityEncodeUtil.decodeBase64(SecurityEncodeUtil.encodeBase64(friendsUsingApp.getPaging().getNext())));
    }
}
