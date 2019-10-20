package io.github.asharapov.logtrace;

/**
 * Отвечает за сопоставление активацию нового экземпляра {@link LogSpan} и предоставлением доступа к активному экземпляру {@link LogSpan}.
 *
 * @author Anton Sharapov
 */
public interface ScopeManager {

    /**
     * Возвращает идентификатор пользовательской бизнес-операции.
     */
    default String getActiveTraceId() {
        final LogSpan activeSpan = getActive();
        return activeSpan != null ? activeSpan.getTraceId() : null;
    }

    /**
     * Сбрасывает все сведения о выполнении каких-либо работ в рамках текущего потока.
     */
    default void reset() {
        activate(null);
    }

    /**
     * Возвращает для текущего потока дескриптор активной на данный момент работы или <code>null</code>.
     */
    LogSpan getActive();

    /**
     * <p>Прикрепляет экземпляр {@link LogSpan} в качестве дескриптора активной задачи в рамках текущего контекста выполнения.</p>
     * <p>Данный экземпляр будет доступен в течение всего времени жизни соответствующей задачи путем вызова метода {@link #getActive()}.</p>
     * <p>
     * Примеры использования:
     * <ol>
     * <li><br/>
     * <pre><code>
     * try (Span = tracer.buildSpan("auth")
     *              .withTag("host", "localhost")
     *              .withTag("account", "demouser")
     *              .withTag("role", "guest")
     *              .activate(span)) {
     *     span.setTag("...", "...");
     *     ...
     * }
     * </code></pre>
     * </li>
     *
     * <li><br/>
     * <pre><code>
     * Span span = tracer.buildSpan("auth")
     *          .withTag("host", "localhost")
     *          .withTag("acc", "demouser")
     *          .withTag("role", "guest")
     *          .activate();
     * try {
     *     span.setTag("...", "...");
     *     ...
     * } catch (Exception e) {
     *     span.markAsFailed(e);
     *     throw e;
     * } finally {
     *     span.close();
     * }
     * </code></pre>
     * </li>
     * </ol>
     *
     * @param span новый актуальный экземпляр дескриптора блока работ для текущего состояния обработки бизнес-операции
     *             или <code>null</code> если требуется сбросить сведения о проводимых работах в текущем потоке.
     * @return актуальный дескриптор блока работ, совпадает с соответствующим значением переданным в аргументе метода
     * либо может являться некоторого рода производной (декоратором) от него.
     */
    LogSpan activate(LogSpan span);

}
