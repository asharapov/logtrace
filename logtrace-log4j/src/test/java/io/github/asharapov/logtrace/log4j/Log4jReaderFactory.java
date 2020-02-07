package io.github.asharapov.logtrace.log4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Set;

import io.github.asharapov.logtrace.tests.LogReader;
import io.github.asharapov.logtrace.tests.LogReaderFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.Log4jLoggerFactory;

/**
 * @author Anton Sharapov
 */
public class Log4jReaderFactory extends LogReaderFactory {

    @Override
    public boolean isEnabled() {
        return LoggerFactory.getILoggerFactory() instanceof Log4jLoggerFactory;
    }

    @Override
    public LogReader getReader(final String appenderName) {
        if (!isEnabled())
            throw new IllegalStateException();

        final Set<Category> checked = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Enumeration<Logger> loggers = LogManager.getCurrentLoggers(); loggers.hasMoreElements(); ) {
            final Logger logger = loggers.nextElement();
            final FileAppender appender = findAppender(appenderName, logger, checked);
            if (appender == null)
                continue;
            final LogTraceJsonLayout layout = (LogTraceJsonLayout) appender.getLayout();
            final Path path = Paths.get(appender.getFile());
            final boolean formatted = layout.isFormatted();
            return new LogReader(path, formatted, EnumSet.of(LogReader.Capabilities.MDC_SUPPORT));
        }
        throw new IllegalArgumentException("No file appender '" + appenderName + "' with LogTraceJsonLayout");
    }

    private FileAppender findAppender(final String appenderName, final Logger logger, final Set<Category> checked) {
        for (Category log = logger; log != null && !checked.contains(log); log = log.getParent()) {
            final Appender appender = log.getAppender(appenderName);
            if ((appender instanceof FileAppender)) {
                final Layout layout = appender.getLayout();
                if (layout instanceof LogTraceJsonLayout) {
                    return (FileAppender) appender;
                } else {
                    throw new IllegalArgumentException("No file appender '" + appenderName + "' with LogTraceJsonLayout");
                }
            }
            checked.add(log);
        }
        return null;
    }
}
