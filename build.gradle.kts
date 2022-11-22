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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "io.err0"
version = "1.0.3-SNAPSHOT"

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
  implementation("org.apache.httpcomponents.client5:httpclient5:5.1")

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
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
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

  // Check-out bts projects...

  /*
  args = listOf(
    "--token", "../ccc/api-masterdata-devel/err0-ccc-api-masterdata-ee9bfddb-94b3-11ec-8263-ae3b21a6cd73.json", "--insert", "../ccc/api-masterdata-devel/",
  )
  
   */

  /*
  args = listOf(
    "--token", "../fp/futurepay-portal/err0-futurepay-portal-1f8d9570-90b7-11ec-b6db-1613dacd7971.json", "--insert", "../fp/futurepay-portal/",
    "--token", "../fp/futurepay-checkout-backend/err0-futurepay-checkout-backend-1ac49bae-90b7-11ec-b6db-1613dacd7971.json", "--insert", "../fp/futurepay-checkout-backend/",
    "--token", "../fp/futurepay-credit-api/err0-futurepay-credit-service-1d26f5af-90b7-11ec-b6db-1613dacd7971.json", "--insert", "../fp/futurepay-credit-api/"
  )

  */

  /*
  // Check-out bts projects...
  args = listOf(
    "--token", "tokens/err0-bts-internal-projects-bts-platform-8ae046e8-8f12-11ec-b3b7-de7ff53b7565.json", "--insert", "../bts_internship_2019_be_app"
  )

   */

  // Check-out the open-source-bundle project at the same parent level as this project:
  args = listOf(
    // pass #1 -- insert error codes (or re-insert error codes).
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-spring-framework-918d1448-3731-11ec-9e2d-46a00e0b2797.json", "--insert", "../open-source-bundle/spring-framework", // java
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-django-74df2c06-19f4-11ec-8253-56f6e5f49a04.json", "--insert", "../open-source-bundle/django", // python
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-kubernetes-f26f7103-0ff0-11ec-b8c2-a63d063ada96.json", "--insert", "../open-source-bundle/kubernetes", // go
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-roslyn-01c36447-0ff1-11ec-b8c2-a63d063ada96.json", "--insert", "../open-source-bundle/roslyn", // c#
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-node-bb-000ed676-0ff1-11ec-b8c2-a63d063ada96.json", "--insert", "../open-source-bundle/NodeBB", // node.js
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-zf2-orders-057066b9-0ff1-11ec-b8c2-a63d063ada96.json", "--insert", "../open-source-bundle/zf2-orders", // php + Zend framework
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-moodle-fcc1f565-0ff0-11ec-b8c2-a63d063ada96.json", "--insert", "../open-source-bundle/moodle", // very tidy php lms for universities
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-magneto-2-f9bf8ad4-0ff0-11ec-b8c2-a63d063ada96.json", "--insert", "../open-source-bundle/magento2", // php e-commerce
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-umbraco-0387f7a8-0ff1-11ec-b8c2-a63d063ada96.json", "--insert", "../open-source-bundle/Umbraco-CMS", // c#
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-drupal-c153bfac-9005-11ec-9269-f219fdfef40a.json", "--insert", "../open-source-bundle/drupal", // php
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-tomcat-c5cc286d-9005-11ec-9269-f219fdfef40a.json", "--insert", "../open-source-bundle/tomcat", // java
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-wordpress-c982eede-9005-11ec-9269-f219fdfef40a.json", "--insert", "../open-source-bundle/WordPress", // php
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-ratpack-96a0adb0-9195-11ec-9b74-f67018ac8828.json", "--insert", "../open-source-bundle/ratpack", // java
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-strapi-fda7a5e2-9195-11ec-9b74-f67018ac8828.json", "--insert", "../open-source-bundle/strapi", // javascript
    "--token", "../open-source-bundle/dev-localhost/err0-cerbos-test-cf03016a-600f-11ed-bfcd-305a3ac84b71.json", "--insert", "../open-source-bundle/cerbos", // go
    "--token", "../open-source-bundle/dev-localhost/err0-mender-test-a5474660-6401-11ed-92e0-4401bb8de3b3.json", "--insert", "../open-source-bundle/mender", // go
    // pass #2 -- analyse source code, ignoring dirty checkout.
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-spring-framework-918d1448-3731-11ec-9e2d-46a00e0b2797.json", "--analyse", "--dirty", "../open-source-bundle/spring-framework", // java
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-django-74df2c06-19f4-11ec-8253-56f6e5f49a04.json", "--analyse", "--dirty", "../open-source-bundle/django", // python
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-kubernetes-f26f7103-0ff0-11ec-b8c2-a63d063ada96.json", "--analyse", "--dirty", "../open-source-bundle/kubernetes", // go
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-roslyn-01c36447-0ff1-11ec-b8c2-a63d063ada96.json", "--analyse", "--dirty", "../open-source-bundle/roslyn", // c#
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-node-bb-000ed676-0ff1-11ec-b8c2-a63d063ada96.json", "--analyse", "--dirty", "../open-source-bundle/NodeBB", // node.js
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-zf2-orders-057066b9-0ff1-11ec-b8c2-a63d063ada96.json", "--analyse", "--dirty", "../open-source-bundle/zf2-orders", // php + Zend framework
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-moodle-fcc1f565-0ff0-11ec-b8c2-a63d063ada96.json", "--analyse", "--dirty", "../open-source-bundle/moodle", // very tidy php lms for universities
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-magneto-2-f9bf8ad4-0ff0-11ec-b8c2-a63d063ada96.json", "--analyse", "--dirty", "../open-source-bundle/magento2", // php e-commerce
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-umbraco-0387f7a8-0ff1-11ec-b8c2-a63d063ada96.json", "--analyse", "--dirty", "../open-source-bundle/Umbraco-CMS", // c#
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-drupal-c153bfac-9005-11ec-9269-f219fdfef40a.json", "--analyse", "--dirty", "../open-source-bundle/drupal", // php
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-tomcat-c5cc286d-9005-11ec-9269-f219fdfef40a.json", "--analyse", "--dirty", "../open-source-bundle/tomcat", // java
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-wordpress-c982eede-9005-11ec-9269-f219fdfef40a.json", "--analyse", "--dirty", "../open-source-bundle/WordPress", // php
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-ratpack-96a0adb0-9195-11ec-9b74-f67018ac8828.json", "--analyse", "--dirty", "../open-source-bundle/ratpack", // java
    "--token", "../open-source-bundle/dev-localhost/err0-open-source-software-strapi-fda7a5e2-9195-11ec-9b74-f67018ac8828.json", "--analyse", "--dirty", "../open-source-bundle/strapi", // javascript
    "--token", "../open-source-bundle/dev-localhost/err0-cerbos-test-cf03016a-600f-11ed-bfcd-305a3ac84b71.json", "--analyse", "--dirty", "../open-source-bundle/cerbos", // go
    "--token", "../open-source-bundle/dev-localhost/err0-mender-test-a5474660-6401-11ed-92e0-4401bb8de3b3.json", "--analyse", "--dirty", "../open-source-bundle/mender", // go

  )
}
