package io.github.asharapov.logtrace.jul;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import io.github.asharapov.logtrace.Initializer;

/**
 * @author Anton Sharapov
 */
public class JULInitializer extends Initializer {

    @Override
    public void initLoggingFramework() {
        try {
            final Path baseDir = resolveRootProjectDir();
            final Path path = baseDir.resolve("logs/jul-elastic.log");
            Files.createDirectories(path.getParent());
            final Handler handler = new FileHandler(path.toString(), false);
            handler.setFormatter(new LogTraceJsonFormatter(false));
            handler.setLevel(Level.ALL);

            for (String name : new String[]{""}) {
                final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
                logger.setLevel(Level.ALL);
                logger.setUseParentHandlers(false);
                for (Handler oldHandler : logger.getHandlers()) {
                    logger.removeHandler(oldHandler);
                }
                logger.addHandler(handler);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
