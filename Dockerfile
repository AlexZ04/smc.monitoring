FROM gradle:9.3.0-jdk21 AS builder

WORKDIR /workspace

COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8024

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--server.port=8024"]
