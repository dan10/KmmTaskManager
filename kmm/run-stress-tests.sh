#!/bin/bash

# Enhanced Stress Testing Script for Dart vs Kotlin JVM vs Kotlin Native
# Supports multiple load patterns and comprehensive performance analysis

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
DART_BASE_URL="http://localhost:8082"
KOTLIN_JVM_BASE_URL="http://localhost:8081"
KOTLIN_NATIVE_BASE_URL="http://localhost:8085"

# Test configurations - using functions instead of associative arrays for compatibility
get_test_config() {
    case $1 in
        "light") echo "50 30 5" ;;      # users, duration(s), ramp-up(s)
        "medium") echo "200 60 10" ;;   # users, duration(s), ramp-up(s)
        "heavy") echo "500 120 15" ;;   # users, duration(s), ramp-up(s)
        "extreme") echo "1000 300 30" ;; # users, duration(s), ramp-up(s)
        "spike") echo "100 60 1" ;;     # users, duration(s), ramp-up(s) - spike test
        "endurance") echo "200 1800 60" ;; # users, duration(s), ramp-up(s) - 30min test
        *) echo "" ;;
    esac
}

# Global variables
RESULTS_DIR="stress-test-results-$(date +%Y%m%d-%H%M%S)"
TEMP_DIR="/tmp/stress-test-$$"
SERVERS=()

# Function to print colored output
print_status() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Function to show usage
show_usage() {
    echo "🚀 Enhanced Stress Testing Script"
    echo "=================================="
    echo ""
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  setup                    - Setup stress testing environment"
    echo "  build                    - Build all server variants"
    echo "  start [servers]          - Start specified servers (dart,jvm,native,all)"
    echo "  stop                     - Stop all services"
    echo "  test [pattern] [servers] - Run stress tests"
    echo "  compare [pattern]        - Run comparative stress tests on all servers"
    echo "  monitor                  - Open monitoring dashboards"
    echo "  clean                    - Clean up test environment"
    echo ""
    echo "Test Patterns:"
    echo "  light     - 50 users, 30s duration"
    echo "  medium    - 200 users, 60s duration"
    echo "  heavy     - 500 users, 120s duration"
    echo "  extreme   - 1000 users, 300s duration"
    echo "  spike     - 100 users, 60s duration, 1s ramp-up"
    echo "  endurance - 200 users, 30min duration"
    echo ""
    echo "Servers:"
    echo "  dart      - Dart server (port 8082)"
    echo "  jvm       - Kotlin JVM server (port 8081)"
    echo "  native    - Kotlin Native server (port 8085)"
    echo "  all       - All servers"
    echo ""
    echo "Examples:"
    echo "  $0 setup"
    echo "  $0 build"
    echo "  $0 start all"
    echo "  $0 test medium jvm"
    echo "  $0 compare heavy"
    echo "  $0 monitor"
}

# Function to check if port is available
check_port() {
    local port=$1
    local service=$2
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        print_status $RED "❌ Port $port is already in use (needed for $service)"
        return 1
    else
        print_status $GREEN "✅ Port $port is available for $service"
        return 0
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local url=$1
    local service=$2
    local max_attempts=60
    local attempt=1
    
    print_status $YELLOW "⏳ Waiting for $service to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            print_status $GREEN "✅ $service is ready!"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_status $RED "❌ $service failed to start within expected time"
    return 1
}

# Function to setup stress testing environment
setup_environment() {
    print_status $BLUE "🔧 Setting up stress testing environment..."
    
    # Create results directory
    mkdir -p "$RESULTS_DIR"
    mkdir -p "$TEMP_DIR"
    
    # Create stress test scripts directory
    mkdir -p stress-tests
    
    # Create Grafana provisioning directories
    mkdir -p grafana/stress-dashboards
    mkdir -p grafana/stress-datasources
    
    # Create datasource configuration
    cat > grafana/stress-datasources/prometheus.yml << 'EOF'
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus-stress:9090
    isDefault: true
    editable: true
EOF

    # Create dashboard configuration
    cat > grafana/stress-dashboards/dashboard.yml << 'EOF'
apiVersion: 1

providers:
  - name: 'stress-dashboards'
    orgId: 1
    folder: 'Stress Testing'
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    allowUiUpdates: true
    options:
      path: /etc/grafana/provisioning/dashboards
EOF

    print_status $GREEN "✅ Environment setup complete"
}

# Function to build all server variants
build_servers() {
    print_status $BLUE "🏗️ Building all server variants..."
    
    # Build Dart server first (fast approach)
    print_status $YELLOW "Building Dart server (fast build)..."
    cd ../task_manager_dart
    ./build-fast.sh
    cd ../kmm
    
    # Build Kotlin JVM server (fast approach)
    print_status $YELLOW "Building Kotlin JVM server (fast build)..."
    ./build-fast.sh
    
    # Build Kotlin Native server (Docker approach)
    print_status $YELLOW "Building Kotlin Native server (Docker build - may take 5-10 minutes)..."
    ./build-native-docker.sh
    
    print_status $GREEN "✅ All servers built successfully"
}

# Function to start servers
start_servers() {
    local servers_to_start=${1:-"all"}
    
    print_status $BLUE "🚀 Starting servers: $servers_to_start"
    
    # Start shared services first
    print_status $YELLOW "Starting shared services (database, monitoring)..."
    docker compose -f docker-compose.stress.yml up -d db minio prometheus-stress grafana-stress cadvisor-stress node-exporter-stress
    
    # Wait for database
    wait_for_service "http://localhost:5432" "PostgreSQL"
    
    case $servers_to_start in
        "dart")
            print_status $YELLOW "Starting Dart server..."
            cd ../task_manager_dart && docker compose up -d && cd ../kmm
            wait_for_service "$DART_BASE_URL/health" "Dart Server"
            SERVERS=("dart")
            ;;
        "jvm")
            print_status $YELLOW "Starting Kotlin JVM server..."
            docker compose -f docker-compose.stress.yml up -d app-jvm
            wait_for_service "$KOTLIN_JVM_BASE_URL/api/health" "Kotlin JVM Server"
            SERVERS=("jvm")
            ;;
        "native")
            print_status $YELLOW "Starting Kotlin Native server..."
            docker compose -f docker-compose.stress.yml up -d app-native
            wait_for_service "$KOTLIN_NATIVE_BASE_URL/api/health" "Kotlin Native Server"
            SERVERS=("native")
            ;;
        "all")
            print_status $YELLOW "Starting all servers..."
            cd ../task_manager_dart && docker compose up -d && cd ../kmm
            docker compose -f docker-compose.stress.yml up -d app-jvm app-native app-dart
            
            wait_for_service "$DART_BASE_URL/health" "Dart Server"
            wait_for_service "$KOTLIN_JVM_BASE_URL/api/health" "Kotlin JVM Server"
            wait_for_service "$KOTLIN_NATIVE_BASE_URL/api/health" "Kotlin Native Server"
            SERVERS=("dart" "jvm" "native")
            ;;
        *)
            print_status $RED "❌ Unknown server option: $servers_to_start"
            exit 1
            ;;
    esac
    
    print_status $GREEN "✅ Servers started successfully"
    show_access_points
}

# Function to show access points
show_access_points() {
    print_status $CYAN "📊 Access Points:"
    echo "┌─────────────────────────────────────────────────────────────┐"
    echo "│                        SERVERS                              │"
    echo "├─────────────────────────────────────────────────────────────┤"
    echo "│ Dart Server:         http://localhost:8082                 │"
    echo "│ Kotlin JVM:          http://localhost:8081                 │"
    echo "│ Kotlin Native:       http://localhost:8085                 │"
    echo "├─────────────────────────────────────────────────────────────┤"
    echo "│                      MONITORING                             │"
    echo "├─────────────────────────────────────────────────────────────┤"
    echo "│ Grafana:             http://localhost:3000 (admin/admin)   │"
    echo "│ Prometheus:          http://localhost:9090                 │"
    echo "│ cAdvisor:            http://localhost:8084                 │"
    echo "│ Node Exporter:       http://localhost:9100                 │"
    echo "└─────────────────────────────────────────────────────────────┘"
}

# Function to run stress test on a single server
run_stress_test() {
    local server=$1
    local pattern=$2
    local base_url=""
    local server_name=""
    
    case $server in
        "dart")
            base_url=$DART_BASE_URL
            server_name="Dart"
            ;;
        "jvm")
            base_url=$KOTLIN_JVM_BASE_URL
            server_name="Kotlin JVM"
            ;;
        "native")
            base_url=$KOTLIN_NATIVE_BASE_URL
            server_name="Kotlin Native"
            ;;
        *)
            print_status $RED "❌ Unknown server: $server"
            return 1
            ;;
    esac
    
    local config=$(get_test_config "$pattern")
    if [ -z "$config" ]; then
        print_status $RED "❌ Unknown test pattern: $pattern"
        return 1
    fi
    
    read -r users duration ramp_up <<< "$config"
    
    print_status $BLUE "🔥 Running $pattern stress test on $server_name server"
    print_status $YELLOW "   Users: $users, Duration: ${duration}s, Ramp-up: ${ramp_up}s"
    
    local test_name="${server}_${pattern}_$(date +%H%M%S)"
    local results_file="$RESULTS_DIR/${test_name}.json"
    
    # Create test script
    cat > "$TEMP_DIR/test_${test_name}.sh" << EOF
#!/bin/bash
echo "Starting stress test: $test_name"
echo "Target: $base_url"
echo "Pattern: $pattern ($users users, ${duration}s duration, ${ramp_up}s ramp-up)"

# Simple load test using curl and parallel processing
start_time=\$(date +%s)
success_count=0
error_count=0
total_requests=0
response_times=()

# Function to make a request and measure time
make_request() {
    local start=\$(date +%s.%N)
    local response=\$(curl -s -w "%{http_code}" -o /dev/null "$base_url/health" 2>/dev/null)
    local end=\$(date +%s.%N)
    local duration=\$(echo "\$end - \$start" | bc -l)
    
    if [ "\$response" = "200" ]; then
        echo "SUCCESS \$duration"
    else
        echo "ERROR \$duration \$response"
    fi
}

export -f make_request
export base_url

# Calculate requests per second
requests_per_second=\$((users * 2))  # Approximate 2 requests per user per second

# Run the test
echo "Starting load generation..."
for ((i=0; i<duration; i++)); do
    echo "Second \$((i+1))/$duration"
    
    # Generate load for this second
    for ((j=0; j<requests_per_second; j++)); do
        make_request &
    done
    
    # Control the rate
    sleep 1
    
    # Wait for background jobs to complete every 10 seconds
    if [ \$((i % 10)) -eq 9 ]; then
        wait
    fi
done

# Wait for all remaining jobs
wait

echo "Test completed: $test_name"
EOF

    chmod +x "$TEMP_DIR/test_${test_name}.sh"
    
    # Run the test
    print_status $YELLOW "⚡ Executing stress test..."
    bash "$TEMP_DIR/test_${test_name}.sh" > "$TEMP_DIR/${test_name}_output.log" 2>&1
    
    # Analyze results
    local success_count=$(grep "SUCCESS" "$TEMP_DIR/${test_name}_output.log" | wc -l)
    local error_count=$(grep "ERROR" "$TEMP_DIR/${test_name}_output.log" | wc -l)
    local total_requests=$((success_count + error_count))
    
    if [ $total_requests -gt 0 ]; then
        local success_rate=$(echo "scale=2; $success_count * 100 / $total_requests" | bc -l)
        local avg_response_time=$(grep "SUCCESS" "$TEMP_DIR/${test_name}_output.log" | awk '{sum+=$2; count++} END {if(count>0) print sum/count; else print 0}')
        
        # Create results JSON
        cat > "$results_file" << EOF
{
    "server": "$server",
    "server_name": "$server_name",
    "pattern": "$pattern",
    "timestamp": "$(date -Iseconds)",
    "config": {
        "users": $users,
        "duration": $duration,
        "ramp_up": $ramp_up
    },
    "results": {
        "total_requests": $total_requests,
        "successful_requests": $success_count,
        "failed_requests": $error_count,
        "success_rate": $success_rate,
        "average_response_time": $avg_response_time,
        "requests_per_second": $(echo "scale=2; $total_requests / $duration" | bc -l)
    }
}
EOF
        
        print_status $GREEN "✅ Test completed: $server_name"
        print_status $CYAN "   📊 Results:"
        print_status $CYAN "      Total Requests: $total_requests"
        print_status $CYAN "      Success Rate: ${success_rate}%"
        print_status $CYAN "      Avg Response Time: ${avg_response_time}s"
        print_status $CYAN "      Requests/sec: $(echo "scale=2; $total_requests / $duration" | bc -l)"
        
    else
        print_status $RED "❌ No requests completed for $server_name"
        return 1
    fi
}

# Function to run comparative stress tests
run_comparative_test() {
    local pattern=${1:-"medium"}
    
    print_status $BLUE "🏁 Running comparative stress test: $pattern"
    
    # Ensure all servers are running
    if [ ${#SERVERS[@]} -eq 0 ]; then
        print_status $YELLOW "No servers specified, starting all servers..."
        start_servers "all"
    fi
    
    # Run tests on all servers
    for server in "${SERVERS[@]}"; do
        print_status $PURPLE "🎯 Testing $server server..."
        run_stress_test "$server" "$pattern"
        
        # Wait between tests to let servers recover
        print_status $YELLOW "⏸️ Cooling down for 30 seconds..."
        sleep 30
    done
    
    # Generate comparison report
    generate_comparison_report "$pattern"
}

# Function to generate comparison report
generate_comparison_report() {
    local pattern=$1
    local report_file="$RESULTS_DIR/comparison_${pattern}_$(date +%H%M%S).md"
    
    print_status $BLUE "📊 Generating comparison report..."
    
    cat > "$report_file" << EOF
# Stress Test Comparison Report

 **Test Pattern:** $pattern  
 **Timestamp:** $(date)  
 **Configuration:** $(get_test_config "$pattern")

## Results Summary

| Server | Total Requests | Success Rate | Avg Response Time | Requests/sec |
|--------|----------------|--------------|-------------------|--------------|
EOF

    # Add results for each server
    for server in "${SERVERS[@]}"; do
        local latest_result=$(ls -t "$RESULTS_DIR"/${server}_${pattern}_*.json 2>/dev/null | head -1)
        if [ -f "$latest_result" ]; then
            local total_requests=$(jq -r '.results.total_requests' "$latest_result")
            local success_rate=$(jq -r '.results.success_rate' "$latest_result")
            local avg_response_time=$(jq -r '.results.average_response_time' "$latest_result")
            local requests_per_second=$(jq -r '.results.requests_per_second' "$latest_result")
            local server_name=$(jq -r '.server_name' "$latest_result")
            
            echo "| $server_name | $total_requests | ${success_rate}% | ${avg_response_time}s | $requests_per_second |" >> "$report_file"
        fi
    done
    
    cat >> "$report_file" << EOF

## Detailed Analysis

### Performance Ranking (by Requests/sec)
EOF

    # Sort and rank by requests per second
    local temp_ranking="$TEMP_DIR/ranking.tmp"
    for server in "${SERVERS[@]}"; do
        local latest_result=$(ls -t "$RESULTS_DIR"/${server}_${pattern}_*.json 2>/dev/null | head -1)
        if [ -f "$latest_result" ]; then
            local requests_per_second=$(jq -r '.results.requests_per_second' "$latest_result")
            local server_name=$(jq -r '.server_name' "$latest_result")
            echo "$requests_per_second $server_name" >> "$temp_ranking"
        fi
    done
    
    sort -nr "$temp_ranking" | nl | while read rank rps server_name; do
        echo "$rank. **$server_name**: $rps requests/sec" >> "$report_file"
    done
    
    cat >> "$report_file" << EOF

### Memory Usage Analysis
*Check Grafana dashboards for detailed memory usage comparison*

### Startup Time Analysis
*Native images typically start faster than JVM applications*

### Resource Efficiency
*Native images typically use less memory but may have different CPU characteristics*

## Recommendations

Based on the test results:
- **Throughput Leader**: Check the ranking above
- **Memory Efficiency**: Native images typically use less memory
- **Startup Speed**: Native images start faster
- **Development Experience**: JVM offers better debugging and development tools

## Monitoring Links

- [Grafana Dashboard](http://localhost:3000)
- [Prometheus Metrics](http://localhost:9090)
- [Container Metrics](http://localhost:8084)

EOF

    print_status $GREEN "✅ Comparison report generated: $report_file"
    print_status $CYAN "📄 View the report: cat $report_file"
}

# Function to open monitoring dashboards
open_monitoring() {
    print_status $BLUE "🖥️ Opening monitoring dashboards..."
    
    if command -v open >/dev/null 2>&1; then
        # macOS
        open "http://localhost:3000"
        open "http://localhost:9090"
        open "http://localhost:8084"
    elif command -v xdg-open >/dev/null 2>&1; then
        # Linux
        xdg-open "http://localhost:3000"
        xdg-open "http://localhost:9090"
        xdg-open "http://localhost:8084"
    else
        print_status $YELLOW "Please open these URLs manually:"
        print_status $CYAN "  Grafana: http://localhost:3000"
        print_status $CYAN "  Prometheus: http://localhost:9090"
        print_status $CYAN "  cAdvisor: http://localhost:8084"
    fi
}

# Function to stop all services
stop_services() {
    print_status $BLUE "🛑 Stopping all services..."
    
    docker compose -f docker-compose.stress.yml down
    cd ../task_manager_dart && docker compose down && cd ../kmm
    
    print_status $GREEN "✅ All services stopped"
}

# Function to clean up
cleanup() {
    print_status $BLUE "🧹 Cleaning up..."
    
    stop_services
    
    # Remove temporary files
    rm -rf "$TEMP_DIR"
    
    # Optionally remove Docker volumes
    read -p "Remove Docker volumes? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker volume prune -f
        print_status $GREEN "✅ Docker volumes cleaned"
    fi
    
    print_status $GREEN "✅ Cleanup complete"
}

# Main script logic
case "${1:-help}" in
    "setup")
        setup_environment
        ;;
    "build")
        build_servers
        ;;
    "start")
        start_servers "${2:-all}"
        ;;
    "stop")
        stop_services
        ;;
    "test")
        pattern="${2:-medium}"
        servers="${3:-all}"
        if [ "$servers" = "all" ]; then
            start_servers "all"
            run_comparative_test "$pattern"
        else
            start_servers "$servers"
            run_stress_test "$servers" "$pattern"
        fi
        ;;
    "compare")
        pattern="${2:-medium}"
        start_servers "all"
        run_comparative_test "$pattern"
        ;;
    "monitor")
        open_monitoring
        ;;
    "clean")
        cleanup
        ;;
    "help"|*)
        show_usage
        ;;
esac 