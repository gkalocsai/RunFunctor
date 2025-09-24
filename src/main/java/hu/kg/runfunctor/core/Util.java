package hu.kg.runfunctor.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Util {
    private Util() {}

    public static List<String> splitAndremoveComments(String whole) throws IOException {
        
    
    	List<String> lines=splitByNewLine(whole);
        
        List<String> linesWithOutComments = new ArrayList<>();
        boolean isMultilineComment = false;
        for ( String line :lines) {
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
    public static String readFileToString(String filePath) {
        try  {
            Path path = Paths.get(filePath);
            Stream<String> lines = Files.lines(path);
            String data = lines.collect(Collectors.joining("\n"));
            lines.close();
            return data;
         } catch (IOException e)  {
             e.printStackTrace();
             return null;
         }
     }

    public static List<String> splitByNewLine(String input) {
        List<String> lines = new ArrayList<>();
        
        int startIndex = 0;
        for (int currentIndex = 0; currentIndex < input.length(); ++currentIndex) {
            if (input.charAt(currentIndex) == '\n' || input.charAt(currentIndex) == '\r') {
                // If it's a newline character, add the line to our list and move start index
                lines.add(input.substring(startIndex, currentIndex));
                startIndex = currentIndex + 1;
                
                // Check for Windows style line endings ("\r\n")
                if (currentIndex + 1 < input.length() && input.charAt(currentIndex) == '\r' && input.charAt(currentIndex + 1) == '\n') {
                    ++startIndex;  // Skip the next character as it is a part of "\r\n" pair
                }
            }
        }
        
        // Add last line
        if (startIndex < input.length()) {
            lines.add(input.substring(startIndex));
        }
        
        return lines;
    }
    
}

