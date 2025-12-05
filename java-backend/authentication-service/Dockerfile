# Authentication Service Dockerfile
# Replaces COSGN00C (CC00 transaction)
#
# Build instructions:
#   1. Build the project: cd java-backend && mvn clean package -DskipTests
#   2. Build the image: docker build -t carddemo-auth ./authentication-service

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Add curl for health checks
RUN apk add --no-cache curl

# Copy the pre-built jar
COPY target/*.jar app.jar

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
