package hu.kg.runfunctor.core;

import java.util.ArrayList;
import java.util.List;

public final class QueryParser {
    private final Lexer lex;
    private Token la;

    private QueryParser(String input) {
        this.lex = new Lexer(input);
        this.la = lex.next();
    }

    public static List<BodyElement> parse(String input) {
        return new QueryParser(input).parseGoals();
    }

    private List<BodyElement> parseGoals() {
        List<BodyElement> goals = new ArrayList<>();
        if (!accept(".")) {
            goals.add(parseBodyElement());
            while (accept(",")) {
                goals.add(parseBodyElement());
            }
            accept("."); // optional trailing dot
        }
        return goals;
    }

    private BodyElement parseBodyElement() {
        if (acceptBang()) {
            String name = expectName("builtin name after '!'");
            expect("(");
            List<Term> args = new ArrayList<>();
            if (!accept(")")) {
                do {
                    args.add(parseTerm());
                } while (accept(","));
                expect(")");
            }
            expect("->");
            Variable var = parseVariableFromName(expectName("variable after '->'"));
            return new RunnableAssignment(name, args, var);
        } else {
            return parseFunctor();
        }
    }

    private Functor parseFunctor() {
        String name = expectName("functor name");
        expect("(");
        List<Term> args = new ArrayList<>();
        if (!accept(")")) {
            do {
                args.add(parseTerm());
            } while (accept(","));
            expect(")");
        }
        return new Functor(name, args);
    }

    private Term parseTerm() {
        if (la.type == TokType.STRING) {
            String v = la.text;
            consume();
            return new Constant(v);
        } else if (la.type == TokType.THING) {
            String v = la.text;
            consume();
            return new Thing(v);
        } else if (la.type == TokType.NAME) {
            String name = la.text;
            consume();
            if (accept("(")) {
                List<Term> args = new ArrayList<>();
                if (!accept(")")) {
                    do {
                        args.add(parseTerm());
                    } while (accept(","));
                    expect(")");
                }
                return new Functor(name, args);
            } else {
                return parseVariableFromName(name);
            }
        } else {
            throw err("Expected term, got: " + la);
        }
    }

    private Variable parseVariableFromName(String nameToken) {
        return new Variable(nameToken);
    }

    private boolean accept(String sym) {
        if (sym.equals("->") && la.type == TokType.ARROW) { consume(); return true; }
        if (sym.equals("(") && la.type == TokType.LPAREN) { consume(); return true; }
        if (sym.equals(")") && la.type == TokType.RPAREN) { consume(); return true; }
        if (sym.equals(",") && la.type == TokType.COMMA) { consume(); return true; }
        if (sym.equals(".") && la.type == TokType.DOT) { consume(); return true; }
        return false;
    }

    private boolean acceptBang() {
        if (la.type == TokType.BANG) { consume(); return true; }
        return false;
    }

    private void expect(String sym) {
        if (!accept(sym)) throw err("Expected '" + sym + "', got: " + la);
    }

    private String expectName(String ctx) {
        if (la.type != TokType.NAME) throw err("Expected " + ctx + ", got: " + la);
        String n = la.text;
        consume();
        return n;
    }

    private void consume() {
        la = lex.next();
    }

    private RuntimeException err(String msg) {
        return new RuntimeException("Parse error: " + msg);
    }
}


