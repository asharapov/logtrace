package io.github.asharapov.logtrace;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
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
        INT,
        LONG,
        DOUBLE,
        FLOAT,
        BIG_DECIMAL,
        BIG_INTEGER,
        DATE,
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

    public Tag(final String name, final Integer value) {
        this(Type.INT, name, value);
    }

    public Tag(final String name, final Long value) {
        this(Type.LONG, name, value);
    }

    public Tag(final String name, final Double value) {
        this(Type.DOUBLE, name, value);
    }

    public Tag(final String name, final Float value) {
        this(Type.FLOAT, name, value);
    }

    public Tag(final String name, final BigDecimal value) {
        this(Type.BIG_DECIMAL, name, value);
    }

    public Tag(final String name, final BigInteger value) {
        this(Type.BIG_INTEGER, name, value);
    }

    public Tag(final String name, final Date value) {
        this(Type.DATE, name, value);
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
