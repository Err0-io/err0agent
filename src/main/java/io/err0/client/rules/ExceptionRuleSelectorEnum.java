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
            default: throw new RuntimeException("[AGENT-000026] Unexpected exception rule selector value=" + value);
        }
    }
}
