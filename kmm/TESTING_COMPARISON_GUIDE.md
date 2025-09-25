# 🚀 Complete Testing & Comparison Guide

## Overview

This guide demonstrates how to test and compare performance between three server implementations:

- **Dart Server** (Alpine-based, AOT compiled)
- **Kotlin JVM Server** (Alpine-based, JAR)
- **Kotlin Native Server** (Alpine-based, GraalVM native binary)

## 🏗️ Fast Build System

### Build All Servers (Optimized)

```bash
# Build all three servers with fast approach
./build-all-fast.sh

# Or build individually
cd ../task_manager_dart && ./build-fast.sh    # ~14 seconds
cd ../kmm && ./build-fast.sh                  # ~8 seconds
cd ../kmm && ./build-native-fast.sh           # ~5-10 minutes
```

### Build Performance Comparison

| Server            | Before Optimization | After Optimization | Improvement     |
|-------------------|---------------------|--------------------|-----------------|
| **Dart**          | ~2-3 minutes        | **~14 seconds**    | **10x faster**  |
| **Kotlin JVM**    | 1+ hour             | **~8 seconds**     | **450x faster** |
| **Kotlin Native** | 10-15 minutes       | **~5-10 minutes**  | **2x faster**   |

## 🧪 Comprehensive Testing Framework

### 1. Setup Testing Environment

```bash
./run-stress-tests.sh setup
```

### 2. Build All Servers

```bash
./run-stress-tests.sh build
```

### 3. Start Servers for Testing

#### Start Individual Servers

```bash
./run-stress-tests.sh start dart     # Dart server only
./run-stress-tests.sh start jvm      # Kotlin JVM only  
./run-stress-tests.sh start native   # Kotlin Native only
```

#### Start All Servers

```bash
./run-stress-tests.sh start all      # All three servers + monitoring
```

### 4. Run Performance Tests

#### Test Individual Servers

```bash
# Test specific server with specific pattern
./run-stress-tests.sh test light dart      # Light load on Dart
./run-stress-tests.sh test medium jvm      # Medium load on JVM
./run-stress-tests.sh test heavy native    # Heavy load on Native
```

#### Comparative Testing (All Servers)

```bash
# Compare all servers with same load pattern
./run-stress-tests.sh compare light     # 50 users, 30s
./run-stress-tests.sh compare medium    # 200 users, 60s
./run-stress-tests.sh compare heavy     # 500 users, 120s
./run-stress-tests.sh compare extreme   # 1000 users, 300s
./run-stress-tests.sh compare spike     # Spike test
./run-stress-tests.sh compare endurance # 30-minute test
```

## 📊 Test Patterns Available

| Pattern       | Users | Duration | Ramp-up | Use Case            |
|---------------|-------|----------|---------|---------------------|
| **light**     | 50    | 30s      | 5s      | Quick validation    |
| **medium**    | 200   | 60s      | 10s     | Standard load       |
| **heavy**     | 500   | 120s     | 15s     | High load           |
| **extreme**   | 1000  | 300s     | 30s     | Stress testing      |
| **spike**     | 100   | 60s      | 1s      | Sudden load spikes  |
| **endurance** | 200   | 30min    | 60s     | Long-term stability |

## 🔍 Monitoring & Analysis

### Real-time Monitoring

```bash
./run-stress-tests.sh monitor
```

This opens:

- **Grafana Dashboard**: http://localhost:3000 (admin/admin)
- **Prometheus Metrics**: http://localhost:9090
- **Container Metrics**: http://localhost:8084

### Server Access Points

- **Dart Server**: http://localhost:8082
- **Kotlin JVM**: http://localhost:8081
- **Kotlin Native**: http://localhost:8085

## 📈 Expected Performance Characteristics

### Startup Time Comparison

1. **Kotlin Native**: ~100ms (instant)
2. **Dart**: ~2-5 seconds
3. **Kotlin JVM**: ~10-30 seconds

### Memory Usage Comparison

- **Idle**: Native (50-100MB) < Dart (100-200MB) < JVM (200-500MB)
- **Under Load**: Native (100-300MB) < Dart (200-400MB) < JVM (500-2000MB)

### Throughput Expectations

- **Kotlin JVM**: Highest throughput after warmup
- **Dart**: Consistent performance, good balance
- **Kotlin Native**: Fast startup, lower peak throughput

## 🎯 Comparison Testing Workflow

### Complete Comparison Test

```bash
# 1. Setup environment
./run-stress-tests.sh setup

# 2. Build all servers (fast)
./run-stress-tests.sh build

# 3. Run comprehensive comparison
./run-stress-tests.sh compare medium

# 4. View results
ls stress-test-results-*/
cat stress-test-results-*/comparison_medium_*.md
```

### Sample Comparison Report

The system generates detailed reports like:

```markdown
# Stress Test Comparison Report

**Test Pattern:** medium  
**Configuration:** 200 users, 60s duration, 10s ramp-up

## Results Summary

| Server | Total Requests | Success Rate | Avg Response Time | Requests/sec |
|--------|----------------|--------------|-------------------|--------------|
| Dart | 12,450 | 99.8% | 0.045s | 207.5 |
| Kotlin JVM | 15,230 | 99.9% | 0.038s | 253.8 |
| Kotlin Native | 11,890 | 99.7% | 0.048s | 198.2 |

## Performance Ranking (by Requests/sec)
1. **Kotlin JVM**: 253.8 requests/sec
2. **Dart**: 207.5 requests/sec  
3. **Kotlin Native**: 198.2 requests/sec
```

## 🛠️ Advanced Testing Scenarios

### Custom Load Testing

```bash
# Create custom test patterns by modifying get_test_config() in run-stress-tests.sh
# Add new patterns like:
# "custom") echo "300 90 20" ;;  # 300 users, 90s, 20s ramp-up
```

### CI/CD Integration

```bash
# Automated testing in CI/CD
./run-stress-tests.sh setup
./run-stress-tests.sh build
./run-stress-tests.sh compare medium
# Parse results from stress-test-results-*/comparison_*.json
```

### Resource Monitoring

```bash
# Monitor resource usage during tests
docker stats --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}"
```

## 🧹 Cleanup

### Stop Services

```bash
./run-stress-tests.sh stop
```

### Complete Cleanup

```bash
./run-stress-tests.sh clean
```

## 🎯 Technology Selection Guidelines

Based on test results, choose:

### **Dart Server** when you need:

- Fast development cycles
- Good balance of performance and simplicity
- Flutter ecosystem integration
- Consistent performance across loads

### **Kotlin JVM** when you need:

- Maximum throughput under sustained load
- Rich ecosystem and libraries
- Advanced debugging and profiling tools
- Complex business logic

### **Kotlin Native** when you need:

- Minimal memory footprint
- Fast startup times
- Containerized deployments with resource constraints
- Microservices with quick scaling

## 🚀 Quick Start Example

```bash
# Complete workflow in 5 minutes
cd kmm

# 1. Build all servers (fast)
./build-all-fast.sh

# 2. Run comparison test
./run-stress-tests.sh setup
./run-stress-tests.sh compare light

# 3. View results
cat stress-test-results-*/comparison_light_*.md

# 4. Open monitoring
./run-stress-tests.sh monitor

# 5. Cleanup
./run-stress-tests.sh clean
```

This comprehensive testing framework allows you to make data-driven decisions about which server implementation best
fits your specific use case and performance requirements. 