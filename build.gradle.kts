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
  implementation("com.google.code.gson:gson:2.8.7")

  // https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit
  implementation("org.eclipse.jgit:org.eclipse.jgit:5.12.0.202106070339-r")

  // https://mvnrepository.com/artifact/org.apache.httpcomponents.client5/httpclient5
  implementation("org.apache.httpcomponents.client5:httpclient5:5.2")

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
    "--token", "../open-source-bundle/dev-localhost/err0-vapor-20230816-b6fc8533-3c11-11ee-a93e-305a3ac84b71.json", "--insert", "../open-source-bundle/vapor", // Swift
    // pass #2 -- analyse source code, ignoring dirty checkout.
    "--token", "../open-source-bundle/dev-localhost/err0-vapor-20230816-b6fc8533-3c11-11ee-a93e-305a3ac84b71.json", "--analyse", "--dirty", "../open-source-bundle/vapor", // Swift
  )

  //args = listOf("--version", "--help")
}
