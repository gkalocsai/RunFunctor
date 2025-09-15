package hu.kg.runfunctor.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

	public static void main(String[] args) throws IOException {
        if (args.length != 2) {   // Check that we have exactly two arguments
            System.out.println("Usage: java Main <knowledgebase_file> <query_file>");
            return;
        }
        Program program = new Program();        
        List<String> lines = Util.readFileWithoutComments(args[0]);          
            for (String csrc : lines) {   
                Clause c = ClauseParser.parse(csrc);
                program.add(c);
            }
    
               
        Builtins builtins = new Builtins();
        Solver solver = new Solver(program, builtins);
        
        List<String> lines2 = Util.readFileWithoutComments(args[1]);
        List<BodyElement> query = QueryParser.parse(lines2.get(0));
        
        
        solver.solve(query, solution -> {
            Map<String, Term> visible = Solver.filterQueryVars(solution);
            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, Term> e : visible.entrySet()) {
                parts.add(e.getKey() + "=" + e.getValue().toSource());
            }
            System.out.println("  {" + String.join(", ", parts) + "}");
        });
    }

}
