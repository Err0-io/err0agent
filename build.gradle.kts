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
  //args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
    /*
  args = listOf(
          "--realm", "policies/realm/amb1ent.json",
          "--app", "policies/application/streetstall.json",
          "--insert", "/tank/street-stall/street-stall-space",
          "--app", "policies/application/corefabric.json",
          "--insert", "/tank/street-stall/street-stall-space/corefabric",
          "--app", "policies/application/streetstallangularlib.json",
          "--insert", "/tank/street-stall/street-stall-space/web/site/src/app/library"
  ) // java, typescript
    */
  /*
  args = listOf("--realm", "policies/realm/opensource-realm.json", "--app", "policies/application/umbraco.json", "--insert", "/tank/bts/examples/Umbraco-CMS/src", // c#
    "--realm", "policies/realm/opensource-realm.json", "--app", "policies/application/roslyn.json", "--insert", "/tank/bts/examples/roslyn", // c#
    "--realm", "policies/realm/opensource-realm.json", "--app", "policies/application/nodebb.json", "--insert", "/tank/bts/examples/NodeBB/src", // node.js
    "--realm", "policies/realm/opensource-realm.json", "--app", "policies/application/zf2-orders.json", "--insert", "/tank/bts/examples/zf2-orders", // php + Zend framework
    "--realm", "policies/realm/opensource-realm.json", "--app", "policies/application/moodle.json", "--insert", "/tank/bts/examples/moodle", // very tidy php lms for universities
    "--realm", "policies/realm/opensource-realm.json", "--app", "policies/application/magneto2.json", "--insert", "/tank/bts/examples/magento2", // php e-commerce
    "--realm", "policies/realm/opensource-realm.json", "--app", "policies/application/kubernetes.json", "--insert", "/tank/bts/examples/kubernetes") // a big go project

   */
  /*
  args = listOf(
    // pass #1 -- insert error codes (or re-insert error codes).
    "--token", "tokens/err0-open-source-software-spring-framework-918d1448-3731-11ec-9e2d-46a00e0b2797.json", "--insert", "/tank/bts/examples/spring-framework", // java
    "--token", "tokens/err0-open-source-software-django-74df2c06-19f4-11ec-8253-56f6e5f49a04.json", "--insert", "/tank/bts/examples/django", // python
    "--token", "tokens/err0-open-source-software-kubernetes-f26f7103-0ff0-11ec-b8c2-a63d063ada96.json", "--insert", "/tank/bts/examples/kubernetes", // go
    "--token", "tokens/err0-open-source-software-roslyn-01c36447-0ff1-11ec-b8c2-a63d063ada96.json", "--insert", "/tank/bts/examples/roslyn", // c#
    "--token", "tokens/err0-open-source-software-node-bb-000ed676-0ff1-11ec-b8c2-a63d063ada96.json", "--insert", "/tank/bts/examples/NodeBB/src", // node.js
    "--token", "tokens/err0-open-source-software-zf2-orders-057066b9-0ff1-11ec-b8c2-a63d063ada96.json", "--insert", "/tank/bts/examples/zf2-orders", // php + Zend framework
    "--token", "tokens/err0-open-source-software-moodle-fcc1f565-0ff0-11ec-b8c2-a63d063ada96.json", "--insert", "/tank/bts/examples/moodle", // very tidy php lms for universities
    "--token", "tokens/err0-open-source-software-magneto-2-f9bf8ad4-0ff0-11ec-b8c2-a63d063ada96.json", "--insert", "/tank/bts/examples/magento2", // php e-commerce
    "--token", "tokens/err0-open-source-software-umbraco-0387f7a8-0ff1-11ec-b8c2-a63d063ada96.json", "--insert", "/tank/bts/examples/Umbraco-CMS/src", // c#
    // pass #2 -- analyse source code, ignoring dirty checkout.
    "--token", "tokens/err0-open-source-software-spring-framework-918d1448-3731-11ec-9e2d-46a00e0b2797.json", "--analyse", "--dirty", "/tank/bts/examples/spring-framework", // java
    "--token", "tokens/err0-open-source-software-django-74df2c06-19f4-11ec-8253-56f6e5f49a04.json", "--analyse", "--dirty", "/tank/bts/examples/django", // python
    "--token", "tokens/err0-open-source-software-kubernetes-f26f7103-0ff0-11ec-b8c2-a63d063ada96.json", "--analyse", "--dirty", "/tank/bts/examples/kubernetes", // go
    "--token", "tokens/err0-open-source-software-roslyn-01c36447-0ff1-11ec-b8c2-a63d063ada96.json", "--analyse", "--dirty", "/tank/bts/examples/roslyn", // c#
    "--token", "tokens/err0-open-source-software-node-bb-000ed676-0ff1-11ec-b8c2-a63d063ada96.json", "--analyse", "--dirty", "/tank/bts/examples/NodeBB/src", // node.js
    "--token", "tokens/err0-open-source-software-zf2-orders-057066b9-0ff1-11ec-b8c2-a63d063ada96.json", "--analyse", "--dirty", "/tank/bts/examples/zf2-orders", // php + Zend framework
    "--token", "tokens/err0-open-source-software-moodle-fcc1f565-0ff0-11ec-b8c2-a63d063ada96.json", "--analyse", "--dirty", "/tank/bts/examples/moodle", // very tidy php lms for universities
    "--token", "tokens/err0-open-source-software-magneto-2-f9bf8ad4-0ff0-11ec-b8c2-a63d063ada96.json", "--analyse", "--dirty", "/tank/bts/examples/magento2", // php e-commerce
    "--token", "tokens/err0-open-source-software-umbraco-0387f7a8-0ff1-11ec-b8c2-a63d063ada96.json", "--analyse", "--dirty", "/tank/bts/examples/Umbraco-CMS/src", // c#

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
  )
}
