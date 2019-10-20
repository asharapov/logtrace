package io.github.asharapov.logtrace;

import java.util.function.Function;

/**
 * Предикат для фильтрации событий сигнализирующих о начале и завершении блоков задач.
 *
 * @author Anton Sharapov
 */
@FunctionalInterface
public interface EventFilter extends Function<LogSpan, Boolean> {
}
