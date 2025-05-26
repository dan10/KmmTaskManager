# Grafana Monitoring Dashboard

This directory contains the configuration for a Grafana dashboard that monitors the Task Manager application. The
dashboard provides real-time metrics for:

1. **Active Requests per Minute** - Rate of change in active HTTP requests
2. **Success Responses (2xx)** - Rate of successful HTTP responses
3. **Error Responses (4xx/5xx)** - Rate of client and server error responses
4. **JVM CPU Usage (per minute)** - Rate of CPU usage by the Java Virtual Machine process
5. **System CPU Usage (per minute)** - Rate of CPU usage by the entire system
6. **Memory Usage** - Heap and non-heap memory consumption
7. **System Mode CPU Usage** - The average amount of CPU time spent in system mode, per second, over the last minute (
   from node_exporter)
8. **Container Memory Usage** - Memory usage for each container (from cAdvisor)
9. **Host Disk Usage** - Disk usage percentage for each mount point (from node_exporter)
10. **Host Network Traffic** - Network traffic for each network interface (from node_exporter)

## Accessing the Dashboard

1. Start the application using Docker Compose:
   ```bash
   docker-compose up -d
   ```

2. Access Grafana at http://localhost:3000
    - Username: admin
    - Password: admin (as configured in docker-compose.yml)

3. The Task Manager Application Dashboard should be automatically loaded and available on the Grafana home page.

## Dashboard Structure

The dashboard is divided into ten panels:

1. **Active Requests per Minute**
    - Shows the rate of change in active HTTP requests per minute
    - Helps monitor request activity and identify sudden changes in traffic patterns

2. **Success Responses (2xx)**
    - Shows the rate of successful HTTP responses per second
    - Helps monitor the application's normal operation

3. **Error Responses (4xx/5xx)**
    - Shows the rate of client errors (4xx) and server errors (5xx)
    - Helps identify issues with the application

4. **JVM CPU Usage (per minute)**
    - Shows the rate of CPU usage by the Java Virtual Machine process
    - Helps monitor application-specific CPU utilization and identify performance bottlenecks

5. **System CPU Usage (per minute)**
    - Shows the rate of CPU usage by the entire system
    - Helps monitor overall system load and identify if the application is competing for resources

6. **Memory Usage**
    - Shows heap and non-heap memory usage
    - Helps identify memory leaks and optimize memory allocation

7. **System Mode CPU Usage**
    - Shows the average amount of CPU time spent in system mode, per second, over the last minute
    - Helps monitor kernel-level operations and system calls
    - Data collected by node_exporter

8. **Container Memory Usage**
    - Shows memory usage in bytes for each container
    - Helps identify containers with high memory consumption
    - Data collected by cAdvisor

9. **Host Disk Usage**
    - Shows disk usage percentage for each mount point
    - Helps monitor storage capacity and prevent disk space issues
    - Data collected by node_exporter

10. **Host Network Traffic**
    - Shows network traffic (received and transmitted) for each network interface
    - Helps monitor network utilization and identify potential bottlenecks
    - Data collected by node_exporter

## Configuration Files

- `dashboards/dashboard.json` - The Grafana dashboard configuration
- `provisioning/datasources/datasource.yml` - Configures Prometheus as a data source
- `provisioning/dashboards/dashboard.yml` - Configures Grafana to load the dashboard

## Customizing the Dashboard

You can customize the dashboard by:

1. Accessing it in Grafana UI
2. Making changes through the Grafana interface
3. Saving the changes

Alternatively, you can modify the `dashboard.json` file directly, but this requires knowledge of the Grafana dashboard
JSON structure.

## Metrics Collection

The metrics are collected by Prometheus from multiple sources:

1. **Application Metrics**: Collected from the application's `/metrics` endpoint, which is exposed by the Micrometer and
   Prometheus libraries integrated into the Ktor application.

2. **Container Metrics**: Collected by cAdvisor, which provides container-level metrics including CPU usage, memory
   usage, network statistics, and more.

3. **Host System Metrics**: Collected by node_exporter, which provides host-level metrics including CPU usage, memory
   usage, disk usage, network statistics, and more.

## Additional Monitoring Services

The monitoring setup includes the following services:

1. **Prometheus**: Collects and stores metrics from the application, cAdvisor, and node_exporter.

2. **Grafana**: Visualizes the metrics collected by Prometheus through customizable dashboards.

3. **cAdvisor**: Analyzes resource usage and performance characteristics of running containers.

4. **node_exporter**: Exposes hardware and OS metrics from the host system.
