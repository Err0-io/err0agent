package io.err0.client.core;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorCodeFormatter {
    enum FieldType {
        NONE,
        ERROR,
        REALM,
        APPLICATION,
        VERSION
    }
    static class Pair {
        Pair(FieldType fieldType, String content) {
            this.fieldType = fieldType;
            this.content = content;
        }
        FieldType fieldType;
        String content;
    }
    final Pattern re = Pattern.compile("%([\\S]+)");
    ArrayList<Pair> list = new ArrayList<>();
    private final ApplicationPolicy policy;
    public ErrorCodeFormatter(final ApplicationPolicy policy, final String error_code_template) {
        this.policy = policy;
        final Matcher m = re.matcher(error_code_template);
        int index = 0;
        while (m.find()) {
            list.add(new Pair(FieldType.NONE, error_code_template.substring(index, m.start())));
            switch (m.group(1)) {
                case "error":
                    list.add(new Pair(FieldType.ERROR, null));
                    break;
                case "realm":
                    list.add(new Pair(FieldType.REALM, null));
                    break;
                case "app":
                    list.add(new Pair(FieldType.APPLICATION, null));
                    break;
                case "version":
                    list.add(new Pair(FieldType.VERSION, null));
                    break;
                default:
            }
            index = m.end();
        }
        list.add(new Pair(FieldType.NONE, error_code_template.substring(index)));

        boolean hasErrorCode = false;
        for (int i = 0, l = list.size(); !hasErrorCode && i < l; ++i) {
            hasErrorCode = list.get(i).fieldType == FieldType.ERROR;
        }
        if (!hasErrorCode) {
            throw new RuntimeException("[AGENT-000010] Error code template must include %error");
        }
    }
    public ErrorCodeFormatter(final ApplicationPolicy policy) {
        this.policy = policy;
        this.list.add(new Pair(FieldType.ERROR, null));
    }
    public String formatErrorCode(final long errorCode) {
        StringBuilder sb = new StringBuilder();
        list.forEach(pair -> {
            switch (pair.fieldType) {
                case NONE:
                    sb.append(pair.content);
                    break;
                case ERROR:
                    formatErrorCodeOnly(sb, errorCode);
                    break;
                case REALM:
                    sb.append(policy.realmPolicy.realm_code);
                    break;
                case APPLICATION:
                    sb.append(policy.app_code);
                    break;
                case VERSION:
                    sb.append("unknown"); // TODO git short hash if no tag?
                    break;
            }
        });
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
