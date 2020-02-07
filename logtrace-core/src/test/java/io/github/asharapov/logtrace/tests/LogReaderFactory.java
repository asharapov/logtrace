package io.github.asharapov.logtrace.tests;

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anton Sharapov
 */
public abstract class LogReaderFactory {

    private static final Logger log = LoggerFactory.getLogger(LogReaderFactory.class);

    private static class Lazy {
        private static final LogReaderFactory INSTANCE = resolveFactory();

        private static LogReaderFactory resolveFactory() {
            final ServiceLoader<? extends LogReaderFactory> loader = ServiceLoader.load(LogReaderFactory.class);
            for (LogReaderFactory f : loader) {
                if (f.isEnabled()) {
                    return f;
                }
            }
            log.error("Can't resolve log reader implementation");
            return null;
        }
    }

    public static LogReaderFactory getDefault() {
        return Lazy.INSTANCE;
    }

    public abstract boolean isEnabled();

    public abstract LogReader getReader(final String appenderName);
}
