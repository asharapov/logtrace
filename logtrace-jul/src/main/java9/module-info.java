open module io.github.asharapov.logtrace.jul {
    requires java.logging;
    requires org.slf4j;
    requires com.fasterxml.jackson.core;
    requires io.github.asharapov.logtrace;
    exports io.github.asharapov.logtrace.jul;
}