package io.github.asharapov.logtrace.log4j2;

import io.github.asharapov.logtrace.LogSpan;
import io.github.asharapov.logtrace.LogTracer;
import io.github.asharapov.logtrace.ScopeManager;

/**
 * @author Anton Sharapov
 */
public final class SimpleMessage extends org.apache.logging.log4j.message.SimpleMessage implements LogTraceAwareMessage {

    private static final long serialVersionUID = 7037217528012077280L;

    private static final class Lazy {
        private static final ScopeManager SCOPE_MANAGER = LogTracer.getDefault().scopeManager();
    }

    private final LogSpan activeSpan;

    public SimpleMessage() {
        this(null);
    }

    public SimpleMessage(final String message) {
        super(message);
        this.activeSpan = Lazy.SCOPE_MANAGER.getActive();
    }

    public SimpleMessage(final CharSequence charSequence) {
        super(charSequence);
        this.activeSpan = Lazy.SCOPE_MANAGER.getActive();
    }

    @Override
    public LogSpan getActiveSpan() {
        return activeSpan;
    }

}
