package hu.kg.runfunctor.core;
import java.security.SecureRandom;
import java.util.*;

/***
 * Renames colliding Prolog variables across namespaces separated by one or more blank lines.
 * - Variables: [A-Z_][A-Za-z0-9_]*, "_" alone is anonymous and never renamed.
 * - Namespaces: segments of code separated by one or more entirely blank lines (only whitespace),
 *   counted only outside comments/quotes/@...@.
 * - Ignores variables inside: line comments (% ... \n), block comments (/*),
 *   single-quoted atoms ('...'), double-quoted strings ("..."), and @...@ segments.
 * - Collision policy: only variables that appear in a later namespace with a name already used
 *   in any earlier namespace are renamed.
 * - Renaming: X -> X<random ascii letters and digits>, ensuring the new name does NOT collide
 *   with any variable name anywhere in the input (nor with other generated names).
 */
public final class Namespace {

    private Namespace() {}

    public static String renameCollidingVariables(String prologCode) {
        // Pass 1: collect variables per namespace and all original variable names.
        List<Set<String>> nsVars = collectNamespaceVariables(prologCode);
        Set<String> allOriginalVars = new HashSet<>();
        for (Set<String> s : nsVars) allOriginalVars.addAll(s);

        // Compute per-namespace rename maps for collisions with any previous namespace.
        List<Map<String, String>> renameMaps = new ArrayList<>(nsVars.size());
        Set<String> generatedNames = new HashSet<>();
        Set<String> seenOriginals = new HashSet<>();

        SecureRandom rng = new SecureRandom();

        for (int i = 0; i < nsVars.size(); i++) {
            Set<String> vars = nsVars.get(i);
            Map<String, String> map = new HashMap<>();
            for (String v : vars) {
                if (seenOriginals.contains(v)) {
                    String renamed = uniqueRenamed(v, allOriginalVars, generatedNames, rng);
                    map.put(v, renamed);
                    generatedNames.add(renamed);
                }
            }
            renameMaps.add(map);
            seenOriginals.addAll(vars);
        }

        // Pass 2: apply renames.
        return applyRenames(prologCode, renameMaps);
    }

    // ---------- Helpers ----------

    private enum Mode { DEFAULT, LINE_COMMENT, BLOCK_COMMENT, SINGLE_QUOTE, DOUBLE_QUOTE, AT_TEXT }

    private static List<Set<String>> collectNamespaceVariables(String s) {
        List<Set<String>> nsVars = new ArrayList<>();
        nsVars.add(new HashSet<>()); // start with first namespace
        int nsIndex = 0;

        Mode mode = Mode.DEFAULT;
        boolean startNewNsOnNextCode = false;
        boolean lineBlankCandidate = true; // only tracks DEFAULT mode
        int n = s.length();

        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);

            // Mode transitions first, but some depend on DEFAULT checks
            switch (mode) {
                case DEFAULT:
                    // Handle namespace boundary start trigger
                    if (startNewNsOnNextCode && !Character.isWhitespace(c)) {
                        nsVars.add(new HashSet<>());
                        nsIndex++;
                        startNewNsOnNextCode = false;
                    }

                    // Comments/quotes/@
                    if (c == '%') {
                        mode = Mode.LINE_COMMENT;
                        // '%' is non-whitespace, mark line as non-blank
                        lineBlankCandidate = false;
                        continue;
                    }
                    if (c == '/' && i + 1 < n && s.charAt(i + 1) == '*') {
                        mode = Mode.BLOCK_COMMENT;
                        lineBlankCandidate = false;
                        i++; // consume '*'
                        continue;
                    }
                    if (c == '\'') {
                        mode = Mode.SINGLE_QUOTE;
                        lineBlankCandidate = false;
                        continue;
                    }
                    if (c == '"') {
                        mode = Mode.DOUBLE_QUOTE;
                        lineBlankCandidate = false;
                        continue;
                    }
                    if (c == '@') {
                        mode = Mode.AT_TEXT;
                        lineBlankCandidate = false;
                        continue;
                    }

                    // Track blank line status on DEFAULT content
                    if (!Character.isWhitespace(c)) {
                        lineBlankCandidate = false;
                    }

                    // Namespace splitting on a blank line: trigger on LF only
                    if (c == '\n') {
                        if (lineBlankCandidate) {
                            startNewNsOnNextCode = true; // saw a blank line
                        } else {
                            startNewNsOnNextCode = false;
                        }
                        lineBlankCandidate = true; // new line starts blank candidate
                    }

                    // Variable detection
                    if (isVarStart(c)) {
                        char prev = (i > 0) ? s.charAt(i - 1) : '\0';
                        if (!isWordChar(prev)) {
                            int j = i + 1;
                            while (j < n && isWordChar(s.charAt(j))) j++;
                            String var = s.substring(i, j);
                            if (!"_".equals(var)) {
                                nsVars.get(nsIndex).add(var);
                            }
                            i = j - 1; // advance
                        }
                    }
                    break;

                case LINE_COMMENT:
                    if (c == '\n') {
                        mode = Mode.DEFAULT;
                        // New line (after comment) is blank candidate until non-whitespace code seen
                        lineBlankCandidate = true;
                        startNewNsOnNextCode = false; // a comment line is not blank
                    }
                    break;

                case BLOCK_COMMENT:
                    if (c == '*' && i + 1 < n && s.charAt(i + 1) == '/') {
                        mode = Mode.DEFAULT;
                        i++; // consume '/'
                    }
                    break;

                case SINGLE_QUOTE:
                    if (c == '\\') {
                        // escape next char if any
                        if (i + 1 < n) i++;
                    } else if (c == '\'') {
                        // Prolog often allows '' as escaped quote inside atoms
                        if (i + 1 < n && s.charAt(i + 1) == '\'') {
                            i++; // consume second quote, stay in SINGLE_QUOTE
                        } else {
                            mode = Mode.DEFAULT;
                        }
                    }
                    break;

                case DOUBLE_QUOTE:
                    if (c == '\\') {
                        if (i + 1 < n) i++;
                    } else if (c == '"') {
                        mode = Mode.DEFAULT;
                    }
                    break;

                case AT_TEXT:
                    if (c == '@') {
                        mode = Mode.DEFAULT;
                    }
                    // newlines inside @...@ do not affect namespace detection
                    break;
            }
        }

        return nsVars;
    }

    private static String applyRenames(String s, List<Map<String, String>> renameMaps) {
        StringBuilder out = new StringBuilder(s.length() + 64);

        Mode mode = Mode.DEFAULT;
        boolean startNewNsOnNextCode = false;
        boolean lineBlankCandidate = true;
        int nsIndex = 0;
        int n = s.length();

        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);

            switch (mode) {
                case DEFAULT:
                    if (startNewNsOnNextCode && !Character.isWhitespace(c)) {
                        nsIndex = Math.min(nsIndex + 1, renameMaps.size() - 1);
                        startNewNsOnNextCode = false;
                    }

                    if (c == '%') {
                        mode = Mode.LINE_COMMENT;
                        lineBlankCandidate = false;
                        out.append(c);
                        continue;
                    }
                    if (c == '/' && i + 1 < n && s.charAt(i + 1) == '*') {
                        mode = Mode.BLOCK_COMMENT;
                        lineBlankCandidate = false;
                        out.append(c).append(s.charAt(i + 1));
                        i++;
                        continue;
                    }
                    if (c == '\'') {
                        mode = Mode.SINGLE_QUOTE;
                        lineBlankCandidate = false;
                        out.append(c);
                        continue;
                    }
                    if (c == '"') {
                        mode = Mode.DOUBLE_QUOTE;
                        lineBlankCandidate = false;
                        out.append(c);
                        continue;
                    }
                    if (c == '@') {
                        mode = Mode.AT_TEXT;
                        lineBlankCandidate = false;
                        out.append(c);
                        continue;
                    }

                    if (!Character.isWhitespace(c)) {
                        lineBlankCandidate = false;
                    }
                    if (c == '\n') {
                        if (lineBlankCandidate) {
                            startNewNsOnNextCode = true;
                        } else {
                            startNewNsOnNextCode = false;
                        }
                        lineBlankCandidate = true;
                        out.append(c);
                        continue;
                    }

                    if (isVarStart(c)) {
                        char prev = (i > 0) ? s.charAt(i - 1) : '\0';
                        if (!isWordChar(prev)) {
                            int j = i + 1;
                            while (j < n && isWordChar(s.charAt(j))) j++;
                            String var = s.substring(i, j);
                            String replacement = var;
                            if (!"_".equals(var)) {
                                Map<String, String> map = renameMaps.isEmpty() ? Collections.emptyMap() : renameMaps.get(Math.min(nsIndex, renameMaps.size() - 1));
                                String renamed = map.get(var);
                                if (renamed != null) {
                                    replacement = renamed;
                                }
                            }
                            out.append(replacement);
                            i = j - 1;
                            continue;
                        }
                    }

                    out.append(c);
                    break;

                case LINE_COMMENT:
                    out.append(c);
                    if (c == '\n') {
                        mode = Mode.DEFAULT;
                        lineBlankCandidate = true;
                        startNewNsOnNextCode = false; // comment line is not blank
                    }
                    break;

                case BLOCK_COMMENT:
                    out.append(c);
                    if (c == '*' && i + 1 < n && s.charAt(i + 1) == '/') {
                        out.append(s.charAt(i + 1));
                        i++;
                        mode = Mode.DEFAULT;
                    }
                    break;

                case SINGLE_QUOTE:
                    out.append(c);
                    if (c == '\\') {
                        if (i + 1 < n) {
                            out.append(s.charAt(i + 1));
                            i++;
                        }
                    } else if (c == '\'') {
                        if (i + 1 < n && s.charAt(i + 1) == '\'') {
                            out.append(s.charAt(i + 1));
                            i++; // stay in SINGLE_QUOTE
                        } else {
                            mode = Mode.DEFAULT;
                        }
                    }
                    break;

                case DOUBLE_QUOTE:
                    out.append(c);
                    if (c == '\\') {
                        if (i + 1 < n) {
                            out.append(s.charAt(i + 1));
                            i++;
                        }
                    } else if (c == '"') {
                        mode = Mode.DEFAULT;
                    }
                    break;

                case AT_TEXT:
                    out.append(c);
                    if (c == '@') {
                        mode = Mode.DEFAULT;
                    }
                    break;
            }
        }

        return out.toString();
    }

    private static boolean isVarStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '*';
    }

    private static boolean isWordChar(char c) {
        return (c >= 'A' && c <= 'Z')
            || (c >= 'a' && c <= 'z')
            || (c >= '0' && c <= '9')
            || c == '_';
    }

    private static final char[] ALNUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private static String uniqueRenamed(String base, Set<String> allOriginalVars,
                                        Set<String> generatedNames, SecureRandom rng) {
        int len = 1;
        while (true) {
            String suffix = randomString(rng, len);
            String candidate = base + suffix;
            if (!allOriginalVars.contains(candidate) && !generatedNames.contains(candidate)) {
                return candidate;
            }
            len++;
        }
    }

    private static String randomString(SecureRandom rng, int len) {
        char[] buf = new char[len];
        for (int i = 0; i < len; i++) {
            buf[i] = ALNUM[rng.nextInt(ALNUM.length)];
        }
        return new String(buf);
    }
}