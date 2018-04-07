package guru.h4t_eng.datasource;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import guru.h4t_eng.exception.CassandraException;
import guru.h4t_eng.logs.AppLoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CassandraDataSource.
 *
 * Created by Alexey Alexeenka on 03.07.2015.
 */
public class CassandraDataSource
{
    private static final Logger LOG = AppLoggerFactory.getH4TLog(CassandraDataSource.class);

    private static final CassandraDataSource instance;

    static
    {
        try
        {
            instance = new CassandraDataSource();
        }
        catch (Exception e)
        {
            LOG.error("Can't create instance of CassandraDataSource", e);
            throw new RuntimeException("Can't create instance of CassandraDataSource.", e);
        }
    }

    public static CassandraDataSource getInstance()
    {
        return instance;
    }

    private volatile Session session = null;

    // config
    private String keyspace;
    private String contactPoints;
    private String user;
    private String password;
    private Integer connectTimeout;
    private Integer readTimeout;


    private Map<String, PreparedStatement> psCache = new ConcurrentHashMap<>();

    /**
     * Creates a new CassandraDataSource from properties file for the given
     * name. Attempts to load cassandra-name.properties, where name is the name
     * passed in to the constructor.
     */
    protected CassandraDataSource() throws IOException, CassandraException
    {
        this("cassandra.properties");
    }

    protected CassandraDataSource(String fileName) throws CassandraException, IOException {
        Properties prop = new Properties();
        InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
        if (in == null) {
            throw new CassandraException("Can't find cassandra.properties file");
        }

        prop.load(in);
        in.close();

        keyspace = prop.getProperty("keyspace");
        contactPoints = prop.getProperty("contactPoints");
        user = prop.getProperty("user");
        password = prop.getProperty("password");
        connectTimeout = 10000;
        readTimeout = 25000;

        if (user == null || user.length() == 0)
            throw new CassandraException("No user provided.");

        if (password == null || password.length() == 0)
            throw new CassandraException("No password provided.");
    }

    private Cluster getNewCluster()
    {
        Cluster.Builder builder = Cluster.builder();
        PoolingOptions po = new PoolingOptions();

        SocketOptions so = new SocketOptions();
        so.setConnectTimeoutMillis(connectTimeout);
        so.setReadTimeoutMillis(readTimeout);
        so.setKeepAlive(true);

        builder
                .withSocketOptions(so)
                .withReconnectionPolicy(new ConstantReconnectionPolicy(1000L))
                .addContactPoint(contactPoints);

        builder.withAuthProvider(new PlainTextAuthProvider(user, password));

        builder.withPoolingOptions(po);
        builder.withLoadBalancingPolicy(new TokenAwarePolicy(new RoundRobinPolicy()));

        return builder.build();
    }

    public Session getSession()
    {
        if (session != null)
            return session;

        synchronized (this)
        {
            if (session != null)
                return session;

            session = getNewCluster().connect(keyspace);
            return session;
        }
    }

    public PreparedStatement prepare(String stmt)
    {
        PreparedStatement ps;

        ps = psCache.get(stmt);

        if (ps != null)
            return ps;

        ps = getSession().prepare(stmt);
        psCache.put(stmt, ps);

        return ps;
    }

    public ResultSet execute(BoundStatement bs)
    {
        return getSession().execute(bs);
    }

    public ResultSetFuture executeAsync(BoundStatement bs)
    {
        return getSession().executeAsync(bs);
    }


    public void shutdown()
    {
        if (session == null) {
            return;
        }
        synchronized (this) {
            final Cluster cluster = session.getCluster();
            session.close();
            cluster.close();
        }
    }

    /**
     * Query helper
     */
    public ResultSet runQuery(String query, boolean quorum, Object... params)
    {
        BoundStatement bs = new BoundStatement(prepare(query));
        bs.bind(params);
        if (quorum)
        {
            bs.setConsistencyLevel(ConsistencyLevel.QUORUM);
        }
        return execute(bs);
    }

    public ResultSet runQuery(String query, boolean quorum, ArrayList<Object> params) {
        return runQuery(query, quorum, params.toArray(new Object[params.size()]));
    }

    private List<ResultSetFuture> sendQueries(String query, Object[][] partitionKeysArray) {
        List<ResultSetFuture> futures = Lists.newArrayListWithExpectedSize(partitionKeysArray.length);
        for (Object[] partitionKeys : partitionKeysArray)
            futures.add(session.executeAsync(query, partitionKeys));
        return futures;
    }

    public List<ListenableFuture<ResultSet>> queryAll( String query, Object[]... partitionKeysArray) {
        List<ResultSetFuture> futures = sendQueries(query, partitionKeysArray);
        return Futures.inCompletionOrder(futures);
    }

    // http://www.datastax.com/dev/blog/java-driver-async-queries
    public List<ListenableFuture<ResultSet>> sendQueries(String cql, Object... partitionKeys) {
        PreparedStatement statement = prepare(cql);
        List<ResultSetFuture> futures = Lists.newArrayListWithExpectedSize(partitionKeys.length);
        for (Object partitionKey  : partitionKeys) {
            futures.add(getSession().executeAsync(statement.bind(partitionKey)));
        }

        return Futures.inCompletionOrder(futures);
    }
}