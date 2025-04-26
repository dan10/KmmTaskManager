#!/bin/bash

# Script to set up Grafana dashboard for Task Manager application

echo "Setting up Grafana dashboard for Task Manager application..."

# Create necessary directories
mkdir -p grafana/dashboards
mkdir -p grafana/provisioning/datasources
mkdir -p grafana/provisioning/dashboards

# Check if files already exist
if [ -f grafana/dashboards/dashboard.json ]; then
  echo "Dashboard file already exists. Skipping..."
else
  echo "Creating dashboard.json..."
  cat > grafana/dashboards/dashboard.json << 'EOF'
{
  "annotations": {
    "list": [
      {
        "builtIn": 1,
        "datasource": {
          "type": "grafana",
          "uid": "-- Grafana --"
        },
        "enable": true,
        "hide": true,
        "iconColor": "rgba(0, 211, 255, 1)",
        "name": "Annotations & Alerts",
        "type": "dashboard"
      }
    ]
  },
  "editable": true,
  "fiscalYearStartMonth": 0,
  "graphTooltip": 0,
  "id": null,
  "links": [],
  "liveNow": false,
  "panels": [
    {
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisCenteredZero": false,
            "axisColorMode": "text",
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 10,
            "gradientMode": "none",
            "hideFrom": {
              "legend": false,
              "tooltip": false,
              "viz": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "never",
            "spanNulls": false,
            "stacking": {
              "group": "A",
              "mode": "none"
            },
            "thresholdsStyle": {
              "mode": "off"
            }
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          },
          "unit": "reqps"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 0,
        "y": 0
      },
      "id": 1,
      "options": {
        "legend": {
          "calcs": ["mean", "max", "min"],
          "displayMode": "table",
          "placement": "bottom",
          "showLegend": true
        },
        "tooltip": {
          "mode": "single",
          "sort": "none"
        }
      },
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "expr": "sum(rate(http_server_requests_seconds_count{status=~\"2..\"}[1m]))",
          "legendFormat": "Success Responses",
          "range": true,
          "refId": "A"
        }
      ],
      "title": "Success Responses (2xx)",
      "type": "timeseries"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisCenteredZero": false,
            "axisColorMode": "text",
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 10,
            "gradientMode": "none",
            "hideFrom": {
              "legend": false,
              "tooltip": false,
              "viz": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "never",
            "spanNulls": false,
            "stacking": {
              "group": "A",
              "mode": "none"
            },
            "thresholdsStyle": {
              "mode": "off"
            }
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "red",
                "value": null
              }
            ]
          },
          "unit": "reqps"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 12,
        "y": 0
      },
      "id": 2,
      "options": {
        "legend": {
          "calcs": ["mean", "max", "min"],
          "displayMode": "table",
          "placement": "bottom",
          "showLegend": true
        },
        "tooltip": {
          "mode": "single",
          "sort": "none"
        }
      },
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "expr": "sum(rate(http_server_requests_seconds_count{status=~\"4..\"}[1m]))",
          "legendFormat": "Client Errors (4xx)",
          "range": true,
          "refId": "A"
        },
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "expr": "sum(rate(http_server_requests_seconds_count{status=~\"5..\"}[1m]))",
          "hide": false,
          "legendFormat": "Server Errors (5xx)",
          "range": true,
          "refId": "B"
        }
      ],
      "title": "Error Responses (4xx/5xx)",
      "type": "timeseries"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisCenteredZero": false,
            "axisColorMode": "text",
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 10,
            "gradientMode": "none",
            "hideFrom": {
              "legend": false,
              "tooltip": false,
              "viz": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "never",
            "spanNulls": false,
            "stacking": {
              "group": "A",
              "mode": "none"
            },
            "thresholdsStyle": {
              "mode": "off"
            }
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "percent"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 0,
        "y": 8
      },
      "id": 3,
      "options": {
        "legend": {
          "calcs": ["mean", "max", "min"],
          "displayMode": "table",
          "placement": "bottom",
          "showLegend": true
        },
        "tooltip": {
          "mode": "single",
          "sort": "none"
        }
      },
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "expr": "100 - (avg by (instance) (rate(node_cpu_seconds_total{mode=\"idle\"}[1m])) * 100)",
          "legendFormat": "CPU Usage",
          "range": true,
          "refId": "A"
        }
      ],
      "title": "CPU Usage",
      "type": "timeseries"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisCenteredZero": false,
            "axisColorMode": "text",
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 10,
            "gradientMode": "none",
            "hideFrom": {
              "legend": false,
              "tooltip": false,
              "viz": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "never",
            "spanNulls": false,
            "stacking": {
              "group": "A",
              "mode": "none"
            },
            "thresholdsStyle": {
              "mode": "off"
            }
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "bytes"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 12,
        "y": 8
      },
      "id": 4,
      "options": {
        "legend": {
          "calcs": ["mean", "max", "min"],
          "displayMode": "table",
          "placement": "bottom",
          "showLegend": true
        },
        "tooltip": {
          "mode": "single",
          "sort": "none"
        }
      },
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "expr": "jvm_memory_used_bytes{area=\"heap\"}",
          "legendFormat": "Heap Memory Used",
          "range": true,
          "refId": "A"
        },
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "expr": "jvm_memory_used_bytes{area=\"nonheap\"}",
          "hide": false,
          "legendFormat": "Non-Heap Memory Used",
          "range": true,
          "refId": "B"
        }
      ],
      "title": "Memory Usage",
      "type": "timeseries"
    }
  ],
  "refresh": "5s",
  "schemaVersion": 38,
  "style": "dark",
  "tags": ["ktor", "kotlin", "monitoring"],
  "templating": {
    "list": []
  },
  "time": {
    "from": "now-1h",
    "to": "now"
  },
  "timepicker": {
    "refresh_intervals": [
      "5s",
      "10s",
      "30s",
      "1m",
      "5m",
      "15m",
      "30m",
      "1h",
      "2h",
      "1d"
    ]
  },
  "timezone": "",
  "title": "Task Manager Application Dashboard",
  "uid": "task-manager-dashboard",
  "version": 1,
  "weekStart": ""
}
EOF
fi

if [ -f grafana/provisioning/datasources/datasource.yml ]; then
  echo "Datasource configuration already exists. Skipping..."
else
  echo "Creating datasource.yml..."
  cat > grafana/provisioning/datasources/datasource.yml << 'EOF'
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    uid: prometheus
    url: http://prometheus:9090
    isDefault: true
    editable: false
    version: 1
EOF
fi

if [ -f grafana/provisioning/dashboards/dashboard.yml ]; then
  echo "Dashboard provisioning configuration already exists. Skipping..."
else
  echo "Creating dashboard.yml..."
  cat > grafana/provisioning/dashboards/dashboard.yml << 'EOF'
apiVersion: 1

providers:
  - name: 'Task Manager Dashboards'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    editable: true
    options:
      path: /etc/grafana/dashboards
EOF
fi

# Create README if it doesn't exist
if [ -f grafana/README.md ]; then
  echo "README already exists. Skipping..."
else
  echo "Creating README.md..."
  cat > grafana/README.md << 'EOF'
# Grafana Monitoring Dashboard

This directory contains the configuration for a Grafana dashboard that monitors the Task Manager application. The dashboard provides real-time metrics for:

1. **Success Responses (2xx)** - Rate of successful HTTP responses
2. **Error Responses (4xx/5xx)** - Rate of client and server error responses
3. **CPU Usage** - Percentage of CPU being used by the application
4. **Memory Usage** - Heap and non-heap memory consumption

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

The dashboard is divided into four panels:

1. **Success Responses (2xx)**
   - Shows the rate of successful HTTP responses per second
   - Helps monitor the application's normal operation

2. **Error Responses (4xx/5xx)**
   - Shows the rate of client errors (4xx) and server errors (5xx)
   - Helps identify issues with the application

3. **CPU Usage**
   - Shows the percentage of CPU being used
   - Helps monitor resource utilization and identify performance bottlenecks

4. **Memory Usage**
   - Shows heap and non-heap memory usage
   - Helps identify memory leaks and optimize memory allocation

## Configuration Files

- `dashboards/dashboard.json` - The Grafana dashboard configuration
- `provisioning/datasources/datasource.yml` - Configures Prometheus as a data source
- `provisioning/dashboards/dashboard.yml` - Configures Grafana to load the dashboard

## Customizing the Dashboard

You can customize the dashboard by:

1. Accessing it in Grafana UI
2. Making changes through the Grafana interface
3. Saving the changes

Alternatively, you can modify the `dashboard.json` file directly, but this requires knowledge of the Grafana dashboard JSON structure.

## Metrics Collection

The metrics are collected by Prometheus from the application's `/metrics` endpoint, which is exposed by the Micrometer and Prometheus libraries integrated into the Ktor application.
EOF
fi

# Check if docker-compose.yml needs to be updated
if grep -q "./grafana/dashboards:/etc/grafana/dashboards" docker-compose.yml; then
  echo "Docker Compose file already configured for Grafana dashboards. Skipping..."
else
  echo "Updating Docker Compose file..."
  # This is a simple sed command to add the volume mounts
  # In a real scenario, you might want to use a more robust approach
  sed -i.bak '/grafana-data:\/var\/lib\/grafana/a\      - ./grafana/dashboards:/etc/grafana/dashboards\n      - ./grafana/provisioning/datasources:/etc/grafana/provisioning/datasources\n      - ./grafana/provisioning/dashboards:/etc/grafana/provisioning/dashboards' docker-compose.yml
  if [ $? -eq 0 ]; then
    echo "Docker Compose file updated successfully."
    rm docker-compose.yml.bak
  else
    echo "Failed to update Docker Compose file. Please update it manually."
    echo "Add the following volume mounts to the grafana service:"
    echo "  - ./grafana/dashboards:/etc/grafana/dashboards"
    echo "  - ./grafana/provisioning/datasources:/etc/grafana/provisioning/datasources"
    echo "  - ./grafana/provisioning/dashboards:/etc/grafana/provisioning/dashboards"
  fi
fi

echo "Setup complete! You can now run 'docker-compose up -d' to start the application with Grafana monitoring."
echo "Access the dashboard at http://localhost:3000 (username: admin, password: admin)"