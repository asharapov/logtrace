open module io.github.asharapov.logtrace.log4j2v {
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires com.fasterxml.jackson.core;
    requires io.github.asharapov.logtrace;
    exports io.github.asharapov.logtrace.log4j2;
}