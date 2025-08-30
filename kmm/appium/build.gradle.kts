plugins {
    id("java-library")
    alias(libs.plugins.kotlinJvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    testImplementation("io.appium:java-client:9.5.0")
    testImplementation(kotlin("test"))
}