package io.err0.client.rules;

public class ExceptionRuleOperation {
    public ExceptionRuleOperation(final ExceptionRuleOperationEnum operation, final String operationValue) {
        this.operation = operation;
        this.operationValue = operationValue;
        switch (this.operation) {
            case SET_CODE_COMMENT:
                if (null == operationValue) {
                    throw new RuntimeException("[AGENT-000012] Needs a value.");
                }
                if (operationValue.contains("/*") ||
                    operationValue.contains("//") ||
                    operationValue.contains("*/") ||
                    operationValue.contains("#")
                ) {
                    throw new RuntimeException("[AGENT-000013] Invalid code comment: " + operationValue);
                }
                break;
        }
    }
    public final ExceptionRuleOperationEnum operation;
    public final String operationValue;
}
