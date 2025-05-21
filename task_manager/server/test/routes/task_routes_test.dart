import 'dart:convert';
import 'package:shelf_test_handler/shelf_test_handler.dart';
import 'package:test/test.dart';
import 'package:shelf/shelf.dart' as shelf;
import 'package:mockito/mockito.dart';
import 'package:shared/models.dart' as shared_models;

import '../../../lib/src/config/app_config.dart';
import '../../../lib/src/data/database.dart';
import '../../../lib/src/repositories/task_repository.dart';
import '../../../lib/src/repositories/auth_repository.dart'; // For user setup
import '../../../lib/src/repositories/project_repository.dart'; // For project setup
import '../../../lib/src/services/task_service.dart';
import '../../../lib/src/services/jwt_service.dart';
import '../../../lib/src/services/auth_service.dart'; // For user setup
import '../../../lib/src/routes/task_routes.dart';
import '../../../lib/src/middleware/auth_middleware.dart';
import '../../../lib/src/middleware/error_handling_middleware.dart';
import '../../../lib/src/dto/task/task_assign_request_dto.dart';
import '../../../lib/src/dto/task/task_status_change_request_dto.dart';
import '../../../lib/src/dto/error_response_dto.dart';
import '../../../lib/src/exceptions/custom_exceptions.dart';

import '../services/auth_service_test.mocks.dart'; // Use AppConfig mock

// Helper to create test users and projects directly in the InMemoryDatabase
Future<shared_models.User> _createTestUser(AuthRepository repo, String id, String email, String name) async {
  final user = shared_models.User(id: id, name: name, email: email, passwordHash: 'hashed_password_for_task_test');
  return repo.createUser(user);
}

Future<shared_models.Project> _createTestProject(ProjectRepository repo, String id, String name, String creatorId) async {
  final project = shared_models.Project(id: id, name: name, description: 'Desc for $name', creatorId: creatorId, memberIds: [creatorId]);
  return repo.create(project);
}


void main() {
  late Database db;
  late TaskRepository taskRepository;
  late AuthRepository authRepository;
  late ProjectRepository projectRepository;
  late TaskService taskService;
  late JwtService jwtService;
  late AuthMiddleware authMiddleware;
  late shelf.Pipeline pipeline;
  late MockAppConfig mockAppConfig;

  late String user1Token;
  late String user2Token;
  late shared_models.User dbUser1;
  late shared_models.User dbUser2;
  late shared_models.Project dbProject1; // Created by user1
  late shared_models.Project dbProject2; // Created by user2

  setUpAll(() async {
    db = InMemoryDatabase();
    taskRepository = TaskRepository(db);
    authRepository = AuthRepository(db);
    projectRepository = ProjectRepository(db);
    
    mockAppConfig = MockAppConfig();
    when(mockAppConfig.jwtSecret).thenReturn('test-super-secret-key-for-jwt-task-routes');

    jwtService = JwtService(mockAppConfig);
    // Real AuthService for user creation and token generation in setup
    final authServiceForSetup = AuthServiceImpl(authRepository, jwtService, mockAppConfig); 
    taskService = TaskServiceImpl(taskRepository);
    authMiddleware = AuthMiddleware(jwtService);
    
    final taskRoutes = TaskRoutes(taskService, authMiddleware).router;
    pipeline = const shelf.Pipeline()
        .addMiddleware(errorHandlingMiddleware())
        .addHandler(taskRoutes);

    // Create users
    dbUser1 = await _createTestUser(authRepository, 'task-user-1', 'taskuser1@example.com', 'TaskUser1');
    dbUser2 = await _createTestUser(authRepository, 'task-user-2', 'taskuser2@example.com', 'TaskUser2');
    user1Token = jwtService.generateToken(dbUser1);
    user2Token = jwtService.generateToken(dbUser2);

    // Create projects
    dbProject1 = await _createTestProject(projectRepository, 'task-proj-1', 'Task Project 1', dbUser1.id);
    dbProject2 = await _createTestProject(projectRepository, 'task-proj-2', 'Task Project 2', dbUser2.id);
  });

  group('TaskRoutes', () {
    late ShelfTestHandler handler;
    late shared_models.Task task1ByUser1; // Assigned to user1, on project1
    late shared_models.Task task2ByUser1; // Assigned to user2, on project1
    late shared_models.Task task3ByUser2; // Assigned to user2, on project2

    setUp(() async {
       handler = ShelfTestHandler(pipeline);
       // Clear tasks before each test
       (db as InMemoryDatabase).tasks.clear();

       // Create some initial tasks
       task1ByUser1 = await taskRepository.create(shared_models.Task(
          id: 'task-id-1', title: 'User1 Task Alpha', description: 'Desc 1', 
          status: shared_models.TaskStatus.todo, priority: shared_models.Priority.medium,
          creatorId: dbUser1.id, assigneeId: dbUser1.id, projectId: dbProject1.id));
      
       task2ByUser1 = await taskRepository.create(shared_models.Task(
          id: 'task-id-2', title: 'User1 Task Beta (assigned user2)', description: 'Desc 2', 
          status: shared_models.TaskStatus.inProgress, priority: shared_models.Priority.high,
          creatorId: dbUser1.id, assigneeId: dbUser2.id, projectId: dbProject1.id));

       task3ByUser2 = await taskRepository.create(shared_models.Task(
          id: 'task-id-3', title: 'User2 Task Gamma', description: 'Desc 3 searchable', 
          status: shared_models.TaskStatus.done, priority: shared_models.Priority.low,
          creatorId: dbUser2.id, assigneeId: dbUser2.id, projectId: dbProject2.id));
    });

    group('POST /tasks', () {
      test('should create a new task successfully', () async {
        final newTaskDto = shared_models.Task(
            id: 'new-task-id', title: 'New Unique Task', description: 'A new one',
            status: shared_models.TaskStatus.todo, priority: shared_models.Priority.low,
            creatorId: '', // Will be overridden by context
        );
        final response = await handler.post(
          '/tasks',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $user1Token'},
          body: jsonEncode(newTaskDto.toJson()..remove('creatorId')),
        );
        expect(response.statusCode, 200);
        final created = shared_models.Task.fromJson(jsonDecode(await response.readAsString()));
        expect(created.title, 'New Unique Task');
        expect(created.creatorId, dbUser1.id);
      });

       test('should return 400 for task with empty title', () async {
         final newTaskDto = shared_models.Task(id: 'new-task-fail', title: '', description: 'No title', status: shared_models.TaskStatus.todo, priority: shared_models.Priority.low, creatorId: '');
         final response = await handler.post(
          '/tasks',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $user1Token'},
          body: jsonEncode(newTaskDto.toJson()..remove('creatorId')),
        );
        expect(response.statusCode, 400);
        final error = ErrorResponseDto.fromJson(jsonDecode(await response.readAsString()));
        expect(error.message, 'Task title is required.');
      });
    });

    group('GET /tasks/<id>', () {
      test('should return task by ID if creator or assignee', () async {
        // User1 is creator and assignee of task1ByUser1
        var response = await handler.get('/tasks/${task1ByUser1.id}', headers: {'Authorization': 'Bearer $user1Token'});
        expect(response.statusCode, 200);
        var fetched = shared_models.Task.fromJson(jsonDecode(await response.readAsString()));
        expect(fetched.id, task1ByUser1.id);

        // User2 is assignee of task2ByUser1
        response = await handler.get('/tasks/${task2ByUser1.id}', headers: {'Authorization': 'Bearer $user2Token'});
        expect(response.statusCode, 200);
        fetched = shared_models.Task.fromJson(jsonDecode(await response.readAsString()));
        expect(fetched.id, task2ByUser1.id);
      });

      test('should return 404 if task not found or user not authorized', () async {
        final response = await handler.get('/tasks/non-existent-task', headers: {'Authorization': 'Bearer $user1Token'});
        expect(response.statusCode, 404);
      });
    });
    
    group('PUT /tasks/<id>', () {
      test('should update task successfully if creator', () async {
        final updateData = task1ByUser1.copyWith(title: "Updated Alpha Title");
        final response = await handler.put(
          '/tasks/${task1ByUser1.id}',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $user1Token'},
          body: jsonEncode(updateData.toJson()),
        );
        expect(response.statusCode, 200);
        final updated = shared_models.Task.fromJson(jsonDecode(await response.readAsString()));
        expect(updated.title, "Updated Alpha Title");
      });

      test('should return 403 Forbidden if user is not creator (even if assignee)', () async {
        // task2ByUser1 is created by user1, assigned to user2. User2 tries to update.
        final updateData = task2ByUser1.copyWith(title: "User2 Update Attempt");
         final response = await handler.put(
          '/tasks/${task2ByUser1.id}',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $user2Token'},
          body: jsonEncode(updateData.toJson()),
        );
        expect(response.statusCode, 403);
      });
    });

    group('DELETE /tasks/<id>', () {
       test('should delete task successfully if creator', () async {
        final response = await handler.delete(
          '/tasks/${task1ByUser1.id}',
          headers: {'Authorization': 'Bearer $user1Token'},
        );
        expect(response.statusCode, 200);
        
        final getResponse = await handler.get('/tasks/${task1ByUser1.id}', headers: {'Authorization': 'Bearer $user1Token'});
        expect(getResponse.statusCode, 404); // Task should be gone
      });
      test('should return 403 Forbidden if user is not creator for delete', () async {
         final response = await handler.delete(
          '/tasks/${task2ByUser1.id}', // Created by user1
          headers: {'Authorization': 'Bearer $user2Token'}, // user2 tries to delete
        );
        expect(response.statusCode, 403);
      });
    });

    group('GET /tasks (filtered list)', () {
      test('should return tasks assigned to user1 by default', async () {
        final response = await handler.get('/tasks', headers: {'Authorization': 'Bearer $user1Token'});
        expect(response.statusCode, 200);
        final tasks = (jsonDecode(await response.readAsString()) as List).map((t) => shared_models.Task.fromJson(t)).toList();
        expect(tasks.length, 1);
        expect(tasks.first.id, task1ByUser1.id);
      });
       test('should filter tasks by projectId for assignee user1', async () {
        final response = await handler.get('/tasks?projectId=${dbProject1.id}', headers: {'Authorization': 'Bearer $user1Token'});
        expect(response.statusCode, 200);
        final tasks = (jsonDecode(await response.readAsString()) as List).map((t) => shared_models.Task.fromJson(t)).toList();
        expect(tasks.length, 1); // task1ByUser1 is in dbProject1 and assigned to user1
        expect(tasks.first.id, task1ByUser1.id);
      });
    });

    group('GET /tasks/created-by-me', () {
       test('should return tasks created by user1', async () {
        final response = await handler.get('/tasks/created-by-me', headers: {'Authorization': 'Bearer $user1Token'});
        expect(response.statusCode, 200);
        final tasks = (jsonDecode(await response.readAsString()) as List).map((t) => shared_models.Task.fromJson(t)).toList();
        expect(tasks.length, 2);
        expect(tasks.any((t) => t.id == task1ByUser1.id), isTrue);
        expect(tasks.any((t) => t.id == task2ByUser1.id), isTrue);
      });
    });
    
    group('GET /tasks/project/<projectId>', () {
      test('should return all tasks for project1', async () {
        final response = await handler.get('/tasks/project/${dbProject1.id}', headers: {'Authorization': 'Bearer $user1Token'});
        expect(response.statusCode, 200);
        final tasks = (jsonDecode(await response.readAsString()) as List).map((t) => shared_models.Task.fromJson(t)).toList();
        expect(tasks.length, 2); // task1ByUser1, task2ByUser1
      });
       test('should filter tasks by assigneeId for project1', async () {
        final response = await handler.get('/tasks/project/${dbProject1.id}?assigneeId=${dbUser2.id}', headers: {'Authorization': 'Bearer $user1Token'});
        expect(response.statusCode, 200);
        final tasks = (jsonDecode(await response.readAsString()) as List).map((t) => shared_models.Task.fromJson(t)).toList();
        expect(tasks.length, 1); 
        expect(tasks.first.id, task2ByUser1.id);
      });
    });

    group('POST /tasks/<id>/assign', () {
      test('should assign task to user2', () async {
        final assignDto = TaskAssignRequestDto(assigneeId: dbUser2.id);
        final response = await handler.post(
          '/tasks/${task1ByUser1.id}/assign',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $user1Token'},
          body: jsonEncode(assignDto.toJson()),
        );
        expect(response.statusCode, 200);
        final updatedTask = shared_models.Task.fromJson(jsonDecode(await response.readAsString()));
        expect(updatedTask.assigneeId, dbUser2.id);
      });
       test('should return 404 if task to assign not found', () async {
        final assignDto = TaskAssignRequestDto(assigneeId: dbUser2.id);
        final response = await handler.post(
          '/tasks/fake-task-id/assign',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $user1Token'},
          body: jsonEncode(assignDto.toJson()),
        );
        expect(response.statusCode, 404);
      });
    });

    group('POST /tasks/<id>/status', () {
      test('should change task status', () async {
        final statusDto = TaskStatusChangeRequestDto(status: shared_models.TaskStatus.done);
        final response = await handler.post(
          '/tasks/${task1ByUser1.id}/status',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $user1Token'},
          body: jsonEncode(statusDto.toJson()),
        );
        expect(response.statusCode, 200);
        final updatedTask = shared_models.Task.fromJson(jsonDecode(await response.readAsString()));
        expect(updatedTask.status, shared_models.TaskStatus.done);
      });
       test('should return 404 if task to change status not found', () async {
        final statusDto = TaskStatusChangeRequestDto(status: shared_models.TaskStatus.done);
        final response = await handler.post(
          '/tasks/fake-task-id/status',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $user1Token'},
          body: jsonEncode(statusDto.toJson()),
        );
        expect(response.statusCode, 404);
      });
    });

  });
}
