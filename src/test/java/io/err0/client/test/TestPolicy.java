package io.err0.client.test;

import io.err0.client.core.ApplicationPolicy;
import io.err0.client.core.RealmPolicy;

import java.io.IOException;

public class TestPolicy {
    public static ApplicationPolicy getPolicy() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy("policies/realm/example-realm.json");
            return new ApplicationPolicy(realmPolicy, "policies/application/example-app.json");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
