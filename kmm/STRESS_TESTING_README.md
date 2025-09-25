# Comprehensive Stress Testing: Dart vs Kotlin JVM vs Kotlin Native (GraalVM)

This document describes the enhanced stress testing setup that compares three different server implementations:

1. **Dart Server** - High-performance Dart server with Shelf framework
2. **Kotlin JVM Server** - Traditional Ktor server running on JVM
3. **Kotlin Native Server** - GraalVM native image compiled Ktor server

## 🏗️ Architecture Overview

### Server Variants

| Server        | Technology     | Port | Memory Usage | Startup Time | Throughput |
|---------------|----------------|------|--------------|--------------|------------|
| Dart          | Dart + Shelf   | 8082 | Medium       | Fast         | High       |
| Kotlin JVM    | Ktor + JVM     | 8081 | High         | Slow         | High       |
| Kotlin Native | Ktor + GraalVM | 8085 | Low          | Very Fast    | Variable   |

### Key Differences

#### Dart Server

- **Runtime**: Dart VM
- **Compilation**: JIT compilation
- **Memory**: Efficient garbage collection
- **Startup**: Fast startup time
- **Development**: Excellent hot reload

#### Kotlin JVM Server

- **Runtime**: Java Virtual Machine
- **Compilation**: Bytecode + JIT
- **Memory**: JVM heap management
- **Startup**: Slower due to JVM initialization
- **Development**: Excellent debugging tools

#### Kotlin Native Server

- **Runtime**: Native executable
- **Compilation**: Ahead-of-time (AOT)
- **Memory**: No garbage collection overhead
- **Startup**: Instant startup
- **Development**: Limited debugging capabilities

## 🚀 Quick Start

### 1. Setup Environment

```bash
./run-stress-tests.sh setup
```

### 2. Build All Server Variants

```bash
./run-stress-tests.sh build
```

*Note: GraalVM native compilation takes 10-15 minutes*

### 3. Run Comparative Tests

```bash
# Light load test on all servers
./run-stress-tests.sh compare light

# Heavy load test on all servers
./run-stress-tests.sh compare heavy

# Test specific server
./run-stress-tests.sh test medium jvm
```

### 4. Monitor Performance

```bash
./run-stress-tests.sh monitor
```

## 📊 Test Patterns

### Available Test Patterns

| Pattern     | Users | Duration | Ramp-up | Use Case              |
|-------------|-------|----------|---------|-----------------------|
| `light`     | 50    | 30s      | 5s      | Development testing   |
| `medium`    | 200   | 60s      | 10s     | Standard load testing |
| `heavy`     | 500   | 120s     | 15s     | High load scenarios   |
| `extreme`   | 1000  | 300s     | 30s     | Stress testing        |
| `spike`     | 100   | 60s      | 1s      | Spike load testing    |
| `endurance` | 200   | 30min    | 60s     | Long-running tests    |

### Custom Test Patterns

You can modify test patterns in the script:

```bash
# Edit the TEST_CONFIGS array in run-stress-tests.sh
declare -A TEST_CONFIGS=(
    ["custom"]="300 180 20"  # users, duration(s), ramp-up(s)
)
```

## 🔧 Configuration

### Docker Compose Configuration

The stress testing uses `docker-compose.stress.yml` which includes:

- **Resource Limits**: Controlled CPU and memory allocation
- **Health Checks**: Automated service health monitoring
- **Networking**: Isolated stress testing network
- **Monitoring**: Prometheus, Grafana, cAdvisor integration

### GraalVM Native Image Configuration

The native image build includes optimizations for:

- **HTTP/HTTPS Support**: Web server functionality
- **Reflection**: Runtime reflection handling
- **Resources**: Configuration file inclusion
- **Memory**: Optimized memory usage
- **Startup**: Fast startup optimizations

### Build Configuration

```kotlin
graalvmNative {
    binaries {
        named("main") {
            buildArgs.addAll(
                "--no-fallback",
                "--enable-http",
                "--enable-https",
                "--initialize-at-build-time=kotlinx.coroutines",
                "-H:+ReportExceptionStackTraces",
                "-H:+AddAllCharsets"
            )
        }
    }
}
```

## 📈 Performance Metrics

### Collected Metrics

#### Application Metrics

- **Throughput**: Requests per second
- **Latency**: Response time percentiles
- **Success Rate**: Percentage of successful requests
- **Error Rate**: Failed request analysis

#### Resource Metrics

- **CPU Usage**: Per-container CPU utilization
- **Memory Usage**: Heap, non-heap, and native memory
- **Network I/O**: Bandwidth utilization
- **Disk I/O**: Storage performance

#### Runtime Metrics

- **Startup Time**: Time to first request
- **Garbage Collection**: GC frequency and duration (JVM only)
- **Thread Usage**: Thread pool utilization
- **Connection Pools**: Database connection efficiency

### Monitoring Stack

#### Prometheus (Port 9090)

- Metrics collection from all servers
- 2-second scrape interval for stress testing
- Custom alerting rules

#### Grafana (Port 3000)

- Real-time performance dashboards
- Comparative analysis views
- Resource utilization monitoring

#### cAdvisor (Port 8084)

- Container resource monitoring
- Docker metrics collection
- Performance bottleneck identification

## 🎯 Testing Scenarios

### 1. Throughput Comparison

```bash
# Compare maximum throughput
./run-stress-tests.sh compare extreme
```

### 2. Memory Efficiency Test

```bash
# Test memory usage under load
./run-stress-tests.sh compare endurance
```

### 3. Startup Time Analysis

```bash
# Stop all services
./run-stress-tests.sh stop

# Start and measure startup times
time ./run-stress-tests.sh start native  # Fastest
time ./run-stress-tests.sh start dart    # Fast
time ./run-stress-tests.sh start jvm     # Slowest
```

### 4. Spike Load Testing

```bash
# Test response to sudden load spikes
./run-stress-tests.sh compare spike
```

### 5. Resource Constraint Testing

```bash
# Test with limited resources
docker compose -f docker-compose.stress.yml up -d --scale app-jvm=1 --scale app-native=1
```

## 📊 Expected Performance Characteristics

### Startup Time

1. **Kotlin Native**: ~100ms (instant)
2. **Dart**: ~2-5 seconds
3. **Kotlin JVM**: ~10-30 seconds

### Memory Usage (Idle)

1. **Kotlin Native**: ~50-100MB
2. **Dart**: ~100-200MB
3. **Kotlin JVM**: ~200-500MB

### Memory Usage (Under Load)

1. **Kotlin Native**: ~100-300MB
2. **Dart**: ~200-400MB
3. **Kotlin JVM**: ~500-2000MB

### Throughput (Expected)

- **All servers**: 1000-5000 req/sec (varies by hardware)
- **Performance order**: Usually JVM ≥ Dart > Native (but varies)

### Latency

- **All servers**: <50ms for simple requests
- **Native**: Consistent low latency
- **JVM**: May have GC pauses
- **Dart**: Consistent performance

## 🔍 Analysis and Interpretation

### When to Choose Each Technology

#### Choose Dart When:

- ✅ Fast development cycles needed
- ✅ Good balance of performance and productivity
- ✅ Team familiar with Dart/Flutter ecosystem
- ✅ Moderate resource constraints

#### Choose Kotlin JVM When:

- ✅ Maximum throughput required
- ✅ Complex business logic
- ✅ Rich ecosystem and libraries needed
- ✅ Advanced debugging and profiling required
- ✅ Team familiar with JVM ecosystem

#### Choose Kotlin Native When:

- ✅ Minimal memory footprint critical
- ✅ Fast startup time essential
- ✅ Containerized/serverless deployments
- ✅ Resource-constrained environments
- ✅ Predictable performance required

### Performance Trade-offs

#### Kotlin Native Considerations

- **Pros**: Fast startup, low memory, predictable performance
- **Cons**: Longer build times, limited debugging, potential throughput limitations
- **Best for**: Microservices, serverless, edge computing

#### JVM Considerations

- **Pros**: High throughput, mature ecosystem, excellent tooling
- **Cons**: High memory usage, slow startup, GC pauses
- **Best for**: High-traffic applications, complex business logic

#### Dart Considerations

- **Pros**: Good balance, fast development, consistent performance
- **Cons**: Smaller ecosystem compared to JVM
- **Best for**: Full-stack development, rapid prototyping

## 🛠️ Troubleshooting

### Common Issues

#### GraalVM Build Failures

```bash
# Check GraalVM installation
java -version

# Increase build memory
export GRADLE_OPTS="-Xmx8g"

# Clean and rebuild
./gradlew clean
./run-stress-tests.sh build
```

#### Port Conflicts

```bash
# Check port usage
netstat -tlnp | grep :808

# Stop conflicting services
./run-stress-tests.sh stop
```

#### Memory Issues

```bash
# Increase Docker memory allocation
# Docker Desktop > Settings > Resources > Memory

# Monitor memory usage
docker stats
```

#### Performance Inconsistencies

- Ensure consistent hardware state
- Run tests multiple times
- Check for background processes
- Verify resource limits

### Debugging Performance Issues

#### Application Level

```bash
# Check application logs
docker compose -f docker-compose.stress.yml logs app-jvm
docker compose -f docker-compose.stress.yml logs app-native
docker compose logs server  # Dart server
```

#### System Level

```bash
# Monitor system resources
htop
iostat -x 1
vmstat 1
```

#### Network Level

```bash
# Check network performance
iftop
netstat -i
ss -tuln
```

## 📝 Results Analysis

### Automated Reports

The stress testing script generates:

1. **JSON Results**: Detailed metrics for each test
2. **Markdown Reports**: Human-readable comparison
3. **Performance Rankings**: Sorted by key metrics

### Manual Analysis

#### Grafana Dashboards

- Navigate to http://localhost:3000
- Use "Stress Testing" folder
- Compare metrics across servers

#### Prometheus Queries

```promql
# Request rate comparison
rate(http_requests_total[5m])

# Memory usage comparison
container_memory_usage_bytes{name=~".*app.*"}

# CPU usage comparison
rate(container_cpu_usage_seconds_total{name=~".*app.*"}[5m])
```

## 🚀 Advanced Usage

### Custom Load Patterns

Create custom test scripts in `stress-tests/` directory:

```bash
#!/bin/bash
# custom-load-pattern.sh

# Your custom load testing logic
for i in {1..1000}; do
    curl -s "$1/api/endpoint" &
    if [ $((i % 100)) -eq 0 ]; then
        wait
    fi
done
```

### CI/CD Integration

```yaml
# .github/workflows/stress-test.yml
name: Stress Testing
on: [push, pull_request]

jobs:
  stress-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Run stress tests
        run: |
          ./run-stress-tests.sh setup
          ./run-stress-tests.sh build
          ./run-stress-tests.sh compare medium
```

### Production Monitoring

Deploy monitoring stack to production:

```bash
# Production monitoring setup
docker compose -f docker-compose.stress.yml up -d prometheus-stress grafana-stress
```

## 📚 Additional Resources

- [GraalVM Native Image Documentation](https://www.graalvm.org/native-image/)
- [Ktor Performance Guide](https://ktor.io/docs/performance.html)
- [Dart Server Performance](https://dart.dev/server)
- [Prometheus Monitoring](https://prometheus.io/docs/)
- [Grafana Dashboards](https://grafana.com/docs/)

## 🤝 Contributing

To add new test patterns or improve the testing infrastructure:

1. Fork the repository
2. Create a feature branch
3. Add your improvements
4. Test with all server variants
5. Submit a pull request

## 📄 License

This stress testing framework is part of the KMM Task Manager project and follows the same license terms. 