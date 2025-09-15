package hu.kg.runfunctor.core;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

final class Util {
    private Util() {}

    public static List<String> readFileWithoutComments(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
        List<String> linesWithOutComments = new ArrayList<>();

        String line;
        boolean isMultilineComment = false;
        while ((line = reader.readLine()) != null) {
            int commentIndex = line.indexOf("//");  // Find the index of "//" in each line
            
            if (!isMultilineComment && commentIndex != -1) {  // If there is a "//", remove the part after it from the line
                line = line.substring(0, commentIndex);
            } else if (commentIndex == -1){ // Check if start of multi-line comment exists
                int multilineCommentStartIndex = line.indexOf("/*"); 
                if (multilineCommentStartIndex != -1) { // If there is a "/*", remove the part after it from the line
                    String trimmedLineBeforeComment = line.substring(0, multilineCommentStartIndex);
                    int multilineCommentEndIndex = line.indexOf("*/");
                    
                    if (multilineCommentEndIndex != -1) { // If there is a "*/", remove the part before it from the line
                        line = line.substring(multilineCommentEndIndex + 2);
                        linesWithOutComments.add(trimmedLineBeforeComment);
                    } else {  // The multi-line comment continues on the next line
                        isMultilineComment = true;
                    }
                } else if (isMultilineComment) { // The multi-line comment ends in this line
                    int multilineCommentEndIndex = line.indexOf("*/");
                    
                    if (multilineCommentEndIndex != -1) {  // If there is a "*/", remove the part before it from the line
                        line = line.substring(multilineCommentEndIndex + 2);
                        isMultilineComment = false;
                    } else {  // The multi-line comment continues on the next line
                        continue;
                    }
                }
            }
            
            if (!line.trim().isEmpty()) {  // Only add non-empty lines to our list (ignoring pure whitespace lines)
                linesWithOutComments.add(line.trim()); 
            }
        }
        
        reader.close();
        return linesWithOutComments;
    }
    static String escapeString(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '\"') sb.append('\\');
            sb.append(c);
        }
        return sb.toString();
    }

    static String unescapeString(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        boolean esc = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) {
                sb.append(c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else {
                sb.append(c);
            }
        }
        if (esc) sb.append('\\'); // trailing backslash literal
        return sb.toString();
    }
}

