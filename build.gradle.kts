import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
  val kotlinVersion = "2.0.0"
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.5"
  id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
  id("jacoco")
  id("org.sonarqube") version "4.0.0.2929"
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
  implementation("javax.servlet:javax.servlet-api:4.0.1")

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-data-redis")

  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:4.3.2")
  implementation("software.amazon.awssdk:s3")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")

  implementation("org.flywaydb:flyway-core")

  runtimeOnly("org.springframework.boot:spring-boot-starter-jdbc")
  runtimeOnly("org.postgresql:postgresql:42.7.3")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("io.micrometer:micrometer-registry-prometheus")
  implementation("xyz.capybara:clamav-client:2.1.2")
  implementation("dev.forkhandles:result4k:2.0.0.0")

  implementation("org.apache.commons:commons-lang3")
  implementation("org.apache.commons:commons-text:1.11.0")
  implementation("commons-codec:commons-codec")
  implementation("com.google.code.gson:gson")
  implementation("org.json:json:20240303")
  implementation("io.github.oshai:kotlin-logging-jvm:6.0.9")
  implementation("com.pauldijou:jwt-core_2.11:5.0.0")

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.0.1")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  implementation(platform("org.testcontainers:testcontainers-bom:1.19.8"))
  testImplementation("org.awaitility:awaitility-kotlin")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.5")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.5")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.19")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.wiremock:wiremock-standalone:3.5.3")
  testImplementation("org.testcontainers:postgresql")
  testImplementation("org.testcontainers:localstack")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
  testImplementation("javax.xml.bind:jaxb-api:2.3.1")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing:1.34.1")
  testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
  testImplementation("io.mockk:mockk:1.13.10")
  testImplementation("com.github.victools:jsonschema-generator:4.35.0")
  testImplementation("com.natpryce:hamkrest:1.8.0.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.4.1")

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
