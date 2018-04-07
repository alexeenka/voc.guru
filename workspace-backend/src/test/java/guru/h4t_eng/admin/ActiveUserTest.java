package guru.h4t_eng.admin;

import guru.h4t_eng.config.ApplicationProperties;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static guru.h4t_eng.test.util.ReflectionUtil4Tst.injectStaticFieldInt;

/**
 * ActiveUserTest.
 *
 * Created by aalexeenka on 10.05.2017.
 */
public class ActiveUserTest {

    @BeforeClass
    public static void beforeClass() {
        ActiveUsersLogger.getInstance().start();
    }

    @AfterClass
    public static void afterClass() {
        ActiveUsersLogger.getInstance().stop();
    }

    @Test
    public void simpleTest() throws ExecutionException, InterruptedException {
        final ActiveUsersLogger users = ActiveUsersLogger.getInstance();
        try {
            injectStaticFieldInt(ActiveUsersLogger.class, "EXPIRED_TIME_SEC", 1);

            UUID uuid = UUID.randomUUID();

            CompletableFuture future1 = CompletableFuture.runAsync(() -> {
                users.activate(uuid);
            });

            CompletableFuture future2 = CompletableFuture.runAsync(() -> {
                users.activate(uuid);
            });

            CompletableFuture.allOf(future1, future2).thenAccept(v -> {
                Assert.assertEquals(1, users.size());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Assert.assertEquals(0, users.size());
            }).get();
        } finally {
            injectStaticFieldInt(ActiveUsersLogger.class, "EXPIRED_TIME_SEC", ApplicationProperties.getInstance().getUserActivityLogTime());
        }
    }

    @Test
    public void test3Thread() throws ExecutionException, InterruptedException {
        final ActiveUsersLogger users = ActiveUsersLogger.getInstance();
        try {
            injectStaticFieldInt(ActiveUsersLogger.class, "EXPIRED_TIME_SEC", 1);

            UUID uuid1 = UUID.randomUUID();
            UUID uuid2 = UUID.randomUUID();
            UUID uuid3 = UUID.randomUUID();

            CompletableFuture future1 = CompletableFuture.runAsync(() -> {
                users.activate(uuid1);
                users.activate(uuid2);
                users.activate(uuid3);
            });

            CompletableFuture future2 = CompletableFuture.runAsync(() -> {
                users.activate(uuid1);
                users.activate(uuid2);
                users.activate(uuid3);
            });
            CompletableFuture future3 = CompletableFuture.runAsync(() -> {
                users.activate(uuid3);
                users.activate(uuid2);
                users.activate(uuid1);
            });

            CompletableFuture.allOf(future1, future2, future3).thenAccept(v -> {
                Assert.assertEquals(3, users.size());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Assert.assertEquals(0, users.size());
            }).get();
        } finally {
            injectStaticFieldInt(ActiveUsersLogger.class, "EXPIRED_TIME_SEC", ApplicationProperties.getInstance().getUserActivityLogTime());
        }
    }


    @Test
    public void test1000Thread() throws ExecutionException, InterruptedException {
        final ActiveUsersLogger users = ActiveUsersLogger.getInstance();
        try {
            injectStaticFieldInt(ActiveUsersLogger.class, "EXPIRED_TIME_SEC", 5);
            final int size = 1000;

            final List<UUID> uuidList = IntStream.range(0, size).mapToObj(v -> UUID.randomUUID()).collect(Collectors.toCollection(ArrayList::new));
            final List<List<UUID>> randomList = IntStream.range(0, size).mapToObj(v -> {
                final ArrayList<UUID> list = new ArrayList<>(uuidList);
                Collections.shuffle(list);
                return list;
            }).collect(Collectors.toCollection(ArrayList::new));


            final CompletableFuture[] futureList = IntStream.range(0, size).mapToObj(v -> CompletableFuture.runAsync(() -> {
                for (UUID iUuid : randomList.get(v)) {
                    users.activate(iUuid);
                }
            })).toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futureList).thenAccept(v -> {
                Assert.assertEquals(size, users.size());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Assert.assertEquals(size, users.size());
                try {
                    Thread.sleep(3500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Assert.assertEquals(0, users.size());
            }).get();
        } finally {
            injectStaticFieldInt(ActiveUsersLogger.class, "EXPIRED_TIME_SEC", ApplicationProperties.getInstance().getUserActivityLogTime());
        }
    }


    @Test
    public void test4Thread() throws ExecutionException, InterruptedException {
        final ActiveUsersLogger users = ActiveUsersLogger.getInstance();
        try {
            injectStaticFieldInt(ActiveUsersLogger.class, "EXPIRED_TIME_SEC", 1);

            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();

            CompletableFuture future1 = CompletableFuture.runAsync(() -> {
                users.activate(u1);
            });

            CompletableFuture future2 = CompletableFuture.runAsync(() -> {
                users.activate(u1);
            });

            CompletableFuture future3 = CompletableFuture.runAsync(() -> {
                users.activate(u2);
            });

            CompletableFuture future4 = CompletableFuture.runAsync(() -> {
                users.activate(u2);
            });


            CompletableFuture.allOf(future1, future3, future2, future4).thenAccept(v -> {
                Assert.assertEquals(2, users.size());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Assert.assertEquals(0, users.size());
            }).get();
        } finally {
            injectStaticFieldInt(ActiveUsersLogger.class, "EXPIRED_TIME_SEC", ApplicationProperties.getInstance().getUserActivityLogTime());
        }
    }
}
