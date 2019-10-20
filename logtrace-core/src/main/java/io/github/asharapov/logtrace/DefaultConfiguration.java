package io.github.asharapov.logtrace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.github.asharapov.logtrace.impl.BaseLogTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anton Sharapov
 */
public class DefaultConfiguration {
    private static final Logger log = LoggerFactory.getLogger(DefaultConfiguration.class);
    private static final String DEFAULT_TRACER_CLS = "tracer";
    private static final String DEFAULT_NAMESPACE = "default.namespace";
    private static final String DEFAULT_LOGGER = "default.logger";

    private static class Lazy {
        private static final DefaultConfiguration INSTANCE = new DefaultConfiguration();
    }

    public static DefaultConfiguration getInstance() {
        return Lazy.INSTANCE;
    }

    private final Map<String, String> properties;
    private final Class<? extends LogTracer> cls;
    private final String namespace;
    private final String rootLogCategory;

    private DefaultConfiguration() {
        this.properties = Collections.unmodifiableMap(loadProperties());
        this.cls = resolveTracerClass(properties);
        this.namespace = properties.getOrDefault(DEFAULT_NAMESPACE, "ctx");
        this.rootLogCategory = properties.getOrDefault(DEFAULT_LOGGER, "logtrace");
    }

    public Class<? extends LogTracer> getDefaultTracerClass() {
        return cls;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getRootLogCategory() {
        return rootLogCategory;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public LogTracer makeTracer() {
        try {
            return cls.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            try {
                final Method method = cls.getMethod("getInstance");
                final Object result = method.invoke(null);
                return (LogTracer) result;
            } catch (Exception ee) {
                log.error(ee.getMessage(), ee);
                return new BaseLogTracer();
            }
        }
    }

    private static Class<? extends LogTracer> resolveTracerClass(final Map<String, String> properties) {
        String clsName = System.getProperty("logging.tracer", properties.get(DEFAULT_TRACER_CLS));
        if (clsName == null)
            return BaseLogTracer.class;
        try {
            return Class.forName(clsName).asSubclass(LogTracer.class);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            return BaseLogTracer.class;
        }
    }

    private static Map<String, String> loadProperties() {
        final Map<String, String> properties = new HashMap<>();
        final URL url1 = DefaultConfiguration.class.getClassLoader().getResource("META-INF/logtrace-default.properties");
        if (url1 != null) {
            loadPropertiesFromURL(properties, url1);
        }
        final URL url2 = DefaultConfiguration.class.getClassLoader().getResource("META-INF/logtrace.properties");
        if (url2 != null) {
            loadPropertiesFromURL(properties, url2);
        }
        return properties;
    }

    private static void loadPropertiesFromURL(final Map<String, String> properties, final URL url) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == '#')
                    continue;
                final int p = line.indexOf('=');
                if (p <= 0)
                    continue;
                final String key = line.substring(0, p).trim();
                final String value = line.substring(p + 1).trim();
                if (!key.isEmpty() && !value.isEmpty()) {
                    properties.put(key, value);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
