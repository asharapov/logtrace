package io.github.asharapov.logtrace.impl;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Генератор уникальных строк с монотонно возрастающим их суффиксом.
 *
 * @author Anton Sharapov
 */
public class UIdGenerator implements Supplier<String> {

    private static final String procname;
    private static final ConcurrentHashMap<String, UIdGenerator> generators;
    private static final UIdGenerator dflt;

    static {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        final int p1 = name.indexOf('@');
        if (p1 > 0) {
            final int p2 = name.indexOf('.', p1);
            if (p2 > 0) {
                name = name.substring(0, p2);
            }
        }
        procname = ":" + name + ":";
        generators = new ConcurrentHashMap<>();
        dflt = resolve("id");
    }

    public static UIdGenerator getDefault() {
        return dflt;
    }

    public static UIdGenerator resolve(final String ns) {
        return generators.computeIfAbsent(ns, UIdGenerator::new);
    }


    private final String prefix;  // Неизменяемая (для данного VM процесса) часть идентификатора в формате <ns>:<pid>@<hostname>:
    private final AtomicLong counter;

    private UIdGenerator(final String ns) {
        this.prefix = (ns != null && ns.length() > 0 ? ns : "id") + procname;
        this.counter = new AtomicLong(0);
    }

    @Override
    public String get() {
        return prefix + counter.incrementAndGet();
    }
}
