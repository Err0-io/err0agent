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

package io.err0.client.core;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorCodeFormatter {
    private final ProjectPolicy policy;
    public ErrorCodeFormatter(final ProjectPolicy policy) {
        this.policy = policy;
    }
    public String formatErrorCode(final long errorCode) {
        StringBuilder sb = new StringBuilder();
        formatErrorCodeOnly(sb, errorCode);
        return sb.toString();
    }
    public String formatErrorCodeOnly(final long errorCode) {
        final StringBuilder sb = new StringBuilder();
        formatErrorCodeOnly(sb, errorCode);
        return sb.toString();
    }
    private void formatErrorCodeOnly(final StringBuilder sb, final long errorCode)
    {
        final String sErrorCode = new StringBuilder().append(errorCode).toString();
        sb.append(policy.getErrorPrefix()).append('-');
        final int pad_to_n = policy.getErrorPadToN();
        if (pad_to_n > 0) {
            for (int i = sErrorCode.length(); i < pad_to_n; ++i)
                sb.append('0');
        }
        sb.append(sErrorCode);
    }
}
