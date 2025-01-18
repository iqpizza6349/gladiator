plugins {
    java
    kotlin("jvm") version "1.9.23"
}

group = "io.iqpizza"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val jacksonVersion = "2.17.2"
dependencies {
    implementation("com.github.ocraft:ocraft-s2client-bot:0.4.18") {
        exclude("com.fasterxml.jackson.core", "jackson-databind")
        exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j-impl")
        exclude(group = "org.apache.logging.log4j", module = "log4j-core")
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    implementation("org.slf4j:slf4j-api:2.0.15")
    implementation("org.slf4j:jcl-over-slf4j:2.0.15")
    implementation("ch.qos.logback:logback-core:1.5.15")
    implementation("ch.qos.logback:logback-classic:1.5.15")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}
tasks.withType<JavaExec> {
    jvmArgs(
        "-Dslf4j.internal.verbosity=ERROR"
    )
}
