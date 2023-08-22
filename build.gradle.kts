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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("com.github.gmazzo.buildconfig") version "3.0.3"
}

group = "io.err0"
version = "1.5.0-BETA"

repositories {
  mavenCentral()
}

val vertxVersion = "4.1.1"
val junitJupiterVersion = "5.7.0"

//val mainVerticleName = "io.err0.client.proto.MainVerticle"
//val launcherClassName = "io.vertx.core.Launcher"
val launcherClassName = "io.err0.client.Main"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set(launcherClassName)
}

fun versionBanner(): String {
  val os = org.apache.commons.io.output.ByteArrayOutputStream()
  project.exec {
    commandLine = "git rev-parse --short HEAD".split(" ")
    standardOutput = os
  }
  return String(os.toByteArray()).trim()
}

val buildTime = System.currentTimeMillis()

buildConfig {
  className("BuildConfig")
  packageName("io.err0.client")
  buildConfigField("String", "NAME", "\"io.err0.err0agent\"")
  buildConfigField("String", "VERSION", provider { "\"${project.version}\"" })
  buildConfigField("String", "GIT_SHORT_VERSION", "\"" + versionBanner() + "\"")
  buildConfigField("long", "BUILD_UNIXTIME", "${buildTime}L")
}

dependencies {
  // https://mvnrepository.com/artifact/org.apache.ant/ant-launcher
  implementation("org.apache.ant:ant-launcher:1.10.11") //needed by shadow

  //implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  //implementation("io.vertx:vertx-web-client")
  //testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
  // https://mvnrepository.com/artifact/com.google.code.gson/gson
  implementation("com.google.code.gson:gson:2.10.1")


  // https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit
  implementation("org.eclipse.jgit:org.eclipse.jgit:6.6.0.202305301015-r")

  // https://mvnrepository.com/artifact/org.apache.httpcomponents.client5/httpclient5
  implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")

  // https://mvnrepository.com/artifact/commons-cli/commons-cli
  implementation("commons-cli:commons-cli:1.5.0")

  // the below are uncommented to allow me to use autocomplete to edit the
  // java unit test cases, please comment these out as the tests don't actually
  // depend on these:
  // testImplementation("org.apache.logging.log4j:log4j-core:2.14.1")
  // testImplementation("org.slf4j:slf4j-api:1.7.31")

  // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
  implementation("org.slf4j:slf4j-api:1.7.36")
  // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
  implementation("org.slf4j:slf4j-simple:1.7.36")

}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  archiveFileName.set("${project.name}-java_1_8-fat.jar")

  //manifest {
  //  attributes(mapOf("Main-Verticle" to mainVerticleName))
  //}
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  // Check-out the open-source-bundle project at the same parent level as this project:
  args = listOf(
    // pass #1 -- insert error codes (or re-insert error codes).
    "--token", "../open-source-bundle/dev-localhost/err0-kotlinx-html-20230818-e52cfc88-3dbd-11ee-8cc8-305a3ac84b71.json", "--insert", "../open-source-bundle/kotlinx.html", // Kotlin
    "--token", "../open-source-bundle/dev-localhost/err0-ktor-20230818-d2e19b37-3dbd-11ee-8cc8-305a3ac84b71.json", "--insert", "../open-source-bundle/ktor", // Kotlin
    "--token", "../open-source-bundle/dev-localhost/err0-telegram-20230818-bfd50cc6-3dbd-11ee-8cc8-305a3ac84b71.json", "--insert", "../open-source-bundle/Telegram", // Java/Kotlin
    "--token", "../open-source-bundle/dev-localhost/err0-signal-android-20230818-b0602405-3dbd-11ee-8cc8-305a3ac84b71.json", "--insert", "../open-source-bundle/Signal-Android", // Java/Kotlin
    "--token", "../open-source-bundle/dev-localhost/err0-telegram-ios-20230818-99ec9cd4-3dbd-11ee-8cc8-305a3ac84b71.json", "--insert", "../open-source-bundle/Telegram-iOS", // Swift/Obj-C
    "--token", "../open-source-bundle/dev-localhost/err0-signal-ios-20230818-886f39e3-3dbd-11ee-8cc8-305a3ac84b71.json", "--insert", "../open-source-bundle/Signal-iOS", // Swift/Obj-C
    "--token", "../open-source-bundle/dev-localhost/err0-utm-20230818-69ee3c52-3dbd-11ee-8cc8-305a3ac84b71.json", "--insert", "../open-source-bundle/UTM", // Swift
    "--token", "../open-source-bundle/dev-localhost/err0-vapor-20230816-b6fc8533-3c11-11ee-a93e-305a3ac84b71.json", "--insert", "../open-source-bundle/vapor", // Swift
    "--token", "../open-source-bundle/dev-localhost/err0-smartthingsedgedrivers-20230603-077907dc-0214-11ee-a0ed-305a3ac84b71.json", "--insert", "../open-source-bundle/SmartThingsEdgeDrivers", // Lua
    "--token", "../open-source-bundle/dev-localhost/err0-leptos-20221220-7c62eafd-806b-11ed-b59e-4401bb8de3b3.json", "--insert", "../open-source-bundle/leptos", // Rust
    "--token", "../open-source-bundle/dev-localhost/err0-postfix-20221220-5cd5d49b-806b-11ed-b59e-4401bb8de3b3.json", "--insert", "../open-source-bundle/postfix", // C
    "--token", "../open-source-bundle/dev-localhost/err0-bitcoin-20221220-6dcc7b5c-806b-11ed-b59e-4401bb8de3b3.json", "--insert", "../open-source-bundle/bitcoin", // C++
    "--token", "../open-source-bundle/dev-localhost/err0-spring-framework-20221207-7772c640-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/spring-framework", // java
    "--token", "../open-source-bundle/dev-localhost/err0-django-20221207-3c39a436-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/django", // python
    "--token", "../open-source-bundle/dev-localhost/err0-kubernetes-20221207-4fc303c9-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/kubernetes", // go
    "--token", "../open-source-bundle/dev-localhost/err0-roslyn-20221207-7166770f-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/roslyn", // c#
    "--token", "../open-source-bundle/dev-localhost/err0-node-bb-20221207-65fd799d-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/NodeBB", // node.js
    "--token", "../open-source-bundle/dev-localhost/err0-zf2-orders-20221207-9952cd55-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/zf2-orders", // php + Zend framework
    "--token", "../open-source-bundle/dev-localhost/err0-moodle-20221207-6052f97c-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/moodle", // very tidy php lms for universities
    "--token", "../open-source-bundle/dev-localhost/err0-magneto-2-20221207-55b2551a-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/magento2", // php e-commerce
    "--token", "../open-source-bundle/dev-localhost/err0-umbraco-20221207-8dcaaf23-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/Umbraco-CMS", // c#
    "--token", "../open-source-bundle/dev-localhost/err0-drupal-20221207-41dbe6f7-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/drupal", // php
    "--token", "../open-source-bundle/dev-localhost/err0-tomcat-20221207-88958612-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/tomcat", // java
    "--token", "../open-source-bundle/dev-localhost/err0-wordpress-20221207-93b2d484-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/WordPress", // php
    "--token", "../open-source-bundle/dev-localhost/err0-ratpack-20221207-6ba317be-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/ratpack", // java
    "--token", "../open-source-bundle/dev-localhost/err0-strapi-20221207-82bf8921-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/strapi", // javascript
    "--token", "../open-source-bundle/dev-localhost/err0-cerbos-20221207-33d4bf05-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/cerbos", // go
    "--token", "../open-source-bundle/dev-localhost/err0-mender-20221207-5b47787b-763f-11ed-8b95-4401bb8de3b3.json", "--insert", "../open-source-bundle/mender", // go
    "--token", "../open-source-bundle/dev-localhost/err0-rails-20230723-82c92bf1-1997-11ee-8025-305a3ac84b71.json", "--insert", "../open-source-bundle/rails", // Ruby
    "--token", "../open-source-bundle/dev-localhost/err0-pytorch-20221209-6cd848c3-77ab-11ed-b3c0-4401bb8de3b3.json", "--insert", "../open-source-bundle/pytorch", // Python/C++
    // pass #2 -- analyse source code, ignoring dirty checkout.
    "--token", "../open-source-bundle/dev-localhost/err0-vapor-20230816-b6fc8533-3c11-11ee-a93e-305a3ac84b71.json", "--analyse", "--dirty", "--check", "../open-source-bundle/vapor", // Swift
    "--token", "../open-source-bundle/dev-localhost/err0-smartthingsedgedrivers-20230603-077907dc-0214-11ee-a0ed-305a3ac84b71.json", "--analyse", "--dirty", "--check", "../open-source-bundle/SmartThingsEdgeDrivers", // Lua
    "--token", "../open-source-bundle/dev-localhost/err0-leptos-20221220-7c62eafd-806b-11ed-b59e-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/leptos", // Rust
    "--token", "../open-source-bundle/dev-localhost/err0-postfix-20221220-5cd5d49b-806b-11ed-b59e-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/postfix", // C
    "--token", "../open-source-bundle/dev-localhost/err0-bitcoin-20221220-6dcc7b5c-806b-11ed-b59e-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/bitcoin", // C++
    "--token", "../open-source-bundle/dev-localhost/err0-spring-framework-20221207-7772c640-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/spring-framework", // java
    "--token", "../open-source-bundle/dev-localhost/err0-django-20221207-3c39a436-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/django", // python
    "--token", "../open-source-bundle/dev-localhost/err0-kubernetes-20221207-4fc303c9-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/kubernetes", // go
    "--token", "../open-source-bundle/dev-localhost/err0-roslyn-20221207-7166770f-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/roslyn", // c#
    "--token", "../open-source-bundle/dev-localhost/err0-node-bb-20221207-65fd799d-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/NodeBB", // node.js
    "--token", "../open-source-bundle/dev-localhost/err0-zf2-orders-20221207-9952cd55-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/zf2-orders", // php + Zend framework
    "--token", "../open-source-bundle/dev-localhost/err0-moodle-20221207-6052f97c-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/moodle", // very tidy php lms for universities
    "--token", "../open-source-bundle/dev-localhost/err0-magneto-2-20221207-55b2551a-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/magento2", // php e-commerce
    "--token", "../open-source-bundle/dev-localhost/err0-umbraco-20221207-8dcaaf23-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/Umbraco-CMS", // c#
    "--token", "../open-source-bundle/dev-localhost/err0-drupal-20221207-41dbe6f7-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/drupal", // php
    "--token", "../open-source-bundle/dev-localhost/err0-tomcat-20221207-88958612-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/tomcat", // java
    "--token", "../open-source-bundle/dev-localhost/err0-wordpress-20221207-93b2d484-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/WordPress", // php
    "--token", "../open-source-bundle/dev-localhost/err0-ratpack-20221207-6ba317be-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/ratpack", // java
    "--token", "../open-source-bundle/dev-localhost/err0-strapi-20221207-82bf8921-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/strapi", // javascript
    "--token", "../open-source-bundle/dev-localhost/err0-cerbos-20221207-33d4bf05-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/cerbos", // go
    "--token", "../open-source-bundle/dev-localhost/err0-mender-20221207-5b47787b-763f-11ed-8b95-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/mender", // go
    "--token", "../open-source-bundle/dev-localhost/err0-rails-20230723-82c92bf1-1997-11ee-8025-305a3ac84b71.json", "--analyse", "--dirty", "--check", "../open-source-bundle/rails", // Ruby
    "--token", "../open-source-bundle/dev-localhost/err0-pytorch-20221209-6cd848c3-77ab-11ed-b3c0-4401bb8de3b3.json", "--analyse", "--dirty", "--check", "../open-source-bundle/pytorch", // Python/C++
    "--token", "../open-source-bundle/dev-localhost/err0-kotlinx-html-20230818-e52cfc88-3dbd-11ee-8cc8-305a3ac84b71.json", "--analyse", "--dirty", "--check", "../open-source-bundle/kotlinx.html", // Kotlin
    "--token", "../open-source-bundle/dev-localhost/err0-ktor-20230818-d2e19b37-3dbd-11ee-8cc8-305a3ac84b71.json", "--analyse", "--dirty", "--check", "../open-source-bundle/ktor", // Kotlin
    "--token", "../open-source-bundle/dev-localhost/err0-telegram-20230818-bfd50cc6-3dbd-11ee-8cc8-305a3ac84b71.json", "--analyse", "--dirty", "--check", "../open-source-bundle/Telegram", // Java/Kotlin
    "--token", "../open-source-bundle/dev-localhost/err0-signal-android-20230818-b0602405-3dbd-11ee-8cc8-305a3ac84b71.json", "--analyse", "--dirty", "--check", "../open-source-bundle/Signal-Android", // Java/Kotlin
    "--token", "../open-source-bundle/dev-localhost/err0-telegram-ios-20230818-99ec9cd4-3dbd-11ee-8cc8-305a3ac84b71.json", "--analyse", "--dirty", "--check", "../open-source-bundle/Telegram-iOS", // Swift/Obj-C
    "--token", "../open-source-bundle/dev-localhost/err0-signal-ios-20230818-886f39e3-3dbd-11ee-8cc8-305a3ac84b71.json", "--analyse", "--dirty", "--check", "../open-source-bundle/Signal-iOS", // Swift/Obj-C
    "--token", "../open-source-bundle/dev-localhost/err0-utm-20230818-69ee3c52-3dbd-11ee-8cc8-305a3ac84b71.json", "--analyse", "--dirty", "--check", "../open-source-bundle/UTM", // Swift
  )

  //args = listOf("--version", "--help")
}
