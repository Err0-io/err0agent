package io.err0.client.test;

import com.google.gson.JsonParser;
import io.err0.client.core.ProjectPolicy;
import io.err0.client.core.RealmPolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestPolicy {
    public static ProjectPolicy getPolicy() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Files.readString(Path.of("policies/realm/example-realm.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Files.readString(Path.of("policies/application/example-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProjectPolicy getAdvJavaPolicy() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Files.readString(Path.of("policies/realm/example-realm.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Files.readString(Path.of("policies/application/example-adv-java-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProjectPolicy getAdvPhpPolicy() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Files.readString(Path.of("policies/realm/example-realm.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Files.readString(Path.of("policies/application/example-adv-php-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProjectPolicy getAdvCSharpPolicy() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Files.readString(Path.of("policies/realm/example-realm.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Files.readString(Path.of("policies/application/example-adv-csharp-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
