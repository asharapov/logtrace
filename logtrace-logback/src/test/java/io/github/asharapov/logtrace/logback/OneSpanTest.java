package io.github.asharapov.logtrace.logback;

import java.util.Date;
import java.util.regex.Pattern;

import io.github.asharapov.logtrace.model.SessionInfo;
import io.github.asharapov.logtrace.AbstractTest;
import io.github.asharapov.logtrace.LogSpan;
import io.github.asharapov.logtrace.LogTracer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты включающие в себя работу с одним экземпляром LogSpan.
 *
 * @author Anton Sharapov
 */
public class OneSpanTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(OneSpanTest.class);
    private static final LogTracer tracer = LogTracer.getDefault();

    @Test
    void test01() throws Exception {
        final String sid = "kjdgfu9804332";
        final Date started = new Date();
        final String principal = "ivanov_ii";

        MDC.put("a", "1");
        final LogSpan sessionSpan = tracer.buildSpan("session")
                .withTag("sid", sid)
                .withTag("started", started)
                .withTag("principal", principal)
                .withTag("role", SessionInfo.Role.USER)
                .activate();
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(Level.INFO, "tracer.ctx.session", "SPAN ENTER", "Span \"session\" started")
                .assertThrownIsNull()
                .assertMDC("a", "1")
                .assertContextSession(sid, started, principal, SessionInfo.Role.USER);

        MDC.put("b", "2");
        log.debug("test 01");
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(Level.DEBUG, OneSpanTest.class, null, "test 01")
                .assertThrownIsNull()
                .assertMDC("a", "1", "b", "2")
                .assertContextSession(sid, started, principal, SessionInfo.Role.USER);
        MDC.remove("b");

        sessionSpan.close();
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(sessionSpan.getTraceId(), Level.INFO, "tracer.ctx.session", "SPAN FINISH", Pattern.compile("Span \"session\" completed successfully within [0-9]+ ms"))
                .assertThrownIsNull()
                .assertMDC("a", "1")
                .assertContextSession(sid, started, principal, SessionInfo.Role.USER)
                .assertContextSessionDuration();
    }

    @Test
    void test02() throws Exception {
        final String sid = "kjdgfu9804332";
        final Date started = new Date();
        final String principal = "ivanov_ii";

        final LogSpan sessionSpan = tracer.buildSpan("session")
                .withTag("sid", sid)
                .withTag("started", started)
                .withTag("principal", principal)
                .withTag("role", SessionInfo.Role.USER)
                .withLogger(log)
                .activate();
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(Level.INFO, OneSpanTest.class, "SPAN ENTER", "Span \"session\" started")
                .assertThrownIsNull()
                .assertMDC()
                .assertContextSession(sid, started, principal, SessionInfo.Role.USER);

        log.debug("test 02");
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(Level.DEBUG, OneSpanTest.class, null, "test 02")
                .assertThrownIsNull()
                .assertMDC()
                .assertContextSession(sid, started, principal, SessionInfo.Role.USER);

        sessionSpan.close();
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(sessionSpan.getTraceId(), Level.INFO, OneSpanTest.class, "SPAN FINISH", Pattern.compile("Span \"session\" completed successfully within [0-9]+ ms"))
                .assertThrownIsNull()
                .assertMDC()
                .assertContextSession(sid, started, principal, SessionInfo.Role.USER)
                .assertContextSessionDuration();
    }

    @Test
    void test03() throws Exception {
        final String sid = "kjdgfu9804332";
        final Date started = new Date();
        final String principal = "ivanov_ii";

        final LogSpan sessionSpan = tracer.buildSpan("session")
                .withTag("sid", sid)
                .withTag("started", started)
                .withTag("principal", principal)
                .withTag("role", SessionInfo.Role.USER)
                .withLogger(log)
                .activate();
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(Level.INFO, OneSpanTest.class, "SPAN ENTER", "Span \"session\" started")
                .assertThrownIsNull()
                .assertMDC()
                .assertContextSession(sid, started, principal, SessionInfo.Role.USER);

        log.debug("test 03");
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(Level.DEBUG, OneSpanTest.class, null, "test 03")
                .assertThrownIsNull()
                .assertMDC()
                .assertContextSession(sid, started, principal, SessionInfo.Role.USER);

        try {
            throwDeepTrace(5);
        } catch (Exception e) {
            sessionSpan.markAsFailed(e);
        }

        sessionSpan.close();
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(sessionSpan.getTraceId(), Level.ERROR, OneSpanTest.class, "SPAN FINISH", Pattern.compile("Span \"session\" completed with error within [0-9]+ ms"))
                .assertThrown(Exception.class, "test exception")
                .assertMDC()
                .assertContextSession(sid, started, principal, SessionInfo.Role.USER)
                .assertContextSessionDuration();
    }


    @Test
    void test04() throws Exception {
        final String sid = "kjdgfu9804332";
        final Date started = new Date();
        final String principal = "ivanov_ii";

        MDC.put("a", "1");
        final LogSpan sessionSpan = tracer.buildSpan("session")
                .withTag("sid", sid)
                .withTag("started", started)
                .withTag("principal", principal)
                .withTag("role", SessionInfo.Role.USER)
                // печать только завершающего события если время выполнения работы было больше 1000мс или в ходе ее выполнения была зафиксирована ошибка
                .withEventFilter(span -> span.getDuration() > 1000 || span.getErrorCause() != null)
                .activate();
        assertFalse(logReader.hasNext());
        assertNotNull(tracer.scopeManager().getActiveTraceId());

        log.debug("test 04");
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(Level.DEBUG, OneSpanTest.class, null, "test 04")
                .assertThrownIsNull()
                .assertMDC("a", "1")
                .assertContextSession(sid, started, principal, SessionInfo.Role.USER);

        sessionSpan.close();
        assertFalse(logReader.hasNext());
        assertNull(tracer.scopeManager().getActiveTraceId());
    }

    @Test
    void test05() throws Exception {
        final String sid = "kjdgfu9804332";
        final Date started = new Date();
        final String principal = "ivanov_ii";

        MDC.put("a", "1");
        final LogSpan sessionSpan = tracer.buildSpan("session")
                .withTag("sid", sid)
                .withTag("started", started)
                .withTag("principal", principal)
                .withTag("role", SessionInfo.Role.USER)
                // печать только завершающего события если время выполнения работы было больше 1000мс или в ходе ее выполнения была зафиксирована ошибка
                .withEventFilter(span -> span.getDuration() > 1000 || span.getErrorCause() != null)
                .activate();
        assertFalse(logReader.hasNext());
        assertNotNull(tracer.scopeManager().getActiveTraceId());

        log.debug("test 05");
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(Level.DEBUG, OneSpanTest.class, null, "test 05")
                .assertThrownIsNull()
                .assertMDC("a", "1")
                .assertContextSession(sid, started, principal, SessionInfo.Role.USER);

        try {
            throwDeepTrace(5);
        } catch (Exception e) {
            sessionSpan.markAsFailed(e);
        }
        sessionSpan.close();
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(sessionSpan.getTraceId(), Level.ERROR, "tracer.ctx.session", "SPAN FINISH", Pattern.compile("Span \"session\" completed with error within [0-9]+ ms"))
                .assertThrown(Exception.class, "test exception")
                .assertMDC("a", "1")
                .assertContextSession(sid, started, principal, SessionInfo.Role.USER)
                .assertContextSessionDuration();

        assertFalse(logReader.hasNext());
        assertNull(tracer.scopeManager().getActiveTraceId());
    }


    @Test
    void test06() throws Exception {
        final String sid = "kjdgfu9804332";
        final Date started = new Date();
        final String principal = "ivanov_ii";

        MDC.put("a", "1");
        final LogSpan sessionSpan = tracer.buildSpan("session")
                .withTag("sid", sid)
                .withTag("started", started)
                .withTag("principal", principal)
                .withTag("role", SessionInfo.Role.ADMIN)
                // печать только завершающего события если время выполнения работы было больше 1000мс или в ходе ее выполнения была зафиксирована ошибка
                .withEventFilter(span -> span.getDuration() > 1000 || span.getErrorCause() != null)
                .activate();
        assertFalse(logReader.hasNext());
        assertNotNull(tracer.scopeManager().getActiveTraceId());

        log.debug("test 06");
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(Level.DEBUG, OneSpanTest.class, null, "test 06")
                .assertThrownIsNull()
                .assertMDC("a", "1")
                .assertContextSession(sid, started, principal, SessionInfo.Role.ADMIN);

        Thread.sleep(1000);

        sessionSpan.close();
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(sessionSpan.getTraceId(), Level.INFO, "tracer.ctx.session", "SPAN FINISH", Pattern.compile("Span \"session\" completed successfully within [0-9]+ ms"))
                .assertThrownIsNull()
                .assertMDC("a", "1")
                .assertContextSession(sid, started, principal, SessionInfo.Role.ADMIN)
                .assertContextSessionDuration();

        assertFalse(logReader.hasNext());
        assertNull(tracer.scopeManager().getActiveTraceId());
    }


    @Test
    void test07() throws Exception {
        final String sid = "kjdgfu9804332";
        final Date started = new Date();
        final String principal = "ivanov_ii";

        MDC.put("a", "1");
        final LogSpan sessionSpan = tracer.buildSpan("session")
                .withTag("sid", sid)
                .withTag("started", started)
                .withTag("principal", principal)
                .withTag("role", SessionInfo.Role.GUEST)
                .withLogger(log)
                .withEventFilter((span) -> true)
                .withEventName("<custom event name>")
                .activate();
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(Level.INFO, OneSpanTest.class, "SPAN ENTER", "Span \"<custom event name>\" started")
                .assertThrownIsNull()
                .assertMDC("a", "1")
                .assertContextSession(sid, started, principal, SessionInfo.Role.GUEST);

        log.debug("test 07");
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(Level.DEBUG, OneSpanTest.class, null, "test 07")
                .assertThrownIsNull()
                .assertMDC("a", "1")
                .assertContextSession(sid, started, principal, SessionInfo.Role.GUEST);

        sessionSpan.close();
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(sessionSpan.getTraceId(), Level.INFO, OneSpanTest.class, "SPAN FINISH", Pattern.compile("Span \"<custom event name>\" completed successfully within [0-9]+ ms"))
                .assertThrownIsNull()
                .assertMDC("a", "1")
                .assertContextSession(sid, started, principal, SessionInfo.Role.GUEST)
                .assertContextSessionDuration();

        assertFalse(logReader.hasNext());
        assertNull(tracer.scopeManager().getActiveTraceId());
    }

}
