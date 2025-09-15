package hu.kg.runfunctor.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;



public final class Solver {
    private final Program program;
    private final Builtins builtins;

    public Solver(Program program, Builtins builtins) {
        this.program = Objects.requireNonNull(program);
        this.builtins = Objects.requireNonNull(builtins);
    }

    // Existing multi-solution API (unchanged behavior)
    public void solve(List<BodyElement> query, SolutionConsumer consumer) {
        BacktrackEnvironment env = new BacktrackEnvironment();
        dfs(query, env, consumer, true); // explore all
    }

    // New: return only the first solution (if any)
    public Optional<Map<String, Term>> solveFirst(List<BodyElement> query) {
        BacktrackEnvironment env = new BacktrackEnvironment();
        final Map<String, Term>[] box = new Map[1];
        boolean found = dfs(query, env, sol -> box[0] = sol, true); // stop after first
        return found ? Optional.of(filterQueryVars(box[0])) : Optional.empty();
    }

    // Depth-first search with an early-stop flag.
    // Returns true if a solution was produced and stopAfterFirst=true => callers should stop.
    private boolean dfs(List<BodyElement> goals,
                        BacktrackEnvironment env,
                        SolutionConsumer consumer,
                        boolean stopAfterFirst) {
        if (goals.isEmpty()) {
            consumer.onSolution(env.snapshotResolved());
            return stopAfterFirst; // true => tell caller to stop exploring
        }

        BodyElement g = goals.get(0);
        List<BodyElement> rest = goals.subList(1, goals.size());

        if (g instanceof RunnableAssignment ra) {
            int mark = env.mark();
            try {
                ra.execute(env, builtins);
                if (dfs(rest, env, consumer, stopAfterFirst)) {
                    env.undoTo(mark);
                    return true;
                }
            } catch (RuntimeException ex) {
                // runnable failed — backtrack
            }
            env.undoTo(mark);
            return false;
        }

        if (!(g instanceof Functor goal)) {
            throw new IllegalStateException("Unsupported goal: " + g);
        }

        List<Clause> choices = program.clauses(goal.name(), goal.args().size());
        for (Clause c0 : choices) {
            Clause c = Freshener.freshen(c0);
            int mark = env.mark();
            if (Unifier.unify(goal, c.head(), env)) {
                List<BodyElement> newGoals = new ArrayList<>(c.body().size() + rest.size());
                newGoals.addAll(c.body());
                newGoals.addAll(rest);
                if (dfs(newGoals, env, consumer, stopAfterFirst)) {
                    env.undoTo(mark);
                    return true;
                }
            }
            env.undoTo(mark);
        }
        return false;
    }

    public static Map<String, Term> filterQueryVars(Map<String, Term> solution) {
        LinkedHashMap<String, Term> out = new LinkedHashMap<>();
        for (Map.Entry<String, Term> e : solution.entrySet()) {
            if (!e.getKey().contains("__")) {
                out.put(e.getKey(), e.getValue());
            }
        }
        return out;
    }
}