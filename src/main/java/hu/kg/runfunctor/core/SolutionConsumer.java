package hu.kg.runfunctor.core;

import java.util.Map;

@FunctionalInterface
public interface SolutionConsumer {
    void onSolution(Map<String, Term> solution);
}