package io.github.asharapov.logtrace.impl;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import io.github.asharapov.logtrace.DefaultConfiguration;
import io.github.asharapov.logtrace.EventFilter;
import io.github.asharapov.logtrace.LogSpan;
import io.github.asharapov.logtrace.ScopeManager;
import io.github.asharapov.logtrace.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import static io.github.asharapov.logtrace.impl.Utils.checkRequiredJsonAttr;

/**
 * @author Anton Sharapov
 */
public class BaseLogSpan implements LogSpan, Serializable {

    private static final long serialVersionUID = 4842897142736094736L;
    private static final UIdGenerator TRACE_ID_GEN = UIdGenerator.resolve("tid");
    private static final EventFilter ACCEPT_ALL_FILTER = (span -> true);

    private static final Marker SPAN_ENTER_MARKER = MarkerFactory.getMarker("SPAN ENTER");
    private static final Marker SPAN_FINISH_MARKER = MarkerFactory.getMarker("SPAN FINISH");

    private final long startTime;
    private transient final ScopeManager mgr;
    private final String traceId;
    private final LogSpan parent;
    private final String eventName;
    private final String namespace;
    private final String operation;
    private final List<Tag> tags;
    private final EventFilter predicate;
    private final Logger logger;
    private Throwable cause;
    private long completionTime;

    BaseLogSpan(final ScopeManager mgr, final String traceId,
                final String eventName, final String namespace, final String operation, final Collection<Tag> tags,
                final Logger logger, final EventFilter predicate) {
        this.mgr = mgr;
        this.parent = mgr != null ? mgr.getActive() : null;
        if (traceId == null) {
            this.traceId = parent != null ? parent.getTraceId() : TRACE_ID_GEN.get();
        } else {
            this.traceId = traceId;
        }
        this.eventName = eventName == null
                ? namespace != null ? namespace + '.' + operation : operation
                : eventName;

        final DefaultConfiguration cfg = DefaultConfiguration.getInstance();

        if (namespace == null) {
            this.namespace = cfg.getNamespace();
        } else {
            checkRequiredJsonAttr("namespace", namespace);
            this.namespace = namespace;
        }

        checkRequiredJsonAttr("operation", operation);
        this.operation = operation;

        this.tags = new ArrayList<>(tags);
        this.logger = logger == null
                ? LoggerFactory.getLogger(cfg.getRootLogCategory() + '.' + this.namespace + '.' + this.operation)
                : logger;
        this.predicate = predicate != null ? predicate : ACCEPT_ALL_FILTER;
        this.startTime = System.currentTimeMillis();

        activateSpan();
    }

    @Override
    public String getTraceId() {
        return traceId;
    }

    @Override
    public LogSpan getParentSpan() {
        return parent;
    }

    @Override
    public boolean isActive() {
        return mgr != null && completionTime == 0;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getDuration() {
        return completionTime > 0 ? completionTime - startTime : 0;
    }

    public String getEventName() {
        return eventName;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getOperation() {
        return operation;
    }

    @Override
    public List<Tag> getTags() {
        return tags;
    }

    @Override
    public LogSpan setTag(final String key, final Boolean value) {
        if (value != null) {
            setTag(new Tag(key, value));
        } else {
            removeTag(key);
        }
        return this;
    }

    @Override
    public LogSpan setTag(final String key, final String value) {
        if (value != null) {
            setTag(new Tag(key, value));
        } else {
            removeTag(key);
        }
        return this;
    }

    @Override
    public LogSpan setTag(final String key, final Integer value) {
        if (value != null) {
            setTag(new Tag(key, value));
        } else {
            removeTag(key);
        }
        return this;
    }

    @Override
    public LogSpan setTag(final String key, final Long value) {
        if (value != null) {
            setTag(new Tag(key, value));
        } else {
            removeTag(key);
        }
        return this;
    }

    @Override
    public LogSpan setTag(final String key, final Double value) {
        if (value != null) {
            setTag(new Tag(key, value));
        } else {
            removeTag(key);
        }
        return this;
    }

    @Override
    public LogSpan setTag(final String key, final Float value) {
        if (value != null) {
            setTag(new Tag(key, value));
        } else {
            removeTag(key);
        }
        return this;
    }

    @Override
    public LogSpan setTag(final String key, final BigDecimal value) {
        if (value != null) {
            setTag(new Tag(key, value));
        } else {
            removeTag(key);
        }
        return this;
    }

    @Override
    public LogSpan setTag(final String key, final BigInteger value) {
        if (value != null) {
            setTag(new Tag(key, value));
        } else {
            removeTag(key);
        }
        return this;
    }

    @Override
    public LogSpan setTag(final String key, final Date value) {
        if (value != null) {
            setTag(new Tag(key, value));
        } else {
            removeTag(key);
        }
        return this;
    }

    @Override
    public void markAsFailed(final Throwable cause) {
        if (this.cause == null) {
            this.cause = cause;
        }
    }

    @Override
    public Throwable getErrorCause() {
        return cause;
    }

    @Override
    public void close(final Throwable cause) {
        markAsFailed(cause);
        close();
    }

    @Override
    public void close() {
        completionTime = System.currentTimeMillis();
        deactivateSpan();
    }


    void activateSpan() {
        if (mgr != null) {
            mgr.activate(this);
            if (predicate.apply(this)) {
                logger.info(SPAN_ENTER_MARKER, "Span \"{}\" started", getEventName());
            }
        }
    }

    void deactivateSpan() {
        if (mgr != null) {
            if (predicate.apply(this)) {
                final long duration = getDuration();
                final String name = getEventName();
                if (cause == null) {
                    logger.info(SPAN_FINISH_MARKER, "Span \"{}\" completed successfully within {} ms", name, duration);
                } else {
                    logger.error(SPAN_FINISH_MARKER, "Span \"" + name + "\" completed with error within " + duration + " ms", cause);
                }
            }
            mgr.activate(parent);
        }
    }

    private void removeTag(final String name) {
        for (int i = 0, cnt = tags.size(); i < cnt; i++) {
            final Tag t = tags.get(i);
            if (t.getName().equals(name)) {
                tags.remove(i);
                return;
            }
        }
    }

    private void setTag(final Tag tag) {
        for (int i = 0, cnt = tags.size(); i < cnt; i++) {
            final Tag t = tags.get(i);
            if (t.getName().equals(tag.getName())) {
                tags.set(i, tag);
                return;
            }
        }
        tags.add(tag);
    }
}
