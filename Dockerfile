# Multi-stage build for BudgetBuddy Spring Boot app
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Leverage Docker layer caching
COPY pom.xml ./
COPY mvnw .
COPY .mvn .mvn

# Pre-fetch dependencies (optional, will continue if wrapper not executable on host)
RUN chmod +x mvnw || true
RUN ./mvnw -q -DskipTests dependency:go-offline || true

# Copy sources and build the jar
COPY src ./src
RUN ./mvnw -q -DskipTests package

# Runtime image
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Copy the fat jar from the builder stage
COPY --from=build /app/target/*.jar /app/app.jar

# Default JVM and Spring profile (can be overridden at runtime)
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=cloud

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
