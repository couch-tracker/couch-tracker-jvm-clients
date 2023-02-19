import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    kotlin("plugin.serialization") version "1.8.0"
    id("app.cash.sqldelight") version "2.0.0-alpha05"
}

group = "com.github.couchtracker"
version = "1.0-SNAPSHOT"

kotlin {
    android()
    jvm("desktop") {
        jvmToolchain(11)
    }
    sourceSets {
        val ktorVersion = "2.2.3"
        val sqldelightVersion = "2.0.0-alpha05"
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.materialIconsExtended)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

                // HTTP client
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-auth:$ktorVersion")
                // Database
                implementation("app.cash.sqldelight:primitive-adapters:$sqldelightVersion")
                implementation("app.cash.sqldelight:coroutines-extensions:$sqldelightVersion")
                implementation("app.cash.sqldelight:async-extensions:$sqldelightVersion")
                // Images
                implementation("io.github.qdsfdhvh:image-loader:1.2.9")
                // Logging
                implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.9.0")
                // Database
                implementation("app.cash.sqldelight:android-driver:$sqldelightVersion")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
                // Database
                implementation("app.cash.sqldelight:sqlite-driver:$sqldelightVersion")
                // Cross-platform directory
                implementation("net.harawata:appdirs:1.2.1")
            }
        }
        val desktopTest by getting
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
        targetSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.github.couchtracker.jvmclients.common.data")
        }
    }
}
