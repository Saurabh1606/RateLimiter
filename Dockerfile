FROM eclipse-temurin:21-jdk-alpine

RUN apk add --no-cache curl


RUN addgroup -g 1001 appgroup && adduser -u 1001 -G appgroup -s /bin/sh -D appuser

WORKDIR /app

COPY target/rate_limit-0.0.1-SNAPSHOT.jar app.jar


RUN chown -R appuser:appgroup /app


USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]