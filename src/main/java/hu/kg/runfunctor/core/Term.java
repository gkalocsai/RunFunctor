package hu.kg.runfunctor.core;

public interface Term {
    Term resolve(BindingEnvironment env);
    String toSource();
}


