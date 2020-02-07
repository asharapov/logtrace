open module io.github.asharapov.logtrace.log4j1v {
    requires log4j;
    requires com.fasterxml.jackson.core;
    requires io.github.asharapov.logtrace;
    exports io.github.asharapov.logtrace.log4j;
}