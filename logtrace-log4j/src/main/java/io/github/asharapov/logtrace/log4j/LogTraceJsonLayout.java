package io.github.asharapov.logtrace.log4j;

import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import io.github.asharapov.logtrace.LogSpan;
import io.github.asharapov.logtrace.LogTracer;
import io.github.asharapov.logtrace.ScopeManager;
import io.github.asharapov.logtrace.impl.EncoderUtils;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * @author Anton Sharapov
 */
public class LogTraceJsonLayout extends Layout {

    private final ScopeManager scopeManager;
    private final JsonFactory factory;
    private boolean formatted;

    public LogTraceJsonLayout() {
        super();
        this.scopeManager = LogTracer.getDefault().scopeManager();
        this.factory = new JsonFactory();
        this.factory.enable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        this.formatted = false;
    }

    public boolean isFormatted() {
        return formatted;
    }

    public void setFormatted(final boolean formatted) {
        this.formatted = formatted;
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    @Override
    public void activateOptions() {
    }

    @Override
    public String format(final LoggingEvent event) {
        final EncoderUtils.State encstate = EncoderUtils.getState();
        final SegmentedStringWriter out = new SegmentedStringWriter(encstate.getBufferRecycler());
        try (JsonGenerator jg = factory.createGenerator(out)) {
            if (formatted) {
                jg.useDefaultPrettyPrinter();
            }
            final LogSpan activeSpan = scopeManager.getActive();
            jg.writeStartObject();
            jg.writeStringField("@timestamp", encstate.formatISODateTime(event.getTimeStamp()));
            if (activeSpan != null) {
                jg.writeStringField("@trace", activeSpan.getTraceId());
            }
            jg.writeStringField("thread", event.getThreadName());
            jg.writeStringField("lvl", event.getLevel().toString());
            jg.writeStringField("logger", event.getLoggerName());
            jg.writeStringField("msg", event.getRenderedMessage());
            final ThrowableInformation ti = event.getThrowableInformation();
            if (ti != null) {
                encstate.writeThrowable(jg, ti.getThrowable());
            }
            @SuppressWarnings("unchecked") final Map<String, ?> mdc = event.getProperties();
            encstate.writeMessageContext(jg, mdc);
            encstate.writeSpanContext(jg, activeSpan);

            jg.writeEndObject();
        } catch (Throwable e) {
            // Should this be an ISE or IAE?
            LogLog.warn(e.getMessage(), e);
            return "";
        }
        out.write(EncoderUtils.DEFAULT_EOL);
        out.flush();
        return out.getAndClear();
    }

}
