import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.mavenCentral

rootProject.name = "hmpps-resettlement-passport-api"

pluginManagement {
  repositories {
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("/Users/mary.ford/Documents/dev/gitleaks-kotlin-plugin/plugins.local") }
    mavenCentral()
    gradlePluginPortal()
  }
}