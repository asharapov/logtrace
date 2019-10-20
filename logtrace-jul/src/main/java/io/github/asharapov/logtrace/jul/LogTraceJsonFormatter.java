package io.github.asharapov.logtrace.jul;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import io.github.asharapov.logtrace.LogSpan;
import io.github.asharapov.logtrace.LogTracer;
import io.github.asharapov.logtrace.ScopeManager;
import io.github.asharapov.logtrace.impl.EncoderUtils;

public class LogTraceJsonFormatter extends Formatter {

    private final boolean formatted;
    private final ScopeManager scopeManager;
    private final JsonFactory jsonFactory;

    public LogTraceJsonFormatter() {
        this(false);
    }

    public LogTraceJsonFormatter(final boolean formatted) {
        this.formatted = formatted;
        this.scopeManager = LogTracer.getDefault().scopeManager();
        this.jsonFactory = new JsonFactory();
        this.jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
    }

    public boolean isFormatted() {
        return formatted;
    }

    @Override
    public String format(final LogRecord event) {
        final EncoderUtils.State encstate = EncoderUtils.getState();
        final SegmentedStringWriter out = new SegmentedStringWriter(encstate.getBufferRecycler());
        try (JsonGenerator jg = jsonFactory.createGenerator(out)) {
            if (formatted) {
                jg.useDefaultPrettyPrinter();
            }
            final LogSpan activeSpan = scopeManager.getActive();

            jg.writeStartObject();
            jg.writeStringField("@timestamp", encstate.formatISODateTime(event.getMillis()));
            if (activeSpan != null) {
                jg.writeStringField("@trace", activeSpan.getTraceId());
            }
//            jg.writeStringField("thread", String.valueOf(event.getThreadID()));
            jg.writeStringField("thread", Thread.currentThread().getName());
            jg.writeStringField("lvl", mapLevel(event.getLevel()));
            jg.writeStringField("logger", event.getLoggerName());
            jg.writeStringField("msg", event.getMessage());
            encstate.writeThrowable(jg, event.getThrown());
            encstate.writeMessageContext(jg, org.slf4j.MDC.getCopyOfContextMap());
            encstate.writeSpanContext(jg, activeSpan);

            jg.writeEndObject();
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            return null;
        }
        out.write(EncoderUtils.DEFAULT_EOL);
        out.flush();
        return out.getAndClear();
    }

    private String mapLevel(final Level level) {
        final int lvl = level.intValue();
        switch (lvl) {
            case 1000:
                return org.slf4j.event.Level.ERROR.name();
            case 900:
                return org.slf4j.event.Level.WARN.name();
            case 800:
                return org.slf4j.event.Level.INFO.name();
            case 700:
            case 600:
                return org.slf4j.event.Level.DEBUG.name();
            case 300:
                return org.slf4j.event.Level.TRACE.name();
            default: {
                if (lvl >= Level.SEVERE.intValue())
                    return org.slf4j.event.Level.ERROR.name();
                if (lvl >= Level.WARNING.intValue())
                    return org.slf4j.event.Level.WARN.name();
                if (lvl >= Level.INFO.intValue())
                    return org.slf4j.event.Level.WARN.name();
                if (lvl >= Level.FINEST.intValue())
                    return org.slf4j.event.Level.DEBUG.name();
                return org.slf4j.event.Level.TRACE.name();
            }
        }
    }

}