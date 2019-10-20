package io.github.asharapov.logtrace.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.github.asharapov.logtrace.EventFilter;
import io.github.asharapov.logtrace.LogSpan;
import io.github.asharapov.logtrace.LogTracer;
import io.github.asharapov.logtrace.ScopeManager;
import io.github.asharapov.logtrace.Tag;
import io.github.asharapov.logtrace.SpanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anton Sharapov
 */
public class BaseLogTracer implements LogTracer {

    private final ScopeManager mgr;

    public BaseLogTracer() {
        this.mgr = new ThreadLocalScopeManager();
    }

    @Override
    public ScopeManager scopeManager() {
        return mgr;
    }

    @Override
    public SpanBuilder buildSpan(final String operation) {
        return new SpanBuilderImpl(operation);
    }


    class SpanBuilderImpl implements SpanBuilder {
        private String traceId;
        private Logger logger;
        private EventFilter predicate;
        private String eventName;
        private String namespace;
        private String operation;
        private final Map<String, Tag> tags;

        private SpanBuilderImpl(final String operation) {
            this.operation = operation;
            this.tags = new HashMap<>();
        }

        @Override
        public LogTracer getTracer() {
            return BaseLogTracer.this;
        }

        @Override
        public SpanBuilder asPartOf(final String traceId) {
            this.traceId = traceId;
            return this;
        }

        @Override
        public SpanBuilder withLogger(final String category) {
            if (category != null) {
                this.logger = LoggerFactory.getLogger(category);
            }
            return this;
        }

        @Override
        public SpanBuilder withLogger(final Class cls) {
            if (cls != null) {
                this.logger = LoggerFactory.getLogger(cls);
            }
            return this;
        }

        @Override
        public SpanBuilder withLogger(final Logger logger) {
            if (logger != null) {
                this.logger = logger;
            }
            return this;
        }

        @Override
        public SpanBuilder withEventFilter(final EventFilter predicate) {
            this.predicate = predicate;
            return this;
        }

        @Override
        public SpanBuilder withEventName(final String name) {
            this.eventName = name;
            return this;
        }

        @Override
        public SpanBuilder withNamespace(final String namespace) {
            this.namespace = namespace;
            return this;
        }

        @Override
        public SpanBuilder withOperation(final String operation) {
            this.operation = operation;
            return this;
        }

        @Override
        public SpanBuilder withTag(final String key, final Enum<?> value) {
            final Tag tag = new Tag(key, value != null ? value.toString() : null);
            tags.put(key, tag);
            return this;
        }

        @Override
        public SpanBuilder withTag(final String key, final String value) {
            final Tag tag = new Tag(key, value);
            tags.put(key, tag);
            return this;
        }

        @Override
        public SpanBuilder withTag(final String key, final Boolean value) {
            final Tag tag = new Tag(key, value);
            tags.put(key, tag);
            return this;
        }

        @Override
        public SpanBuilder withTag(final String key, final Integer value) {
            final Tag tag = new Tag(key, value);
            tags.put(key, tag);
            return this;
        }

        @Override
        public SpanBuilder withTag(final String key, final Long value) {
            final Tag tag = new Tag(key, value);
            tags.put(key, tag);
            return this;
        }

        @Override
        public SpanBuilder withTag(final String key, final Double value) {
            final Tag tag = new Tag(key, value);
            tags.put(key, tag);
            return this;
        }

        @Override
        public SpanBuilder withTag(final String key, final Float value) {
            final Tag tag = new Tag(key, value);
            tags.put(key, tag);
            return this;
        }

        @Override
        public SpanBuilder withTag(final String key, final BigDecimal value) {
            final Tag tag = new Tag(key, value);
            tags.put(key, tag);
            return this;
        }

        @Override
        public SpanBuilder withTag(final String key, final BigInteger value) {
            final Tag tag = new Tag(key, value);
            tags.put(key, tag);
            return this;
        }

        @Override
        public SpanBuilder withTag(final String key, final Date value) {
            final Tag tag = new Tag(key, value);
            tags.put(key, tag);
            return this;
        }

        @Override
        public LogSpan activate() {
            return new BaseLogSpan(mgr, traceId, eventName, namespace, operation, tags.values(), logger, predicate);
        }

    }
}
