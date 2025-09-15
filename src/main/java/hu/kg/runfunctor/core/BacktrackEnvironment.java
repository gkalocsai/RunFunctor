package hu.kg.runfunctor.core;

import java.util.*;

/**
 * A backtrackable binding environment.
 * Records changes on a trail so they can be undone to a choicepoint mark.
 */
public class BacktrackEnvironment extends BindingEnvironment {
    private final Map<String, Term> map = new HashMap<>();
    private final ArrayDeque<TrailEntry> trail = new ArrayDeque<>();

    private static final class TrailEntry {
        final String varName;
        final Term previous; // null means previously unbound

        TrailEntry(String varName, Term previous) {
            this.varName = varName;
            this.previous = previous;
        }
    }

    public int mark() {
        return trail.size();
    }

    public void undoTo(int mark) {
        while (trail.size() > mark) {
            TrailEntry e = trail.removeLast();
            if (e.previous == null) {
                map.remove(e.varName);
            } else {
                map.put(e.varName, e.previous);
            }
        }
    }

    @Override
    public Term get(Variable v) {
        return map.get(v.name());
    }

    @Override
    public boolean isBound(Variable v) {
        return map.containsKey(v.name());
    }

    @Override
    public void bind(Variable v, Term value) {
        bindInternal(v, value, false);
    }

    @Override
    public void bindFromRunnable(Variable v, Term value) {
        bindInternal(v, value, true);
    }

    private void bindInternal(Variable v, Term value, boolean fromRunnable) {
        Objects.requireNonNull(v);
        Objects.requireNonNull(value);
        String key = v.name();
        Term existing = map.get(key);
        if (existing == null) {
            trail.addLast(new TrailEntry(key, null));
            map.put(key, value);
            return;
        }
        if (fromRunnable && v.isOverrideable()) {
            trail.addLast(new TrailEntry(key, existing));
            map.put(key, value);
            return;
        }
        if (existing.equals(value)) {
            return; // idempotent
        }
        throw new RuntimeException("Variable " + v.name() + " is already bound and not overrideable: " + existing + " vs " + value);
    }

    public Map<String, Term> snapshotResolved() {
        LinkedHashMap<String, Term> out = new LinkedHashMap<>();
        for (Map.Entry<String, Term> e : map.entrySet()) {
            out.put(e.getKey(), e.getValue().resolve(this));
        }
        return out;
    }

    @Override
    public String toString() {
        return snapshotResolved().toString();
    }
}


