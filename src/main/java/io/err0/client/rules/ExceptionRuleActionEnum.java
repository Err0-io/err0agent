package io.err0.client.rules;

public enum ExceptionRuleActionEnum {
    NO_ACTION(0),
    NO_ERROR_NUMBER(1);

    private final int value;

    ExceptionRuleActionEnum(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }

    public static ExceptionRuleActionEnum forValue(int value) {
        switch (value) {
            case 0: return NO_ACTION;
            case 1: return NO_ERROR_NUMBER;
            default: throw new RuntimeException();
        }
    }
}
