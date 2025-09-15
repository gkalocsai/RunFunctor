package hu.kg.runfunctor.core;

import java.security.SecureRandom;
import java.util.Objects;

public final class Thing implements Term {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ";
    private static final SecureRandom RND = new SecureRandom();

    private final String content;

    public Thing(String content) {
        this.content = Objects.requireNonNull(content);
    }

    public static Thing newRandom() {        
        int len = 12;
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHABET.charAt(RND.nextInt(ALPHABET.length())));
        }
        return new Thing(sb.toString());
    }

    public String content() {
        return content;
    }

    @Override
    public Term resolve(BindingEnvironment env) {
        return this;
    }

    @Override
    public String toSource() {
        return "@" + content + "@";
    }

    @Override
    public String toString() {
        return toSource();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Thing t) && t.content.equals(this.content);
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }
}


