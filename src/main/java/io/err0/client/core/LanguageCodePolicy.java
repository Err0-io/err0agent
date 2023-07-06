/*
Copyright 2022 BlueTrailSoftware, Holding Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package io.err0.client.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
                    throw new RuntimeException("[AGENT-000018] Unexpected logger rule type value="+value);
            }
        }
    }

    public static class LoggerRule {
        LoggerRuleType type = LoggerRuleType.LINE_SHOULD_MATCH;
        ArrayList<Pattern> pattern = new ArrayList<>();
        String level = null;
    }

    public LanguageCodePolicy(final JsonObject languagePolicyJson, final CodePolicy codePolicy)
    {
        this.codePolicy = codePolicy;
        if (null != languagePolicyJson) {
            this.disable_language = GsonHelper.asBoolean(languagePolicyJson, "disable_language", false);
            this.disable_builtin_log_detection = GsonHelper.asBoolean(languagePolicyJson, "disable_builtin_log_detection", false);
            JsonElement rules = languagePolicyJson.get("rules");
            if (null != rules) {
                JsonArray ary = rules.getAsJsonArray();
                for (int i = 0, l = ary.size(); i < l; ++i) {
                    JsonObject rule = ary.get(i).getAsJsonObject();
                    int type = GsonHelper.asInt(rule, "type", 0);
                    LoggerRule loggerRule = new LoggerRule();
                    loggerRule.type = LoggerRuleType.ofValue(type);
                    JsonArray patternAry = rule.getAsJsonArray("pattern");
                    for (int j = 0, m = patternAry.size(); j < m; ++j) {
                        String pattern = patternAry.get(j).getAsString();
                        if (null != pattern && !"".equals(pattern.trim())) {
                            loggerRule.pattern.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE));
                        }
                    }
                    loggerRule.level = rule.has("level") && !rule.get("level").isJsonNull() ? rule.get("level").getAsString() : null;
                    if (loggerRule.pattern.size() > 0) {
                        this.rules.add(loggerRule);
                    }
                }
            }
        }
    }

    final CodePolicy codePolicy;

    public boolean disable_language = false;
    public boolean disable_builtin_log_detection = false;
    public ArrayList<LoggerRule> rules = new ArrayList<>();

    public static class ClassificationResult {
        public ClassificationResult(Token.Classification classification, String loggerLevel) {
            this.classification = classification;
            this.loggerLevel = loggerLevel;
        }
        public Token.Classification classification;
        public String loggerLevel;
    }

    public ClassificationResult classify(String lineOfCode, String stringLiteral) {
        Token.Classification classification = Token.Classification.NOT_FULLY_CLASSIFIED;
        if (codePolicy.getDisableLogs()) {
            return new ClassificationResult(classification, null);
        }

        String loggerLevel = null;
        for (LoggerRule rule : rules) {
            switch (rule.type) {
                case LINE_SHOULD_MATCH:
                    if (null == lineOfCode) continue;
                    for (Pattern p : rule.pattern) {
                        if (p.matcher(lineOfCode).find()) {
                            classification = Token.Classification.LOG_OUTPUT;
                            loggerLevel = rule.level;
                        }
                    }
                    break;
                case LINE_MUST_NOT_MATCH:
                    if (null == lineOfCode) continue;
                    for (Pattern p : rule.pattern) {
                        if (p.matcher(lineOfCode).find())
                            return new ClassificationResult(Token.Classification.NOT_LOG_OUTPUT, null);
                    }
                    break;
                case LITERAL_SHOULD_MATCH:
                    if (null == stringLiteral) continue;
                    for (Pattern p : rule.pattern) {
                        if (p.matcher(stringLiteral).find()) {
                            classification = Token.Classification.MAYBE_LOG_OR_EXCEPTION;
                            loggerLevel = rule.level;
                        }
                    }
                    break;
                case LITERAL_MUST_NOT_MATCH:
                    if (null == stringLiteral) continue;
                    for (Pattern p : rule.pattern) {
                        if (p.matcher(stringLiteral).find()) {
                            return new ClassificationResult(Token.Classification.NOT_LOG_OUTPUT, null);
                        }
                    }
                    break;
                default:
                    throw new RuntimeException("[AGENT-000019] Unexpected logger rule type " + rule.type.name());
            }
        }
        return new ClassificationResult(classification, loggerLevel);
    }
}
