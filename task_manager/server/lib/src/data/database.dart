import 'package:postgres/postgres.dart';

class Database {
  final PostgreSQLConnection _connection;

  Database()
      : _connection = PostgreSQLConnection(
          'localhost',
          5432,
          'task_manager',
          username: 'postgres',
          password: 'postgres',
        );

  Future<void> connect() async {
    await _connection.open();
    await createTables(_connection);
  }

  Future<void> disconnect() async {
    await _connection.close();
  }

  static Future<void> createTables(PostgreSQLConnection connection) async {
    await connection.execute('''
      CREATE TABLE IF NOT EXISTS users (
        id TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        email TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL
      );

      CREATE TABLE IF NOT EXISTS projects (
        id TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        description TEXT NOT NULL,
        creator_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE
      );

      CREATE TABLE IF NOT EXISTS project_members (
        project_id TEXT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
        user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        PRIMARY KEY (project_id, user_id)
      );

      CREATE TABLE IF NOT EXISTS tasks (
        id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        description TEXT NOT NULL,
        status TEXT NOT NULL,
        priority TEXT NOT NULL,
        project_id TEXT REFERENCES projects(id) ON DELETE SET NULL,
        assignee_id TEXT REFERENCES users(id) ON DELETE SET NULL,
        creator_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        due_date TIMESTAMP
      );
    ''');
  }

  Future<List<Map<String, dynamic>>> query(
    String query, {
    Map<String, dynamic>? parameters,
  }) async {
    final results = await _connection.mappedResultsQuery(
      query,
      substitutionValues: parameters,
    );

    return results.map((row) {
      final values = row.values.first;
      return Map<String, dynamic>.from(values);
    }).toList();
  }

  Future<void> execute(
    String query, {
    Map<String, dynamic>? parameters,
  }) async {
    await _connection.execute(
      query,
      substitutionValues: parameters,
    );
  }

  static Future<void> dropTables(PostgreSQLConnection connection) async {
    await connection.execute('DROP TABLE IF EXISTS tasks');
    await connection.execute('DROP TABLE IF EXISTS project_members');
    await connection.execute('DROP TABLE IF EXISTS projects');
    await connection.execute('DROP TABLE IF EXISTS users');
  }
}
