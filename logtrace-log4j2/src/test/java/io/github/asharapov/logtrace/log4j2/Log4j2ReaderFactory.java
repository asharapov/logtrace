package io.github.asharapov.logtrace.log4j2;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.github.asharapov.logtrace.tests.LogReader;
import io.github.asharapov.logtrace.tests.LogReaderFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.slf4j.Log4jLoggerFactory;
import org.slf4j.LoggerFactory;

/**
 * @author Anton Sharapov
 */
public class Log4j2ReaderFactory extends LogReaderFactory {

    @Override
    public boolean isEnabled() {
        return LoggerFactory.getILoggerFactory() instanceof Log4jLoggerFactory;
    }

    @Override
    public LogReader getReader(final String appenderName) {
        if (!isEnabled())
            throw new IllegalStateException();

        final Logger coreLogger = (Logger) LogManager.getRootLogger();
        final Configuration cfg = coreLogger.getContext().getConfiguration();
        final Appender appender = cfg.getAppender(appenderName);
        if (!(appender instanceof FileAppender))
            throw new IllegalArgumentException("No file appender '" + appenderName + "'");
        final FileAppender fa = (FileAppender) appender;
        if (!(appender.getLayout() instanceof LogTraceJsonLayout))
            throw new IllegalArgumentException("No file appender '" + appenderName + "' with LogTraceJsonLayout");

        final LogTraceJsonLayout layout = (LogTraceJsonLayout) appender.getLayout();

        final Path path = Paths.get(fa.getFileName());
        final boolean formatted = layout.isFormatted();

        return new LogReader(path, formatted);
    }
}
