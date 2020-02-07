package io.github.asharapov.logtrace.tests.model;

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.asharapov.logtrace.tests.LogReader;
import io.github.asharapov.logtrace.api.LogTracer;
import org.slf4j.event.Level;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anton Sharapov
 */
public class LogRecord {

    @JsonProperty(required = true, value = "@timestamp")
    public Date timestamp;
    @JsonProperty(required = false, value = "@trace")
    public String traceId;
    @JsonProperty(required = true, value = "thread")
    public String thread;
    @JsonProperty(required = true, value = "lvl")
    public Level level;
    @JsonProperty(required = false, value = "logger")
    public String logger;
    @JsonProperty(required = false, value = "msg")
    public String message;
    @JsonProperty(required = false, value = "marker")
    public String marker;
    @JsonProperty(required = false, value = "mdc")
    public Map<String, String> mdc = Collections.emptyMap();
    @JsonProperty("thrown")
    public ThrownInfo thrown;

    @JsonProperty("ctx")
    public Context ctx;
    @JsonProperty("altctx")
    public Context altctx;

    public EnumSet<LogReader.Capabilities> capabilities;

    public class Context {
        @JsonProperty("session")
        public SessionInfo session;
        @JsonProperty("user-action")
        public UserAction userAction;
        @JsonProperty("internal-action")
        public List<InternalAction> actions;
    }


    public LogRecord assertTraceIdIsNull() {
        assertNull(traceId, "'@trace' must be null");
        return this;
    }

    public LogRecord assertTraceIdIsNotNull() {
        assertNotNull(traceId, "'@trace' must be specified");
        return this;
    }

    public LogRecord assertTraceId(final String expectedTraceId) {
        assertEquals(expectedTraceId, this.traceId, "Invalid '@trace' value");
        return this;
    }

    public LogRecord assertRecordFromSameThread(String expectedTraceId, Level expectedLevel, Class expectedLogger, String expectedMarker, Pattern expectedMsgPattern) {
        return assertRecordFromSameThread(expectedTraceId, expectedLevel, expectedLogger.getName(), expectedMarker, expectedMsgPattern);
    }

    public LogRecord assertRecordFromSameThread(String expectedTraceId, Level expectedLevel, Class expectedLogger, String expectedMarker, String expectedMsg) {
        return assertRecordFromSameThread(expectedTraceId, expectedLevel, expectedLogger.getName(), expectedMarker, Pattern.compile(expectedMsg));
    }

    public LogRecord assertRecordFromSameThread(String expectedTraceId, Level expectedLevel, String expectedLogger, String expectedMarker, String expectedMsg) {
        return assertRecordFromSameThread(expectedTraceId, expectedLevel, expectedLogger, expectedMarker, Pattern.compile(expectedMsg));
    }

    public LogRecord assertRecordFromSameThread(String expectedTraceId, Level expectedLevel, String expectedLogger, String expectedMarker, Pattern expectedMsgPattern) {
        assertEquals(expectedTraceId, this.traceId, "Invalid '@trace' value");
        final String expectedThread = Thread.currentThread().getName();
        assertEquals(expectedThread, this.thread, "Invalid 'thread' value");

        assertEquals(expectedLogger, this.logger, "Invalid 'logger' value");
        assertEquals(expectedLevel, this.level, "Invalid 'lvl' value");

        final boolean strictMarkerChecks = capabilities == null || capabilities.contains(LogReader.Capabilities.MARKER_SUPPORT);
        if (strictMarkerChecks || this.marker != null) {
            assertEquals(expectedMarker, this.marker, "Invalid 'marker' value");
        }
        assertTrue(expectedMsgPattern.matcher(this.message).matches(), "Invalid 'msg' value (not matches pattern)");

        return this;
    }

    public LogRecord assertRecordFromSameThread(Level expectedLevel, Class expectedLogger, String expectedMarker, Pattern expectedMsgPattern) {
        final String expectedTraceId = LogTracer.getDefault().scopeManager().getActiveTraceId();
        return assertRecordFromSameThread(expectedTraceId, expectedLevel, expectedLogger.getName(), expectedMarker, expectedMsgPattern);
    }

    public LogRecord assertRecordFromSameThread(Level expectedLevel, String expectedLogger, String expectedMarker, Pattern expectedMsgPattern) {
        final String expectedTraceId = LogTracer.getDefault().scopeManager().getActiveTraceId();
        return assertRecordFromSameThread(expectedTraceId, expectedLevel, expectedLogger, expectedMarker, expectedMsgPattern);
    }

    public LogRecord assertRecordFromSameThread(Level expectedLevel, Class expectedLogger, String expectedMarker, String expectedMessage) {
        return assertRecordFromSameThread(expectedLevel, expectedLogger.getName(), expectedMarker, expectedMessage);
    }

    public LogRecord assertRecordFromSameThread(Level expectedLevel, String expectedLogger, String expectedMarker, String expectedMessage) {
        final String expectedTraceId = LogTracer.getDefault().scopeManager().getActiveTraceId();
        assertEquals(expectedTraceId, this.traceId, "Invalid '@trace' value");
        final String expectedThread = Thread.currentThread().getName();
        assertEquals(expectedThread, this.thread, "Invalid 'thread' value");

        assertEquals(expectedLogger, this.logger, "Invalid 'logger' value");
        assertEquals(expectedLevel, this.level, "Invalid 'lvl' value");

        final boolean strictMarkerChecks = capabilities == null || capabilities.contains(LogReader.Capabilities.MARKER_SUPPORT);
        if (strictMarkerChecks || this.marker != null) {
            assertEquals(expectedMarker, this.marker, "Invalid 'marker' value");
        }

        assertEquals(expectedMessage != null ? expectedMessage : "", this.message != null ? this.message : "", "Invalid 'msg' value");

        return this;
    }

    public LogRecord assertMDC(final String... expectedElements) {
        if (expectedElements == null || expectedElements.length == 0) {
            assertTrue(this.mdc == null || this.mdc.isEmpty(), "'mdc' must be empty");
        } else {
            if (expectedElements.length % 2 > 0)
                throw new IllegalArgumentException("Incorrect expected mdc elements count");
            final Map<String, String> expected = new HashMap<>();
            for (int i = 0; i < expectedElements.length; i += 2) {
                expected.put(expectedElements[i], expectedElements[i + 1]);
            }
            final boolean strictMDCChecks = capabilities == null || capabilities.contains(LogReader.Capabilities.MDC_SUPPORT);
            if (strictMDCChecks || !this.mdc.isEmpty()) {
                assertEquals(expected.size(), this.mdc.size(), "Invalid 'mdc' value");
                assertEquals(expected, this.mdc, "Invalid 'mdc' value");
            }
        }
        return this;
    }

    public LogRecord assertThrownIsNull() {
        assertNull(this.thrown, "'thrown' must be empty");
        return this;
    }

    public LogRecord assertThrown(final Class<? extends Throwable> throwableCls, final String expectedMessage) {
        assertNotNull(this.thrown, "'thrown' must be specified");
        assertEquals(throwableCls.getName(), this.thrown.cls, "Invalid 'thrown.cls' value");
        assertEquals(expectedMessage != null ? expectedMessage : "", this.thrown.message != null ? this.thrown.message : "", "Invalid 'thrown.msg' value");
        assertNotNull(this.thrown.stack, "Invalid 'thrown.stack' value");
        assertNotNull(this.thrown.hash, "Invalid 'thrown.hash' value");
        return this;
    }


    public LogRecord assertContextIsNull() {
        assertNull(this.ctx, "'ctx' must be empty");
        return this;
    }

    public LogRecord assertAltContextIsNull() {
        assertNull(this.altctx, "'altctx' must be empty");
        return this;
    }

    public LogRecord assertContextSessionIsNull() {
        assertNotNull(this.ctx, "'ctx' must be specified");
        assertNull(this.ctx.session, "'ctx.session' must be empty");
        return this;
    }

    public LogRecord assertContextSession(String expectedSid, Date expectedSessionStartTime, String expectedPrincipal, SessionInfo.Role expectedRole) {
        assertNotNull(this.ctx, "'ctx' must be specified");
        assertNotNull(this.ctx.session, "'ctx.session' must be specified");
        assertEquals(expectedSid, this.ctx.session.sessionId, "Invalid 'ctx.session.sid' value");
        assertEquals(expectedSessionStartTime, this.ctx.session.started, "Invalid 'ctx.session.started' value");
        assertEquals(expectedPrincipal, this.ctx.session.userName, "Invalid 'ctx.session.principal' value");
        assertEquals(expectedRole, this.ctx.session.role, "Invalid 'ctx.session.role' value");
        return this;
    }

    public LogRecord assertContextSessionDuration() {
        assertNotNull(this.ctx, "'ctx' must be specified");
        assertNotNull(this.ctx.session, "'ctx.session' must be specified");
        assertNotNull(this.ctx.session.duration, "Invalid 'ctx.session.@time' value");
        assertTrue(this.ctx.session.duration >= 0, "Invalid 'ctx.session.@time' value");
        return this;
    }


    public LogRecord assertAltContextSessionIsNull() {
        assertNotNull(this.altctx, "'altctx' must be specified");
        assertNull(this.altctx.session, "'altctx.session' must be empty");
        return this;
    }

    public LogRecord assertAltContextSession(String expectedSid, Date expectedSessionStartTime, String expectedPrincipal, SessionInfo.Role expectedRole) {
        assertNotNull(this.altctx, "'altctx' must be specified");
        assertNotNull(this.altctx.session, "'altctx.session' must be specified");
        assertEquals(expectedSid, this.altctx.session.sessionId, "Invalid 'altctx.session.sid' value");
        assertEquals(expectedSessionStartTime, this.altctx.session.started, "Invalid 'altctx.session.started' value");
        assertEquals(expectedPrincipal, this.altctx.session.userName, "Invalid 'altctx.session.principal' value");
        assertEquals(expectedRole, this.altctx.session.role, "Invalid 'altctx.session.role' value");
        return this;
    }

    public LogRecord assertAltContextSessionDuration() {
        assertNotNull(this.altctx, "'ctx' must be specified");
        assertNotNull(this.altctx.session, "'altctx.session' must be specified");
        assertNotNull(this.altctx.session.duration, "Invalid 'altctx.session.@time' value");
        assertTrue(this.altctx.session.duration >= 0, "Invalid 'altctx.session.@time' value");
        return this;
    }


    public LogRecord assertContextUserActionIsNull() {
        assertNotNull(this.ctx, "'ctx' must be specified");
        assertNull(this.ctx.userAction, "'ctx.user-action' must be empty");
        return this;
    }

    public LogRecord assertContextUserAction(final String expectedUID, final String expectedName, final String expectedArg1, final String expectedArg2) {
        assertNotNull(this.ctx, "'ctx' must be specified");
        assertNotNull(this.ctx.userAction, "'ctx.user-action' must be specified");
        assertEquals(expectedUID, this.ctx.userAction.uid, "Invalid 'ctx.user-action.uid' value");
        assertEquals(expectedName, this.ctx.userAction.name, "Invalid 'ctx.user-action.name' value");
        assertEquals(expectedArg1, this.ctx.userAction.arg1, "Invalid 'ctx.user-action.arg1' value");
        assertEquals(expectedArg2, this.ctx.userAction.arg2, "Invalid 'ctx.user-action.arg2' value");
        return this;
    }

    public LogRecord assertContextUserActionDuration() {
        assertNotNull(this.ctx, "'ctx' must be specified");
        assertNotNull(this.ctx.userAction, "'ctx.user-action' must be specified");
        assertNotNull(this.ctx.userAction.duration, "Invalid 'ctx.user-action.@time' value");
        assertTrue(this.ctx.userAction.duration >= 0, "Invalid 'ctx.user-action.@time' value");
        return this;
    }


    public LogRecord assertContextInternalActionIsNull() {
        assertNotNull(this.ctx, "'ctx' must be specified");
        assertTrue(this.ctx.actions == null || this.ctx.actions.isEmpty(), "'ctx.internal-action' must be empty");
        return this;
    }

    public LogRecord assertContextInternalAction(final int expectedActionsCount, final String expectedName, final String expectedArgs) {
        assertNotNull(this.ctx, "'ctx' must be specified");
        assertNotNull(this.ctx.actions, "'ctx.internal-action' must be specified");
        assertTrue(this.ctx.actions.size() > 0, "'ctx.internal-action' must be specified");
        assertEquals(expectedActionsCount, this.ctx.actions.size(), "'ctx.internal-action' must be specified");
        for (InternalAction action : this.ctx.actions) {
            if (Objects.equals(expectedName, action.name)) {
                assertEquals(expectedArgs, action.args, "Invalid 'ctx.internal-action.args' value");
                return this;
            }
        }
        fail("Can't resolve expected 'ctx.internal-action' value");
        return this;
    }

    public LogRecord assertContextInternalActionDuration(final int expectedActionsCount, final String expectedName) {
        assertNotNull(this.ctx, "'ctx' must be specified");
        assertNotNull(this.ctx.actions, "'ctx.internal-action' must be specified");
        assertTrue(this.ctx.actions.size() > 0, "'ctx.internal-action' must be specified");
        assertEquals(expectedActionsCount, this.ctx.actions.size(), "'ctx.internal-action' must be specified");
        for (InternalAction action : this.ctx.actions) {
            if (Objects.equals(expectedName, action.name)) {
                assertNotNull(action.duration, "Invalid 'ctx.internal-action.@time' value");
                assertTrue(action.duration >= 0, "Invalid 'ctx.internal-action.@time' value");
                return this;
            }
        }
        fail("Can't resolve expected 'ctx.internal-action' value");
        return this;
    }
}
