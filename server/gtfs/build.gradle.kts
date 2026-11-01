plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.5"
    kotlin("jvm")
}

group = "io.github.mirancz"
version = "0.1"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation("org.tukaani:xz:1.10")
    implementation("com.google.code.gson:gson:2.14.0")

    implementation("tools.jackson.dataformat:jackson-dataformat-csv:3.2.4")
    implementation("tools.jackson.core:jackson-databind:3.2.4")

    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("org.java-websocket:Java-WebSocket:1.6.0")
    // slf4j binding for Java-WebSocket (avoids the "no providers" warning)
    implementation("org.slf4j:slf4j-simple:2.0.16")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}
tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.github.mirancz.Main"
    }
}

tasks.shadowJar {
    // Keep slf4j's ServiceLoader registration intact in the fat jar
    mergeServiceFiles()
}

kotlin {
    jvmToolchain(25)
}