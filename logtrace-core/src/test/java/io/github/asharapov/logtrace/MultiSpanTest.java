package io.github.asharapov.logtrace;

import java.util.Date;

import io.github.asharapov.logtrace.model.SessionInfo;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты включающие в себя работу с несколькими экземплярами LogSpan.
 *
 * @author Anton Sharapov
 */
public abstract class MultiSpanTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(MultiSpanTest.class);
    private static final LogTracer tracer = LogTracer.getDefault();

    @Test
    void test01() throws Exception {
        final Date started1 = new Date();

        LogSpan session1 = activateSessionSpan("ctx", "session-1", started1, "ivanov_ii", SessionInfo.Role.USER);
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(Level.INFO, MultiSpanTest.class, "SPAN ENTER", "Span \"ctx.session\" started")
                .assertThrownIsNull()
                .assertContextSession("session-1", started1, "ivanov_ii", SessionInfo.Role.USER);
        assertNull(session1.getParentSpan());

        LogSpan user1 = activateUserActionSpan("ctx", "req-1", "list-items", null, null);
        logReader.nextRecord()
                .assertRecordFromSameThread(session1.getTraceId(), Level.INFO, MultiSpanTest.class, "SPAN ENTER", "Span \"ctx.user-action\" started")
                .assertThrownIsNull()
                .assertContextSession("session-1", started1, "ivanov_ii", SessionInfo.Role.USER)
                .assertContextUserAction("req-1", "list-items", null, null);
        assertSame(session1, user1.getParentSpan());

        user1.close();
        logReader.nextRecord()
                .assertRecordFromSameThread(session1.getTraceId(), Level.INFO, MultiSpanTest.class, "SPAN FINISH", "Span \"ctx.user-action\" completed successfully within [0-9]+ ms")
                .assertThrownIsNull()
                .assertContextSession("session-1", started1, "ivanov_ii", SessionInfo.Role.USER)
                .assertContextUserAction("req-1", "list-items", null, null)
                .assertContextUserActionDuration();


        user1 = activateUserActionSpan("ctx", "req-2", "get-item", "#1", null);
        logReader.nextRecord()
                .assertRecordFromSameThread(session1.getTraceId(), Level.INFO, MultiSpanTest.class, "SPAN ENTER", "Span \"ctx.user-action\" started")
                .assertThrownIsNull()
                .assertContextSession("session-1", started1, "ivanov_ii", SessionInfo.Role.USER)
                .assertContextUserAction("req-2", "get-item", "#1", null);
        assertSame(session1, user1.getParentSpan());

        LogSpan action1 = activateInternalAction("ctx", "action1", "x=42");
        logReader.nextRecord()
                .assertRecordFromSameThread(session1.getTraceId(), Level.INFO, MultiSpanTest.class, "SPAN ENTER", "Span \"ctx.internal-action\" started")
                .assertThrownIsNull()
                .assertContextSession("session-1", started1, "ivanov_ii", SessionInfo.Role.USER)
                .assertContextUserAction("req-2", "get-item", "#1", null)
                .assertContextInternalAction(1, "action1", "x=42")
                .assertAltContextIsNull();
        assertSame(user1, action1.getParentSpan());


        final Date started2 = new Date();
        LogSpan session2 = activateSessionSpan("altctx", "session-2", started2, "ivanov_ii", SessionInfo.Role.ADMIN);
        logReader.nextRecord()
                .assertRecordFromSameThread(session1.getTraceId(), Level.INFO, MultiSpanTest.class, "SPAN ENTER", "Span \"altctx.session\" started")
                .assertThrownIsNull()
                .assertContextSession("session-1", started1, "ivanov_ii", SessionInfo.Role.USER)
                .assertContextUserAction("req-2", "get-item", "#1", null)
                .assertContextInternalAction(1, "action1", "x=42")
                .assertAltContextSession("session-2", started2, "ivanov_ii", SessionInfo.Role.ADMIN);
        assertSame(action1, session2.getParentSpan());

        log.info("test");
        logReader.nextRecord()
                .assertRecordFromSameThread(session1.getTraceId(), Level.INFO, MultiSpanTest.class, null, "test")
                .assertThrownIsNull()
                .assertContextSession("session-1", started1, "ivanov_ii", SessionInfo.Role.USER)
                .assertContextUserAction("req-2", "get-item", "#1", null)
                .assertContextInternalAction(1, "action1", "x=42")
                .assertAltContextSession("session-2", started2, "ivanov_ii", SessionInfo.Role.ADMIN);

        session2.close();
        logReader.nextRecord()
                .assertRecordFromSameThread(session1.getTraceId(), Level.INFO, MultiSpanTest.class, "SPAN FINISH", "Span \"altctx.session\" completed successfully within [0-9]+ ms")
                .assertThrownIsNull()
                .assertContextSession("session-1", started1, "ivanov_ii", SessionInfo.Role.USER)
                .assertContextUserAction("req-2", "get-item", "#1", null)
                .assertContextInternalAction(1, "action1", "x=42")
                .assertAltContextSession("session-2", started2, "ivanov_ii", SessionInfo.Role.ADMIN)
                .assertAltContextSessionDuration();

        LogSpan action2 = activateInternalAction("ctx", "action2", "x=99");
        logReader.nextRecord()
                .assertRecordFromSameThread(session1.getTraceId(), Level.INFO, MultiSpanTest.class, "SPAN ENTER", "Span \"ctx.internal-action\" started")
                .assertThrownIsNull()
                .assertContextSession("session-1", started1, "ivanov_ii", SessionInfo.Role.USER)
                .assertContextUserAction("req-2", "get-item", "#1", null)
                .assertContextInternalAction(2, "action1", "x=42")
                .assertContextInternalAction(2, "action2", "x=99")
                .assertAltContextIsNull();
        assertSame(action1, action2.getParentSpan());

        action2.close();
        logReader.nextRecord()
                .assertRecordFromSameThread(session1.getTraceId(), Level.INFO, MultiSpanTest.class, "SPAN FINISH", "Span \"ctx.internal-action\" completed successfully within [0-9]+ ms")
                .assertThrownIsNull()
                .assertContextSession("session-1", started1, "ivanov_ii", SessionInfo.Role.USER)
                .assertContextUserAction("req-2", "get-item", "#1", null)
                .assertContextInternalAction(2, "action2", "x=99")
                .assertContextInternalActionDuration(2, "action2");


        action1.close();
        logReader.nextRecord()
                .assertRecordFromSameThread(session1.getTraceId(), Level.INFO, MultiSpanTest.class, "SPAN FINISH", "Span \"ctx.internal-action\" completed successfully within [0-9]+ ms")
                .assertThrownIsNull()
                .assertContextSession("session-1", started1, "ivanov_ii", SessionInfo.Role.USER)
                .assertContextUserAction("req-2", "get-item", "#1", null)
                .assertContextInternalAction(1, "action1", "x=42")
                .assertContextInternalActionDuration(1, "action1");

        user1.close();
        logReader.nextRecord()
                .assertRecordFromSameThread(session1.getTraceId(), Level.INFO, MultiSpanTest.class, "SPAN FINISH", "Span \"ctx.user-action\" completed successfully within [0-9]+ ms")
                .assertThrownIsNull()
                .assertContextSession("session-1", started1, "ivanov_ii", SessionInfo.Role.USER)
                .assertContextUserAction("req-2", "get-item", "#1", null)
                .assertContextUserActionDuration();

        session1.close();
        logReader.nextRecord()
                .assertTraceIdIsNotNull()
                .assertRecordFromSameThread(session1.getTraceId(), Level.INFO, MultiSpanTest.class, "SPAN FINISH", "Span \"ctx.session\" completed successfully within [0-9]+ ms")
                .assertThrownIsNull()
                .assertContextSession("session-1", started1, "ivanov_ii", SessionInfo.Role.USER)
                .assertContextSessionDuration();

        assertNull(tracer.scopeManager().getActiveTraceId(), "Invalid active traceId");
        assertFalse(logReader.hasNext());
    }


    private static LogSpan activateSessionSpan(final String context, final String sid, final Date started, final String principal, final SessionInfo.Role role) {
        return tracer.buildSpan("session")
                .withNamespace(context)
                .withTag("sid", sid)
                .withTag("started", started)
                .withTag("principal", principal)
                .withTag("role", role)
                .withLogger(log)
                .activate();
    }

    private static LogSpan activateUserActionSpan(final String context, final String uid, final String name, final String arg1, final String arg2) {
        return tracer.buildSpan("user-action")
                .withNamespace(context)
                .withTag("uid", uid)
                .withTag("name", name)
                .withTag("arg1", arg1)
                .withTag("arg2", arg2)
                .withLogger(log)
                .activate();
    }

    private static LogSpan activateInternalAction(final String context, final String name, final String args) {
        return tracer.buildSpan("internal-action")
                .withNamespace(context)
                .withTag("name", name)
                .withTag("args", args)
                .withLogger(log)
                .activate();
    }
}
