plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.9.25"
}

allOpen { annotation("jakarta.persistence.Entity") }        // or open all entities
noArg { annotation("jakarta.persistence.Entity") }

group = "org.iot_platform"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // PostgreSQL JDBC driver
    runtimeOnly("org.postgresql:postgresql")

    // Кэширование
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.2")

    // Для валидаций
    implementation("jakarta.validation:jakarta.validation-api")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Liquibase
    implementation("org.liquibase:liquibase-core")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
