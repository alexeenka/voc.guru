package guru.h4t_eng.schedule;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import guru.h4t_eng.datasource.CassandraDataSource;
import guru.h4t_eng.logs.AppLoggerFactory;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static guru.h4t_eng.schedule.utils.WorkExecutorUtils.minskDateTime;

/**
 * CassandraMonitoringWorkExecutor.
 * <br>
 * <a href="http://docs.datastax.com/en/developer/java-driver/3.1/manual/pooling/" target="_blank">More info</a>
 * <p>
 * <p>
 * Created by aalexeenka on 20.01.2017.
 */
public class CassandraMonitoringWorkExecutor extends WorkExecutor {

    private static final Logger LOG = AppLoggerFactory.getCassandraLog(CassandraMonitoringWorkExecutor.class);

    public CassandraMonitoringWorkExecutor() {
        super("cassandra-monitoring", getAppWork());
    }

    private static Map<String, String> hostState = new HashMap<>();

    private static AppWork getAppWork() {
        return () -> {
            final Session session = CassandraDataSource.getInstance().getSession();
            final Configuration config = session.getCluster().getConfiguration();

            final LoadBalancingPolicy loadBalancingPolicy = config.getPolicies().getLoadBalancingPolicy();
            final PoolingOptions poolingOptions = config.getPoolingOptions();

            Session.State state = session.getState();
            for (Host host : state.getConnectedHosts()) {
                HostDistance distance = loadBalancingPolicy.distance(host);
                int connections = state.getOpenConnections(host);
                int inFlightQueries = state.getInFlightQueries(host);

                String current = "host=" + host + ", connection=" + connections + ", current_load="
                        + inFlightQueries + ", max_load=" + connections * poolingOptions.getMaxRequestsPerConnection(distance);

                final String hostStr = host.toString();
                final String prev = hostState.computeIfAbsent(hostStr, e -> {
                    LOG.info(minskDateTime() + " " + current);
                    return current;
                });

                if (prev.equals(current)) {
                    continue;
                }
                hostState.put(hostStr, current);
                LOG.info(minskDateTime() + " " + current);
            }
        };
    }

    @Override
    Logger getLogger() {
        return LOG;
    }

    @Override
    public void start() {
        scheduledTask = executorService.scheduleWithFixedDelay(doTaskWork(appWork),
                0,
                1,
                TimeUnit.SECONDS
        );
    }

    void doTaskWorkPlain(AppWork appWork) {
        doTaskWorkPlain(appWork, false);
    }
}
