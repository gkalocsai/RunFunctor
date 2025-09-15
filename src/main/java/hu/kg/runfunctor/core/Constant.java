package hu.kg.runfunctor.core;

import java.util.Objects;

public final class Constant implements Term {
    private final String value;

    public Constant(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public String value() {
        return value;
    }

    @Override
    public Term resolve(BindingEnvironment env) {
        return this;
    }

    @Override
    public String toSource() {
        return "\"" + Util.escapeString(value) + "\"";
    }

    @Override
    public String toString() {
        return toSource();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Constant c) && c.value.equals(this.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}


