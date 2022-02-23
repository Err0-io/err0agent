package io.err0.client.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class LanguageCodePolicy {

    public enum LoggerRuleType
    {
        LINE_SHOULD_MATCH(0),
        LINE_MUST_NOT_MATCH(1),
        LITERAL_SHOULD_MATCH(2),
        LITERAL_MUST_NOT_MATCH(3);

        private final int value;
        private LoggerRuleType(int value) { this.value = value; }

        public int getValue() { return value; }

        public static LoggerRuleType ofValue(int value) {
            switch (value) {
                case 0:
                    return LINE_SHOULD_MATCH;
                case 1:
                    return LINE_MUST_NOT_MATCH;
                case 2:
                    return LITERAL_SHOULD_MATCH;
                case 3:
                    return LITERAL_MUST_NOT_MATCH;
                default:
                    throw new RuntimeException();
            }
        }
    }

    public static class LoggerRule {
        LoggerRuleType type = LoggerRuleType.LINE_SHOULD_MATCH;
        ArrayList<Pattern> pattern = new ArrayList<>();
    }

    public LanguageCodePolicy(final JsonObject languagePolicyJson)
    {
        if (null != languagePolicyJson) {
            this.disable_language = GsonHelper.getAsBoolean(languagePolicyJson, "disable_language", false);
            this.disable_builtin_log_detection = GsonHelper.getAsBoolean(languagePolicyJson, "disable_builtin_log_detection", false);
            var rules = languagePolicyJson.get("rules");
            if (null != rules) {
                JsonArray ary = rules.getAsJsonArray();
                for (int i = 0, l = ary.size(); i < l; ++i) {
                    JsonObject rule = ary.get(i).getAsJsonObject();
                    int type = GsonHelper.getAsInt(rule, "type", 0);
                    LoggerRule loggerRule = new LoggerRule();
                    loggerRule.type = LoggerRuleType.ofValue(type);
                    JsonArray patternAry = rule.getAsJsonArray("pattern");
                    for (int j = 0, m = patternAry.size(); j < m; ++j) {
                        String pattern = patternAry.get(j).getAsString();
                        if (null != pattern && !"".equals(pattern.trim())) {
                            loggerRule.pattern.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE));
                        }
                    }
                    if (loggerRule.pattern.size() > 0) {
                        this.rules.add(loggerRule);
                    }
                }
            }
        }
    }

    public boolean disable_language = false;
    public boolean disable_builtin_log_detection = false;
    public ArrayList<LoggerRule> rules = new ArrayList<>();

    public Token.Classification classify(String lineOfCode, String stringLiteral) {
        Token.Classification classification = Token.Classification.NOT_FULLY_CLASSIFIED;
        for (LoggerRule rule : rules) {
            switch (rule.type) {
                case LINE_SHOULD_MATCH:
                    if (null == lineOfCode) continue;
                    for (Pattern p : rule.pattern) {
                        if (p.matcher(lineOfCode).find())
                            classification = Token.Classification.LOG_OUTPUT;
                    }
                    break;
                case LINE_MUST_NOT_MATCH:
                    if (null == lineOfCode) continue;
                    for (Pattern p : rule.pattern) {
                        if (p.matcher(lineOfCode).find())
                            return Token.Classification.NO_MATCH;
                    }
                    break;
                case LITERAL_SHOULD_MATCH:
                    if (null == stringLiteral) continue;
                    for (Pattern p : rule.pattern) {
                        if (p.matcher(stringLiteral).find())
                            classification = Token.Classification.LOG_OUTPUT;
                    }
                    break;
                case LITERAL_MUST_NOT_MATCH:
                    if (null == stringLiteral) continue;
                    for (Pattern p : rule.pattern) {
                        if (p.matcher(stringLiteral).find())
                            return Token.Classification.NO_MATCH;
                    }
                    break;
                default:
                    throw new RuntimeException();
            }
        }
        return classification;
    }
}
