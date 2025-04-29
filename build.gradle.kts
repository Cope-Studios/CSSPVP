import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.copestudios"
version = "1.0.1"
description = "Cope Studios Simple PVP Plugin"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Paper API - using 1.20.4 which is compatible with Java 17
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    // Kotlin
    implementation(kotlin("stdlib"))

    // For colorful text (these are included in Paper API, so we just need them at compile time)
    compileOnly("net.kyori:adventure-api:4.14.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.14.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        filesMatching("plugin.yml") {
            expand(
                "name" to project.name,
                "version" to project.version,
                "description" to project.description,
                "apiVersion" to "1.20"  // Changed to match Paper API version
            )
        }
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    shadowJar {
        archiveClassifier.set("")

        // We need to include Kotlin stdlib in the JAR
        dependencies {
            include(dependency("org.jetbrains.kotlin:.*"))
        }

        // Relocate Kotlin stdlib to avoid conflicts with other plugins
        relocate("kotlin", "com.copestudios.csspvp.libs.kotlin")
    }

    build {
        dependsOn(shadowJar)
    }
}