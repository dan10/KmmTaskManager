import 'package:test/test.dart';
import 'package:shelf/shelf.dart';
import '../../lib/src/routes/project_task_routes.dart';
import '../../lib/src/services/task_service.dart';
import '../../lib/src/services/jwt_service.dart';
import '../../lib/src/repositories/task_repository.dart';
import '../../lib/src/middleware/auth_middleware.dart';
import '../../lib/src/middleware/error_handling_middleware.dart';
import '../helpers/test_base.dart';
import 'dart:convert';

class _MockJwtService implements JwtService {
  @override
  String generateToken(user) => 'mock_token';

  @override
  Map<String, dynamic>? validateToken(String token) => {'sub': 'test_user_id'};
  
  @override
  String? getUserIdFromToken(String token) => 'test_user_id';
  
  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

void main() {
  late TestBase testBase;
  late Handler handler;
  late String testUserId;

  setUpAll(() async {
    testBase = TestBase();
    await testBase.setUp();

    // Create a mock JWT service that always validates
    final jwtService = _MockJwtService();
    final authMiddleware = AuthMiddleware(jwtService);
    final taskRepository = TaskRepository(testBase.connection);
    final taskService = TaskServiceImpl(taskRepository);

    final projectTaskRoutes = ProjectTaskRoutes(taskService, authMiddleware);
    
    // Wrap router with error handling middleware like in production
    handler = Pipeline()
        .addMiddleware(errorHandlingMiddleware())
        .addHandler(projectTaskRoutes.router.call);
    
    // Create test user
    testUserId = 'test_user_id';
    
    await testBase.connection.execute('''
      INSERT INTO users (id, display_name, email, password_hash, created_at)
      VALUES ('$testUserId', 'Test User', 'test@example.com', 'hash', CURRENT_TIMESTAMP)
    ''');
  });

  tearDownAll(() async {
    await testBase.tearDown();
  });

  setUp(() async {
    // Clear task and project data before each test
    await testBase.connection.execute('DELETE FROM tasks');
    await testBase.connection.execute('DELETE FROM project_assignments');
    await testBase.connection.execute('DELETE FROM projects');
  });

  group('ProjectTaskRoutes Integration Tests', () {
    test('GET /<projectId>/tasks should return tasks for project', () async {
      // Create a test project
      await testBase.connection.execute('''
        INSERT INTO projects (id, name, description, creator_id)
        VALUES ('test_project', 'Test Project', 'Description', '$testUserId')
      ''');

      // Create tasks for the project
      await testBase.connection.execute('''
        INSERT INTO tasks (id, title, description, status, priority, project_id, creator_id)
        VALUES 
          ('task-1', 'Task 1', 'Description 1', 'todo', 'medium', 'test_project', '$testUserId'),
          ('task-2', 'Task 2', 'Description 2', 'in_progress', 'high', 'test_project', '$testUserId')
      ''');

      final request = Request(
        'GET',
        Uri.parse('http://localhost/test_project/tasks?page=0&size=10'),
        headers: {'Authorization': 'Bearer mock_token'},
        context: {'userId': testUserId},
      );

      final response = await handler(request);

      expect(response.statusCode, 200);
      final body = await response.readAsString();
      final tasks = jsonDecode(body) as List;
      expect(tasks.length, 2);
    });

    test('POST /<projectId>/tasks should create task in project', () async {
      // Create a test project
      await testBase.connection.execute('''
        INSERT INTO projects (id, name, description, creator_id)
        VALUES ('test_project', 'Test Project', 'Description', '$testUserId')
      ''');

      final taskData = {
        'title': 'New Task',
        'description': 'Task Description',
        'priority': 'HIGH',
        'dueDate': DateTime.now().add(const Duration(days: 7)).toIso8601String(),
      };

      final request = Request(
        'POST',
        Uri.parse('http://localhost/test_project/tasks'),
        headers: {
          'Authorization': 'Bearer mock_token',
          'content-type': 'application/json',
        },
        body: jsonEncode(taskData),
        context: {'userId': testUserId},
      );

      final response = await handler(request);

      expect(response.statusCode, 201);
      final body = await response.readAsString();
      final task = jsonDecode(body) as Map<String, dynamic>;
      expect(task['title'], 'New Task');
      expect(task['projectId'], 'test_project'); // Verify projectId is set from URL
      expect(task['creatorId'], testUserId);
    });

    test('GET /<projectId>/tasks should support pagination', () async {
      // Create a test project
      await testBase.connection.execute('''
        INSERT INTO projects (id, name, description, creator_id)
        VALUES ('test_project', 'Test Project', 'Description', '$testUserId')
      ''');

      // Create multiple tasks
      for (int i = 0; i < 15; i++) {
        await testBase.connection.execute('''
          INSERT INTO tasks (id, title, description, status, priority, project_id, creator_id)
          VALUES ('task-$i', 'Task $i', 'Description $i', 'todo', 'medium', 'test_project', '$testUserId')
        ''');
      }

      // Get first page
      final request1 = Request(
        'GET',
        Uri.parse('http://localhost/test_project/tasks?page=0&size=10'),
        headers: {'Authorization': 'Bearer mock_token'},
        context: {'userId': testUserId},
      );

      final response1 = await handler(request1);
      expect(response1.statusCode, 200);
      final body1 = await response1.readAsString();
      final tasks1 = jsonDecode(body1) as List;
      expect(tasks1.length, 10);

      // Get second page
      final request2 = Request(
        'GET',
        Uri.parse('http://localhost/test_project/tasks?page=1&size=10'),
        headers: {'Authorization': 'Bearer mock_token'},
        context: {'userId': testUserId},
      );

      final response2 = await handler(request2);
      expect(response2.statusCode, 200);
      final body2 = await response2.readAsString();
      final tasks2 = jsonDecode(body2) as List;
      expect(tasks2.length, 5);
    });

    test('POST /<projectId>/tasks should validate required fields', () async {
      // Create a test project
      await testBase.connection.execute('''
        INSERT INTO projects (id, name, description, creator_id)
        VALUES ('test_project', 'Test Project', 'Description', '$testUserId')
      ''');

      final invalidTaskData = {
        'description': 'Missing title',
        // title is missing
      };

      final request = Request(
        'POST',
        Uri.parse('http://localhost/test_project/tasks'),
        headers: {
          'Authorization': 'Bearer mock_token',
          'content-type': 'application/json',
        },
        body: jsonEncode(invalidTaskData),
        context: {'userId': testUserId},
      );

      final response = await handler(request);

      expect(response.statusCode, 400); // Validation error
    });
  });
}

