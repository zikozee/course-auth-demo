FROM amazoncorretto:25 AS build
WORKDIR /tmp

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Extract layers using Spring Boot 4.1's tools mode
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

FROM amazoncorretto:25-al2023-headless
WORKDIR /auth

EXPOSE 8080

COPY --from=build --chown=10001:10001 /tmp/extracted/dependencies/ ./
COPY --from=build --chown=10001:10001 /tmp/extracted/spring-boot-loader/ ./
COPY --from=build --chown=10001:10001 /tmp/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=10001:10001 /tmp/extracted/application/ ./

# Use a numeric UID/GID without installing user-management packages
USER 10001:10001

ENTRYPOINT ["java", "-Duser.timezone=Africa/Lagos", "-jar", "app.jar"]
