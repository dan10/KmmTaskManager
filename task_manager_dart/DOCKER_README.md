# Task Manager Docker Setup (Dart)

This document describes the Docker setup for the Dart Task Manager application with monitoring for local development. **This setup uses different ports to avoid conflicts with the Ktor application.**

## Architecture Overview

The Docker setup includes the following services:

### Core Services
- **server**: Dart application server (main API with built-in Prometheus metrics)
- **db-dart**: PostgreSQL database for Dart application

### Monitoring Stack
- **prometheus-dart**: Metrics collection
- **grafana-dart**: Metrics visualization and dashboards
- **cadvisor-dart**: Container resource monitoring
- **node_exporter-dart**: Host system metrics

## Port Configuration (No Conflicts with Ktor)

| Service       | Dart Port | Ktor Port | Description           |
|---------------|-----------|-----------|-----------------------|
| Application   | 8082      | 8080      | Main API server       |
| Database      | 5433      | 5432      | PostgreSQL database   |
| Prometheus    | 9091      | 9090      | Metrics collection    |
| Grafana       | 3001      | 3000      | Metrics visualization |
| cAdvisor      | 8083      | 8082      | Container monitoring  |
| Node Exporter | 9101      | 9100      | Host metrics          |

## Quick Start

### Development
```bash
# Start core services only
docker-compose up server db-dart

# Or start everything
docker-compose up

# Run both Dart and Ktor applications simultaneously
# (they use different ports and networks)
```

### Production
```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f server

# Scale the application
docker-compose up -d --scale server=3
```

## Service Details

### Dart Server (Port 8082)
- **Container**: `dart-task-manager-server`
- **URL**: http://localhost:8082
- **Health Check**: http://localhost:8082/health
- **Metrics**: http://localhost:8082/metrics (built-in Prometheus metrics)
- **Environment Variables**:
  - `DATABASE_URL`: PostgreSQL connection string
  - `JWT_SECRET`: JWT signing secret (change in production!)
  - `LOG_LEVEL`: Logging level (DEBUG, INFO, WARN, ERROR)

### PostgreSQL Database (Port 5433)
- **Container**: `postgres-dart-task-manager`
- **Database**: `task_manager`
- **User**: `postgres`
- **Password**: `postgres` (change in production!)
- **Connection**: localhost:5433
- **Performance Tuning**: Optimized for small to medium workloads

### Monitoring Services

#### Prometheus (Port 9091)
- Metrics collection server
- **URL**: http://localhost:9091
- **Config**: `monitoring/prometheus.yml`
- **Retention**: 15 days
- **Targets**: Dart server metrics, cAdvisor, Node Exporter

#### Grafana (Port 3001)
- Metrics visualization
- **URL**: http://localhost:3001
- **Login**: admin/admin (change in production!)
- **Datasource**: Automatically configured Prometheus
- **Dashboards**: Dart-specific dashboard included

#### cAdvisor (Port 8083)
- Container resource monitoring
- **URL**: http://localhost:8083
- **Metrics**: CPU, Memory, Network, Disk usage per container

#### Node Exporter (Port 9101)
- Host system metrics
- **URL**: http://localhost:9101
- **Metrics**: Host CPU, Memory, Disk, Network

## Configuration Files

### Required Files
```
task_manager/
├── docker-compose.yml
├── server/
│   └── Dockerfile
└── monitoring/
    ├── prometheus.yml
    └── grafana/
        ├── provisioning/
        │   ├── datasources/
        │   │   └── prometheus.yml
        │   └── dashboards/
        │       └── dashboard.yml
        └── dashboards/
            ├── dart-dashboard.json (Dart-specific)
            ├── dashboard.json
            └── dashboard-v2.json
```

### Environment Configuration

Create a `.env` file for environment-specific configuration:

```env
# Database
POSTGRES_PASSWORD=your-secure-password
DATABASE_URL=postgres://postgres:your-secure-password@db-dart:5432/task_manager

# Application
JWT_SECRET=your-super-secure-jwt-secret-key-at-least-32-characters
LOG_LEVEL=INFO

# Monitoring
GRAFANA_ADMIN_PASSWORD=your-grafana-password
```

## Networking

All services run in the `dart-task-manager-network` bridge network:
- Internal communication uses service names (e.g., `server`, `db-dart`)
- External access via exposed ports
- **Isolated from Ktor application network**
- Direct access to application on port 8082

## Performance Comparison Setup

To compare performance between Dart and Ktor applications:

1. **Start Dart Application**:
   ```bash
   cd task_manager
   docker-compose up -d
   ```

2. **Start Ktor Application** (in separate terminal):
   ```bash
   cd .. # Go to Ktor directory
   docker-compose up -d
   ```

3. **Access Both Applications**:
    - Dart: http://localhost:8082
    - Ktor: http://localhost:8081

4. **Monitor Both**:
   - Dart Grafana: http://localhost:3001
   - Ktor Grafana: http://localhost:3000
   - Dart Prometheus: http://localhost:9091
   - Ktor Prometheus: http://localhost:9090

## Built-in Metrics

The Dart server includes comprehensive Prometheus metrics:

### HTTP Metrics
- `http_requests_total`: Total HTTP requests by method, route, and status code
- `http_request_duration_seconds`: Request duration histogram
- `http_active_connections`: Number of active connections

### Database Metrics
- `database_queries_total`: Total database queries by operation and table
- `database_connections_active`: Active database connections

### Authentication Metrics
- `auth_attempts_total`: Authentication attempts by method
- `auth_success_total`: Successful authentications
- `auth_failures_total`: Failed authentications with reasons

### Business Metrics
- `active_users`: Currently active users
- `projects_created_total`: Total projects created
- `tasks_created_total`: Total tasks created
- `total_projects`: Current total projects in system
- `total_tasks`: Current total tasks in system

### Runtime Metrics
- Dart VM runtime metrics (memory, garbage collection, etc.)
- Application info (version, name)

## Grafana Dashboards

### Dart-Specific Dashboard
The `dart-dashboard.json` includes panels for:
- HTTP Requests Rate
- Active HTTP Connections
- HTTP Request Duration (95th/50th percentiles)
- Authentication Metrics
- Business Metrics (Projects, Tasks, Users)
- Dart VM Memory Usage
- Database Query Rate

### Accessing Dashboards
1. Go to http://localhost:3001
2. Login with admin/admin
3. Navigate to "Dart Task Manager Dashboard"

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure Ktor application is using different ports
   ```bash
   netstat -tlnp | grep :8082
   ```

2. **Permission issues**: Ensure Docker has proper permissions
   ```bash
   sudo chown -R $USER:$USER task_manager/
   ```

3. **Memory issues**: Increase Docker Desktop memory allocation

4. **Database connection**: Check database logs
   ```bash
   docker-compose logs db-dart
   ```

5. **Metrics not showing**: Check metrics endpoint
   ```bash
   curl http://localhost:8082/metrics
   ```

### Useful Commands

```bash
# View all container status
docker-compose ps

# Follow logs for specific service
docker-compose logs -f server

# Execute command in container
docker-compose exec server dart --version

# Clean up everything
docker-compose down -v --remove-orphans

# Rebuild specific service
docker-compose build server
docker-compose up -d server

# Test metrics endpoint
curl http://localhost:8081/metrics

# Test health endpoint
curl http://localhost:8081/health

# Compare with Ktor
curl http://localhost:8080/health  # Ktor
curl http://localhost:8081/health  # Dart
```

## Scaling

### Horizontal Scaling
```bash
# Scale application servers
docker-compose up -d --scale server=3
```

### Vertical Scaling
Adjust resource limits in `docker-compose.yml`:
```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'
      memory: 4G
```

## Backup and Recovery

### Database Backup
```bash
# Create backup
docker-compose exec db-dart pg_dump -U postgres task_manager > dart_backup.sql

# Restore backup
docker-compose exec -T db-dart psql -U postgres task_manager < dart_backup.sql
```

### Volume Backup
```bash
# Backup all volumes
docker run --rm -v task_manager_postgres_dart_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_dart_backup.tar.gz -C /data .
```

## Performance Tuning

### Database Optimization
- Adjust PostgreSQL settings in docker-compose.yml
- Monitor slow queries via metrics
- Configure connection pooling

### Application Optimization
- Monitor metrics via `/metrics` endpoint
- Configure proper logging levels
- Monitor memory usage and garbage collection via runtime metrics

### Monitoring Optimization
- Use Prometheus for alerting on key metrics
- Set up Grafana dashboards for business metrics
- Monitor authentication patterns for security insights
- Compare performance metrics with Ktor application 