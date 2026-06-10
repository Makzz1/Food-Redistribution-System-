FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the packaged jar file from your Windows machine into the container
COPY target/food-redistribution.jar app.jar


# Expose the port your Spring Boot app runs on
EXPOSE 8080

# The command to run when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
