package io.err0.client.rules;

public enum ExceptionRuleCheckEnum {
    NO_CHECK(0),
    EQUALS(1),
    DOES_NOT_EQUAL(2),
    MATCHES_REGEX(3),
    DOES_NOT_MATCH_REGEX(4);

    private final int value;

    ExceptionRuleCheckEnum(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }

    public static ExceptionRuleCheckEnum forValue(int value) {
        switch (value) {
            case 0: return NO_CHECK;
            case 1: return EQUALS;
            case 2: return DOES_NOT_EQUAL;
            case 3: return MATCHES_REGEX;
            case 4: return DOES_NOT_MATCH_REGEX;
            default: throw new RuntimeException();
        }
    }
}
