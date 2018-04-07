package guru.h4t_eng.test.rest.friends;

import guru.h4t_eng.config.ApplicationProperties;
import guru.h4t_eng.datasource.UserDataSource;
import guru.h4t_eng.test.rest.config.AbstractRest4Tst;
import guru.h4t_eng.util.SimpleEncryptUtil;
import org.junit.Assert;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Common for VK and FB.
 *
 * Created by aalexeenka on 26.12.2016.
 */
public abstract class AbstractSocialNetworkTst extends AbstractRest4Tst {

    protected static final ApplicationProperties appProperties = ApplicationProperties.getInstance();

    public void removeUserFriends(UUID userId) {
        final Set<UUID> friends = UserDataSource.getInstance().getFriends(userId);
        int i = appProperties.getMaxFriendPerUser();
        for (UUID friendUUID : friends) {
            final Invocation.Builder request = target("/friends").path("/remove-friend").request();
            final Response response = request.post(Entity.text("{\"uid\":\"" + SimpleEncryptUtil.encode(friendUUID.toString()) + "\"}"));
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            i--;
            Assert.assertEquals(i, UserDataSource.getInstance().getFriends(userId).size());
        }
        Assert.assertEquals(0, UserDataSource.getInstance().getFriends(userId).size());
    }

}
