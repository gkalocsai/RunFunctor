package hu.kg.runfunctor.core;

import java.util.List;

public final class Unifier {
    private Unifier() {}

    public static boolean unify(Term a, Term b, BacktrackEnvironment env) {
        a = deref(a, env);
        b = deref(b, env);

        if (a instanceof Variable va) {
            return unifyVar(va, b, env);
        }
        if (b instanceof Variable vb) {
            return unifyVar(vb, a, env);
        }
        if (a instanceof Constant ca && b instanceof Constant cb) {
            return ca.equals(cb);
        }
        if (a instanceof Thing ta && b instanceof Thing tb) {
            return ta.equals(tb);
        }
        if (a instanceof Functor fa && b instanceof Functor fb) {
            if (!fa.name().equals(fb.name())) return false;
            List<Term> aa = fa.args();
            List<Term> bb = fb.args();
            if (aa.size() != bb.size()) return false;
            for (int i = 0; i < aa.size(); i++) {
                if (!unify(aa.get(i), bb.get(i), env)) return false;
            }
            return true;
        }
        return false;
    }

    private static boolean unifyVar(Variable v, Term t, BacktrackEnvironment env) {
        Term dv = deref(v, env);
        if (dv instanceof Variable vv) v = vv; // dereferenced variable

        Term dt = deref(t, env);
        if (dt instanceof Variable vt) {
            if (v.equals(vt)) return true; // same variable
            env.bind(v, vt);
            return true;
        } else {
            if (occurs(v, dt, env)) return false;
            env.bind(v, dt);
            return true;
        }
    }

    private static Term deref(Term t, BindingEnvironment env) {
        return t.resolve(env);
    }

    private static boolean occurs(Variable v, Term t, BacktrackEnvironment env) {
        t = deref(t, env);
        if (t instanceof Variable vv) {
            return v.equals(vv);
        } else if (t instanceof Functor f) {
            for (Term a : f.args()) {
                if (occurs(v, a, env)) return true;
            }
            return false;
        } else {
            return false;
        }
    }
}


