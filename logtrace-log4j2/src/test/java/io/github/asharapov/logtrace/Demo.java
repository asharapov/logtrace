package io.github.asharapov.logtrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo {
    private static final Logger log = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) {
        example1();
        example2("item-23", 1, true);
        example3("item-23", 1, true);
        example4();
    }

    private static void example1() {
        log.info("Hello, world!");
        try {
            throw new IllegalStateException("some error");
        } catch (Exception e) {
            log.error("An error handled: " + e.getMessage(), e);
        }
    }

    private static void example2(String name, int count, boolean enabled) {
        LogSpan span = LogTracer.getDefault().buildSpan("test-action")
                .withTag("name", name)
                .withTag("count", count)
                .withTag("enabled", enabled)
                .activate();
        try {
            // ...
            example1();
            // ...
        } finally {
            span.close();
        }
    }

    private static void example3(String name, int count, boolean enabled) {
        LogSpan span = LogTracer.getDefault().buildSpan("test-action")
                .withTag("name", name)
                .withTag("count", count)
                .withTag("enabled", enabled)
                .activate();
        try {
            // ...
            log.info("Hello, {}!", name);
            throw new IllegalArgumentException("Test error");
            // ...
        } catch (Exception e) {
            span.markAsFailed(e);
        } finally {
            span.close();
        }
    }

    private static void example4() {
        LogSpan span = LogTracer.getDefault().buildSpan("auth")
                .withTag("user", "demouser")
                .withTag("role", "guest")
                .withoutEvents()
                .withEventFilter(s -> s.getDuration() > 100)
                .activate();
        try {
            example2("item-6", 2, false);
            log.debug("completed");
        } finally {
            span.close();
        }
    }
}
