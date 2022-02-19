package io.err0.client.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

public abstract class SourceCodeParse {

    public SourceCodeParse(final Language language) {
        this.language = language;
    }

    public final Language language;

    public enum Language {
        JAVA, C_SHARP, GOLANG, PYTHON, JAVASCRIPT, TYPESCRIPT, PHP
    }

    public final ArrayList<Token> tokenList = new ArrayList<>();

    public static Pattern reLeadingWhitespace = Pattern.compile("^\\s+", Pattern.MULTILINE);

    /**
     * first pass classification - error code container, log or exception?
     * @param token
     */
    public abstract void classifyForErrorCode(ApiProvider apiProvider, GlobalState globalState, ProjectPolicy policy, StateItem stateItem, Token token);

    /**
     * second pass classification - is this suitable for the call stack?
     * assumes first pass has already run
     * @param token
     */
    public abstract void classifyForCallStack(Token token);

    public abstract boolean couldContainErrorNumber(Token token);

    public final JsonArray getNLinesOfContext(final int lineNumber, final int nLines, final int charRadius) {
        if (nLines < 0) {
            System.err.println("Invalid number of lines of context = " + nLines);
            System.exit(-1);
        }
        if (charRadius < 0) {
            System.err.println("Invalid number of chars of context = " + charRadius);
            System.exit(-1);
        }
        int startLineNumber = lineNumber - nLines;
        int endLineNumber = lineNumber + nLines;

        JsonArray context = new JsonArray();
        StringBuilder currentLineContent = new StringBuilder();
        int currentLine = 1;

        for (Token token : tokenList) {
            boolean abort = false;
            char chars[] = token.source.toCharArray();
            int n = 0;
            for (char ch : chars) {
                if (startLineNumber <= currentLine && currentLine <= endLineNumber) {
                    if (++n >= charRadius) { abort = true; context = new JsonArray(); break; }
                    currentLineContent.append(ch);
                }
                if (ch == '\n') {
                    if (currentLine == endLineNumber) {
                        JsonObject o = new JsonObject();
                        o.addProperty("l", currentLine);
                        o.addProperty("c", currentLineContent.toString());
                        context.add(o);
                        currentLineContent = null;
                        abort = true;
                        break;
                    } else if (startLineNumber <= currentLine && currentLine < endLineNumber) {
                        JsonObject o = new JsonObject();
                        o.addProperty("l", currentLine);
                        o.addProperty("c", currentLineContent.toString());
                        context.add(o);
                        currentLineContent = new StringBuilder();
                    }
                    ++currentLine;
                }
            }
            if (abort) break;
        }

        if (startLineNumber <= currentLine && currentLine <= endLineNumber && currentLineContent != null && currentLineContent.length() > 0) {
            JsonObject o = new JsonObject();
            o.addProperty("l", currentLine);
            o.addProperty("c", currentLineContent.toString());
            context.add(o);
        }

        return context;
    }
}
