# Part 1: build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

# copy wrapper and settings
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Copy
COPY user-service ./user-service

# Compile only
RUN chmod +x gradlew
RUN ./gradlew :user-service:build -x test

# Part 2: runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /workspace/user-service/build/libs/*.jar user_service.jar

EXPOSE 8080

# for non-blocking instant startup of Tomcat
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "user_service.jar"]