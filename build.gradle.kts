repositories { mavenCentral() }

plugins {
    kotlin("jvm") version "2.3.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("plugin.serialization") version "2.3.20"
}

dependencies {
    val ktor_version = "2.3.11"

    implementation("ch.qos.logback:logback-classic:1.5.34")
    implementation("io.github.classgraph:classgraph:4.8.174")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")

    implementation("io.ktor:ktor-server-cio:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")

    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-gson:$ktor_version")

    implementation("dev.kord:kord-core:0.14.0")
}

tasks.jar { enabled = false }
tasks.shadowJar {
    manifest { attributes["Main-Class"] = rootProject.name }
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("")
    archiveVersion.set("")

    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.build.get().dependsOn(tasks.shadowJar)