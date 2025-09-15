package hu.kg.runfunctor.core;

import java.util.*;

public class Functor implements Term, BodyElement {
    private final String name;
    private final List<Term> args;

    public Functor(String name, List<Term> args) {
        this.name = Objects.requireNonNull(name);
        this.args = List.copyOf(args);
    }

    public String name() {
        return name;
    }

    public List<Term> args() {
        return args;
    }

    @Override
    public Term resolve(BindingEnvironment env) {
        List<Term> ra = new ArrayList<>(args.size());
        for (Term t : args) ra.add(t.resolve(env));
        return new Functor(name, ra);
    }

    @Override
    public String toSource() {
        if (args.isEmpty()) return name;
        StringBuilder sb = new StringBuilder();
        sb.append(name).append('(');
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(args.get(i).toSource());
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public String toString() {
        return toSource();
    }

    @Override
    public void execute(BindingEnvironment env, Builtins builtins) {
        // Non-runnable functor: no execution here (handled by a Prolog engine if desired).
    }

    @Override
    public boolean isRunnable() {
        return false;
    }
}


