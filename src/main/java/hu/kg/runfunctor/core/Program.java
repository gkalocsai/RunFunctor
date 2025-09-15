package hu.kg.runfunctor.core;

import java.util.*;

public final class Program {
    private final List<Clause> all = new ArrayList<>();
    private final Map<String, List<Clause>> index = new HashMap<>();

    public void add(Clause c) {
        all.add(c);
        String key = key(c.head().name(), c.head().args().size());
        index.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
    }

    public List<Clause> clauses(String name, int arity) {
        return index.getOrDefault(key(name, arity), List.of());
    }

    public List<Clause> allClauses() {
        return Collections.unmodifiableList(all);
    }

    private static String key(String name, int arity) {
        return name + "/" + arity;
    }
}


