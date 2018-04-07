package guru.h4t_eng.test.rest.config;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Rule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

/**
 * AbstractRest4Tst.
 *
 * For more information look at:
 *   https://jersey.java.net/documentation/latest/deployment.html - Chapter 4. Application Deployment and Runtime Environments
 *   https://jersey.java.net/documentation/latest/test-framework.html - Chapter 25. Jersey Test Framework
 *
 * (!): spent 6 hours to find it:
 * [
 * Problems running JerseyTest when dealing with HttpServletResponse:
 * http://stackoverflow.com/questions/17973277/problems-running-jerseytest-when-dealing-with-httpservletresponse
 * ]
 *
 * Created by aalexeenka on 12/17/2015.
 */
public abstract class AbstractRest4Tst extends JerseyTest {

    @Rule
    public TestName testName = new TestName();

    public String getTestName() {
        return testName.getMethodName();
    }

    @Rule
    public TestWatcher watcher = new TestWatcher()
    {
        @Override
        protected void starting(Description desc)
        {
            System.out.print(desc.getDisplayName());
        }

        @Override
        protected void succeeded(Description description)
        {
            System.out.println(" OK");
        }

        @Override
        protected void failed(Throwable e, Description description)
        {
            System.out.println(" FAILED");
        }

        @Override
        protected void skipped(AssumptionViolatedException e, Description description)
        {
            System.out.println(" SKIPPED");
        }
    };


    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new ResourceConfig();
    }

    abstract protected Class getRestClass();

    protected Class getUserSecurityFilter() {
        // Default Samanta User
        return SamantaVkSecurity4TstFilter.class;
    }

    @Override
    protected Client getClient() {
        final Client client = super.getClient();
        // Add support MultiPartFeature for client code.
        client.register(MultiPartFeature.class);
        return client;
    }


    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {

        return new TestContainerFactory() {
            @Override
            public TestContainer create(URI baseUri, DeploymentContext deploymentContext) {
                return new TestContainer() {
                    private HttpServer server;

                    @Override
                    public ClientConfig getClientConfig() {
                        return null;
                    }

                    @Override
                    public URI getBaseUri() {
                        return baseUri;
                    }

                    @Override
                    public void start() {
                        try {

                            String classNames = MultiPartFeature.class.getCanonicalName() + ",";
                            classNames += getUserSecurityFilter().getCanonicalName() + "," + getRestClass().getCanonicalName();

                            this.server = GrizzlyWebContainerFactory.create(
                                    baseUri, Collections.singletonMap(ServerProperties.PROVIDER_CLASSNAMES, classNames)
                            );
                        } catch (ProcessingException | IOException e) {
                            throw new TestContainerException(e);
                        }
                    }

                    @Override
                    public void stop() {
                        this.server.shutdownNow();

                    }
                };
            }
        };
    }

}
