# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set work directory inside the container
WORKDIR /app

# Copy the built JAR file from the host to the container
COPY target/SpringShield-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8081

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Local
## docker build -t springshield-app .
## docker run -p 8081:8081 --name springshield-container springshield-app
## curl http://localhost:8081