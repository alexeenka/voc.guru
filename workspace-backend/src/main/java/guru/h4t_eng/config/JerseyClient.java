package guru.h4t_eng.config;

import org.apache.http.client.config.RequestConfig;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * JerseyClient.
 *
 * Created by Alexey Alexeenka on 11.07.2015.
 */
public class JerseyClient {

    private static final Client instance;

    // example: http://www.theotherian.com/2013/08/jersey-client-2.0-httpclient-timeouts-max-connections.html
    static {
        ClientConfig clientConfig = new ClientConfig();

        clientConfig.connectorProvider(new ApacheConnectorProvider());

        RequestConfig reqConfig = RequestConfig.custom()                         // apache HttpClient specific
                .setConnectTimeout(2000)
                .setSocketTimeout(2000)
                //.setConnectionRequestTimeout(200)
                .build();
        clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, reqConfig); // jersey specific
        clientConfig.property(ApacheClientProperties.DISABLE_COOKIES, true); // jersey specific

        // values are in milliseconds
//        clientConfig.property(ClientProperties.READ_TIMEOUT, 2000);
//        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, 500);

//        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
//        connectionManager.setMaxTotal(100);
//        connectionManager.setDefaultMaxPerRoute(20);
//        connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost("localhost")), 40);
//
//        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);

        //noinspection UnnecessaryLocalVariable
        Client client = ClientBuilder.newClient(clientConfig);
        //client.register(JacksonFeature.class);

        instance =  client;
    }

    public static Client getInstance() {
        return instance;
    }
}
