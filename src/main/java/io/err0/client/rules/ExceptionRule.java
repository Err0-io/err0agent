package io.err0.client.rules;

import java.util.ArrayList;

public class ExceptionRule {
    public String name;
    public boolean selectorAnd;
    public ArrayList<ExceptionRuleSelection> selectors = new ArrayList<>();
    public ArrayList<ExceptionRuleOperation> operations = new ArrayList<>();
    public ArrayList<ExceptionRuleAction> actions = new ArrayList<>();
}
