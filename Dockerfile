# ── Stage 1: Build ───────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

ARG MODULE

WORKDIR /workspace

# Copy Gradle wrapper and build files first for better layer caching
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/

# Copy all module build files
COPY carddemo-shared/build.gradle.kts           carddemo-shared/
COPY carddemo-schema/build.gradle.kts           carddemo-schema/
COPY carddemo-auth-service/build.gradle.kts     carddemo-auth-service/
COPY carddemo-account-service/build.gradle.kts  carddemo-account-service/
COPY carddemo-card-service/build.gradle.kts     carddemo-card-service/
COPY carddemo-transaction-service/build.gradle.kts carddemo-transaction-service/
COPY carddemo-batch-service/build.gradle.kts    carddemo-batch-service/
COPY carddemo-report-service/build.gradle.kts   carddemo-report-service/
COPY carddemo-migration-tool/build.gradle.kts   carddemo-migration-tool/

# Copy source code
COPY carddemo-shared/src/   carddemo-shared/src/
COPY carddemo-schema/src/   carddemo-schema/src/
COPY ${MODULE}/src/          ${MODULE}/src/
COPY config/                 config/

# Build the target module
RUN chmod +x gradlew && ./gradlew :${MODULE}:bootJar --no-daemon -x test -x checkstyleMain -x checkstyleTest -x spotbugsMain -x spotbugsTest

# ── Stage 2: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

ARG MODULE

RUN addgroup -S carddemo && adduser -S carddemo -G carddemo

WORKDIR /app

COPY --from=builder /workspace/${MODULE}/build/libs/*.jar app.jar

RUN chown -R carddemo:carddemo /app

USER carddemo

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
