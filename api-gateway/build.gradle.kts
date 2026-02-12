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
    // core gateway
    implementation(libs.spring.gateway)

    // discovery
    implementation(libs.eureka.client)

    // Metrics
    implementation(libs.spring.actuator)
    implementation(libs.micrometer.prometheus)

    // cloud
    implementation(libs.spring.gateway.server.webflux)

    // Security and OAuth2
    implementation(libs.spring.security)
    implementation(libs.spring.oauth2.resource)

//    implementation(libs.spring.oauth2.client)
//    implementation(libs.spring.oauth2.jose)

    // Kotlin support
    implementation(libs.jackson.kotlin)
    implementation(libs.kotlin.reflect)
    implementation(libs.reactor.kotlin)
    implementation(libs.coroutines.reactor)

    // Logging
    implementation(libs.kotlin.logging)

    // Redis

    // Testing
    testImplementation(libs.spring.test)
    testImplementation(libs.reactor.test)
    testImplementation(libs.security.test)
}

springBoot {
    mainClass.set("org.iot_platform.apigateway.ApiGatewayApplicationKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
//    useJUnitPlatform()
}
