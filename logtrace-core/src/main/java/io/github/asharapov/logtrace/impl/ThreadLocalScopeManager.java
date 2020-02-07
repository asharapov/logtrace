package io.github.asharapov.logtrace.impl;

import io.github.asharapov.logtrace.api.LogSpan;
import io.github.asharapov.logtrace.api.ScopeManager;

/**
 * @author Anton Sharapov
 */
public class ThreadLocalScopeManager implements ScopeManager {

    private final ThreadLocal<LogSpan> tls;

    public ThreadLocalScopeManager() {
        this.tls = new ThreadLocal<>();
    }

    @Override
    public LogSpan getActive() {
        return tls.get();
    }

    @Override
    public LogSpan activate(final LogSpan newSpan) {
        if (newSpan != null) {
            tls.set(newSpan);
        } else {
            tls.remove();
        }
        return newSpan;
    }

    public LogSpan deactivate(final LogSpan currentSpan) {
        final LogSpan span = tls.get();
        if (currentSpan != null && currentSpan.equals(span)) {
            return activate(currentSpan.getParentSpan());
        }
        return span;
    }
}
