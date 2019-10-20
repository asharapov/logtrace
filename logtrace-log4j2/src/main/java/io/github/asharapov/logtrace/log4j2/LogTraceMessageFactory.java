package io.github.asharapov.logtrace.log4j2;

import org.apache.logging.log4j.message.AbstractMessageFactory;
import org.apache.logging.log4j.message.FormattedMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.MessageFactory2;

/**
 * Creates {@link FormattedMessage} instances for {@link MessageFactory2} methods (and {@link MessageFactory} by
 * extension.)
 * <p>
 * Enables the use of <code>{}</code> parameter markers in message strings.
 * </p>
 * <p>
 * Creates {@link SimpleMessage} or {@link ParameterizedMessage} instances for {@link #newMessage(String, Object...)}.
 * </p>
 * <p>
 * This class is immutable.
 * </p>
 *
 * <h4>Note to implementors</h4>
 * <p>
 * This class implements all {@link MessageFactory2} methods.
 * </p>
 */
public final class LogTraceMessageFactory extends AbstractMessageFactory {

    private static final long serialVersionUID = 904952155997109054L;

    /**
     * Constructs a message factory.
     */
    public LogTraceMessageFactory() {
        super();
    }

    @Override
    public Message newMessage(final CharSequence message) {
        return new SimpleMessage(message);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.logging.log4j.message.MessageFactory#newMessage(java.lang.Object)
     */
    @Override
    public Message newMessage(final Object message) {
        return new SimpleMessage(String.valueOf(message));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.logging.log4j.message.MessageFactory#newMessage(java.lang.String)
     */
    @Override
    public Message newMessage(final String message) {
        return new SimpleMessage(message);
    }

    /**
     * Creates {@link ParameterizedMessage} instances.
     *
     * @param message The message pattern.
     * @param params  The message parameters.
     * @return The Message.
     * @see MessageFactory#newMessage(String, Object...)
     */
    @Override
    public Message newMessage(final String message, final Object... params) {
        return new ParameterizedMessage(message, params);
    }

    /**
     * @since 2.6.1
     */
    @Override
    public Message newMessage(final String message, final Object p0) {
        return new ParameterizedMessage(message, p0);
    }

    /**
     * @since 2.6.1
     */
    @Override
    public Message newMessage(final String message, final Object p0, final Object p1) {
        return new ParameterizedMessage(message, p0, p1);
    }

    /**
     * @since 2.6.1
     */
    @Override
    public Message newMessage(final String message, final Object p0, final Object p1, final Object p2) {
        return new ParameterizedMessage(message, p0, p1, p2);
    }

    /**
     * @since 2.6.1
     */
    @Override
    public Message newMessage(final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        return new ParameterizedMessage(message, p0, p1, p2, p3);
    }

    /**
     * @since 2.6.1
     */
    @Override
    public Message newMessage(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        return new ParameterizedMessage(message, p0, p1, p2, p3, p4);
    }

    /**
     * @since 2.6.1
     */
    @Override
    public Message newMessage(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        return new ParameterizedMessage(message, p0, p1, p2, p3, p4, p5);
    }

    /**
     * @since 2.6.1
     */
    @Override
    public Message newMessage(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5,
                              final Object p6) {
        return new ParameterizedMessage(message, p0, p1, p2, p3, p4, p5, p6);
    }

    /**
     * @since 2.6.1
     */
    @Override
    public Message newMessage(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5,
                              final Object p6, final Object p7) {
        return new ParameterizedMessage(message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    /**
     * @since 2.6.1
     */
    @Override
    public Message newMessage(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5,
                              final Object p6, final Object p7, final Object p8) {
        return new ParameterizedMessage(message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    /**
     * @since 2.6.1
     */
    @Override
    public Message newMessage(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5,
                              final Object p6, final Object p7, final Object p8, final Object p9) {
        return new ParameterizedMessage(message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }
}
