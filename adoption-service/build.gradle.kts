plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.github.narcispurghel"
version = "0.0.1-SNAPSHOT"
description = "adoption-service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] =
    libs.versions.spring.cloud
        .get()

dependencies {
    implementation(project(":common"))
    implementation(libs.spring.cloud.config.client)
    implementation(libs.spring.boot.data.jpa)
    implementation(libs.spring.boot.validation)
    implementation(libs.spring.boot.webmvc)
    implementation(libs.spring.boot.security)
    implementation(libs.spring.cloud.eureka.client)
    implementation(libs.dotenv.java)
    runtimeOnly(libs.postgresql)
    testImplementation(libs.spring.boot.data.jpa.test)
    testImplementation(libs.spring.boot.validation.test)
    testImplementation(libs.spring.boot.webmvc.test)
    testImplementation(libs.spring.boot.security.test)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation("org.testcontainers:postgresql:1.21.4")
    testRuntimeOnly(libs.junit.platform.launcher)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
