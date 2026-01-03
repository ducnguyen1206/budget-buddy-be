## Multi-stage Dockerfile for BudgetBuddy (Spring Boot, Java 21)

# 1) Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Leverage Docker layer caching for dependencies
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./mvnw
RUN chmod +x ./mvnw
RUN ./mvnw -q -B -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN ./mvnw -q -B -DskipTests package

# 2) Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy fat jar from builder (artifact name may vary); copy into /app directory
# We intentionally copy with wildcard to handle versioned artifact names
COPY --from=builder /workspace/target/*SNAPSHOT.jar /app/

# Non-root user for better security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

EXPOSE 8080

# Allow custom JVM options via JAVA_OPTS
ENV JAVA_OPTS=""

# Run any jar in /app (boot jar) — avoids hardcoding artifact name
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/*.jar"]
