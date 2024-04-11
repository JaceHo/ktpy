plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
    mavenLocal()

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    maven {
        url = uri("https://repo1.maven.org/maven2/")
    }

    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    api("org.slf4j:slf4j-api:2.0.7")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
    testImplementation("org.slf4j:slf4j-simple:2.0.7")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
}

group = "org.codearena.ktpython"
version = "1.1.0-SNAPSHOT"

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks {
    test {
        useJUnitPlatform()
        testLogging.showStandardStreams = true
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

java.sourceCompatibility = JavaVersion.VERSION_17
