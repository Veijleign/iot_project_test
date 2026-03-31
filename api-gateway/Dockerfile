# Part 1: build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY gradle.properties .

COPY api-gateway ./api-gateway

RUN chmod +x gradlew
RUN ./gradlew :api-gateway:build -x test

# Part 2: runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# extract the compiled JAR from the builder stage
COPY --from=builder /workspace/api-gateway/build/libs/*.jar api_gateway.jar

EXPOSE 8080

# for non-blocking instant startup of Tomcat
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "api_gateway.jar"]