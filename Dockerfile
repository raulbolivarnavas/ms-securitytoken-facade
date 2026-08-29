FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

COPY . .

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :bootstrap:bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update && \
    apt-get install --no-install-recommends -y curl && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd --system --gid 1001 spring && \
    useradd --system --uid 1001 --gid spring --create-home --shell /usr/sbin/nologin spring

COPY --from=builder /workspace/bootstrap/build/libs/*.jar app.jar

USER spring:spring

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENV SERVER_PORT=8080

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=5 \
    CMD curl --fail --silent http://127.0.0.1:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]