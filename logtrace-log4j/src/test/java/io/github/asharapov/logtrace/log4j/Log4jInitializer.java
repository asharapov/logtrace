package io.github.asharapov.logtrace.log4j;

import java.net.URL;

import io.github.asharapov.logtrace.Initializer;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author Anton Sharapov
 */
public class Log4jInitializer extends Initializer {

    @Override
    public void initLoggingFramework() {
        resolveRootProjectDir();
        final URL url = Log4jInitializer.class.getResource("/log4j.properties");
        PropertyConfigurator.configure(url);
    }

}
