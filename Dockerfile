# Runtime-only image. Build the executable jar on the host first:
#   ./mvnw clean package -DskipTests
# then build the image:
#   docker build -t intercitycz/icsec_app:latest .
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/ICSec_App-*.jar app.jar
VOLUME /app/data
EXPOSE 8080
# Activate the prod profile (disables H2 console).
# CLAUDE_API_KEY is optional - omit it and AI endpoints return 503.
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]