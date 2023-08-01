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

package io.err0.client.test;

import com.google.gson.JsonParser;
import io.err0.client.core.ProjectPolicy;
import io.err0.client.core.RealmPolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import io.err0.client.core.Utils;

public class TestPolicy {
    public static ProjectPolicy getPolicy() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Utils.readString(Utils.pathOf("policies/realm/example-realm.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Utils.readString(Utils.pathOf("policies/application/example-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProjectPolicy getPolicyEnablePlaceholder() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Utils.readString(Utils.pathOf("policies/realm/example-realm-placeholder.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Utils.readString(Utils.pathOf("policies/application/example-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProjectPolicy getPolicyDisableLogs() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Utils.readString(Utils.pathOf("policies/realm/example-realm-disable-logs.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Utils.readString(Utils.pathOf("policies/application/example-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProjectPolicy getPolicyDisableExceptions() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Utils.readString(Utils.pathOf("policies/realm/example-realm-disable-exceptions.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Utils.readString(Utils.pathOf("policies/application/example-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProjectPolicy getAdvJavaPolicy() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Utils.readString(Utils.pathOf("policies/realm/example-realm.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Utils.readString(Utils.pathOf("policies/application/example-adv-java-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProjectPolicy getAdvPhpPolicy() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Utils.readString(Utils.pathOf("policies/realm/example-realm.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Utils.readString(Utils.pathOf("policies/application/example-adv-php-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProjectPolicy getAdvCSharpPolicy() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Utils.readString(Utils.pathOf("policies/realm/example-realm.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Utils.readString(Utils.pathOf("policies/application/example-adv-csharp-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProjectPolicy getAdvGolangPolicy() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Utils.readString(Utils.pathOf("policies/realm/example-realm.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Utils.readString(Utils.pathOf("policies/application/example-adv-golang-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProjectPolicy getAdvJavaScriptPolicy() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Utils.readString(Utils.pathOf("policies/realm/example-realm.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Utils.readString(Utils.pathOf("policies/application/example-adv-javascript-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProjectPolicy getAdvTypeScriptPolicy() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Utils.readString(Utils.pathOf("policies/realm/example-realm.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Utils.readString(Utils.pathOf("policies/application/example-adv-typescript-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProjectPolicy getAdvPythonPolicy() {
        try {
            final RealmPolicy realmPolicy = new RealmPolicy(JsonParser.parseString(Utils.readString(Utils.pathOf("policies/realm/example-realm.json"))).getAsJsonObject());
            return new ProjectPolicy(realmPolicy, JsonParser.parseString(Utils.readString(Utils.pathOf("policies/application/example-adv-python-app.json"))).getAsJsonObject());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
