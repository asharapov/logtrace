package io.github.asharapov.logtrace.logback;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import io.github.asharapov.logtrace.LogReader;
import io.github.asharapov.logtrace.LogReaderFactory;
import org.slf4j.LoggerFactory;

/**
 * @author Anton Sharapov
 */
public class LogbackReaderFactory extends LogReaderFactory {

    @Override
    public boolean isEnabled() {
        return LoggerFactory.getILoggerFactory() instanceof LoggerContext;
    }

    @Override
    public LogReader getReader(final String appenderName) {
        if (!isEnabled())
            throw new IllegalStateException();

        final LoggerContext lctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (ch.qos.logback.classic.Logger logger : lctx.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext(); ) {
                final Appender<ILoggingEvent> appender = index.next();
                if (!appenderName.equals(appender.getName()))
                    continue;
                if (!(appender instanceof FileAppender))
                    throw new IllegalArgumentException("No file appender '" + appenderName + "'");
                final FileAppender fa = (FileAppender) appender;
                if (!(fa.getEncoder() instanceof LogTraceJsonEncoder))
                    throw new IllegalArgumentException("No file appender '" + appenderName + "' with LogTraceJsonEncoder");
                final LogTraceJsonEncoder encoder = (LogTraceJsonEncoder) fa.getEncoder();

                final Path path = Paths.get(fa.getFile());
                final boolean formatted = encoder.getFormatted();
                return new LogReader(path, formatted);
            }
        }
        throw new IllegalArgumentException("No file appender '" + appenderName + "' with LogTraceJsonEncoder");
    }

}
