package guru.h4t_eng.datasource;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import guru.h4t_eng.exception.H4TApplicationException;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.model.dictionary.DictionaryWord;
import guru.h4t_eng.security.model.H4TUserInfo;
import guru.h4t_eng.service.friends.FriendsData;
import guru.h4t_eng.util.FormDataUtil;
import guru.h4t_eng.util.SimpleEncryptUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * UserDataSource.
 *
 * Created by Alexey Alexeenka on 03.07.2015.
 */
public class UserDataSource
{
    private static final Logger LOG = AppLoggerFactory.getH4TLog(UserDataSource.class);

    private static final UserDataSource instance = new UserDataSource();
    private static final String CQL_SELECT_USER_INFO = "select user_id, photo_url, first_name, last_name from user where user_id = ?";
    private static final WordDataSource wds = WordDataSource.getInstance();

    private UserDataSource() {
    }

    public static UserDataSource getInstance() {
        return instance;
    }

    private static CassandraDataSource mds = CassandraDataSource.getInstance();

    public UUID getUserIdByVkID (long vkID) {
        final ResultSet rows = mds.runQuery("select user_id from VK_USERS where vk_user = ?", false, vkID);
        if (rows.isExhausted()) return null;

        final Row one = rows.one();
        return one.getUUID("user_id");
    }

    public UUID getUserIdByFbID (long fbID) {
        final ResultSet rows = mds.runQuery("select user_id from FB_USERS where fb_user = ?", false, fbID);
        if (rows.isExhausted()) return null;

        final Row one = rows.one();
        return one.getUUID("user_id");
    }

    public void logoutUser(UUID userId) {
        if (userId != null) {
            mds.runQuery("update USER SET last_logout_date=? where user_id = ?", false, new Date(), userId);
        }
    }

    public UUID loginUser(H4TUserInfo userInfo, String userAgent, String clientIpAddress) throws H4TApplicationException {
        try {
            UUID userId = null;
            // DOES THAT USER ALREADY EXIST IN SYSTEM
            switch (userInfo.getUserAuthType()) {
                case FACEBOOK:
                    userId = getUserIdByFbID(userInfo.getSocialNetworkId());
                    break;
                case VK:
                    userId = getUserIdByVkID(userInfo.getSocialNetworkId());
                    break;
            }

            // FOR NEW USERS
            if (userId == null) {
                userId = createNewUser(userInfo, userAgent, clientIpAddress);
                insertGiftWord(userId);
                return userId;
            }

            // FOR OLD USERS
            {
                userInfo.setUserId(userId);
                updateExistedUser(userInfo, userAgent, clientIpAddress, userId);
                return userId;
            }
        } catch (Throwable th) {
            final String msg = "Can't loginUser [" + userInfo + "]";
            LOG.error(msg, th);
            throw new H4TApplicationException(msg);
        }
    }

    private void updateExistedUser(H4TUserInfo userInfo, String userAgent, String clientIpAddress, UUID userId) {
        // information how to user SET: https://docs.datastax.com/en/cql/3.0/cql/cql_using/use_set_t.html
        final StringBuilder attributes = new StringBuilder("first_name=?, last_name=?, email=?, last_login_date=?,user_agents=user_agents+?,client_ip_addresses=client_ip_addresses+?");
        switch (userInfo.getUserAuthType()) {
            case FACEBOOK:
                attributes.append(",fb_access_token=?");
                break;
            case VK:
                attributes.append(",vk_access_token=?");
                break;
        }

        mds.runQuery(
                "update USER SET " + attributes + " where user_id = ?",
                false,
                userInfo.getFirstName(),
                userInfo.getLastName(),
                userInfo.getEmail(),
                userInfo.getLastLoginDate(),
                Collections.singleton(userAgent),
                Collections.singleton(clientIpAddress),
                userInfo.getAccessToken(),
                userId
        );
    }

    private UUID createNewUser(H4TUserInfo userInfo, String userAgent, String clientIpAddress) {
        UUID userId;
        userId = UUIDs.timeBased();
        userInfo.setUserId(userId);

        // 1. insert into FAST_SEARCH_TABLES
        switch (userInfo.getUserAuthType()) {
            case FACEBOOK:
                mds.runQuery("insert into FB_USERS(user_id,fb_user) values(?,?)", false, userId, userInfo.getSocialNetworkId());
                break;
            case VK:
                mds.runQuery("insert into VK_USERS(user_id,vk_user) values(?,?)", false, userId, userInfo.getSocialNetworkId());
                break;
        }

        // 2. insert into user information table
        final StringBuilder attributes = new StringBuilder("user_id,first_name,last_name,photo_url,email,last_login_date,user_agents,client_ip_addresses");
        switch (userInfo.getUserAuthType()) {
            case FACEBOOK:
                attributes.append(",fbuid,fb_access_token");
                break;
            case VK:
                attributes.append(",vkuid,vk_access_token");
                break;
        }
        final String query = "insert into USER( "+ attributes + " ) values(?,?,?,?,?,?,?,?,?,?)";
        mds.runQuery(query,
                false,
                userId, userInfo.getFirstName(), userInfo.getLastName(), userInfo.getPhotoURL(),userInfo.getEmail(),
                userInfo.getLastLoginDate(),
                Collections.singleton(userAgent),
                Collections.singleton(clientIpAddress),
                userInfo.getSocialNetworkId(),
                userInfo.getAccessToken()
        );
        return userId;
    }

    private static final List<DictionaryWord> NEW_USER_WORDS;

    static {
        try {
            final InputStream is = UserDataSource.class.getResource("/words-set/new-user-set/new-user-set.json").openStream();
            final String json = IOUtils.toString(is, "UTF-8");
            IOUtils.closeQuietly(is);

            NEW_USER_WORDS = FormDataUtil.parseJsonDictionaryWordList(json);
        } catch (Exception e) {
            LOG.error("Can't set new words: ", e);
            throw new RuntimeException(e);
        }
    }

    private void insertGiftWord(UUID userId) {
        for (DictionaryWord word : NEW_USER_WORDS) {
            wds.saveWord(word, userId, false);
        }
    }

    public List<FriendsData> getAppUsersByVkUsers(List<Long> vkUsersIds) {
        if (vkUsersIds == null || vkUsersIds.size() == 0) {
            return new ArrayList<>();
        }

        ArrayList<FriendsData> result = new ArrayList<>();

        List<ListenableFuture<ResultSet>> appUserFutureSet = new ArrayList<>();

        // 1. Async: Get id of user from vk_users table
        {
            @SuppressWarnings("ConfusingArgumentToVarargsMethod")
            List<ListenableFuture<ResultSet>> listenableFutures = mds.sendQueries(
                    "select user_id from vk_users where vk_user = ?",
                    vkUsersIds.toArray(new Long[vkUsersIds.size()])
            );
            for (ListenableFuture<ResultSet> future : listenableFutures) {
                try {
                    ResultSet rs = future.get();
                    if (rs.isExhausted()) {
                        LOG.error("Can\'t find app user by vk_id");
                        continue;
                    }

                    final UUID user_id = rs.one().getUUID("user_id");
                    final BoundStatement statement = mds
                            .prepare(CQL_SELECT_USER_INFO)
                            .bind(user_id);
                    appUserFutureSet.add(mds.executeAsync(statement));
                } catch (InterruptedException | ExecutionException e) {
                    LOG.error("Error: GetAppUsersByVkUsers: Get id of user from vk_users table", e);
                }
            }
        }

        // 2: Async: Get user data from user table
        {
            final ImmutableList<ListenableFuture<ResultSet>> appUserResult = Futures.inCompletionOrder(appUserFutureSet);
            for (ListenableFuture<ResultSet> future : appUserResult) {
                try {
                    final ResultSet rs = future.get();
                    if (rs.isExhausted()) {
                        LOG.error("Can\'t find app user by user_id");
                        continue;
                    }
                    final Row row = rs.one();
                    FriendsData friendsData = new FriendsData();
                    // id
                    friendsData.setId(SimpleEncryptUtil.encode(row.getUUID("user_id").toString()));
                    // name
                    final String firstName = row.getString("first_name");
                    final String lastName = row.getString("last_name");
                    friendsData.setName((firstName + " " + lastName).trim());
                    // image
                    friendsData.setProfileImg(row.getString("photo_url"));


                    result.add(friendsData);
                } catch (InterruptedException | ExecutionException e) {
                    LOG.error("Error: GetAppUsersByVkUsers: async: Get user data from user table", e);
                }

            }
        }

        return result;
    }

    public void addFriends(UUID userId, Set<UUID> friendIds) {
        if (friendIds == null || friendIds.size() == 0) {
            return;
        }

        mds.runQuery("UPDATE user SET friends = friends + ? WHERE user_id = ?", false, friendIds, userId);
    }

    public void deleteFriends(UUID userId, Set<UUID> friendIds) {
        if (friendIds == null || friendIds.size() == 0) {
            return;
        }

        mds.runQuery("UPDATE user SET friends = friends - ? WHERE user_id = ?", false, friendIds, userId);
    }

    public void removeAllFriends(UUID userId) {
        mds.runQuery("UPDATE user SET friends = {} WHERE user_id = ?", false, userId);
    }

    public Optional<String[]> getUserDetails(UUID userId) {
        ResultSet rows = mds.runQuery("select first_name, photo_url, fbuid, vkuid from user WHERE user_id = ?", false, userId);

        if (rows.isExhausted()) {
            String msg = "[!!!] Can\'t find app user by id [" + userId + "]";
            LOG.error(msg);
            return Optional.empty();
        }

        Row one = rows.one();
        return Optional.of(new String[] {
                one.getString("first_name"),
                one.getString("photo_url"),
                Long.toString(one.getLong("fbuid")),
                Long.toString(one.getLong("vkuid")),
        });
    }

    public String getVkAccessToken(UUID userId) {
        ResultSet rows = mds.runQuery("select vk_access_token from user where user_id = ?", false, userId);

        if (rows.isExhausted()) {
            String msg = "[!!!] Can\'t find app user by id [" + userId + "]";
            LOG.error(msg);
            throw new RuntimeException(msg);
        }

        Row one = rows.one();

        return one.getString("vk_access_token");
    }

    public Set<UUID> getFriends(UUID userId) {
        ResultSet rows = mds.runQuery("select friends from user where user_id = ?", false, userId);

        if (rows.isExhausted()) {
            String msg = "[!!!] Can\'t find app user by id [" + userId + "]";
            LOG.error(msg);
            throw new RuntimeException(msg);
        }

        Row one = rows.one();

        return one.getSet("friends", UUID.class);
    }

    public Object[] getShortFriendInfo(UUID userId) {
        ResultSet rows = mds.runQuery("select photo_url, first_name, vkuid, fbuid from user  where user_id = ?", false, userId);

        if (rows.isExhausted()) {
            String msg = "[!!!] Can\'t find app user by id [" + userId + "]";
            LOG.error(msg);
            throw new RuntimeException(msg);
        }

        Row one = rows.one();

        return new Object[] {one.getString("photo_url"), one.getString("first_name"), one.getLong("vkuid"), one.getLong("fbuid")};
    }

    public String getFbAccessToken(UUID userId) {
        ResultSet rows = mds.runQuery("select fb_access_token from user where user_id = ?", false, userId);

        if (rows.isExhausted()) {
            String msg = "[!!!] Can\'t find app user by id [" + userId + "]";
            LOG.error(msg);
            throw new RuntimeException(msg);
        }

        Row one = rows.one();

        return one.getString("fb_access_token");
    }

    public void updateCachedVkFriends(UUID userId, String vkFriends) {
        final PreparedStatement prepare = mds.prepare("update user set cached_vk_friends = ? where user_id = ?");
        final BoundStatement bind = prepare.bind(vkFriends, userId);
        mds.executeAsync(bind);
    }

    public String getCachedVkFriends(UUID userId) {
        final PreparedStatement prepare = mds.prepare("select cached_vk_friends from user where user_id = ?");
        final BoundStatement bind = prepare.bind(userId);
        final ResultSet rows = mds.execute(bind);

        if (rows.isExhausted()) {
            throw new RuntimeException("Can't find user by id: " + userId);
        }

        return rows.one().getString("cached_vk_friends");
    }
}
