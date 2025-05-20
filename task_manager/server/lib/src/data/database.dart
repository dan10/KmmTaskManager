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
    await _createTables();
  }

  Future<void> disconnect() async {
    await _connection.close();
  }

  Future<void> _createTables() async {
    await _connection.execute('''
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
        creator_id TEXT NOT NULL REFERENCES users(id)
      );

      CREATE TABLE IF NOT EXISTS project_members (
        project_id TEXT REFERENCES projects(id),
        user_id TEXT REFERENCES users(id),
        PRIMARY KEY (project_id, user_id)
      );

      CREATE TABLE IF NOT EXISTS tasks (
        id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        description TEXT NOT NULL,
        is_completed BOOLEAN NOT NULL DEFAULT FALSE,
        creator_id TEXT NOT NULL REFERENCES users(id),
        project_id TEXT REFERENCES projects(id),
        assignee_id TEXT REFERENCES users(id),
        priority INTEGER NOT NULL DEFAULT 0,
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
}
