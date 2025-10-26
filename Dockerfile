FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

# Download database file from GitHub Release
RUN apt-get update && apt-get install -y wget && \
    wget -O /app/gtfs.db https://github.com/Pratik110320/LiveVehicleMapDelhi/releases/download/v1.db/gtfs.db

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]