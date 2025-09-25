# 🚀 Live Comparison Demo: Dart vs Kotlin JVM

Since we have working builds for Dart and Kotlin JVM servers, let me show you exactly how the comparison system works.

## 🏗️ Current Working Setup

### ✅ Successfully Built Servers:

- **Dart Server**: `dart-server:latest` (~14 seconds build)
- **Kotlin JVM Server**: `ktor-jvm-server:latest` (~8 seconds build)

### ⚠️ Native Server Status:

- **Kotlin Native**: Requires GraalVM setup (in progress)

## 🧪 How to Run Comparisons

### 1. Quick Comparison Test

```bash
# Build the working servers
cd ../task_manager_dart && ./build-fast.sh
cd ../kmm && ./build-fast.sh

# Start individual servers for testing
docker run -d --name dart-test -p 8082:8080 dart-server:latest
docker run -d --name jvm-test -p 8081:8081 ktor-jvm-server:latest

# Wait for startup and test
sleep 10
curl http://localhost:8082/health
curl http://localhost:8081/api/health
```

### 2. Manual Performance Testing

```bash
# Test Dart server
time curl -s http://localhost:8082/health
ab -n 1000 -c 10 http://localhost:8082/health

# Test JVM server  
time curl -s http://localhost:8081/api/health
ab -n 1000 -c 10 http://localhost:8081/api/health
```

### 3. Resource Usage Comparison

```bash
# Monitor resource usage
docker stats dart-test jvm-test --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"
```

## 📊 Expected Comparison Results

### Startup Time

- **Dart**: ~2-5 seconds (AOT compiled)
- **JVM**: ~10-30 seconds (JIT warmup)

### Memory Usage

- **Dart**: ~100-200MB (efficient AOT)
- **JVM**: ~200-500MB (JVM overhead)

### Throughput (after warmup)

- **Dart**: Consistent ~200-300 req/sec
- **JVM**: Higher peak ~300-500 req/sec

### Container Size

- **Dart**: ~15MB (Alpine + binary)
- **JVM**: ~200MB (Alpine + JRE + JAR)

## 🎯 Comparison Framework Features

### Automated Testing Script

The `run-stress-tests.sh` script provides:

1. **Multiple Test Patterns**:
   ```bash
   ./run-stress-tests.sh compare light    # 50 users, 30s
   ./run-stress-tests.sh compare medium   # 200 users, 60s
   ./run-stress-tests.sh compare heavy    # 500 users, 120s
   ```

2. **Automated Reports**:
    - JSON results for each test
    - Markdown comparison tables
    - Performance rankings
    - Resource usage analysis

3. **Real-time Monitoring**:
    - Grafana dashboards
    - Prometheus metrics
    - Container resource monitoring

### Sample Comparison Output

```markdown
# Stress Test Comparison Report

**Test Pattern:** medium (200 users, 60s)

## Results Summary

| Server | Total Requests | Success Rate | Avg Response Time | Requests/sec |
|--------|----------------|--------------|-------------------|--------------|
| Dart   | 12,450        | 99.8%        | 0.045s           | 207.5        |
| JVM    | 15,230        | 99.9%        | 0.038s           | 253.8        |

## Performance Ranking

1. **Kotlin JVM**: 253.8 requests/sec
2. **Dart**: 207.5 requests/sec

## Memory Usage Analysis

- **Dart**: 150MB average, 180MB peak
- **JVM**: 350MB average, 450MB peak

## Recommendations

- **Choose Dart** for: Lower memory usage, faster startup, consistent performance
- **Choose JVM** for: Higher peak throughput, rich ecosystem, complex logic
```

## 🛠️ Fixing Native Build (Future)

To complete the native comparison:

1. **Install GraalVM properly**:
   ```bash
   # Remove quarantine (macOS security)
   sudo xattr -r -d com.apple.quarantine "/Library/Java/JavaVirtualMachines/graalvm-community-openjdk-21"
   
   # Set environment
   export JAVA_HOME="/Library/Java/JavaVirtualMachines/graalvm-community-openjdk-21/Contents/Home"
   export PATH="$JAVA_HOME/bin:$PATH"
   ```

2. **Test native-image**:
   ```bash
   native-image --version
   ```

3. **Build native binary**:
   ```bash
   ./build-native-fast.sh
   ```

## 🚀 Current Capabilities

Even without the native build, you can:

✅ **Compare Dart vs JVM** performance  
✅ **Analyze startup times** and memory usage  
✅ **Run automated stress tests** with multiple patterns  
✅ **Generate comparison reports** with rankings  
✅ **Monitor real-time metrics** during tests  
✅ **Make data-driven decisions** about technology choice

The framework is fully functional for comparing the two most common server deployment scenarios: AOT-compiled binaries (
Dart) vs JVM applications (Kotlin).

## 🎯 Quick Demo Commands

```bash
# 1. Build both servers (fast)
cd ../task_manager_dart && ./build-fast.sh && cd ../kmm && ./build-fast.sh

# 2. Start for comparison
docker run -d --name dart-demo -p 8082:8080 dart-server:latest
docker run -d --name jvm-demo -p 8081:8081 ktor-jvm-server:latest

# 3. Quick performance test
echo "Testing Dart..." && time curl -s http://localhost:8082/health
echo "Testing JVM..." && time curl -s http://localhost:8081/api/health

# 4. Resource monitoring
docker stats dart-demo jvm-demo --no-stream

# 5. Cleanup
docker rm -f dart-demo jvm-demo
```

This gives you a complete comparison framework for making informed decisions about server technology based on actual
performance data! 