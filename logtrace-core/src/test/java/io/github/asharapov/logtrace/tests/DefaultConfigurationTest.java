package io.github.asharapov.logtrace.tests;

import io.github.asharapov.logtrace.api.DefaultConfiguration;
import io.github.asharapov.logtrace.api.LogTracer;
import io.github.asharapov.logtrace.impl.BaseLogTracer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anton Sharapov
 */
public class DefaultConfigurationTest {

    @Test
    void testConfiguration() throws Exception {
        final DefaultConfiguration cfg = DefaultConfiguration.getInstance();
        assertNotNull(cfg);
        assertEquals("ctx", cfg.getNamespace());
        assertEquals("tracer", cfg.getRootLogCategory());
        assertEquals("42", cfg.getProperties().get("customproperty"));
    }

    @Test
    void testDefaultTracer() throws Exception {
        final LogTracer tracer = LogTracer.getDefault();
        assertNotNull(tracer);
        assertTrue(tracer instanceof BaseLogTracer);
        assertSame(tracer, LogTracer.getDefault());
    }
}
