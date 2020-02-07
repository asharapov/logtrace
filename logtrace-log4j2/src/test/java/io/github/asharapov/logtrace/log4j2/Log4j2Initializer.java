package io.github.asharapov.logtrace.log4j2;

import io.github.asharapov.logtrace.tests.Initializer;

/**
 * @author Anton Sharapov
 */
public class Log4j2Initializer extends Initializer {

    @Override
    public void initLoggingFramework() {
        resolveRootProjectDir();
    }

}
