package io.github.asharapov.logtrace.impl;

import java.util.regex.Pattern;

/**
 * @author Anton Sharapov
 */
public class Utils {
    private static final Pattern NS_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9_\\-]*");

    public static void checkRequiredJsonAttr(final String name, final String value) {
        if (value == null)
            throw new IllegalArgumentException("Argument '" + name + "' must be specified");
        if (!NS_PATTERN.matcher(value).matches())
            throw new IllegalArgumentException("Argument '" + name + "' has illegal value: " + value);
    }

    public static void checkNotEmpty(final String value) {
        if (value == null) {
            throw new IllegalArgumentException("Argument must be specified");
        }
    }

}
