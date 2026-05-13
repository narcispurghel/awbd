import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.github.irinabotea"
version = "0.0.1-SNAPSHOT"
description = "web-ui"

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
    implementation(libs.spring.boot.webmvc)
    implementation(libs.spring.boot.thymeleaf)
    implementation(libs.spring.boot.security)
    implementation(libs.spring.boot.validation)
    implementation(libs.spring.cloud.eureka.client)
    implementation(libs.thymeleaf.extras.springsecurity)
    implementation(libs.webjars.locator)
    implementation(libs.webjars.bootstrap)
    implementation(libs.webjars.bootstrap.icons)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    testImplementation(libs.spring.boot.webmvc.test)
    testImplementation(libs.spring.boot.security.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

// Override the NullAway annotated package set by the root build (which keys off
// `com.github.narcispurghel.<module>`) so this module is checked under its own
// `com.github.irinabotea.webui` namespace.
tasks.withType<JavaCompile>().configureEach {
    options.errorprone.option("NullAway:AnnotatedPackages", "com.github.irinabotea.webui")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
