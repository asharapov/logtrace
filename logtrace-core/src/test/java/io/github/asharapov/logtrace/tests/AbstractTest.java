package io.github.asharapov.logtrace.tests;

import java.io.IOException;
import java.util.ServiceLoader;

import io.github.asharapov.logtrace.api.LogTracer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * @author Anton Sharapov
 */
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public abstract class AbstractTest {

    static {
        final ServiceLoader<? extends Initializer> loaded = ServiceLoader.load(Initializer.class);
        for (Initializer initializer : loaded) {
            initializer.initLoggingFramework();
        }
    }

    protected static final Marker APP = MarkerFactory.getMarker("APP");
    protected static final Marker APP_START = MarkerFactory.getMarker("START");
    protected static final Marker APP_FINISH = MarkerFactory.getMarker("FINISH");
    protected static LogReader logReader;

    static {
        APP_START.add(APP);
        APP_FINISH.add(APP);
    }

    @BeforeAll
    static void beforeAllTests() {
        logReader = LogReaderFactory.getDefault().getReader("logtrace-file");
    }

    @AfterAll
    static void afterAllTests() throws IOException {
        logReader.close();
    }

    @BeforeEach
    protected void beforeEachTest() throws IOException {
        MDC.clear();
        LogTracer.getDefault().scopeManager().reset();
        logReader.positionToEOF();
    }


    protected static void throwDeepTrace(final int deep) throws Exception {
        try {
            throwDeepTraceImpl(deep);
        } catch (Throwable e) {
            throw new Exception("test exception", e);
        }
    }

    private static void throwDeepTraceImpl(final int deep) throws Exception {
        if (deep > 0) {
            throwDeepTraceImpl(deep - 1);
        } else {
            throw new IllegalStateException("original test exception");
        }
    }

}
