repositories { mavenCentral() }

plugins {
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    implementation("net.dv8tion:JDA:5.2.1")
    implementation("org.reflections:reflections:0.10.2")
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.jar { enabled = false }
tasks.shadowJar {
    manifest { attributes["Main-Class"] = "NoammBot" }
    archiveBaseName.set("noammbot")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.build.get().dependsOn(tasks.shadowJar)