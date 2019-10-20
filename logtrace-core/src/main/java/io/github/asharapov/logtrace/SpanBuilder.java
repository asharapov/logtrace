package io.github.asharapov.logtrace;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

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

    SpanBuilder withEventFilter(EventFilter predicate);

    SpanBuilder withEventName(String name);

    SpanBuilder withNamespace(String ns);

    SpanBuilder withOperation(String operation);

    SpanBuilder withTag(String key, Enum<?> value);

    SpanBuilder withTag(String key, String value);

    SpanBuilder withTag(String key, Boolean value);

    SpanBuilder withTag(String key, Integer value);

    SpanBuilder withTag(String key, Long value);

    SpanBuilder withTag(String key, Double value);

    SpanBuilder withTag(String key, Float value);

    SpanBuilder withTag(String key, BigDecimal value);

    SpanBuilder withTag(String key, BigInteger value);

    SpanBuilder withTag(String key, Date value);

    LogSpan activate();
}
