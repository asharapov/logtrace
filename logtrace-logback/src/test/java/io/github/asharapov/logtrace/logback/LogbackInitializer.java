package io.github.asharapov.logtrace.logback;

import io.github.asharapov.logtrace.Initializer;

/**
 * @author Anton Sharapov
 */
public class LogbackInitializer extends Initializer {

    @Override
    public void initLoggingFramework() {
        resolveRootProjectDir();
    }

}
