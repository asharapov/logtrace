package io.github.asharapov.logtrace.tests;

import java.io.IOException;

import io.github.asharapov.logtrace.tests.model.LogRecord;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты log appenders без использования публичного LogTrace API.
 *
 * @author Anton Sharapov
 */
public abstract class SimpleTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(SimpleTest.class);

    @Test
    void test01() throws IOException {
        assertFalse(logReader.hasNext());
        assertNull(logReader.nextRecord());
    }

    @Test
    void test02() throws IOException {
        log.info("Test 1");
        assertTrue(logReader.hasNext());
        logReader.nextRecord()
                .assertRecordFromSameThread(Level.INFO, SimpleTest.class, null, "Test 1")
                .assertMDC()
                .assertContextIsNull();
        assertFalse(logReader.hasNext());

        MDC.put("x", "1");
        MDC.put("y", "2");
        try {
            log.trace(APP_START, "Test 2");
            logReader.nextRecord()
                    .assertRecordFromSameThread(Level.TRACE, SimpleTest.class, "APP START", "Test 2")
                    .assertMDC("y", "2", "x", "1")
                    .assertThrownIsNull()
                    .assertContextIsNull();

            throwDeepTrace(10);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            logReader.nextRecord()
                    .assertRecordFromSameThread(Level.ERROR, SimpleTest.class, null, "test exception")
                    .assertMDC("x", "1", "y", "2")
                    .assertThrown(Exception.class, "test exception")
                    .assertContextIsNull();
        } finally {
            MDC.remove("x");
            MDC.remove("y");
        }

        log.trace(APP_FINISH, "Test 4");
        logReader.nextRecord()
                .assertRecordFromSameThread(Level.TRACE, SimpleTest.class, "APP FINISH", "Test 4")
                .assertMDC()
                .assertThrownIsNull()
                .assertContextIsNull();
        assertFalse(logReader.hasNext());
    }

    @Test
    void test03() throws Exception {
        assertFalse(logReader.hasNext());
        try {
            try {
                throwDeepTrace(3);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new Exception(e.getMessage(), e);
            }
        } catch (Exception ee) {
            log.error("Wrapped exception for a: " + ee.getMessage(), ee);
        }
        final LogRecord r1 = logReader.nextRecord();
        final LogRecord r2 = logReader.nextRecord();
        assertNotNull(r1.thrown);
        assertNotNull(r2.thrown);
        assertNotNull(r1.thrown.hash);
        assertNotNull(r2.thrown.hash);
        assertEquals(r1.thrown.hash, r2.thrown.hash);
        assertNotEquals(r1.thrown.stack, r2.thrown.stack);
        assertFalse(logReader.hasNext());
    }

}
