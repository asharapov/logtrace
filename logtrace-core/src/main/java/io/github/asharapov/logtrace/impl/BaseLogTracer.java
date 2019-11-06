package io.github.asharapov.logtrace.impl;

import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.github.asharapov.logtrace.EventFilter;
import io.github.asharapov.logtrace.LogSpan;
import io.github.asharapov.logtrace.LogTracer;
import io.github.asharapov.logtrace.ScopeManager;
import io.github.asharapov.logtrace.SpanBuilder;
import io.github.asharapov.logtrace.Tag;
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

        public SpanBuilder withoutEvents() {
            this.predicate = (s -> false);
            return this;
        }

        public SpanBuilder withClosingEvent() {
            this.predicate = (s -> !s.isActive());
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
        public SpanBuilder withTag(final String key, final Number value) {
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
        public SpanBuilder withTag(final String key, final TemporalAccessor value) {
            final Tag tag = new Tag(key, value);
            tags.put(key, tag);
            return this;
        }

        public SpanBuilder withTags(final Map<String, ?> attrs) {
            if (attrs == null || attrs.isEmpty())
                return this;

            for (Map.Entry<String, ?> entry : attrs.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                if (key == null || value == null)
                    continue;

                final Tag tag;
                if (value instanceof CharSequence) {
                    tag = new Tag(key, value.toString());
                } else if (value instanceof Enum) {
                    tag = new Tag(key, value.toString());
                } else if (value instanceof Boolean) {
                    tag = new Tag(key, (Boolean) value);
                } else if (value instanceof Number) {
                    tag = new Tag(key, (Number) value);
                } else if (value instanceof Date) {
                    tag = new Tag(key, (Date) value);
                } else if (value instanceof TemporalAccessor) {
                    tag = new Tag(key, (TemporalAccessor) value);
                } else
                    continue;
                tags.put(key, tag);
            }

            return this;
        }

        @Override
        public LogSpan activate() {
            return new BaseLogSpan(mgr, traceId, eventName, namespace, operation, tags.values(), logger, predicate);
        }

    }
}
