package guru.h4t_eng.config;

import guru.h4t_eng.logs.AppLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application properties.
 *
 * Created by aalexeenka on 12.12.2016.
 */
public abstract class AppProperties {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(AppProperties.class);

    AppProperties(String fileName)  {
        // Load properties from file
        Properties prop = new Properties();

        InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
        if (in == null) {
            throw new RuntimeException("Can't find " + fileName);
        }
        try {
            prop.load(in);
        } catch (IOException e) {
            final String msg = "Can't read properties from " + fileName;
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        } finally {
            IOUtils.closeQuietly(in);
        }

        reading(prop);
    }

    abstract protected void reading(Properties prop);
}
