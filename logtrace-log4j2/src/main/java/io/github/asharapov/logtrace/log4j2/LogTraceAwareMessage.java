package io.github.asharapov.logtrace.log4j2;

import io.github.asharapov.logtrace.api.LogSpan;

/**
 * @author Anton Sharapov
 */
public interface LogTraceAwareMessage {

    LogSpan getActiveSpan();
}
