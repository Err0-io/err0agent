/*
Copyright 2023 ERR0 LLC

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
            default: throw new RuntimeException("[AGENT-000022] Unexpected exception rule check value=" + value);
        }
    }
}
