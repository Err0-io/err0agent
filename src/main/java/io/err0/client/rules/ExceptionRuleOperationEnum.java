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

public enum ExceptionRuleOperationEnum {
    NO_OPERATION(0),
    SET_ERROR_SEVERITY_LEVEL(1),
    SET_CODE_COMMENT(2),
    SET_AS_SUBNUMBER_OF_PREVIOUS_ERROR(3),
    SET_HTTP_STATUS(4),
    SET_CATEGORY(5),
    SET_ERROR_PRIORITY_LEVEL(6);

    private final int value;

    ExceptionRuleOperationEnum(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }

    public static ExceptionRuleOperationEnum forValue(int value) {
        switch (value) {
            case 0: return NO_OPERATION;
            case 1: return SET_ERROR_SEVERITY_LEVEL;
            case 2: return SET_CODE_COMMENT;
            case 3: return SET_AS_SUBNUMBER_OF_PREVIOUS_ERROR;
            case 4: return SET_HTTP_STATUS;
            case 5: return SET_CATEGORY;
            case 6: return SET_ERROR_PRIORITY_LEVEL;
            default: throw new RuntimeException("[AGENT-000023] Unexpected exception rule operation value=" + value);
        }
    }
}
