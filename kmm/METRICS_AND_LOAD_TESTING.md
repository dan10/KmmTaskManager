# Metrics and Load Testing

This document describes how to use the metrics collection and load testing capabilities in the KMM Task Manager project.

## Metrics Collection

The server component includes metrics collection using Micrometer and Prometheus. Metrics are exposed at the `/metrics`
endpoint.

### Available Metrics

- JVM memory usage
- Garbage collection statistics
- CPU usage
- Class loader statistics

## Running the Application in Different Modes

The application can be run in different modes to compare performance:

### JVM Mode

To run the application in standard JVM mode:

```bash
./gradlew :server:runJvm
```

This runs the application using the JVM, which is the traditional way of running Java/Kotlin applications.

### GraalVM Native Mode

To compile and run the application as a GraalVM native executable:

```bash
# First compile the native executable (this may take several minutes)
./gradlew :server:nativeCompile

# Then run the native executable
./gradlew :server:runNative
```

GraalVM native executables typically have faster startup times and lower memory footprint compared to JVM applications,
but may have different performance characteristics for long-running processes.

## Running the Application with Docker

The application can be run using Docker Compose, which sets up the following services:

1. **app**: The Task Manager application server
2. **db**: PostgreSQL database
3. **prometheus**: Prometheus for metrics collection
4. **grafana**: Grafana for metrics visualization

### AWS-like Resource Constraints

The Docker configuration includes AWS-like resource constraints to simulate a production environment:

#### Application Server (similar to t3.small)

- CPU: 1 vCPU (limit), 0.5 vCPU (reservation)
- Memory: 2GB (limit), 1GB (reservation)

#### PostgreSQL Database (similar to db.t3.medium)

- CPU: 2 vCPU (limit), 1 vCPU (reservation)
- Memory: 4GB (limit), 2GB (reservation)
- Optimized PostgreSQL configuration parameters for constrained environments

These constraints help simulate a realistic AWS deployment environment and are useful for:

- Testing application performance under realistic resource constraints
- Identifying potential resource bottlenecks
- Ensuring the application can run efficiently in a cloud environment

To start all services:

```bash
docker-compose up -d
```

### Accessing Services

- **Application**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (username: admin, password: admin)

### Setting Up Grafana

1. Log in to Grafana at http://localhost:3000
2. Go to Configuration > Data Sources
3. Add a new Prometheus data source with URL `http://prometheus:9090`
4. Create a new dashboard with panels for:
    - Request rate
    - Response time
    - Error rate
    - JVM memory usage

## Running Load Tests

The project includes Gatling load tests to simulate user traffic and measure performance.

### Standard Load Test (Short Duration)

To run the standard load tests (short duration):

```bash
./gradlew :server:gatlingRun
```

### Extended Load Test (30 Minutes)

To run the extended load test that runs for 30 minutes:

```bash
./gradlew :server:gatlingRunLong
```

> **Note:** The `gatlingRunLong` task runs all Gatling simulations, including the TaskApiSimulation.

This is useful for:

- Testing application stability over longer periods
- Identifying memory leaks or performance degradation
- Comparing JVM vs. GraalVM native performance under sustained load

Both tests execute the load test scenarios defined in `server/src/test/kotlin/com/danioliveira/taskmanager/loadtest/`.

### Load Test Scenarios

1. **Create Task**: Simulates users creating new tasks
2. **Get Tasks**: Simulates users retrieving task lists

### Interpreting Load Test Results

After running the tests, Gatling generates HTML reports in `server/build/reports/gatling/`. These reports include:

- Request count and response time statistics
- Response time distribution
- Number of requests per second
- Error rate

## Comparing JVM vs. GraalVM Native Performance

To compare the performance between JVM and GraalVM native modes:

1. Run the application in JVM mode:
   ```bash
   ./gradlew :server:runJvm
   ```

2. Run the load test:
   ```bash
   ./gradlew :server:gatlingRunLong
   ```

3. Collect and save the metrics and Gatling reports

4. Stop the application and run it in GraalVM native mode:
   ```bash
   ./gradlew :server:runNative
   ```

5. Run the same load test again:
   ```bash
   ./gradlew :server:gatlingRunLong
   ```

6. Compare the metrics and reports between the two runs

Key metrics to compare:

- Startup time
- Memory usage
- Response times under load
- Throughput (requests per second)
- CPU usage
- Stability over the 30-minute test period

## Performance Tuning

Based on the metrics and load test results, you can tune the application by:

1. Adjusting JVM memory settings in the Dockerfile
2. Optimizing database queries
3. Implementing caching where appropriate
4. Scaling the application horizontally

## Docker Configuration

The project includes a Dockerfile for containerizing the server component. The Dockerfile uses a multi-stage build
process:

1. First stage: Uses the Gradle image to build the application
2. Second stage: Uses a slim OpenJDK image for running the application

The docker-compose.yml file sets up the complete environment, including the database, Prometheus, and Grafana.
