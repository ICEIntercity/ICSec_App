# Build stage
FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# Run stage
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/ICSec_App-0.0.1-SNAPSHOT.jar app.jar
VOLUME /app/data
EXPOSE 8080
# Activate the prod profile (disables H2 console).
# CLAUDE_API_KEY is optional — omit it and AI endpoints return 503.
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]