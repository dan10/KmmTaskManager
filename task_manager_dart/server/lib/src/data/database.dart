import 'package:postgres/postgres.dart';
import '../config/app_config.dart';

class Database {
  final AppConfig _appConfig;
  late final Connection _connection;

  Database(this._appConfig);

  Future<void> connect() async {
    final endpoint = Endpoint(
      host: _appConfig.dbHost,
      port: _appConfig.dbPort,
      database: _appConfig.dbName,
      username: _appConfig.dbUsername,
      password: _appConfig.dbPassword,
    );

    _connection = await Connection.open(
      endpoint,
      settings: ConnectionSettings(
        sslMode: SslMode.disable,
      ),
    );

    await createTables(_connection);
  }

  Future<void> disconnect() async {
    await _connection.close();
  }

  // Expose the connection for repositories
  Connection get connection => _connection;

  static Future<void> createTables(Connection connection) async {
    await connection.execute('''
      CREATE TABLE IF NOT EXISTS users (
        id TEXT PRIMARY KEY,
        display_name TEXT NOT NULL,
        email TEXT UNIQUE NOT NULL,
        password_hash TEXT,
        google_id TEXT,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
      )
    ''');

    await connection.execute('''
      CREATE TABLE IF NOT EXISTS projects (
        id TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        description TEXT NOT NULL,
        creator_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
      )
    ''');

    await connection.execute('''
      CREATE TABLE IF NOT EXISTS project_assignments (
        id TEXT PRIMARY KEY,
        project_id TEXT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
        user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        assigned_by TEXT NOT NULL REFERENCES users(id),
        UNIQUE (project_id, user_id)
      )
    ''');

    await connection.execute('''
      CREATE TABLE IF NOT EXISTS tasks (
        id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        description TEXT NOT NULL,
        status TEXT NOT NULL,
        priority TEXT NOT NULL,
        project_id TEXT REFERENCES projects(id) ON DELETE SET NULL,
        assignee_id TEXT REFERENCES users(id) ON DELETE SET NULL,
        creator_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        due_date TIMESTAMP,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
      )
    ''');
    
    // Create indexes for users table
    await connection.execute('CREATE UNIQUE INDEX IF NOT EXISTS uq_users_google_id ON users(google_id) WHERE google_id IS NOT NULL');
    await connection.execute('CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at)');

    // Create indexes for projects table
    await connection.execute('CREATE INDEX IF NOT EXISTS idx_projects_creator_id ON projects(creator_id)');
    await connection.execute('CREATE INDEX IF NOT EXISTS idx_projects_created_at ON projects(created_at)');
    await connection.execute('CREATE UNIQUE INDEX IF NOT EXISTS uq_projects_name_creator ON projects(name, creator_id)');

    // Create indexes for project_assignments table
    await connection.execute('CREATE INDEX IF NOT EXISTS idx_project_assignments_project_id ON project_assignments(project_id)');
    await connection.execute('CREATE INDEX IF NOT EXISTS idx_project_assignments_user_id ON project_assignments(user_id)');
    await connection.execute('CREATE INDEX IF NOT EXISTS idx_project_assignments_assigned_at ON project_assignments(assigned_at)');

    // Create indexes for tasks table
    await connection.execute('CREATE INDEX IF NOT EXISTS idx_tasks_project_id ON tasks(project_id)');
    await connection.execute('CREATE INDEX IF NOT EXISTS idx_tasks_assignee_id ON tasks(assignee_id)');
    await connection.execute('CREATE INDEX IF NOT EXISTS idx_tasks_creator_id ON tasks(creator_id)');
    await connection.execute('CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status)');
    await connection.execute('CREATE INDEX IF NOT EXISTS idx_tasks_created_at ON tasks(created_at)');
  }

  Future<List<Map<String, dynamic>>> query(
    String query, {
    Map<String, dynamic>? parameters,
  }) async {
    final result = await _connection.execute(
      Sql.named(query),
      parameters: parameters,
    );

    return result.map((row) => row.toColumnMap()).toList();
  }

  Future<void> execute(
    String query, {
    Map<String, dynamic>? parameters,
  }) async {
    await _connection.execute(
      Sql.named(query),
      parameters: parameters,
    );
  }

  static Future<void> dropTables(Connection connection) async {
    await connection.execute('DROP TABLE IF EXISTS tasks');
    await connection.execute('DROP TABLE IF EXISTS project_assignments');
    await connection.execute('DROP TABLE IF EXISTS projects');
    await connection.execute('DROP TABLE IF EXISTS users');
  }
}
