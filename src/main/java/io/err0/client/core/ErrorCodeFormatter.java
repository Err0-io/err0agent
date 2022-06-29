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
