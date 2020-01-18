package io.github.asharapov.logtrace.log4j2;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.impl.MutableLogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.FlowMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.Strings;
import org.apache.logging.log4j.util.TriConsumer;
import io.github.asharapov.logtrace.LogSpan;
import io.github.asharapov.logtrace.LogTracer;
import io.github.asharapov.logtrace.impl.EncoderUtils;

/**
 * @author Anton Sharapov
 */
@Plugin(name = "LogTraceJsonLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class LogTraceJsonLayout extends AbstractStringLayout {

    private static final String DEFAULT_HEADER = null;
    private static final String DEFAULT_FOOTER = null;

    public static class Builder<B extends Builder<B>> extends AbstractStringLayout.Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<LogTraceJsonLayout> {

        @PluginBuilderAttribute
        private boolean formatted;

        public Builder() {
            super();
            setCharset(StandardCharsets.UTF_8);
        }

        public boolean isFormatted() {
            return formatted;
        }

        public B setFormatted(final boolean formatted) {
            this.formatted = formatted;
            return asBuilder();
        }

        @Override
        public LogTraceJsonLayout build() {
            final Configuration cfg = getConfiguration();
            final String headerPattern = toStringOrNull(getHeader());
            final String footerPattern = toStringOrNull(getFooter());
            return new LogTraceJsonLayout(
                    cfg,
                    getCharset(),
                    PatternLayout.newSerializerBuilder().setConfiguration(cfg).setPattern(headerPattern).setDefaultPattern(DEFAULT_HEADER).build(),
                    PatternLayout.newSerializerBuilder().setConfiguration(cfg).setPattern(footerPattern).setDefaultPattern(DEFAULT_FOOTER).build(),
                    isFormatted()
            );
        }

        private String toStringOrNull(final byte[] header) {
            return header == null ? null : new String(header, Charset.defaultCharset());
        }
    }


    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return new Builder<B>().asBuilder();
    }

    private static LogEvent convertMutableToLog4jEvent(final LogEvent event) {
        // TODO Jackson-based layouts have certain filters set up for Log4jLogEvent.
        // TODO Need to set up the same filters for MutableLogEvent but don't know how...
        // This is a workaround.
        return event instanceof MutableLogEvent
                ? ((MutableLogEvent) event).createMemento()
                : event;
    }

    private static final TriConsumer<String, Object, JsonGenerator> WRITE_STRING_FIELD_INTO =
            new TriConsumer<String, Object, JsonGenerator>() {
                @Override
                public void accept(final String key, final Object value, final JsonGenerator jg) {
                    try {
                        if (value != null)
                            jg.writeStringField(key, value.toString());
                    } catch (final Exception ex) {
                        throw new IllegalStateException("Problem with key " + key, ex);
                    }
                }
            };

    private final boolean formatted;
    private final JsonFactory factory;

    public LogTraceJsonLayout(final Configuration config, final Charset charset, final Serializer headerSerializer, final Serializer footerSerializer,
                              final boolean formatted) {
        super(config, charset, headerSerializer, footerSerializer);
        this.formatted = formatted;
        this.factory = new JsonFactory();
        this.factory.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
    }

    public boolean isFormatted() {
        return formatted;
    }

    @Override
    public String toSerializable(LogEvent event) {
        event = convertMutableToLog4jEvent(event);
        final EncoderUtils.State encstate = EncoderUtils.getState();
        final SegmentedStringWriter out = new SegmentedStringWriter(encstate.getBufferRecycler());
        try (JsonGenerator jg = factory.createGenerator(out)) {
            if (formatted) {
                jg.useDefaultPrettyPrinter();
            }
            final LogSpan activeSpan = getActiveSpan(event.getMessage());
            jg.writeStartObject();
            jg.writeStringField("@timestamp", encstate.formatISODateTime(event.getTimeMillis()));
            if (activeSpan != null) {
                jg.writeStringField("@trace", activeSpan.getTraceId());
            }
            jg.writeStringField("thread", event.getThreadName());
            jg.writeStringField("lvl", event.getLevel().toString());
            jg.writeStringField("logger", event.getLoggerName());
            jg.writeStringField("msg", event.getMessage().getFormattedMessage());
            writeMarker(jg, event.getMarker());
            encstate.writeThrowable(jg, event.getThrown());
            writeMessageContext(jg, event.getContextData());
            encstate.writeSpanContext(jg, activeSpan);

            jg.writeEndObject();
        } catch (Throwable e) {
            // Should this be an ISE or IAE?
            LOGGER.error(e);
            return Strings.EMPTY;
        }
        out.write(EncoderUtils.DEFAULT_EOL);
        out.flush();
        markEvent();
        return out.getAndClear();
    }

    private LogSpan getActiveSpan(Message msg) {
        if (msg instanceof FlowMessage) {
            msg = ((FlowMessage) msg).getMessage();
        }
        if (msg instanceof LogTraceAwareMessage) {
            return ((LogTraceAwareMessage) msg).getActiveSpan();
        } else {
            // Внимание! Асинхронная обработка данных типов сообщений может оказаться не совместимой с используемым механизом привязки к глобальным операциям.
            return LogTracer.getDefault().scopeManager().getActive();
        }
    }

    private void writeMarker(final JsonGenerator jg, final Marker marker) throws IOException {
        if (marker != null) {
            final Marker[] mp = marker.getParents();
            jg.writeStringField("marker", mp != null && mp.length == 1 ? mp[0].getName() + ' ' + marker.getName() : marker.getName());
        }
    }

    private void writeMessageContext(final JsonGenerator jg, final ReadOnlyStringMap mdc) throws IOException {
        if (!mdc.isEmpty()) {
            jg.writeObjectFieldStart("mdc");
            mdc.forEach(WRITE_STRING_FIELD_INTO, jg);
            jg.writeEndObject();
        }
    }

}
