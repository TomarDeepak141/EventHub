FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S eventhub && adduser -S -G eventhub eventhub
WORKDIR /app
COPY --from=build /workspace/target/demo-0.0.1-SNAPSHOT.jar /app/eventhub.jar
USER eventhub
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=5s --start-period=60s --retries=6 \
  CMD wget -q -O /dev/null http://127.0.0.1:8080/actuator/health/readiness || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/eventhub.jar"]
