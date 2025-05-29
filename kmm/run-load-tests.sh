#!/bin/bash

# Enhanced Gatling Load Testing Script for KMM Task Manager
# Supports both Kotlin and Dart servers with data cleanup options

set -e

echo "🚀 KMM Task Manager - Enhanced Gatling Load Testing"
echo "=================================================="

# Configuration
KOTLIN_BASE_URL="http://localhost:8081"
DART_BASE_URL="http://localhost:8082"  # Dart server runs on 8082 to avoid conflicts
KOTLIN_COMPOSE_DIR="kmm"
DART_COMPOSE_DIR="task_manager_dart"

# Function to show help
show_help() {
    echo ""
    echo "Enhanced Load Testing Script - Available commands:"
    echo ""
    echo "🧪 Test Execution:"
    echo "  ./run-load-tests.sh quick [kotlin|dart]      - Quick 2-minute test"
    echo "  ./run-load-tests.sh long [kotlin|dart]       - 30-minute load test"
    echo "  ./run-load-tests.sh stress [kotlin|dart]     - High-load stress test"
    echo "  ./run-load-tests.sh compare                  - Run tests on both servers"
    echo ""
    echo "🧹 Data Management:"
    echo "  ./run-load-tests.sh cleanup [kotlin|dart]    - Clean database data"
    echo "  ./run-load-tests.sh reset [kotlin|dart]      - Full reset (cleanup + restart)"
    echo ""
    echo "🐳 Service Management:"
    echo "  ./run-load-tests.sh up [kotlin|dart]         - Start specific server"
    echo "  ./run-load-tests.sh down [kotlin|dart]       - Stop specific server"
    echo "  ./run-load-tests.sh status [kotlin|dart]     - Show service status"
    echo "  ./run-load-tests.sh logs [kotlin|dart]       - Show server logs"
    echo ""
    echo "📊 Test Modes:"
    echo "  • Quick:  Low load validation (1-10 users/sec, 2 min)"
    echo "  • Long:   Sustained load test (10-100 & 20-200 users/sec, 30 min)"
    echo "  • Stress: Performance limits (50-500 & 100-1000 users/sec, 10 min)"
    echo ""
    echo "🎯 Server Targets:"
    echo "  • Kotlin: Ktor-based server (default)"
    echo "  • Dart:   Shelf-based server"
    echo "  • If no server specified, defaults to Kotlin"
    echo ""
    echo "📈 Monitoring URLs:"
    echo "  Kotlin Server:"
    echo "    • Grafana: http://localhost:3000"
    echo "    • Prometheus: http://localhost:9090"
    echo "    • App metrics: http://localhost:8081/metrics"
    echo ""
    echo "  Dart Server:"
    echo "    • Grafana: http://localhost:3001"
    echo "    • Prometheus: http://localhost:9091"
    echo "    • App metrics: http://localhost:8082/metrics"
    echo ""
    echo "💡 Examples:"
    echo "  ./run-load-tests.sh quick kotlin            # Quick test on Kotlin server"
    echo "  ./run-load-tests.sh cleanup dart            # Clean Dart server data"
    echo "  ./run-load-tests.sh compare                 # Test both servers"
    echo "  ./run-load-tests.sh reset kotlin            # Full reset Kotlin server"
    echo ""
}

# Function to detect server type and set configuration
setup_server_config() {
    local server_type=${1:-kotlin}
    
    case $server_type in
        "dart")
            export COMPOSE_DIR=$DART_COMPOSE_DIR
            export BASE_URL=$DART_BASE_URL
            export SERVER_NAME="Dart (Shelf)"
            export GRAFANA_PORT="3001"
            export PROMETHEUS_PORT="9091"
            ;;
        "kotlin"|*)
            export COMPOSE_DIR=$KOTLIN_COMPOSE_DIR
            export BASE_URL=$KOTLIN_BASE_URL
            export SERVER_NAME="Kotlin (Ktor)"
            export GRAFANA_PORT="3000"
            export PROMETHEUS_PORT="9090"
            ;;
    esac
    
    echo "🎯 Target: $SERVER_NAME server"
    echo "📍 URL: $BASE_URL"
    echo "📂 Directory: $COMPOSE_DIR"
}

# Function to check if services are running
check_services() {
    local server_type=${1:-kotlin}
    setup_server_config $server_type
    
    echo "📊 Checking if $SERVER_NAME services are running..."
    
    if [ "$server_type" = "dart" ]; then
        pushd "../$DART_COMPOSE_DIR" > /dev/null
    fi
    
    if ! docker compose ps | grep -q "Up"; then
        echo "⚠️  $SERVER_NAME services are not running. Starting them..."
        docker compose up -d
        echo "⏳ Waiting for services to be ready..."
        sleep 15
        
        # Wait for health check
        echo "🔍 Checking server health..."
        local retries=0
        while [ $retries -lt 30 ]; do
            if curl -s $BASE_URL/health > /dev/null 2>&1 || curl -s $BASE_URL/metrics > /dev/null 2>&1; then
                echo "✅ $SERVER_NAME server is healthy"
                break
            fi
            retries=$((retries + 1))
            echo "⏳ Waiting for server... ($retries/30)"
            sleep 2
        done
        
        if [ $retries -eq 30 ]; then
            echo "❌ Server health check failed after 60 seconds"
            if [ "$server_type" = "dart" ]; then
                popd > /dev/null
            fi
            return 1
        fi
    else
        echo "✅ $SERVER_NAME services are running"
    fi
    
    if [ "$server_type" = "dart" ]; then
        popd > /dev/null
    fi
}

# Function to show service status
show_status() {
    local server_type=${1:-kotlin}
    setup_server_config $server_type
    
    echo ""
    echo "📋 $SERVER_NAME Service Status:"
    
    if [ "$server_type" = "dart" ]; then
        pushd "../$DART_COMPOSE_DIR" > /dev/null
    fi
    
    docker compose ps
    
    if [ "$server_type" = "dart" ]; then
        popd > /dev/null
    fi
    
    echo ""
}

# Function to show logs
show_logs() {
    local server_type=${1:-kotlin}
    setup_server_config $server_type
    
    echo "📜 $SERVER_NAME Logs (last 50 lines):"
    
    if [ "$server_type" = "dart" ]; then
        pushd "../$DART_COMPOSE_DIR" > /dev/null
        docker compose logs --tail=50 server
        popd > /dev/null
    else
        docker compose logs --tail=50 ktor-app
    fi
}

# Function to cleanup database
cleanup_database() {
    local server_type=${1:-kotlin}
    setup_server_config $server_type
    
    echo "🧹 Cleaning $SERVER_NAME database..."
    
    case $server_type in
        "dart")
            echo "🗑️  Cleaning Dart server database..."
            pushd "../$DART_COMPOSE_DIR" > /dev/null
            # Connect to Dart PostgreSQL and clean tables
            docker compose exec db-dart psql -U postgres -d task_manager -c "
                DELETE FROM project_members;
                DELETE FROM tasks;
                DELETE FROM projects;
                DELETE FROM users;
                VACUUM ANALYZE;
            " 2>/dev/null || echo "⚠️  Database cleanup may have failed (tables might not exist yet)"
            popd > /dev/null
            ;;
        "kotlin"|*)
            echo "🗑️  Cleaning Kotlin server database..."
            # Try admin endpoint first, fallback to direct DB access
            if curl -s -X DELETE $BASE_URL/api/admin/cleanup > /dev/null 2>&1; then
                echo "✅ Used admin endpoint for cleanup"
            else
                echo "⚠️  Admin endpoint not available, using direct database access..."
                # Connect to Kotlin PostgreSQL and clean tables
                docker compose exec postgres-db psql -U postgres -d task_manager -c "
                    DELETE FROM project_assignments;
                    DELETE FROM project_invitations;
                    DELETE FROM file_uploads;
                    DELETE FROM tasks;
                    DELETE FROM projects;
                    DELETE FROM users;
                    VACUUM ANALYZE;
                " 2>/dev/null || echo "⚠️  Database cleanup may have failed (tables might not exist yet)"
            fi
            ;;
    esac
    
    echo "✅ Database cleanup completed for $SERVER_NAME"
}

# Function to start services
start_services() {
    local server_type=${1:-kotlin}
    setup_server_config $server_type
    
    echo "🐳 Starting $SERVER_NAME services..."
    
    if [ "$server_type" = "dart" ]; then
        pushd "../$DART_COMPOSE_DIR" > /dev/null
    fi
    
    docker compose up -d
    
    if [ "$server_type" = "dart" ]; then
        popd > /dev/null
    fi
    
    show_status $server_type
}

# Function to stop services
stop_services() {
    local server_type=${1:-kotlin}
    setup_server_config $server_type
    
    echo "🛑 Stopping $SERVER_NAME services..."
    
    if [ "$server_type" = "dart" ]; then
        pushd "../$DART_COMPOSE_DIR" > /dev/null
    fi
    
    docker compose down
    
    if [ "$server_type" = "dart" ]; then
        popd > /dev/null
    fi
}

# Function to reset services (cleanup + restart)
reset_services() {
    local server_type=${1:-kotlin}
    setup_server_config $server_type
    
    echo "🔄 Resetting $SERVER_NAME services..."
    stop_services $server_type
    start_services $server_type
    sleep 5
    cleanup_database $server_type
    echo "✅ Reset completed for $SERVER_NAME"
}

# Function to run load test
run_test() {
    local mode=$1
    local server_type=${2:-kotlin}
    local description=$3
    local cleanup_before=${4:-true}
    local cleanup_after=${5:-false}
    
    setup_server_config $server_type
    
    echo ""
    echo "🧪 Running $description on $SERVER_NAME..."
    echo "Mode: $mode"
    echo "Target: $BASE_URL"
    
    case $mode in
        "quick")
            echo "⚡ Quick test: 1-10 users/sec for 2 minutes"
            ;;
        "long")
            echo "⏰ Long test: 10-100 & 20-200 users/sec for 30 minutes"
            ;;
        "stress")
            echo "💥 Stress test: 50-500 & 100-1000 users/sec for 10 minutes"
            ;;
    esac
    
    # Clean before test if requested
    if [ "$cleanup_before" = "true" ]; then
        echo ""
        echo "🧹 Cleaning database before test..."
        cleanup_database $server_type
    fi
    
    # Set the base URL for Gatling
    export GATLING_BASE_URL=$BASE_URL
    
    echo ""
    echo "🚀 Starting load test..."
    # Gatling tests are always run from the kmm directory
    # Capitalize first letter for Gradle task name
    case $mode in
        "quick") gradle_task="gatlingRunQuick" ;;
        "long") gradle_task="gatlingRunLong" ;;
        "stress") gradle_task="gatlingRunStress" ;;
    esac
    ./gradlew :server:$gradle_task --no-configuration-cache -Dgatling.baseUrl=$BASE_URL
    
    # Clean after test if requested
    if [ "$cleanup_after" = "true" ]; then
        echo ""
        echo "🧹 Cleaning database after test..."
        cleanup_database $server_type
    fi
    
    echo ""
    echo "✅ Test completed! Check the HTML report above for detailed results."
    echo "📊 Monitoring: Grafana: http://localhost:$GRAFANA_PORT | Prometheus: http://localhost:$PROMETHEUS_PORT"
    echo ""
}

# Function to run comparison tests
run_comparison() {
    echo "🔄 Running Comparison Tests on Both Servers"
    echo "==========================================="
    
    # Check both servers are running
    echo "🔍 Checking Kotlin server..."
    check_services kotlin
    
    echo "🔍 Checking Dart server..."
    check_services dart
    
    # Run quick tests on both
    echo ""
    echo "📊 Running comparison tests (quick mode)..."
    
    echo ""
    echo "1️⃣  Testing Kotlin server..."
    run_test quick kotlin "Kotlin Comparison Test"
    
    sleep 5
    
    echo ""
    echo "2️⃣  Testing Dart server..."
    run_test quick dart "Dart Comparison Test"
    
    echo ""
    echo "🏁 Comparison Tests Completed!"
    echo "📈 Review both Grafana dashboards:"
    echo "   • Kotlin: http://localhost:3000"
    echo "   • Dart:   http://localhost:3001"
}

# Main command processing
case ${1:-help} in
    "quick"|"long"|"stress")
        mode=$1
        server_type=${2:-kotlin}
        check_services $server_type
        # Capitalize first letter of mode for description
        case $mode in
            "quick") description="Quick Load Test" ;;
            "long") description="Long Load Test" ;;
            "stress") description="Stress Load Test" ;;
        esac
        run_test $mode $server_type "$description"
        ;;
    "compare")
        run_comparison
        ;;
    "cleanup")
        server_type=${2:-kotlin}
        cleanup_database $server_type
        ;;
    "reset")
        server_type=${2:-kotlin}
        reset_services $server_type
        ;;
    "up")
        server_type=${2:-kotlin}
        start_services $server_type
        ;;
    "down")
        server_type=${2:-kotlin}
        stop_services $server_type
        ;;
    "status")
        server_type=${2:-kotlin}
        show_status $server_type
        ;;
    "logs")
        server_type=${2:-kotlin}
        show_logs $server_type
        ;;
    "help"|*)
        show_help
        ;;
esac 