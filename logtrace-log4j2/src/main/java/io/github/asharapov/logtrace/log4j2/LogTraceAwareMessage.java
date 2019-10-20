package io.github.asharapov.logtrace.log4j2;

import io.github.asharapov.logtrace.LogSpan;

/**
 * @author Anton Sharapov
 */
public interface LogTraceAwareMessage {

    LogSpan getActiveSpan();
}
