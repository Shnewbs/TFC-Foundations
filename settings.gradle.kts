pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        exclusiveContent {
            forRepository { maven("https://maven.neoforged.net/releases") }
            filter { includeGroupAndSubgroups("net.neoforged") }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "TFC-Foundations"
