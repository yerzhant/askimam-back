import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath("com.google.cloud.tools:jib-spring-boot-extension-gradle:0.1.0")
    }
}

plugins {
    id("org.springframework.boot") version "2.4.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.google.cloud.tools.jib") version "3.0.0"
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.spring") version "1.4.30"
}

group = "kz.azan"
version = System.getenv("version")
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

//extra["solaceSpringBootVersion"] = "1.1.0"
//extra["springCloudVersion"] = "2020.0.1"
extra["testcontainersVersion"] = "1.15.2"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
//    implementation("org.springframework.boot:spring-boot-starter-integration")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//    implementation("com.solace.spring.boot:solace-spring-boot-starter")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//    implementation("org.springframework.integration:spring-integration-http")
//    implementation("org.springframework.integration:spring-integration-jdbc")
//    implementation("org.springframework.integration:spring-integration-security")
    implementation("io.vavr:vavr-kotlin:0.10.2")
    implementation("com.auth0:java-jwt:3.14.0")
    implementation("com.google.firebase:firebase-admin:7.2.0")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    runtimeOnly("mysql:mysql-connector-java:5.1.49")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
        exclude(module = "mockito-core")
    }
//    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
//    testImplementation("org.springframework.integration:spring-integration-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
    testImplementation("com.ninja-squad:springmockk:3.0.1")
    testImplementation("com.tngtech.archunit:archunit-junit5:0.17.0")
    testRuntimeOnly("com.h2database:h2")
}

dependencyManagement {
    imports {
//        mavenBom("com.solace.spring.boot:solace-spring-boot-bom:${property("solaceSpringBootVersion")}")
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
//        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    build {
        dependsOn(jib)
    }
}

val registry = System.getenv("AskimamRegistry") ?: "localhost:5000"

jib {
    from {
        @Suppress("SpellCheckingInspection")
        image = "$registry/openjdk:11-slim@sha256:b789d521bbe81ab0991c59c6d604cf5bec6a2257128a0ecafb15b2d63bbce872"
    }
    to {
        image = "$registry/askimam-back"
        tags = setOf("$version")
    }
    container {
        user = "azan"
        creationTime = "USE_CURRENT_TIMESTAMP"
    }
    pluginExtensions {
        pluginExtension {
            implementation = "com.google.cloud.tools.jib.gradle.extension.springboot.JibSpringBootExtension"
        }
    }
}
