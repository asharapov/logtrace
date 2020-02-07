package io.github.asharapov.logtrace.tests;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.asharapov.logtrace.tests.model.LogRecord;

/**
 * @author Anton Sharapov
 */
public class LogReader implements Closeable {

    public enum Capabilities {
        MARKER_SUPPORT,
        MDC_SUPPORT
    }

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    private final Path path;
    private final boolean formatted;
    private final EnumSet<Capabilities> capabilities;
    private BufferedReader reader;
    private String prefetchedRecord;

    public LogReader(final Path path, final boolean formatted) {
        this(path, formatted, EnumSet.allOf(Capabilities.class));
    }

    public LogReader(final Path path, final boolean formatted, final EnumSet<Capabilities> capabilities) {
        assert path != null;
        this.path = path;
        this.formatted = formatted;
        this.capabilities = capabilities != null ? capabilities : EnumSet.allOf(Capabilities.class);
    }

    public void positionToEOF() throws IOException {
        ensureOpened();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) ;
        prefetchedRecord = null;
    }

    public boolean hasNext() throws IOException {
        if (prefetchedRecord != null)
            return true;
        prefetchedRecord = readNextRecord();
        return prefetchedRecord != null;
    }

    public String nextRecordAsText() throws IOException {
        if (prefetchedRecord != null) {
            final String result = prefetchedRecord;
            prefetchedRecord = null;
            return result;
        } else {
            return readNextRecord();
        }
    }

    public LogRecord nextRecord() throws IOException {
        final String text = nextRecordAsText();
        if (text == null)
            return null;
        try {
            final LogRecord rec = mapper.readValue(text, LogRecord.class);
            rec.capabilities = capabilities;
            return rec;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        prefetchedRecord = null;
        if (reader != null) {
            reader.close();
            reader = null;
        }
    }

    private void ensureOpened() throws IOException {
        if (reader == null) {
            reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        }
    }

    private String readNextRecord() throws IOException {
        ensureOpened();
        if (formatted) {
            final StringBuilder buf = new StringBuilder();
            while (true) {
                final String line = readLineEx();
                if (line == null) {
                    return null;
                }
                if (buf.length() == 0) {
                    if (!line.startsWith("{"))
                        throw new IllegalStateException("Invalid record start: " + line);
                }
                buf.append(line).append('\n');
                if (line.startsWith("}")) {
                    return buf.toString();
                }
            }
        } else {
            return readLineEx();
        }
    }

    private String readLineEx() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            try {
                Thread.sleep(250);
                line = reader.readLine();
            } catch (InterruptedException e) {
                return null;
            }
        }
        return line;
    }

}
