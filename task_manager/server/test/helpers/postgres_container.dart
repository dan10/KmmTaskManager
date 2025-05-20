import 'dart:io';
import 'package:postgres/postgres.dart';

class TestPostgresContainer {
  static const String containerName = 'task-manager-test-db';
  static const String image = 'postgres:16-alpine';

  String get host => 'localhost';
  int get port => 5432;
  String get database => 'test_db';
  String get username => 'postgres';
  String get password => 'postgres';

  Future<void> start() async {
    try {
      // Stop and remove existing container if it exists
      await Process.run('docker', ['stop', containerName]);
      await Process.run('docker', ['rm', containerName]);
    } catch (e) {
      // Ignore errors if container doesn't exist
    }

    // Start new container
    final result = await Process.run('docker', [
      'run',
      '--name',
      containerName,
      '-e',
      'POSTGRES_USER=$username',
      '-e',
      'POSTGRES_PASSWORD=$password',
      '-e',
      'POSTGRES_DB=$database',
      '-p',
      '$port:$port',
      '-d',
      image,
    ]);

    if (result.exitCode != 0) {
      throw Exception('Failed to start PostgreSQL container: ${result.stderr}');
    }

    // Wait for PostgreSQL to start
    await Future.delayed(const Duration(seconds: 2));
  }

  Future<void> stop() async {
    try {
      await Process.run('docker', ['stop', containerName]);
      await Process.run('docker', ['rm', containerName]);
    } catch (e) {
      // Ignore errors if container doesn't exist
    }
  }
}
