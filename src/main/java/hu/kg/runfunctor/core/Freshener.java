package hu.kg.runfunctor.core;

import java.util.*;

public final class Freshener {
    private static long COUNTER = 0;

    private Freshener() {}

    public static Clause freshen(Clause c) {
        long id = ++COUNTER;
        Map<String, Variable> ren = new HashMap<>();
        Functor head = (Functor) copyTerm(c.head(), ren, id);
        List<BodyElement> body = new ArrayList<>(c.body().size());
        for (BodyElement be : c.body()) {
            if (be instanceof Functor f) {
                body.add((Functor) copyTerm(f, ren, id));
            } else if (be instanceof RunnableAssignment ra) {
                List<Term> args = new ArrayList<>(ra.args().size());
                for (Term a : ra.args()) args.add(copyTerm(a, ren, id));
                Variable target = renameVar(ra.target(), ren, id);
                body.add(new RunnableAssignment(ra.builtinName(), args, target));
            } else {
                throw new IllegalStateException("Unknown body element: " + be);
            }
        }
        return new Clause(head, body);
    }

    private static Term copyTerm(Term t, Map<String, Variable> ren, long id) {
        if (t instanceof Variable v) {
            return renameVar(v, ren, id);
        } else if (t instanceof Functor f) {
            List<Term> args = new ArrayList<>(f.args().size());
            for (Term a : f.args()) args.add(copyTerm(a, ren, id));
            return new Functor(f.name(), args);
        } else {
            // Constant or Thing are immutable/value objects
            return t;
        }
    }

    private static Variable renameVar(Variable v, Map<String, Variable> ren, long id) {
        return ren.computeIfAbsent(v.name(), n -> {
            boolean star = n.startsWith("*");
            String base = star ? n.substring(1) : n;
            String fresh = (star ? "*" : "") + base + "__" + id;
            return new Variable(fresh);
        });
    }
}


