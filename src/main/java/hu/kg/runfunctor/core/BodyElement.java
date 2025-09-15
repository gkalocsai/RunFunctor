package hu.kg.runfunctor.core;

public interface BodyElement {
    boolean isRunnable();
    void execute(BindingEnvironment env, Builtins builtins);
    String toSource();
}

