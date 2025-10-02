# Use a lightweight Java base image
FROM eclipse-temurin:17-jdk-alpine

# ** NEW: Install tools needed to download and unzip data **
RUN apk add --no-cache curl unzip

# Set the working directory inside the container
WORKDIR /app

# ** NEW: Download and extract the GTFS static data during the build **
# Create a directory for the static data
RUN mkdir -p /app/static
# Download the zip file from the official source and unzip it into the static directory
RUN curl -L "https://otd.delhi.gov.in/gtfs/static/Delhi-GTFS-Static.zip" -o "/tmp/gtfs.zip" && \
    unzip /tmp/gtfs.zip -d /app/static && \
    rm /tmp/gtfs.zip

# Copy the compiled JAR file into the container
# This is done *after* downloading data to leverage Docker layer caching
COPY target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java","-jar","app.jar"]