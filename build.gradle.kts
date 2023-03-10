import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt

group "com.github.couchtracker"
version "1.0-SNAPSHOT"

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    kotlin("multiplatform") apply false
    kotlin("android") apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.compose") apply false
    id("io.gitlab.arturbosch.detekt").version("1.22.0")
    id("com.github.ben-manes.versions") version "0.46.0"
}

detekt {
    toolVersion = "1.22.0"
    config = files("detekt.yml")
    buildUponDefaultConfig = true
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")
}

tasks.withType<Detekt>().configureEach {
    setSource(files(rootDir))
    include("**/*.kt")
    include("**/*.kts")
    exclude("build/")
    exclude("*/build/")

    reports {
        html.required.set(true)
        md.required.set(true)
    }
}

tasks.withType<DependencyUpdatesTask> {
    fun isStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        return stableKeyword || regex.matches(version)
    }
    gradleReleaseChannel = "current"
    rejectVersionIf {
        isStable(candidate.version).not()
    }
}
