# 1) Use standard Java 21 JRE (Lightweight, but full Linux compatibility)
# We use 'eclipse-temurin' (Ubuntu-based) instead of Alpine to avoid
# weird DNS/C-library issues common with Java on Alpine.
FROM eclipse-temurin:21-jre

WORKDIR /app

# 2) COPY the JAR that Jenkins ALREADY built
# We copy it to a fixed name 'app.jar' to simplify the entrypoint
COPY target/*.jar app.jar

# 3) Expose the port
EXPOSE 8080

# 4) Allow custom JVM options (Good for setting memory limits later)
ENV JAVA_OPTS=""

# 5) Run the app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]