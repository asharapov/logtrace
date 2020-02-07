open module io.github.asharapov.logtrace.logback {
    requires org.slf4j;
    requires com.fasterxml.jackson.core;
//    requires ch.qos.logback.classic;
//    requires ch.qos.logback.core;
    requires logback.core;
    requires logback.classic;
    requires io.github.asharapov.logtrace;
    exports io.github.asharapov.logtrace.logback;
}