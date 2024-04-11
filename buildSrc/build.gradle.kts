
import org.jetbrains.changelog.date
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    // Support convention plugins written in Kotlin. Convention plugins are build scripts in 'src/main' that automatically become available as plugins in the main build.
    kotlin("jvm") version "1.9.10"
    id("org.jetbrains.kotlinx.kover") version "0.7.3"
    id("com.gradle.plugin-publish") version "1.2.1"
    id("net.researchgate.release") version "2.8.1"
    id("org.jetbrains.changelog") version "1.3.1"
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    mavenLocal()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("commons-io:commons-io:2.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.wiremock:wiremock:3.0.4")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    test {
        useJUnitPlatform()
        testLogging.showStandardStreams = true
    }
    afterReleaseBuild {
        dependsOn(
            "publish",
            "publishPlugins",
            "patchChangelog"
        )
    }
    compileKotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

gradlePlugin {
    website.set("https://github.com/PrzemyslawSwiderski/python-gradle-plugin")
    vcsUrl.set("https://github.com/PrzemyslawSwiderski/python-gradle-plugin")
    plugins {
        create("python-gradle-plugin") {
            id = "com.pswidersk.python-plugin"
            implementationClass = "com.pswidersk.gradle.python.PythonPlugin"
            displayName = "Gradle plugin to run Python projects in Conda virtual env. "
                .plus("https://github.com/PrzemyslawSwiderski/python-gradle-plugin")
            description = "Gradle plugin to run Python projects."
            tags.set(
                listOf(
                    "python",
                    "venv",
                    "numpy",
                    "conda",
                    "miniconda",
                    "anaconda",
                    "scipy",
                    "pandas",
                    "flask",
                    "matplotlib",
                    "sklearn"
                )
            )
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}

// Configuring changelog Gradle plugin https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    header.set(provider { "[${version.get()}] - ${date()}" })
}
