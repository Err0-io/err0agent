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

package io.err0.client.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.err0.client.core.SourceCodeParse;
import io.err0.client.core.Token;

import java.util.regex.Pattern;

public class ExceptionRuleSelection {
    public ExceptionRuleSelection(final ExceptionRuleSelectorEnum selector, final String selectorValue, final ExceptionRuleCheckEnum check, final String checkValue) {
        this.selector = selector;
        this.selectorValue = selectorValue;
        this.check = check;
        this.checkValue = checkValue;
        switch (this.check) {
            case MATCHES_REGEX:
            case DOES_NOT_MATCH_REGEX:
                checkPattern = Pattern.compile(this.checkValue);
                break;
            default:
                checkPattern = null;
        }
    }
    public final ExceptionRuleSelectorEnum selector;
    public final String selectorValue;
    public final ExceptionRuleCheckEnum check;
    public final String checkValue;
    public final Pattern checkPattern;

    public boolean isMatch(SourceCodeParse.Language language, Token token) {
        switch (selector) {
            case NO_SELECTOR:
                return false;
            case EXCEPTION_CLASS:
                JsonElement exception_class = token.metaData.get("exception_class");
                if (exception_class.isJsonNull()) return false;
                return check(exception_class.getAsString());
            case CODE_SNIPPET: {
                JsonElement context = token.metaData.get("context");
                if (context.isJsonNull()) return false;
                JsonArray ary = context.getAsJsonArray();
                StringBuilder contextBlob = new StringBuilder();
                for (int i=0, l=ary.size(); i<l; ++i) contextBlob.append(ary.get(i).getAsString());
                return check(contextBlob.toString());
            }
            case LINE_OF_CODE: {
                JsonElement lineOfCode = token.metaData.get("line_of_code");
                if (lineOfCode.isJsonNull()) return false;
                return check(lineOfCode.getAsString());
            }
            case ATTRIBUTE: {
                if (null == selectorValue) return false;
                JsonElement attr = token.metaData.get(selectorValue);
                if (attr.isJsonNull()) return false;
                return check(attr.getAsString());
            }
        }
        throw new RuntimeException("[AGENT-000024] Unhandled selector = " + selector.name());
    }

    public boolean check(String value) {
        if (null == value || null == checkValue) {
            return false;
        }
        switch (check) {
            case NO_CHECK:
                return false;
            case EQUALS:
                return value.equals(checkValue);
            case DOES_NOT_EQUAL:
                return ! value.equals(checkValue);
            case MATCHES_REGEX:
                return checkPattern.matcher(value).find();
            case DOES_NOT_MATCH_REGEX:
                return ! checkPattern.matcher(value).find();
        }
        throw new RuntimeException("[AGENT-000025] Unhandled check = " + check.name());
    }
}
