package io.github.asharapov.logtrace.logback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.EncoderBase;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import io.github.asharapov.logtrace.LogSpan;
import io.github.asharapov.logtrace.LogTracer;
import io.github.asharapov.logtrace.ScopeManager;
import io.github.asharapov.logtrace.impl.EncoderUtils;
import org.slf4j.Marker;

/**
 * @author Anton Sharapov
 */
public class LogTraceJsonEncoder<E extends ILoggingEvent> extends EncoderBase<E> {

    private static final byte[] DEFAULT_EOL = EncoderUtils.DEFAULT_EOL.getBytes(StandardCharsets.UTF_8);

    private final ScopeManager scopeManager;
    private final JsonFactory jsonFactory;
    private boolean formatted;

    public LogTraceJsonEncoder() {
        this.scopeManager = LogTracer.getDefault().scopeManager();
        this.jsonFactory = new JsonFactory();
        this.jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
    }

    @Override
    public void start() {
        super.start();
    }

    /**
     * Определяет следует ли форматировать записи или вести их в компактной форме.
     */
    public boolean getFormatted() {
        return formatted;
    }

    /**
     * Определяет следует ли форматировать записи или вести их в компактной форме.
     */
    public void setFormatted(final boolean flag) {
        formatted = flag;
    }


    @Override
    public byte[] headerBytes() {
        return null;
    }

    @Override
    public byte[] footerBytes() {
        return null;
    }

    @Override
    public byte[] encode(final E event) {
        final EncoderUtils.State encstate = EncoderUtils.getState();
        final ByteArrayBuilder out = new ByteArrayBuilder(encstate.getBufferRecycler());
        try (JsonGenerator jg = jsonFactory.createGenerator(out)) {
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
            jg.writeStringField("msg", event.getFormattedMessage());
            writeMarker(jg, event.getMarker());
            writeThrowable(encstate, jg, event.getThrowableProxy());
            encstate.writeMessageContext(jg, event.getMDCPropertyMap());
            encstate.writeSpanContext(jg, activeSpan);

            jg.writeEndObject();
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            return null;
        }
        out.write(DEFAULT_EOL);
        out.flush();
        return out.toByteArray();
    }

    private void writeMarker(final JsonGenerator jg, final Marker marker) throws IOException {
        if (marker != null) {
            final String text = marker.hasReferences()
                    ? marker.iterator().next().getName() + ' ' + marker.getName()
                    : marker.getName();
            jg.writeStringField("marker", text);
        }
    }

    private void writeThrowable(final EncoderUtils.State encstate, final JsonGenerator jg, final IThrowableProxy thrown) throws Throwable {
        if (thrown != null) {
            final StringBuilder buf = new StringBuilder(400);
            for (StackTraceElementProxy step : thrown.getStackTraceElementProxyArray()) {
                buf.append(CoreConstants.TAB);
                buf.append(step.toString());
                buf.append(EncoderUtils.DEFAULT_EOL);
            }
            final String stack = buf.toString();

            final IThrowableProxy rootCause = getRootThrowableProxy(thrown);
            final String rootCauseStack;
            if (rootCause == thrown) {
                rootCauseStack = stack;
            } else {
                buf.setLength(0);
                for (StackTraceElementProxy step : rootCause.getStackTraceElementProxyArray()) {
                    buf.append(CoreConstants.TAB);
                    buf.append(step.toString());
                    buf.append(EncoderUtils.DEFAULT_EOL);
                }
                rootCauseStack = buf.toString();
            }

            final char[] hash = encstate.digest(rootCauseStack);
            jg.writeObjectFieldStart("thrown");
            jg.writeStringField("cls", thrown.getClassName());
            jg.writeStringField("msg", thrown.getMessage());
            jg.writeStringField("stack", stack);
            jg.writeFieldName("hash");
            jg.writeString(hash, 0, hash.length);
            jg.writeEndObject();
        }
    }

    private IThrowableProxy getRootThrowableProxy(final IThrowableProxy thrown) {
        IThrowableProxy result = thrown;
        while (true) {
            final IThrowableProxy parent = result.getCause();
            if (parent != null) {
                result = parent;
            } else {
                break;
            }
        }
        return result;
    }

}
