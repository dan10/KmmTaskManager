import 'package:test/test.dart';
import 'package:shelf/shelf.dart' as shelf;
import 'package:shelf/shelf_io.dart' as io;
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:task_manager_shared/models.dart' as shared_models;

import '../helpers/test_base.dart';
import '../../lib/src/routes/task_routes.dart';
import '../../lib/src/routes/auth_routes.dart';
import '../../lib/src/services/task_service.dart';
import '../../lib/src/services/auth_service.dart';
import '../../lib/src/services/jwt_service.dart';
import '../../lib/src/config/app_config.dart';
import '../../lib/src/repositories/task_repository.dart';
import '../../lib/src/repositories/auth_repository.dart';
import '../../lib/src/middleware/auth_middleware.dart';

void main() {
  late TestBase testBase;
  late http.Client client;
  late String baseUrl;
  late shelf.Handler handler;
  late String authToken;
  late String userId;

  setUpAll(() async {
    testBase = TestBase();
    await testBase.setUp();
    client = http.Client();

    // Create services
    final authRepository = AuthRepository(testBase.connection);
    final jwtService = JwtService();
    final appConfig = testBase.appConfig;
    final authService = AuthServiceImpl(authRepository, jwtService, appConfig);
    final taskRepository = TaskRepository(testBase.connection);
    final taskService = TaskServiceImpl(taskRepository);

    // Create handler with auth middleware
    final authMiddleware = AuthMiddleware(authService);
    
    // Create routes
    final authRoutes = AuthRoutes(authService, jwtService);
    final taskRoutes = TaskRoutes(taskService, authMiddleware);

    handler = shelf.Pipeline()
        .addMiddleware(shelf.logRequests())
        .addMiddleware(authMiddleware.middleware())
        .addHandler((request) {
      // Route requests based on path
      if (request.url.path.startsWith('auth/')) {
        return authRoutes.router.call(request);
      } else if (request.url.path.startsWith('tasks/')) {
        return taskRoutes.router.call(request);
      }
      return shelf.Response.notFound('Not found');
    });

    // Start test server
    final server = await io.serve(handler, 'localhost', 0);
    baseUrl = 'http://localhost:${server.port}';

    // Register and login a test user
    final registerResponse = await client.post(
      Uri.parse('$baseUrl/auth/register'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'email': 'calendar@test.com',
        'password': 'Test123!',
        'displayName': 'Calendar Test User',
      }),
    );

    expect(registerResponse.statusCode, equals(201));

    final loginResponse = await client.post(
      Uri.parse('$baseUrl/auth/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'email': 'calendar@test.com',
        'password': 'Test123!',
      }),
    );

    expect(loginResponse.statusCode, equals(200));
    final loginData = jsonDecode(loginResponse.body);
    authToken = loginData['token'];
    userId = loginData['user']['id'];
  });

  tearDownAll(() async {
    client.close();
    await testBase.tearDown();
  });

  group('Calendar Endpoint Tests', () {
    test('GET /tasks/assigned/due-on returns tasks for specific date', () async {
      // Create tasks with different due dates
      final today = DateTime.utc(2024, 12, 15, 10, 0, 0);
      final tomorrow = DateTime.utc(2024, 12, 16, 10, 0, 0);
      final yesterday = DateTime.utc(2024, 12, 14, 10, 0, 0);

      // Task due today - HIGH priority
      await client.post(
        Uri.parse('$baseUrl/tasks'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $authToken',
        },
        body: jsonEncode({
          'title': 'Task Due Today High',
          'description': 'Should appear in results',
          'assigneeId': userId,
          'priority': 'HIGH',
          'dueDate': today.toIso8601String(),
        }),
      );

      // Task due today - MEDIUM priority
      await client.post(
        Uri.parse('$baseUrl/tasks'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $authToken',
        },
        body: jsonEncode({
          'title': 'Task Due Today Medium',
          'description': 'Should appear in results',
          'assigneeId': userId,
          'priority': 'MEDIUM',
          'dueDate': today.toIso8601String(),
        }),
      );

      // Task due today - LOW priority
      await client.post(
        Uri.parse('$baseUrl/tasks'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $authToken',
        },
        body: jsonEncode({
          'title': 'Task Due Today Low',
          'description': 'Should appear in results',
          'assigneeId': userId,
          'priority': 'LOW',
          'dueDate': today.toIso8601String(),
        }),
      );

      // Task due tomorrow (should not appear)
      await client.post(
        Uri.parse('$baseUrl/tasks'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $authToken',
        },
        body: jsonEncode({
          'title': 'Task Due Tomorrow',
          'description': 'Should not appear',
          'assigneeId': userId,
          'priority': 'HIGH',
          'dueDate': tomorrow.toIso8601String(),
        }),
      );

      // Task due yesterday (should not appear)
      await client.post(
        Uri.parse('$baseUrl/tasks'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $authToken',
        },
        body: jsonEncode({
          'title': 'Task Due Yesterday',
          'description': 'Should not appear',
          'assigneeId': userId,
          'priority': 'HIGH',
          'dueDate': yesterday.toIso8601String(),
        }),
      );

      // Task with DONE status (should not appear)
      final doneTaskResponse = await client.post(
        Uri.parse('$baseUrl/tasks'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $authToken',
        },
        body: jsonEncode({
          'title': 'Task Done Today',
          'description': 'Should not appear because it\'s done',
          'assigneeId': userId,
          'priority': 'HIGH',
          'status': 'DONE',
          'dueDate': today.toIso8601String(),
        }),
      );
      expect(doneTaskResponse.statusCode, equals(201));

      // Wait a moment for database operations to complete
      await Future.delayed(Duration(milliseconds: 100));

      // Get tasks due on 2024-12-15
      final response = await client.get(
        Uri.parse('$baseUrl/tasks/assigned/due-on?date=2024-12-15&tzOffsetMinutes=0'),
        headers: {
          'Authorization': 'Bearer $authToken',
        },
      );

      // Verify the response
      expect(response.statusCode, equals(200));

      final responseData = jsonDecode(response.body) as List;

      // Should return exactly 3 tasks (excluding DONE task and tasks from other days)
      expect(responseData.length, equals(3));

      // Verify tasks are ordered by priority (HIGH > MEDIUM > LOW)
      expect(responseData[0]['title'], equals('Task Due Today High'));
      expect(responseData[0]['priority'], equals('HIGH'));

      expect(responseData[1]['title'], equals('Task Due Today Medium'));
      expect(responseData[1]['priority'], equals('MEDIUM'));

      expect(responseData[2]['title'], equals('Task Due Today Low'));
      expect(responseData[2]['priority'], equals('LOW'));

      // Verify all tasks have the correct assignee
      for (final task in responseData) {
        expect(task['assigneeId'], equals(userId));
      }
    });

    test('GET /tasks/assigned/due-on returns empty list for date with no tasks', () async {
      // Get tasks due on a date with no tasks
      final response = await client.get(
        Uri.parse('$baseUrl/tasks/assigned/due-on?date=2025-01-01&tzOffsetMinutes=0'),
        headers: {
          'Authorization': 'Bearer $authToken',
        },
      );

      // Verify the response
      expect(response.statusCode, equals(200));

      final responseData = jsonDecode(response.body) as List;

      // Should return empty list
      expect(responseData.length, equals(0));
    });

    test('GET /tasks/assigned/due-on supports pagination', () async {
      // Clear previous tasks
      await testBase.clearTables();

      // Re-register user
      await client.post(
        Uri.parse('$baseUrl/auth/register'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'email': 'pagination@test.com',
          'password': 'Test123!',
          'displayName': 'Pagination Test User',
        }),
      );

      final loginResponse = await client.post(
        Uri.parse('$baseUrl/auth/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'email': 'pagination@test.com',
          'password': 'Test123!',
        }),
      );

      final loginData = jsonDecode(loginResponse.body);
      final paginationToken = loginData['token'];
      final paginationUserId = loginData['user']['id'];

      final dueDate = DateTime.utc(2024, 12, 20, 10, 0, 0);

      // Create 5 tasks
      for (int i = 0; i < 5; i++) {
        await client.post(
          Uri.parse('$baseUrl/tasks'),
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer $paginationToken',
          },
          body: jsonEncode({
            'title': 'Task $i',
            'description': 'Description $i',
            'assigneeId': paginationUserId,
            'priority': 'MEDIUM',
            'dueDate': dueDate.toIso8601String(),
          }),
        );
      }

      await Future.delayed(Duration(milliseconds: 100));

      // Get first page (size 2)
      final page1Response = await client.get(
        Uri.parse('$baseUrl/tasks/assigned/due-on?date=2024-12-20&tzOffsetMinutes=0&page=0&size=2'),
        headers: {
          'Authorization': 'Bearer $paginationToken',
        },
      );

      expect(page1Response.statusCode, equals(200));
      final page1Data = jsonDecode(page1Response.body) as List;
      expect(page1Data.length, equals(2));

      // Get second page
      final page2Response = await client.get(
        Uri.parse('$baseUrl/tasks/assigned/due-on?date=2024-12-20&tzOffsetMinutes=0&page=1&size=2'),
        headers: {
          'Authorization': 'Bearer $paginationToken',
        },
      );

      expect(page2Response.statusCode, equals(200));
      final page2Data = jsonDecode(page2Response.body) as List;
      expect(page2Data.length, equals(2));

      // Get third page
      final page3Response = await client.get(
        Uri.parse('$baseUrl/tasks/assigned/due-on?date=2024-12-20&tzOffsetMinutes=0&page=2&size=2'),
        headers: {
          'Authorization': 'Bearer $paginationToken',
        },
      );

      expect(page3Response.statusCode, equals(200));
      final page3Data = jsonDecode(page3Response.body) as List;
      expect(page3Data.length, equals(1));
    });

    test('GET /tasks/assigned/due-on requires authentication', () async {
      // Try to get tasks without authentication
      final response = await client.get(
        Uri.parse('$baseUrl/tasks/assigned/due-on?date=2024-12-15&tzOffsetMinutes=0'),
      );

      // Should return unauthorized
      expect(response.statusCode, equals(401));
    });

    test('GET /tasks/assigned/due-on validates date parameter', () async {
      // Try to get tasks with invalid date format
      final response = await client.get(
        Uri.parse('$baseUrl/tasks/assigned/due-on?date=invalid-date&tzOffsetMinutes=0'),
        headers: {
          'Authorization': 'Bearer $authToken',
        },
      );

      // Should return bad request
      expect(response.statusCode, equals(400));
    });

    test('GET /tasks/assigned/due-on validates tzOffsetMinutes parameter', () async {
      // Try to get tasks with invalid timezone offset
      final response = await client.get(
        Uri.parse('$baseUrl/tasks/assigned/due-on?date=2024-12-15&tzOffsetMinutes=invalid'),
        headers: {
          'Authorization': 'Bearer $authToken',
        },
      );

      // Should return bad request
      expect(response.statusCode, equals(400));
    });

    test('GET /tasks/assigned/due-on handles timezone offset correctly', () async {
      // Clear previous tasks
      await testBase.clearTables();

      // Re-register user
      await client.post(
        Uri.parse('$baseUrl/auth/register'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'email': 'timezone@test.com',
          'password': 'Test123!',
          'displayName': 'Timezone Test User',
        }),
      );

      final loginResponse = await client.post(
        Uri.parse('$baseUrl/auth/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'email': 'timezone@test.com',
          'password': 'Test123!',
        }),
      );

      final loginData = jsonDecode(loginResponse.body);
      final timezoneToken = loginData['token'];
      final timezoneUserId = loginData['user']['id'];

      // Create a task due at midnight UTC on 2024-12-15
      final dueDateUtc = DateTime.utc(2024, 12, 15, 0, 0, 0);

      await client.post(
        Uri.parse('$baseUrl/tasks'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $timezoneToken',
        },
        body: jsonEncode({
          'title': 'Timezone Test Task',
          'description': 'Testing timezone handling',
          'assigneeId': timezoneUserId,
          'priority': 'MEDIUM',
          'dueDate': dueDateUtc.toIso8601String(),
        }),
      );

      await Future.delayed(Duration(milliseconds: 100));

      // Request with UTC timezone (offset 0)
      final utcResponse = await client.get(
        Uri.parse('$baseUrl/tasks/assigned/due-on?date=2024-12-15&tzOffsetMinutes=0'),
        headers: {
          'Authorization': 'Bearer $timezoneToken',
        },
      );

      expect(utcResponse.statusCode, equals(200));
      final utcData = jsonDecode(utcResponse.body) as List;
      expect(utcData.length, equals(1));

      // Request with EST timezone (offset -300 minutes = -5 hours)
      // This should still find the task because it's due on 2024-12-14 19:00 EST
      final estResponse = await client.get(
        Uri.parse('$baseUrl/tasks/assigned/due-on?date=2024-12-14&tzOffsetMinutes=-300'),
        headers: {
          'Authorization': 'Bearer $timezoneToken',
        },
      );

      expect(estResponse.statusCode, equals(200));
      final estData = jsonDecode(estResponse.body) as List;
      expect(estData.length, equals(1));
    });
  });
}

