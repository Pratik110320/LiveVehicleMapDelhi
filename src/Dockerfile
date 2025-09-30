# Use a lightweight, official Java 17 base image
FROM eclipse-temurin:17-jdk-alpine

# Set a working directory inside the container
WORKDIR /app

# Copy the compiled .jar file from the target directory into the container
# The JAR file is created when you run "mvn package"
COPY target/Live-Vehicle-Map-Delhi-0.0.1-SNAPSHOT.jar app.jar

# Expose the port that the Spring Boot application will run on
EXPOSE 8080

# The command to run when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
