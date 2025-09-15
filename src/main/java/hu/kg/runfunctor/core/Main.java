package hu.kg.runfunctor.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

	public static void main(String[] args) throws IOException {
        if (args.length  == 0 || args.length > 2) {   // we have exactly one or two arguments
            System.out.println("Usage: java -jar functor.jar <query_file> [<knowledgebase_file>] ");
            return;
        }
        
        Program program = new Program();        

        if(args.length == 2) {
        List<String> lines = Util.readFileWithoutComments(args[1]);          
            for (String csrc : lines) {   
                Clause c = ClauseParser.parse(csrc);
                program.add(c);
            }
        }
               
        Builtins builtins = new Builtins();
        Solver solver = new Solver(program, builtins);
        
        List<String> lines2 = Util.readFileWithoutComments(args[0]);
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
