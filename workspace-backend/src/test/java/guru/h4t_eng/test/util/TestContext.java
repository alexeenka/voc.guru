package guru.h4t_eng.test.util;

import java.util.UUID;

/**
 * Share object in memory for test.
 *
 *
 * Created by aalexeenka on 15.03.2017.
 */
public class TestContext {

    private TestContext() {}

    private static final TestContext INSTANCE = new TestContext();

    public static TestContext getInstance() {
        return INSTANCE;
    }

    public UUID getTestUserId() {
        return testUserId;
    }

    public void setTestUserId(UUID testUserId) {
        this.testUserId = testUserId;
    }

    public UUID testUserId;

}
