package hu.kg.runfunctor.core;

final class Lexer {
    private final String s;
    private int i;

    Lexer(String s) {
        this.s = s;
        this.i = 0;
    }

    Token next() {
        skipWs();
        if (i >= s.length()) return new Token(TokType.EOF, null, i);
        char c = s.charAt(i);
        int start = i;

        // Two-char operators
        if (c == ':' && peek(1) == '-') { i += 2; return new Token(TokType.MINUS, ":-", start); }
        if (c == '-' && peek(1) == '>') { i += 2; return new Token(TokType.ARROW, "->", start); }

        // Single-char tokens
        if (c == '(') { i++; return new Token(TokType.LPAREN, "(", start); }
        if (c == ')') { i++; return new Token(TokType.RPAREN, ")", start); }
        if (c == ',') { i++; return new Token(TokType.COMMA, ",", start); }
        if (c == '!') { i++; return new Token(TokType.BANG, "!", start); }
        if (c == '.') { i++; return new Token(TokType.DOT, ".", start); }

        // String literal
        if (c == '"') return readString();
        // Thing
        if (c == '@') return readThing();
        // Name or variable (may start with *)
        if (isNameStart(c) || c == '*') return readName();

        throw err("Unexpected char: " + c);
    }

    private Token readString() {
        int start = i++;
        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        while (i < s.length()) {
            char c = s.charAt(i++);
            if (esc) {
                sb.append(c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else if (c == '"') {
                return new Token(TokType.STRING, Util.unescapeString(sb.toString()), start);
            } else {
                sb.append(c);
            }
        }
        throw err("Unterminated string literal");
    }

    private Token readThing() {
        int start = i++;
        StringBuilder sb = new StringBuilder();
        while (i < s.length()) {
            char c = s.charAt(i++);
            if (c == '@') {
                return new Token(TokType.THING, sb.toString(), start);
            } else {
                sb.append(c);
            }
        }
        throw err("Unterminated thing literal @...@");
    }

    private Token readName() {
        int start = i;
        i++; // consume first
        while (i < s.length()) {
            char c = s.charAt(i);
            if (isNamePart(c)) i++;
            else break;
        }
        return new Token(TokType.NAME, s.substring(start, i), start);
    }

    private boolean isNameStart(char c) {
        return Character.isLetter(c);
    }

    private boolean isNamePart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private char peek(int k) {
        int j = i + k;
        return j < s.length() ? s.charAt(j) : '\0';
    }

    private void skipWs() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
    }

    private RuntimeException err(String msg) {
        return new RuntimeException(msg + " at " + i);
    }
}


