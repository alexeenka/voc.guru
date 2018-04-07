package guru.h4t_eng.test.topusers;

import guru.h4t_eng.top_users.TopUser;
import guru.h4t_eng.top_users.TopUserStorage;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.junit.Assert.*;

/**
 * TopUserStorageTest.
 *
 * Created by aalexeenka on 11.05.2017.
 */
public class TopUserStorageTest {

    @Test
    public void simpleTest() throws ExecutionException, InterruptedException {
        final TopUserStorage instance = TopUserStorage.getInstance();

        try {
            CompletableFuture<Void> future = instance.start();
            future.get();

            final TopUser[][] top = instance.getTop();

            assertNotEquals("Always must be records for current month", top[2].length, 0);

            for (TopUser[] users : top) {
                for (TopUser user : users) {
                    assertNotNull(user.getUserId());
                    assertThat(user.getFirstName(), not(isEmptyOrNullString()));
                    assertThat(user.getLastName(), not(isEmptyOrNullString()));
                    assertThat(user.getPhotoUrl(), not(isEmptyOrNullString()));
                    assertNotEquals(user.getWork(), 0);
                    assertTrue(user.getVkuid() != 0 || user.getFbuid() != 0);
                }
            }

        } finally {
            instance.stop();
        }
    }
}
