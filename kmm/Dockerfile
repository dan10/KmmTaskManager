# Stage 1: Cache Gradle dependencies
FROM gradle:latest AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME=/home/gradle/cache_home
COPY gradle.properties /home/gradle/app/gradle.properties
COPY settings.gradle.kts /home/gradle/app/settings.gradle.kts
COPY build.gradle.kts /home/gradle/app/build.gradle.kts
COPY shared/build.gradle.kts /home/gradle/app/shared/build.gradle.kts
COPY server/build.gradle.kts /home/gradle/app/server/build.gradle.kts
COPY gradle /home/gradle/app/gradle
WORKDIR /home/gradle/app
RUN gradle server:build --no-daemon

# Stage 2: Build Application
FROM gradle:latest AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
# Copy all necessary project files for KMP structure
COPY --chown=gradle:gradle gradle.properties /home/gradle/src/gradle.properties
COPY --chown=gradle:gradle settings.gradle.kts /home/gradle/src/settings.gradle.kts
COPY --chown=gradle:gradle build.gradle.kts /home/gradle/src/build.gradle.kts
COPY --chown=gradle:gradle gradle /home/gradle/src/gradle
COPY --chown=gradle:gradle server /home/gradle/src/server
COPY --chown=gradle:gradle shared /home/gradle/src/shared
WORKDIR /home/gradle/src
# Build the server module's fat JAR
RUN gradle server:buildFatJar --no-daemon

# Stage 3: Create the Runtime Image
FROM amazoncorretto:24-alpine AS runtime
EXPOSE 8081
RUN mkdir /app
COPY --from=build /home/gradle/src/server/build/libs/fat.jar /app/server.jar
ENTRYPOINT ["java","-jar","/app/server.jar"]