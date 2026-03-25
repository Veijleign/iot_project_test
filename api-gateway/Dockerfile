FROM eclipse-temurin:17-jre-alpine

# prevent from slowing write-read speed
VOLUME /tmp

# the build path
COPY build/libs/api-gateway.jar api_gateway.jar

EXPOSE 8080

# for non-blocking instant startup of Tomcat
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/api_gateway.jar"]