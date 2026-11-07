plugins {
    id("java")
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0"
}

group = "io.github.mirancz.libreinfo"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.tukaani:xz:1.10")

    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}