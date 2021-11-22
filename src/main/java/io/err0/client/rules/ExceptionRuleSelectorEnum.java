package io.err0.client.rules;

public enum ExceptionRuleSelectorEnum {
    NO_SELECTOR(0),
    EXCEPTION_CLASS(1),
    CODE_SNIPPET(2),
    LINE_OF_CODE(3),
    ATTRIBUTE(4);

    private final int value;

    ExceptionRuleSelectorEnum(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }

    public static ExceptionRuleSelectorEnum forValue(int value) {
        switch (value) {
            case 0: return NO_SELECTOR;
            case 1: return EXCEPTION_CLASS;
            case 2: return CODE_SNIPPET;
            case 3: return LINE_OF_CODE;
            case 4: return ATTRIBUTE;
            default: throw new RuntimeException();
        }
    }
}
