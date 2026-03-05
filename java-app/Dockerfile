FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests -pl carddemo-online -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/carddemo-online/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
