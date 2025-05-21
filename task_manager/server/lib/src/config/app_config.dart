import 'package:dotenv/dotenv.dart' show load, env;

class AppConfig {
  late final int port;
  late final String host;
  late final String _databaseUrl;
  late final String _jwtSecret;
  late final String logLevel;

  // Parsed database connection details
  late final String dbHost;
  late final int dbPort;
  late final String dbName;
  late final String dbUsername;
  late final String dbPassword;

  AppConfig() {
    load();

    port = int.tryParse(env['PORT'] ?? '8080') ?? 8080;
    host = env['HOST'] ?? '0.0.0.0';
    _databaseUrl = env['DATABASE_URL'] ?? '';
    _jwtSecret = env['JWT_SECRET'] ?? '';
    logLevel = env['LOG_LEVEL'] ?? 'INFO';

    _parseDatabaseUrl();
  }

  void _parseDatabaseUrl() {
    if (_databaseUrl.isEmpty) {
      throw StateError('DATABASE_URL is not set or is empty. This is a critical configuration.');
    }
    try {
      final uri = Uri.parse(_databaseUrl);
      if (uri.scheme != 'postgres' && uri.scheme != 'postgresql') {
        throw FormatException('Invalid scheme for DATABASE_URL: ${uri.scheme}. Expected "postgres" or "postgresql".');
      }
      dbHost = uri.host;
      dbPort = uri.port;
      dbName = uri.pathSegments.where((s) => s.isNotEmpty).join('/'); // Handle cases with or without leading slash
      if (dbName.isEmpty) {
        throw FormatException('Database name cannot be empty in DATABASE_URL.');
      }
      dbUsername = uri.userInfo.split(':')[0];
      dbPassword = uri.userInfo.split(':')[1];

      if (dbHost.isEmpty || dbUsername.isEmpty ) { // Password can be empty
        throw FormatException('Host or Username cannot be empty in DATABASE_URL.');
      }

    } catch (e) {
      throw StateError('Failed to parse DATABASE_URL "$_databaseUrl": ${e.toString()}');
    }
  }

  String get databaseUrl {
    if (_databaseUrl.isEmpty) {
      throw StateError('DATABASE_URL is not set or is empty. This is a critical configuration.');
    }
    return _databaseUrl;
  }

  String get jwtSecret {
    if (_jwtSecret.isEmpty) {
      throw StateError('JWT_SECRET is not set or is empty. This is a critical configuration.');
    }
    return _jwtSecret;
  }
}
