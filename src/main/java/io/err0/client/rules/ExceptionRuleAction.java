package io.err0.client.rules;

public class ExceptionRuleAction {
    public ExceptionRuleAction(final ExceptionRuleActionEnum action, final String actionValue) {
        this.action = action;
        this.actionValue = actionValue;
    }
    public final ExceptionRuleActionEnum action;
    public final String actionValue;
}
