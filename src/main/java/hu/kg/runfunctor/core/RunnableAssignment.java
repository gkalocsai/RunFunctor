package hu.kg.runfunctor.core;

import java.util.*;

public final class RunnableAssignment implements BodyElement {
    private final String builtinName; // e.g., CONCAT, NEW
    private final List<Term> args;
    private final Variable target;

    public RunnableAssignment(String builtinName, List<Term> args, Variable target) {
        this.builtinName = Objects.requireNonNull(builtinName);
        this.args = List.copyOf(args);
        this.target = Objects.requireNonNull(target);
    }

    public String builtinName() {
        return builtinName;
    }

    public List<Term> args() {
        return args;
    }

    public Variable target() {
        return target;
    }

    @Override
    public boolean isRunnable() {
        return true;
    }

    @Override
    public void execute(BindingEnvironment env, Builtins builtins) {
        BuiltinFunction fn = builtins.get(builtinName);
        if (fn == null) {
            throw new RuntimeException("Unknown builtin: " + builtinName);
        }
        // Resolve arguments through current bindings
        List<Term> resolved = new ArrayList<>(args.size());
        for (Term t : args) resolved.add(t.resolve(env));
        Term res = fn.apply(resolved, env);
        env.bindFromRunnable(target, res);
    }

    @Override
    public String toSource() {
        StringBuilder sb = new StringBuilder();
        sb.append('!').append(builtinName).append('(');
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(args.get(i).toSource());
        }
        sb.append(")->").append(target.toSource());
        return sb.toString();
    }

    @Override
    public String toString() {
        return toSource();
    }
}



