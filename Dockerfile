# Stage 1: Cache Gradle dependencies
FROM gradle:latest AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME=/home/gradle/cache_home
# Set memory limits for Gradle
ENV GRADLE_OPTS="-Xmx512m -Xms128m -XX:MaxMetaspaceSize=256m"
# Configure Gradle to use less memory for workers
ENV ORG_GRADLE_PROJECT_org.gradle.workers.max=2
COPY build.gradle.* gradle.properties settings.gradle.kts /home/gradle/app/
COPY gradle /home/gradle/app/gradle
WORKDIR /home/gradle/app
RUN gradle server:build --no-daemon  --no-build-cache

# Stage 2: Build Application
FROM gradle:latest AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
# Set memory limits for Gradle
ENV GRADLE_OPTS="-Xmx512m -Xms128m -XX:MaxMetaspaceSize=256m"
# Configure Gradle to use less memory for workers
ENV ORG_GRADLE_PROJECT_org.gradle.workers.max=2
# Only copy necessary files for building the server module
COPY --chown=gradle:gradle build.gradle.* gradle.properties settings.gradle.kts /home/gradle/src/
COPY --chown=gradle:gradle gradle /home/gradle/src/gradle
COPY --chown=gradle:gradle server /home/gradle/src/server
COPY --chown=gradle:gradle shared /home/gradle/src/shared
WORKDIR /home/gradle/src
# Build the server module
RUN gradle server:buildFatJar --no-daemon --no-build-cache

# Stage 3: Create the Runtime Image
FROM amazoncorretto:22 AS runtime
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/server/build/libs/fat.jar /app/server.jar
ENTRYPOINT ["java","-jar","/app/server.jar"]
