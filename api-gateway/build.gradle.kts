import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springboot)
    alias(libs.plugins.springdependency)
}

group = "org.iot_platform"
version = "1.0.0"

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
    // Spring Boot Starters
    implementation(libs.spring.webflux)
    implementation(libs.spring.actuator)
    implementation(libs.spring.validation)

    // cloud
    implementation(libs.spring.gateway)
    implementation(libs.spring.gateway.server.webflux)
    implementation(libs.eureka.client)

    // Security and OAuth2
    implementation(libs.spring.security)
    implementation(libs.spring.oauth2.client)
    implementation(libs.spring.oauth2.jose)
    implementation(libs.spring.oauth2.resource)

    // Kotlin
    implementation(libs.jackson.kotlin)
    implementation(libs.reactor.kotlin)
    implementation(libs.kotlin.reflect)
    implementation(libs.coroutines.reactor)

    // Logging
    implementation(libs.kotlin.logging)

    // Redis

    // Metrics
    implementation(libs.micrometer.prometheus)

    // Testing
    testImplementation(libs.spring.test)
    testImplementation(libs.reactor.test)
    testImplementation(libs.security.test)
}

springBoot {
    mainClass.set("org.iot_platform.apigateway.ApiGatewayApplicationKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
//    useJUnitPlatform()
}
