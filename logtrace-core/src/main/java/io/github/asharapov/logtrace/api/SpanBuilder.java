package io.github.asharapov.logtrace.api;

import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;

/**
 * @author Anton Sharapov
 */
public interface SpanBuilder {

    LogTracer getTracer();

    SpanBuilder asPartOf(String traceId);

    SpanBuilder withLogger(String category);

    SpanBuilder withLogger(Class category);

    SpanBuilder withLogger(Logger logger);

    SpanBuilder withoutEvents();

    SpanBuilder withClosingEvent();

    SpanBuilder withEventFilter(EventFilter predicate);

    SpanBuilder withEventName(String name);

    SpanBuilder withNamespace(String ns);

    SpanBuilder withOperation(String operation);

    SpanBuilder withTag(String key, Enum<?> value);

    SpanBuilder withTag(String key, String value);

    SpanBuilder withTag(String key, Boolean value);

    SpanBuilder withTag(String key, Number value);

    SpanBuilder withTag(String key, Date value);

    SpanBuilder withTag(String key, TemporalAccessor value);

    SpanBuilder withTags(final Map<String, ?> attrs);

    LogSpan activate();
}
