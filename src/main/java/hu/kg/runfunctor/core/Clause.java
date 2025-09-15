package hu.kg.runfunctor.core;

import java.util.*;

public class Clause {
    private final Functor head;
    private final List<BodyElement> body;

    public Clause(Functor head, List<BodyElement> body) {
        this.head = Objects.requireNonNull(head);
        this.body = List.copyOf(body);
    }

    public Functor head() {
        return head;
    }

    public List<BodyElement> body() {
        return body;
    }

    public void evaluateBody(BindingEnvironment env, Builtins builtins) {
        for (BodyElement be : body) {
            if (be.isRunnable()) {
                be.execute(env, builtins);
            } else {
                // Non-runnable body elements placeholders for a full Prolog engine.
            }
        }
    }

    public String toSource() {
        StringBuilder sb = new StringBuilder();
        sb.append(head.toSource());
        if (!body.isEmpty()) {
            sb.append(" :- ");
            for (int i = 0; i < body.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(body.get(i).toSource());
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toSource();
    }
}

