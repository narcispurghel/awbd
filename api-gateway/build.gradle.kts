plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.github.narcispurghel"
version = "0.0.1-SNAPSHOT"
description = "api-gateway"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = libs.versions.spring.cloud.get()

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webmvc")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation(libs.spring.data.redis)
    implementation(libs.dotenv.java)
    testImplementation(libs.spring.boot.webmvc.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.jjwt.api)
    testRuntimeOnly(libs.jjwt.impl)
    testRuntimeOnly(libs.jjwt.jackson)
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
