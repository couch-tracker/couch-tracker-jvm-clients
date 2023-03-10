import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.github.couchtracker"
version = "1.0-SNAPSHOT"

kotlin {
    jvm {
        jvmToolchain(15)
        withJava()
    }
    @Suppress("UnusedPrivateMember")
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)
                // Logging
                implementation("ch.qos.logback:logback-classic:1.4.5")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "client-android-desktop"
            packageVersion = "1.0.0"
        }
    }
}
