import 'package:prometheus_client/prometheus_client.dart';
import 'package:prometheus_client/runtime_metrics.dart' as runtime_metrics;

class MetricsService {
  static final MetricsService _instance = MetricsService._internal();
  factory MetricsService() => _instance;
  MetricsService._internal();

  late final CollectorRegistry _registry;
  bool _initialized = false;

  // HTTP metrics
  late final Counter _httpRequestsTotal;
  late final Histogram _httpRequestDuration;
  late final Gauge _httpActiveConnections;

  // Database metrics
  late final Counter _databaseQueriesTotal;
  late final Gauge _databaseConnections;

  // Authentication metrics
  late final Counter _authAttemptsTotal;
  late final Counter _authSuccessTotal;
  late final Counter _authFailuresTotal;

  // Business metrics
  late final Gauge _activeUsers;
  late final Counter _projectsCreated;
  late final Counter _tasksCreated;
  late final Gauge _totalProjects;
  late final Gauge _totalTasks;

  // Application metrics
  late final Gauge _applicationInfo;

  void initialize() {
    if (_initialized) return;

    _registry = CollectorRegistry();

    // Register runtime metrics (memory, GC, etc.)
    runtime_metrics.register(_registry);

    // HTTP Metrics
    _httpRequestsTotal = Counter(
      name: 'http_requests_total',
      help: 'Total number of HTTP requests',
      labelNames: ['method', 'route', 'status_code'],
    )..register(_registry);

    _httpRequestDuration = Histogram(
      name: 'http_request_duration_seconds',
      help: 'Duration of HTTP requests in seconds',
      labelNames: ['method', 'route'],
      buckets: [0.001, 0.01, 0.1, 0.5, 1.0, 2.5, 5.0, 10.0],
    )..register(_registry);

    _httpActiveConnections = Gauge(
      name: 'http_active_connections',
      help: 'Number of active HTTP connections',
    )..register(_registry);

    // Database Metrics
    _databaseQueriesTotal = Counter(
      name: 'database_queries_total',
      help: 'Total number of database queries',
      labelNames: ['operation', 'table'],
    )..register(_registry);

    _databaseConnections = Gauge(
      name: 'database_connections_active',
      help: 'Number of active database connections',
    )..register(_registry);

    // Authentication Metrics
    _authAttemptsTotal = Counter(
      name: 'auth_attempts_total',
      help: 'Total number of authentication attempts',
      labelNames: ['method'], // 'login', 'register'
    )..register(_registry);

    _authSuccessTotal = Counter(
      name: 'auth_success_total',
      help: 'Total number of successful authentications',
      labelNames: ['method'],
    )..register(_registry);

    _authFailuresTotal = Counter(
      name: 'auth_failures_total',
      help: 'Total number of failed authentications',
      labelNames: ['method', 'reason'],
    )..register(_registry);

    // Business Metrics
    _activeUsers = Gauge(
      name: 'active_users',
      help: 'Number of currently active users',
    )..register(_registry);

    _projectsCreated = Counter(
      name: 'projects_created_total',
      help: 'Total number of projects created',
    )..register(_registry);

    _tasksCreated = Counter(
      name: 'tasks_created_total',
      help: 'Total number of tasks created',
    )..register(_registry);

    _totalProjects = Gauge(
      name: 'total_projects',
      help: 'Total number of projects in the system',
    )..register(_registry);

    _totalTasks = Gauge(
      name: 'total_tasks',
      help: 'Total number of tasks in the system',
    )..register(_registry);

    // Application Info
    _applicationInfo = Gauge(
      name: 'application_info',
      help: 'Application information',
      labelNames: ['version', 'name'],
    )..register(_registry);

    // Set application info
    _applicationInfo.labels(['1.0.0', 'task-manager']).value = 1;

    _initialized = true;
  }

  CollectorRegistry get registry => _registry;

  // HTTP Metrics Methods
  void recordHttpRequest(String method, String route, int statusCode) {
    _httpRequestsTotal.labels([method, route, statusCode.toString()]).inc();
  }

  void recordHttpRequestDuration(String method, String route, double durationSeconds) {
    _httpRequestDuration.labels([method, route]).observe(durationSeconds);
  }

  void incrementActiveConnections() {
    _httpActiveConnections.inc();
  }

  void decrementActiveConnections() {
    _httpActiveConnections.dec();
  }

  // Database Metrics Methods
  void recordDatabaseQuery(String operation, String table) {
    _databaseQueriesTotal.labels([operation, table]).inc();
  }

  void setDatabaseConnections(double count) {
    _databaseConnections.value = count;
  }

  // Authentication Metrics Methods
  void recordAuthAttempt(String method) {
    _authAttemptsTotal.labels([method]).inc();
  }

  void recordAuthSuccess(String method) {
    _authSuccessTotal.labels([method]).inc();
  }

  void recordAuthFailure(String method, String reason) {
    _authFailuresTotal.labels([method, reason]).inc();
  }

  // Business Metrics Methods
  void setActiveUsers(double count) {
    _activeUsers.value = count;
  }

  void recordProjectCreated() {
    _projectsCreated.inc();
  }

  void recordTaskCreated() {
    _tasksCreated.inc();
  }

  void setTotalProjects(double count) {
    _totalProjects.value = count;
  }

  void setTotalTasks(double count) {
    _totalTasks.value = count;
  }
} 