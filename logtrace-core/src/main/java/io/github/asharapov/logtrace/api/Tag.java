package io.github.asharapov.logtrace.api;

import java.io.Serializable;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Objects;

import static io.github.asharapov.logtrace.impl.Utils.checkRequiredJsonAttr;

/**
 * @author Anton Sharapov
 */
public class Tag implements Serializable {

    private static final long serialVersionUID = -9025899114593945432L;

    public enum Type {
        STRING,
        BOOLEAN,
        NUMBER,
        DATE,
        TEMPORAL
    }

    private final Type type;
    private final String name;
    private final Object value;

    public Tag(final String name, final String value) {
        this(Type.STRING, name, value);
    }

    public Tag(final String name, final Boolean value) {
        this(Type.BOOLEAN, name, value);
    }

    public Tag(final String name, final Number value) {
        this(Type.NUMBER, name, value);
    }

    public Tag(final String name, final Date value) {
        this(Type.DATE, name, value);
    }

    public Tag(final String name, final TemporalAccessor value) {
        this(Type.TEMPORAL, name, value);
    }

    private Tag(final Type type, final String name, final Object value) {
        assert type != null;
        checkRequiredJsonAttr("name", name);
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Tag))
            return false;
        final Tag other = (Tag) obj;
        return type == other.type && name.equals(other.name) && Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
        return "[Tag{type:" + type + ", name:" + name + ", value:" + value + "}]";
    }
}
