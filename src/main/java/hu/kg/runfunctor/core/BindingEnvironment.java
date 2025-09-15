package hu.kg.runfunctor.core;

import java.util.*;

public class BindingEnvironment {
    private final Map<String, Term> bindings = new HashMap<>();

    public Term get(Variable v) {
        return bindings.get(v.name());
    }

    public boolean isBound(Variable v) {
        return bindings.containsKey(v.name());
    }

    public void bind(Variable v, Term value) {
        bindInternal(v, value, false);
    }

    public void bindFromRunnable(Variable v, Term value) {
        bindInternal(v, value, true);
    }

    private void bindInternal(Variable v, Term value, boolean fromRunnable) {
        Objects.requireNonNull(v);
        Objects.requireNonNull(value);
        Term existing = bindings.get(v.name());
        if (existing == null) {
            bindings.put(v.name(), value);
            return;
        }
        // Already bound
        if (fromRunnable && v.isOverrideable()) {
            bindings.put(v.name(), value);
            return;
        }
        if (existing.equals(value)) {
            return; // idempotent
        }
        throw new RuntimeException("Variable " + v.name() + " is already bound and not overrideable: " + existing + " vs " + value);
    }

    @Override
    public String toString() {
        return bindings.toString();
    }
}
