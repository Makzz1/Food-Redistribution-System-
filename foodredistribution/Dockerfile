# ── Stage 1: Build ───────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies first (this caches dependencies)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the actual source code
COPY src ./src

# Build the production JAR (skip tests for faster builds)
RUN mvn clean package -DskipTests -B

# ── Stage 2: Run (lightweight JRE only) ─────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/food-redistribution.jar app.jar

# Create uploads directory for file storage
RUN mkdir -p /app/uploads

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# The command to run when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
