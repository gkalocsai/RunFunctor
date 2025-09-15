package demo;

import java.util.*;

import hu.kg.runfunctor.core.BodyElement;
import hu.kg.runfunctor.core.Builtins;
import hu.kg.runfunctor.core.Clause;
import hu.kg.runfunctor.core.ClauseParser;
import hu.kg.runfunctor.core.Program;
import hu.kg.runfunctor.core.QueryParser;
import hu.kg.runfunctor.core.Solver;
import hu.kg.runfunctor.core.Term;



public final class DemoSolve {
    public static void main(String[] args) {
        // Build a small program
        String[] clauses = {
            "parent(\"john\",\"mary\").",
            "parent(\"mary\",\"sue\").",
            "ancestor(X,Y) :- parent(X,Y).",
            "ancestor(X,Y) :- parent(X,Z), ancestor(Z,Y)."
        };

        Program program = new Program();
        for (String csrc : clauses) {
            Clause c = ClauseParser.parse(csrc);
            program.add(c);
        }

        Builtins builtins = new Builtins();
        Solver solver = new Solver(program, builtins);

        // Query: find descendants of "john", also build a greeting with !CONCAT into an overrideable var
        String qsrc = "ancestor(\"john\",Y), !CONCAT(\"Hello \", Y)->*greet.";
        List<BodyElement> query = QueryParser.parse(qsrc);

        System.out.println("Query: " + qsrc);
        System.out.println("Solutions:");
        solver.solve(query, solution -> {
            Map<String, Term> visible = Solver.filterQueryVars(solution);
            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, Term> e : visible.entrySet()) {
                parts.add(e.getKey() + "=" + e.getValue().toSource());
            }
            System.out.println("  {" + String.join(", ", parts) + "}");
        });

        // Show runnable override behavior with backtracking
        String q2 = "!NEW()->*t, !CONCAT(@id:@, *t)->idStr.";
        System.out.println("\nQuery: " + q2);
        solver.solve(QueryParser.parse(q2), solution -> {
            Map<String, Term> visible = Solver.filterQueryVars(solution);
            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, Term> e : visible.entrySet()) {
                parts.add(e.getKey() + "=" + e.getValue().toSource());
            }
            System.out.println("  {" + String.join(", ", parts) + "}");
        });
    }
}