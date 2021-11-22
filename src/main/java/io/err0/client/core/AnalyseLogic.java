package io.err0.client.core;

public interface AnalyseLogic {

    void pass2ResolveDuplicateErrorNumber(TokenStateItem item, long errorOrdinal);

    void pass2AssignNewErrorNumber(TokenStateItem item);

    void pass3AssignNewErrorNumber(Token currentToken);

    void pass3InsertExistingErrorNumber(StateItem stateItem, Token currentToken);

    void pass4CheckIfFileChanged(StateItem stateItem);

    void pass4ProcessResult(StateItem stateItem, String filename, SourceCodeParse parse);

    boolean returnValue();

}
