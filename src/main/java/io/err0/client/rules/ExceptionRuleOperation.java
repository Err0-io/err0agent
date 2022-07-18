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

public class ExceptionRuleOperation {
    public ExceptionRuleOperation(final ExceptionRuleOperationEnum operation, final String operationValue) {
        this.operation = operation;
        this.operationValue = operationValue;
        switch (this.operation) {
            case SET_CODE_COMMENT:
                if (null == operationValue) {
                    throw new RuntimeException("[AGENT-000015] Needs a value.");
                }
                if (operationValue.contains("/*") ||
                    operationValue.contains("//") ||
                    operationValue.contains("*/") ||
                    operationValue.contains("#")
                ) {
                    throw new RuntimeException("[AGENT-000016] Invalid code comment: " + operationValue);
                }
                break;
        }
    }
    public final ExceptionRuleOperationEnum operation;
    public final String operationValue;
}
