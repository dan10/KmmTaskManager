# Use the official Gradle image as a build stage
FROM gradle:8.7.0-jdk17 AS build

# Set the working directory
WORKDIR /app

# Copy the entire project
COPY . .

# Build the application
RUN gradle :server:build --no-daemon

# Use the official OpenJDK image for the runtime stage
FROM openjdk:17-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/server/build/libs/server-1.0.0.jar /app/server.jar

# Expose the port the app runs on
EXPOSE 8080

# Set environment variables
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Command to run the application
CMD ["java", "-jar", "server.jar"]