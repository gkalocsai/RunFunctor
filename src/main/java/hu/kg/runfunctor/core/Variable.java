package hu.kg.runfunctor.core;

import java.util.Objects;

public final class Variable implements Term {
    private final String name; // may include leading '*'
    private final boolean overrideable;

    public Variable(String name) {
        this.name = Objects.requireNonNull(name);
        this.overrideable = name.startsWith("*");
    }

    public String name() {
        return name;
    }

    public boolean isOverrideable() {
        return overrideable;
    }

    @Override
    public Term resolve(BindingEnvironment env) {
        Term b = env.get(this);
        return b == null ? this : b.resolve(env);
    }

    @Override
    public String toSource() {
        return name;
    }

    @Override
    public String toString() {
        return toSource();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Variable v) && v.name.equals(this.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}


