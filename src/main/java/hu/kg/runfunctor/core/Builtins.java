package hu.kg.runfunctor.core;

import java.util.*;

public class Builtins {
    private final Map<String, BuiltinFunction> map = new HashMap<>();

    public Builtins() {
        registerDefaults();
    }

    public BuiltinFunction get(String name) {
        return map.get(name);
    }

    public void register(String name, BuiltinFunction fn) {
        map.put(name, fn);
    }

    private void registerDefaults() {
        // !CONCAT("A","B") -> Variable
        register("CONCAT", (args, env) -> {
            if (args.size() < 2) throw new RuntimeException("CONCAT requires at least 2 arguments");
            StringBuilder sb = new StringBuilder();
            for (Term t : args) {
                t = t.resolve(env);
                if (t instanceof Constant c) {
                    sb.append(c.value());
                } else if (t instanceof Thing th) {
                    sb.append(th.content());
                } else {
                    throw new RuntimeException("CONCAT only supports Constant or Thing args, got: " + t);
                }
            }
            return new Constant(sb.toString());
        });

        // !NEW() -> Variable
        register("NEW", (args, env) -> {
            if (!args.isEmpty()) throw new RuntimeException("NEW takes no arguments");
            return Thing.newRandom();
        });
    }
}


