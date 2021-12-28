module io.github.asharapov.logtrace {
    requires java.management;
    requires org.slf4j;
    requires com.fasterxml.jackson.core;
    exports io.github.asharapov.logtrace.api;
    exports io.github.asharapov.logtrace.spi;
}