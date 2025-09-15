package hu.kg.runfunctor.core;

import java.util.List;

@FunctionalInterface
public interface BuiltinFunction {
    Term apply(List<Term> args, BindingEnvironment env);
}


