plugins {
    application
    id("org.codearena.ktpy.java-conventions")
    kotlin("jvm") version "1.9.22"
}

application {
    mainClass = "org.codearena.ktpy.app.MainKt"
}

group = "org.codearena.ktpy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}