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

import com.google.gson.JsonObject;

import java.util.function.Consumer;

public class Token {

    public Token(final int n, final Token prev) {
        this.n = n;
        this.prev = prev;
        if (null != this.prev) {
            this.prev.setNext(this);
        }
    }

    public final int n;
    public final Token prev;
    private Token next = null;
    private void setNext(Token next) { this.next = next; }
    public Token next() { return this.next; }

    public TokenClassification type;
    public StringBuilder sourceCode = new StringBuilder();
    public String initialSource = null;
    public String source = null;
    public String sourceNoErrorCode = null;
    public int depth = 0;
    public int startLineNumber = 0;
    public int lastLineNumber = 0;

    public Consumer<Void> errorCodeConsumer = null;

    // FIXME: per-language do the escaping calculation on this please
    public String getStringLiteral()
    {
        String s = null != sourceNoErrorCode ? sourceNoErrorCode : source;
        if (null == s) return null;

        switch (type) {
            case QUOT_LITERAL:
                return s.length() > 2 ? s.substring(1, s.length() - 2) : "";
            case APOS_LITERAL:
                return s.length() > 2 ? s.substring(1, s.length() - 2) : "";
            case QUOT3_LITERAL:
                return s.length() > 6 ? s.substring(3, s.length() - 6) : "";
            case BACKTICK_LITERAL:
                return s.length() > 2 ? s.substring(1, s.length() - 2) : "";
            default:
                throw new RuntimeException("[AGENT-000020] Not a string literal, type = " + type.name());
        }
    }
    public int getStringQuoteWidth()
    {
        switch (type)
        {
            case QUOT_LITERAL:
            case APOS_LITERAL:
            case BACKTICK_LITERAL:
                return 1;
            case QUOT3_LITERAL:
                return 3;
            default:
                throw new RuntimeException("[AGENT-000017] Not a string literal, type = " + type.name());
        }
    }

    public Token finish(int lineNumber) {
        this.initialSource = this.source = this.sourceCode.toString();
        this.sourceCode = null;
        this.lastLineNumber = lineNumber;
        return this;
    }

    public enum Classification {
        NOT_CLASSIFIED_YET,
        NOT_FULLY_CLASSIFIED,
        NO_MATCH,
        ERROR_NUMBER,
        POTENTIAL_ERROR_NUMBER,
        LOG_OUTPUT,
        MAYBE_LOG_OR_EXCEPTION,
        NOT_LOG_OUTPUT,
        EXCEPTION_THROW,
        CLASS_SIGNATURE,
        METHOD_SIGNATURE,
        LAMBDA_SIGNATURE
    }

    public Classification classification = Classification.NOT_CLASSIFIED_YET;
    public long errorOrdinal = -1;
    public boolean keepErrorCode = false;
    public String extractedCode = null;

    public boolean insertErrorCode = false;
    public JsonObject metaData = null;
    public Signature signature = null;

    public String exceptionClass = null;

    public String loggerLevel = null;

    public boolean getChanged() { return ! (this.initialSource.hashCode() == this.source.hashCode() && this.initialSource.equals(this.source)); }
}
