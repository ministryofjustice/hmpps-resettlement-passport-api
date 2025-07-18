import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
  val kotlinVersion = "2.0.0"
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.3.2"
  id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
  id("jacoco")
  id("org.sonarqube") version "6.2.0.5505"
  kotlin("plugin.spring") version kotlinVersion
  kotlin("plugin.jpa") version kotlinVersion
  kotlin("plugin.serialization") version kotlinVersion
}

dependencyCheck {
  suppressionFiles.add("reactive-suppressions.xml")
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

repositories {
  maven { url = uri("https://repo.spring.io/milestone") }
  mavenCentral()
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-data-redis")

  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.4.5")
  implementation("software.amazon.awssdk:s3")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")

  implementation("org.flywaydb:flyway-core")

  runtimeOnly("org.springframework.boot:spring-boot-starter-jdbc")
  runtimeOnly("org.postgresql:postgresql:42.7.7")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("io.micrometer:micrometer-registry-prometheus")
  implementation("xyz.capybara:clamav-client:2.1.2")
  implementation("dev.forkhandles:result4k:2.22.3.0")

  implementation("org.apache.commons:commons-lang3")
  implementation("org.apache.commons:commons-text:1.13.1")
  implementation("commons-codec:commons-codec")
  implementation("com.google.code.gson:gson")
  implementation("org.json:json:20250517")
  implementation("io.github.oshai:kotlin-logging-jvm:7.0.7")
  implementation("com.pauldijou:jwt-core_2.11:5.0.0")

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.4.6")

  constraints {
    implementation("io.netty:netty-handler:4.1.118.Final")
  }

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  implementation(platform("org.testcontainers:testcontainers-bom:1.21.2"))
  testImplementation("org.awaitility:awaitility-kotlin")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.6")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.30")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.wiremock:wiremock-standalone:3.13.1")
  testImplementation("org.testcontainers:postgresql")
  testImplementation("org.testcontainers:localstack")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
  testImplementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing:1.51.0")
  testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
  testImplementation("io.mockk:mockk:1.14.4")
  testImplementation("com.github.victools:jsonschema-generator:4.38.0")
  testImplementation("com.natpryce:hamkrest:1.8.0.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:4.1.1")

  if (project.hasProperty("docs")) {
    implementation("com.h2database:h2")
  }
}

openApi {
  outputDir.set(project.layout.buildDirectory.dir("docs"))
  outputFileName.set("openapi.json")
  customBootRun.args.set(listOf("--spring.profiles.active=dev,docs"))
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport)
  // https://github.com/mockk/mockk/issues/681
  jvmArgs("--add-opens", "java.base/java.time=ALL-UNNAMED")
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
  }
}

tasks.named<BootRun>("bootRun") {
  systemProperty("spring.profiles.active", project.findProperty("profiles")?.toString() ?: "dev")
}

dependencyCheck {
  suppressionFiles.add("owasp-suppressions.xml")
}

abstract class EchoTask : DefaultTask() {
  @TaskAction
  fun action() {
    println("Dependencies downloaded")
  }
}

tasks.register<EchoTask>("downloadDependencies")
