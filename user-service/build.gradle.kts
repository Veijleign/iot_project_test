plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springboot)
    alias(libs.plugins.springdependency)
}

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

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${libs.versions.springcloud.get()}")
    }
}

dependencies {
    // CORE
    implementation(libs.spring.web)
    implementation(libs.spring.data.jpa)

    // eureka
    implementation(libs.eureka.client)

    // Understand wtd is this
    implementation(libs.spring.actuator)
    implementation(libs.spring.validation)

    // security
    implementation(libs.spring.security)
    implementation(libs.spring.oauth2.resource)

    // PostgreSQL JDBC driver
    runtimeOnly(libs.postgresql.lib)

    // Кэширование
    implementation(libs.caffeine.lib)

    // Для валидаций
    implementation(libs.jakarta.validation.api)

    // Jackson
    implementation(libs.jackson.java)

    //swagger
    implementation(libs.openapi.starter)

    // Kotlin
    implementation(libs.kotlin.reflect)
    implementation(libs.jackson.kotlin)

    // Liquibase
    implementation(libs.liquibase.core)

    // Logging
    implementation(libs.kotlin.logging)

    // Testing
    testImplementation(libs.spring.test)
    testImplementation(libs.reactor.test)
    testImplementation(libs.security.test)
    testImplementation(libs.junit.launcher)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

//tasks.withType<Test> {
//    useJUnitPlatform()
//}
