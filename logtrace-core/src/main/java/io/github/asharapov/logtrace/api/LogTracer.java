package io.github.asharapov.logtrace.api;

/**
 * @author Anton Sharapov
 */
public interface LogTracer {

    /**
     * Возвращает подходяшую имплементацию данного интерфейса. Ее поиск выполняется по следующему алгоритму:
     * <ol>
     * <li>Поиск по имени класса, заданного в параметре JVM: <strong>logging.tracer</strong></li>
     * <li>Поиск в параметре <strong>tracer</strong> конфигурационного файла <strong>META-INF/logtrace.properties</strong> расположенного в числе ресурсов программы</li>
     * <li>Поиск в параметре <strong>tracer</strong> конфигурационного файла <strong>META-INF/logtrace-default.properties</strong> расположенного в числе ресурсов программы</li>
     * </ol>
     */
    static LogTracer getDefault() {
        return Lazy.INSTANCE;
    }

    SpanBuilder buildSpan(String operation);

    ScopeManager scopeManager();

}

class Lazy {
    static final LogTracer INSTANCE = DefaultConfiguration.getInstance().makeTracer();
}