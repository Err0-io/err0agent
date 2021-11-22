package io.err0.client.core;

import com.google.gson.JsonObject;

import java.util.function.Consumer;

public class Token {

    public Token(final int n) {
        this.n = n;
    }

    public final int n;

    public TokenClassification type;
    public StringBuilder sourceCode = new StringBuilder();
    public String initialSource = null;
    public String source = null;
    public String sourceNoErrorCode = null;
    public int depth = 0;
    public int startLineNumber = 0;
    public int lastLineNumber = 0;

    public Consumer<Void> errorCodeConsumer = null;

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

    public boolean getChanged() { return ! (this.initialSource.hashCode() == this.source.hashCode() && this.initialSource.equals(this.source)); }
}
