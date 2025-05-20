import 'package:postgres/postgres.dart';
import 'package:test/test.dart';
import 'dart:async';
import 'postgres_container.dart';
import '../../lib/src/data/database.dart';
import 'dart:io';
import 'dart:math';

class TestBase {
  late PostgreSQLConnection connection;
  late TestPostgresContainer container;
  late String containerName;
  late int port;

  Future<void> setUp() async {
    try {
      // Generate a random container name and port
      final random = Random();
      containerName = 'task-manager-test-db-${random.nextInt(10000)}';
      port = 5432 +
          random.nextInt(1000); // Use a random port between 5432 and 6432

      // Force remove any lingering test container
      try {
        await Process.run('docker', ['rm', '-f', containerName]);
      } catch (e) {
        // Ignore errors if container doesn't exist
      }

      // Start a new PostgreSQL container
      final startResult = await Process.run('docker', [
        'run',
        '--name',
        containerName,
        '-e',
        'POSTGRES_PASSWORD=postgres',
        '-e',
        'POSTGRES_USER=postgres',
        '-e',
        'POSTGRES_DB=task_manager_test',
        '-p',
        '$port:5432',
        '-d',
        'postgres:latest'
      ]);

      if (startResult.exitCode != 0) {
        throw Exception(
            'Failed to start PostgreSQL container: ${startResult.stderr}');
      }

      // Wait for the container to be ready
      await Future.delayed(Duration(seconds: 20));

      // Initialize the connection
      connection = PostgreSQLConnection(
        'localhost',
        port,
        'task_manager_test',
        username: 'postgres',
        password: 'postgres',
      );

      // Open the connection and wait for it to be ready
      await connection.open();

      // Create the database tables
      await Database.createTables(connection);
    } catch (e) {
      // Clean up on error
      await tearDown();
      rethrow;
    }
  }

  Future<void> tearDown() async {
    try {
      if (connection != null) {
        await clearTables();
        await connection.close();
      }
    } catch (e) {
      print('Error during tearDown: $e');
    }

    // Force remove the test container
    try {
      await Process.run('docker', ['rm', '-f', containerName]);
    } catch (e) {
      // Ignore errors if container doesn't exist
    }
  }

  Future<void> clearTables() async {
    if (connection != null) {
      await Database.dropTables(connection);
      await Database.createTables(connection);
    }
  }
}
