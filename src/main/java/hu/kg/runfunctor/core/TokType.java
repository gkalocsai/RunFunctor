package hu.kg.runfunctor.core;

enum TokType {
    NAME, STRING, THING,
    LPAREN, RPAREN, COMMA,
    CUT, // not used (placeholder)
    COLON, MINUS, BANG, ARROW, DOT, EOF
}

final class Token {
    final TokType type;
    final String text;
    final int pos;

    Token(TokType type, String text, int pos) {
        this.type = type;
        this.text = text;
        this.pos = pos;
    }

    @Override
    public String toString() {
        return type + (text != null ? "(" + text + ")" : "");
    }
}


