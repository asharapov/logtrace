package io.github.asharapov.logtrace.jul;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import io.github.asharapov.logtrace.LogReader;
import io.github.asharapov.logtrace.LogReaderFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.JDK14LoggerFactory;

/**
 * @author Anton Sharapov
 */
public class JULReaderFactory extends LogReaderFactory {

    @Override
    public boolean isEnabled() {
        return LoggerFactory.getILoggerFactory() instanceof JDK14LoggerFactory;
    }

    @Override
    public LogReader getReader(final String appenderName) {
        if (!isEnabled())
            throw new IllegalStateException();

        final LogManager mgr = LogManager.getLogManager();
        for (Enumeration<String> en = mgr.getLoggerNames(); en.hasMoreElements(); ) {
            final String loggerName = en.nextElement();
            final Logger logger = mgr.getLogger(loggerName);
            for (Handler handler : logger.getHandlers()) {
                if (!(handler instanceof FileHandler))
                    continue;
                if (!(handler.getFormatter() instanceof LogTraceJsonFormatter))
                    continue;
                final FileHandler fh = (FileHandler) handler;
                final LogTraceJsonFormatter formatter = (LogTraceJsonFormatter) handler.getFormatter();
                final boolean formatted = formatter.isFormatted();

                final String appDir = System.getProperty("app.dir", ".");
                final Path path = Paths.get(appDir + "/logs/jul-elastic.log");
                return new LogReader(path, formatted, EnumSet.of(LogReader.Capabilities.MDC_SUPPORT));
            }
        }
        throw new IllegalArgumentException("No file appender '" + appenderName + "' with LogTraceJsonFormatter");
    }

}
