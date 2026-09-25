import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Plain data types shared by every layer. Pure JVM: no Android, no storage, no networking.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    // no jvmToolchain(17): it would look for (or download) a separate JDK 17
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    testImplementation(libs.junit)
}
