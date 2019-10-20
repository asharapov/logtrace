package io.github.asharapov.logtrace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Anton Sharapov
 */
public abstract class Initializer {

    public abstract void initLoggingFramework();

    protected Path resolveRootProjectDir() {
        final String appDir = System.getProperty("app.dir", null);
        Path dir = appDir != null ? Paths.get(appDir) : null;
        if (dir != null) {
            if (Files.isDirectory(dir)) {
                return dir;
            }
            try {
                Files.createDirectories(dir);
                return dir;
            } catch (IOException e) {
                // skip that error
            }
        }
        dir = Paths.get(".").toAbsolutePath();
        try {
            dir = dir.toRealPath();
        } catch (IOException e) {
            System.setProperty("app.dir", ".");
            return dir;
        }
        while (Files.isDirectory(dir) && dir.getParent() != null) {
            if (Files.isRegularFile(dir.resolve(".gitignore")) &&
                    Files.isRegularFile(dir.resolve("pom.xml")) &&
                    Files.isDirectory(dir.resolve("logtrace-core")) &&
                    Files.isDirectory(dir.resolve("logtrace-log4j")) &&
                    Files.isDirectory(dir.resolve("logtrace-log4j2")) &&
                    Files.isDirectory(dir.resolve("logtrace-logback"))) {
                System.setProperty("app.dir", dir.toAbsolutePath().toString());
                return dir;
            }
            dir = dir.getParent();
        }
        System.setProperty("app.dir", ".");
        return Paths.get(".");
    }

}
