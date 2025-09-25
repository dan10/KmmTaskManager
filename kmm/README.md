This is a Kotlin Multiplatform project targeting Android, iOS, Server.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that's common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple's CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you're sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* `/server` is for the Ktor server application.

* `/shared` is for the code that will be shared between all targets in the project.
  The most important subfolder is `commonMain`. If preferred, you can add code to the platform-specific folders here too.

## Metrics and Docker Support

This project now includes metrics collection, load testing capabilities, and Docker support.

See [METRICS_AND_LOAD_TESTING.md](METRICS_AND_LOAD_TESTING.md) for detailed information on:

- Setting up and using metrics collection with Prometheus and Grafana
- Running load tests with Gatling (including extended 30-minute tests)
- Running the application in Docker containers
- Comparing performance between JVM and GraalVM native modes

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
