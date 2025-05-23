#!/bin/bash

# Performance Comparison Script for Dart vs Ktor Task Manager
# This script helps start both applications with their monitoring stacks

set -e

echo "🚀 Task Manager Performance Comparison Setup"
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if port is in use
check_port() {
    local port=$1
    local service=$2
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${RED}❌ Port $port is already in use (needed for $service)${NC}"
        echo "   Please stop the service using this port or change the configuration"
        return 1
    else
        echo -e "${GREEN}✅ Port $port is available for $service${NC}"
        return 0
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local url=$1
    local service=$2
    local max_attempts=30
    local attempt=1
    
    echo -e "${YELLOW}⏳ Waiting for $service to be ready...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ $service is ready!${NC}"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}❌ $service failed to start within expected time${NC}"
    return 1
}

# Check required ports
echo -e "${BLUE}🔍 Checking port availability...${NC}"
ports_ok=true

# Dart application ports
check_port 8081 "Dart Application" || ports_ok=false
check_port 5433 "Dart PostgreSQL" || ports_ok=false
check_port 9091 "Dart Prometheus" || ports_ok=false
check_port 3001 "Dart Grafana" || ports_ok=false
check_port 8082 "Dart cAdvisor" || ports_ok=false
check_port 9101 "Dart Node Exporter" || ports_ok=false

# Ktor application ports (assuming they exist)
check_port 8080 "Ktor Application" || echo -e "${YELLOW}⚠️  Ktor application not running (this is OK)${NC}"
check_port 5432 "Ktor PostgreSQL" || echo -e "${YELLOW}⚠️  Ktor PostgreSQL not running (this is OK)${NC}"
check_port 9090 "Ktor Prometheus" || echo -e "${YELLOW}⚠️  Ktor Prometheus not running (this is OK)${NC}"
check_port 3000 "Ktor Grafana" || echo -e "${YELLOW}⚠️  Ktor Grafana not running (this is OK)${NC}"

if [ "$ports_ok" = false ]; then
    echo -e "${RED}❌ Some required ports are not available. Please resolve conflicts before continuing.${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}🐳 Starting Dart Task Manager with monitoring...${NC}"

# Start Dart application
cd "$(dirname "$0")"
docker-compose up -d

echo ""
echo -e "${BLUE}⏳ Waiting for services to be ready...${NC}"

# Wait for core services
wait_for_service "http://localhost:8081/health" "Dart Application"
wait_for_service "http://localhost:9091" "Dart Prometheus"
wait_for_service "http://localhost:3001" "Dart Grafana"

echo ""
echo -e "${GREEN}🎉 Dart Task Manager is ready!${NC}"
echo ""
echo -e "${BLUE}📊 Access Points:${NC}"
echo "┌─────────────────────────────────────────────────────────────┐"
echo "│                    DART APPLICATION                         │"
echo "├─────────────────────────────────────────────────────────────┤"
echo "│ Application:     http://localhost:8081                     │"
echo "│ Health Check:    http://localhost:8081/health              │"
echo "│ Metrics:         http://localhost:8081/metrics             │"
echo "│ Database:        localhost:5433 (postgres/postgres)        │"
echo "├─────────────────────────────────────────────────────────────┤"
echo "│                     MONITORING                              │"
echo "├─────────────────────────────────────────────────────────────┤"
echo "│ Grafana:         http://localhost:3001 (admin/admin)       │"
echo "│ Prometheus:      http://localhost:9091                     │"
echo "│ cAdvisor:        http://localhost:8082                     │"
echo "│ Node Exporter:   http://localhost:9101                     │"
echo "└─────────────────────────────────────────────────────────────┘"

echo ""
echo -e "${YELLOW}💡 Performance Comparison Tips:${NC}"
echo "1. Start Ktor application in another terminal:"
echo "   cd ../ktor-directory && docker-compose up -d"
echo ""
echo "2. Compare metrics in Grafana dashboards:"
echo "   - Dart:  http://localhost:3001"
echo "   - Ktor:  http://localhost:3000"
echo ""
echo "3. Run load tests against both applications:"
echo "   - Dart:  curl http://localhost:8081/api/..."
echo "   - Ktor:  curl http://localhost:8080/api/..."
echo ""
echo "4. Monitor resource usage:"
echo "   docker stats"
echo ""
echo -e "${BLUE}🛑 To stop all services:${NC}"
echo "   docker-compose down"
echo ""
echo -e "${BLUE}📋 To view logs:${NC}"
echo "   docker-compose logs -f [service-name]"
echo ""
echo -e "${GREEN}✨ Happy performance testing!${NC}" 