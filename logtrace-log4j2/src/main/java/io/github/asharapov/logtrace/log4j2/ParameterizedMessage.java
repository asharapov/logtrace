package io.github.asharapov.logtrace.log4j2;

import io.github.asharapov.logtrace.LogSpan;
import io.github.asharapov.logtrace.LogTracer;
import io.github.asharapov.logtrace.ScopeManager;

/**
 * @author Anton Sharapov
 */
public final class ParameterizedMessage extends org.apache.logging.log4j.message.ParameterizedMessage implements LogTraceAwareMessage {

    private static final long serialVersionUID = 938391000439352740L;

    private static final class Lazy {
        private static final ScopeManager SCOPE_MANAGER = LogTracer.getDefault().scopeManager();
    }

    private final LogSpan activeSpan;

    /**
     * Creates a parameterized message.
     *
     * @param messagePattern The message "format" string. This will be a String containing "{}" placeholders
     *                       where parameters should be substituted.
     * @param arguments      The arguments for substitution.
     * @param throwable      A Throwable.
     * @deprecated Use constructor ParameterizedMessage(String, Object[], Throwable) instead
     */
    @Deprecated
    public ParameterizedMessage(final String messagePattern, final String[] arguments, final Throwable throwable) {
        super(messagePattern, arguments, throwable);
        this.activeSpan = Lazy.SCOPE_MANAGER.getActive();
    }

    /**
     * Creates a parameterized message.
     *
     * @param messagePattern The message "format" string. This will be a String containing "{}" placeholders
     *                       where parameters should be substituted.
     * @param arguments      The arguments for substitution.
     * @param throwable      A Throwable.
     */
    public ParameterizedMessage(final String messagePattern, final Object[] arguments, final Throwable throwable) {
        super(messagePattern, arguments, throwable);
        this.activeSpan = Lazy.SCOPE_MANAGER.getActive();
    }

    /**
     * Constructs a ParameterizedMessage which contains the arguments converted to String as well as an optional
     * Throwable.
     *
     * <p>If the last argument is a Throwable and is NOT used up by a placeholder in the message pattern it is returned
     * in {@link #getThrowable()} and won't be contained in the created String[].
     * If it is used up {@link #getThrowable()} will return null even if the last argument was a Throwable!</p>
     *
     * @param messagePattern the message pattern that to be checked for placeholders.
     * @param arguments      the argument array to be converted.
     */
    public ParameterizedMessage(final String messagePattern, final Object... arguments) {
        super(messagePattern, arguments);
        this.activeSpan = Lazy.SCOPE_MANAGER.getActive();
    }

    /**
     * Constructor with a pattern and a single parameter.
     *
     * @param messagePattern The message pattern.
     * @param arg            The parameter.
     */
    public ParameterizedMessage(final String messagePattern, final Object arg) {
        this(messagePattern, new Object[]{arg});
    }

    /**
     * Constructor with a pattern and two parameters.
     *
     * @param messagePattern The message pattern.
     * @param arg0           The first parameter.
     * @param arg1           The second parameter.
     */
    public ParameterizedMessage(final String messagePattern, final Object arg0, final Object arg1) {
        this(messagePattern, new Object[]{arg0, arg1});
    }

    @Override
    public LogSpan getActiveSpan() {
        return activeSpan;
    }
}
