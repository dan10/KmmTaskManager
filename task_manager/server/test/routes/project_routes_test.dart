import 'dart:convert';
import 'package:shelf_test_handler/shelf_test_handler.dart';
import 'package:test/test.dart';
import 'package:shelf/shelf.dart' as shelf;
import 'package:mockito/mockito.dart';
import 'package:shared/models.dart' as shared_models;

import '../../../lib/src/config/app_config.dart';
import '../../../lib/src/data/database.dart';
import '../../../lib/src/repositories/project_repository.dart';
import '../../../lib/src/repositories/auth_repository.dart'; // For creating users
import '../../../lib/src/services/project_service.dart';
import '../../../lib/src/services/jwt_service.dart';
import '../../../lib/src/services/auth_service.dart'; // For creating users
import '../../../lib/src/routes/project_routes.dart';
import '../../../lib/src/middleware/auth_middleware.dart';
import '../../../lib/src/middleware/error_handling_middleware.dart';
import '../../../lib/src/dto/project/project_assign_request_dto.dart';
import '../../../lib/src/dto/error_response_dto.dart';
import '../../../lib/src/exceptions/custom_exceptions.dart';

import '../services/auth_service_test.mocks.dart'; // Use AppConfig mock

// Helper to create a test user directly in the database
Future<shared_models.User> createTestUserInDb(AuthRepository repo, String id, String name, String email, String password) async {
  final hashedPassword = sha256.convert(utf8.encode(password)).toString();
  final user = shared_models.User(id: id, name: name, email: email, passwordHash: hashedPassword);
  return repo.createUser(user);
}


void main() {
  late Database db;
  late ProjectRepository projectRepository;
  late AuthRepository authRepository; // For user setup
  late ProjectService projectService;
  late JwtService jwtService;
  late AuthMiddleware authMiddleware;
  late shelf.Pipeline pipeline;
  late MockAppConfig mockAppConfig;
  late AuthService authService; // For user setup

  late String testUserToken;
  late String testUser2Token;
  late shared_models.User user1;
  late shared_models.User user2;


  setUpAll(() async {
    db = InMemoryDatabase(); // Use InMemoryDatabase for all tests in this file
    projectRepository = ProjectRepository(db);
    authRepository = AuthRepository(db);
    
    mockAppConfig = MockAppConfig();
    when(mockAppConfig.jwtSecret).thenReturn('test-super-secret-key-for-jwt-project-routes');
    // No need to mock googleClientId for these tests unless ProjectService uses it.

    jwtService = JwtService(mockAppConfig);
    projectService = ProjectServiceImpl(projectRepository);
    // authService is needed to create users and generate tokens for tests
    authService = AuthServiceImpl(authRepository, jwtService, mockAppConfig); 

    authMiddleware = AuthMiddleware(jwtService);
    
    final projectRoutes = ProjectRoutes(projectService, authMiddleware).router;
    pipeline = const shelf.Pipeline()
        .addMiddleware(errorHandlingMiddleware())
        // Auth middleware is applied selectively in routes, so not globally here for all tests.
        // Instead, individual handlers will be wrapped, or routes are set up with it.
        // The ProjectRoutes constructor already takes authMiddleware.
        .addHandler(projectRoutes);

    // Create test users and tokens
    user1 = await createTestUserInDb(authRepository, 'user1-id', 'User One', 'user1@example.com', 'password123');
    user2 = await createTestUserInDb(authRepository, 'user2-id', 'User Two', 'user2@example.com', 'password123');
    testUserToken = jwtService.generateToken(user1);
    testUser2Token = jwtService.generateToken(user2);
  });


  group('ProjectRoutes', () {
    late ShelfTestHandler handler;

    setUp(() {
       handler = ShelfTestHandler(pipeline);
       // Clear projects before each test, users are stable for the group
       (db as InMemoryDatabase).projects.clear();
       (db as InMemoryDatabase).projectMembers.clear();
    });
    
    final project1Data = shared_models.Project(
      id: 'proj-1',
      name: 'First Project',
      description: 'My first awesome project',
      creatorId: 'user1-id', // Will be overridden by context in create
      memberIds: [],       // Will be overridden by context in create
    );

    group('POST /projects', () {
      test('should create a new project successfully', () async {
        final response = await handler.post(
          '/projects',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer $testUserToken',
          },
          body: jsonEncode(project1Data.toJson()..remove('id')..remove('creatorId')..remove('memberIds')), // Client shouldn't send id/creatorId
        );
        expect(response.statusCode, 200);
        final created = shared_models.Project.fromJson(jsonDecode(await response.readAsString()));
        expect(created.name, project1Data.name);
        expect(created.creatorId, user1.id);
        expect(created.memberIds, isEmpty); // No members added by default on create currently
      });

      test('should return 400 for project with empty name', () async {
         final response = await handler.post(
          '/projects',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer $testUserToken',
          },
          body: jsonEncode(project1Data.copyWith(name: '').toJson()..remove('id')..remove('creatorId')),
        );
        expect(response.statusCode, 400);
        final error = ErrorResponseDto.fromJson(jsonDecode(await response.readAsString()));
        expect(error.message, 'Project name is required.');
      });
    });

    group('GET /projects (user-specific, paginated, searchable)', () {
      setUp(() async {
        // Create a few projects for user1
        await projectRepository.create(shared_models.Project(id: 'p1', name: 'Alpha Project', description: 'User1 Desc Alpha', creatorId: user1.id, memberIds: [user1.id]));
        await projectRepository.create(shared_models.Project(id: 'p2', name: 'Beta Searchable', description: 'User1 Desc Beta', creatorId: user1.id, memberIds: [user1.id]));
        // Project for user2, user1 is a member
        await projectRepository.create(shared_models.Project(id: 'p3', name: 'Gamma Other User', description: 'User2 Project, User1 member', creatorId: user2.id, memberIds: [user2.id, user1.id]));
        // Another project for user1, but name won't match search
        await projectRepository.create(shared_models.Project(id: 'p4', name: 'Delta Project', description: 'User1 Desc Delta', creatorId: user1.id, memberIds: [user1.id]));
      });

      test('should return paginated projects for user1', () async {
        final response = await handler.get(
          '/projects?page=0&size=2',
          headers: {'Authorization': 'Bearer $testUserToken'},
        );
        expect(response.statusCode, 200);
        final projects = (jsonDecode(await response.readAsString()) as List).map((p) => shared_models.Project.fromJson(p)).toList();
        expect(projects.length, 2);
        expect(projects[0].name, 'Alpha Project'); // Default order is by name
        expect(projects[1].name, 'Beta Searchable');
      });
      
      test('should return user1 projects matching search query "Searchable"', () async {
        final response = await handler.get(
          '/projects?query=Searchable',
          headers: {'Authorization': 'Bearer $testUserToken'},
        );
        expect(response.statusCode, 200);
        final projects = (jsonDecode(await response.readAsString()) as List).map((p) => shared_models.Project.fromJson(p)).toList();
        expect(projects.length, 1);
        expect(projects.first.name, 'Beta Searchable');
      });

       test('should return user1 projects including those where user1 is a member', () async {
        final response = await handler.get(
          '/projects?page=0&size=10', // Get all for user1
          headers: {'Authorization': 'Bearer $testUserToken'},
        );
        expect(response.statusCode, 200);
        final projects = (jsonDecode(await response.readAsString()) as List).map((p) => shared_models.Project.fromJson(p)).toList();
        expect(projects.length, 4); // p1, p2, p3, p4
        expect(projects.any((p)=> p.id == 'p3'), isTrue, reason: "Should include project p3 where user1 is a member");
      });
    });

    group('GET /projects/all (system-wide, paginated, searchable)', () {
       setUp(() async {
        // Create projects for user1
        await projectRepository.create(shared_models.Project(id: 'sys-p1', name: 'System Alpha', description: 'Sys Desc Alpha', creatorId: user1.id, memberIds: [user1.id]));
        await projectRepository.create(shared_models.Project(id: 'sys-p2', name: 'System Beta Common', description: 'Sys Desc Beta', creatorId: user1.id, memberIds: [user1.id]));
        // Create project for user2
        await projectRepository.create(shared_models.Project(id: 'sys-p3', name: 'Another System Project', description: 'Sys Desc Gamma Common', creatorId: user2.id, memberIds: [user2.id]));
      });

      test('should return all system projects paginated', () async {
         final response = await handler.get(
          '/projects/all?page=0&size=2',
          headers: {'Authorization': 'Bearer $testUserToken'}, // Any authenticated user
        );
        expect(response.statusCode, 200);
        final projects = (jsonDecode(await response.readAsString()) as List).map((p) => shared_models.Project.fromJson(p)).toList();
        expect(projects.length, 2);
         // Order is by name: Another, System Alpha
        expect(projects[0].name, 'Another System Project');
        expect(projects[1].name, 'System Alpha');
      });

      test('should return all system projects matching search query "Common"', () async {
        final response = await handler.get(
          '/projects/all?query=Common',
          headers: {'Authorization': 'Bearer $testUserToken'},
        );
        expect(response.statusCode, 200);
        final projects = (jsonDecode(await response.readAsString()) as List).map((p) => shared_models.Project.fromJson(p)).toList();
        expect(projects.length, 2); // sys-p2 and sys-p3
        expect(projects.any((p)=> p.name == 'System Beta Common'), isTrue);
        expect(projects.any((p)=> p.name == 'Another System Project'), isTrue);
      });
    });

    group('GET /projects/<id>', () {
      late shared_models.Project createdProject;
      setUp(() async {
        // Create a project for these tests
        final createResponse = await handler.post(
          '/projects',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer $testUserToken',
          },
          body: jsonEncode(project1Data.toJson()..remove('id')..remove('creatorId')..remove('memberIds')),
        );
        expect(createResponse.statusCode, 200);
        createdProject = shared_models.Project.fromJson(jsonDecode(await createResponse.readAsString()));
      });

      test('should return a project by ID if creator', () async {
        final response = await handler.get(
          '/projects/${createdProject.id}',
          headers: {'Authorization': 'Bearer $testUserToken'},
        );
        expect(response.statusCode, 200);
        final fetched = shared_models.Project.fromJson(jsonDecode(await response.readAsString()));
        expect(fetched.id, createdProject.id);
      });

      test('should return 404 if project not found', () async {
        final response = await handler.get(
          '/projects/non-existent-id',
          headers: {'Authorization': 'Bearer $testUserToken'},
        );
        expect(response.statusCode, 404);
      });
      
      test('should return 404 if user is not creator or member (for getById)', () async {
        // User2 is not creator or member of createdProject
        final response = await handler.get(
          '/projects/${createdProject.id}',
          headers: {'Authorization': 'Bearer $testUser2Token'},
        );
        expect(response.statusCode, 404); // Because ProjectService.getById returns null
        final error = ErrorResponseDto.fromJson(jsonDecode(await response.readAsString()));
        expect(error.message, contains('not found or user not authorized'));
      });
    });
    
    group('PUT /projects/<id>', () {
      late shared_models.Project createdProject;
      setUp(() async {
        final createResponse = await handler.post(
          '/projects',
          headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer $testUserToken'},
          body: jsonEncode(project1Data.toJson()..remove('id')..remove('creatorId')..remove('memberIds')),
        );
        createdProject = shared_models.Project.fromJson(jsonDecode(await createResponse.readAsString()));
      });

      test('should update a project successfully by creator', () async {
        final updateData = createdProject.copyWith(name: 'Updated Name', description: 'New Desc');
        final response = await handler.put(
          '/projects/${createdProject.id}',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $testUserToken'},
          body: jsonEncode(updateData.toJson()),
        );
        expect(response.statusCode, 200);
        final updated = shared_models.Project.fromJson(jsonDecode(await response.readAsString()));
        expect(updated.name, 'Updated Name');
        expect(updated.description, 'New Desc');
      });

      test('should return 403 Forbidden if non-creator tries to update', () async {
         final updateData = createdProject.copyWith(name: 'Attempted Update');
         final response = await handler.put(
          '/projects/${createdProject.id}',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $testUser2Token'}, // User 2
          body: jsonEncode(updateData.toJson()),
        );
        expect(response.statusCode, 403);
      });
      
       test('should return 404 if project to update not found', () async {
         final updateData = createdProject.copyWith(name: 'Attempted Update');
         final response = await handler.put(
          '/projects/fake-id',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $testUserToken'},
          body: jsonEncode(updateData.toJson()),
        );
        expect(response.statusCode, 404);
      });
    });

     group('DELETE /projects/<id>', () {
      late shared_models.Project projectToDelete;
      setUp(() async {
        final createResponse = await handler.post(
          '/projects',
          headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer $testUserToken'},
          body: jsonEncode(project1Data.copyWith(name: "To Delete").toJson()..remove('id')..remove('creatorId')..remove('memberIds')),
        );
        projectToDelete = shared_models.Project.fromJson(jsonDecode(await createResponse.readAsString()));
      });

      test('should delete a project successfully by creator', () async {
        final response = await handler.delete(
          '/projects/${projectToDelete.id}',
          headers: {'Authorization': 'Bearer $testUserToken'},
        );
        expect(response.statusCode, 200); // okJsonResponse for message
        final body = jsonDecode(await response.readAsString());
        expect(body['message'], 'Project deleted successfully');

        // Verify it's gone
        final getResponse = await handler.get(
          '/projects/${projectToDelete.id}',
          headers: {'Authorization': 'Bearer $testUserToken'},
        );
        expect(getResponse.statusCode, 404);
      });

       test('should return 403 Forbidden if non-creator tries to delete', () async {
         final response = await handler.delete(
          '/projects/${projectToDelete.id}',
          headers: {'Authorization': 'Bearer $testUser2Token'}, // User 2
        );
        expect(response.statusCode, 403);
      });

       test('should return 404 if project to delete not found', () async {
         final response = await handler.delete(
          '/projects/fake-id-to-delete',
          headers: {'Authorization': 'Bearer $testUserToken'},
        );
        expect(response.statusCode, 404);
      });
    });

    // TODO: Add tests for GET /projects (pagination, search by query for user's projects)
    // TODO: Add tests for GET /projects/all (pagination, search by query for all system projects)

    group('POST /projects/<id>/assign & DELETE /projects/<id>/assign/<userId>', () {
      late shared_models.Project projectForAssignment;
      
      setUp(() async {
        // Create a project owned by user1
        final createDto = shared_models.Project(id: 'assign-proj', name: 'Assignment Test Project', description: '', creatorId: user1.id, memberIds: [user1.id]);
        await projectRepository.create(createDto); // Direct repo call for setup simplicity
        projectForAssignment = (await projectRepository.findById(createDto.id))!;
      });

      test('POST /assign should assign a user to a project', () async {
        final assignDto = ProjectAssignRequestDto(userId: user2.id);
        final response = await handler.post(
          '/projects/${projectForAssignment.id}/assign',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $testUserToken'},
          body: jsonEncode(assignDto.toJson()),
        );
        expect(response.statusCode, 200);
        final body = jsonDecode(await response.readAsString());
        expect(body['userId'], user2.id);

        final updatedProject = await projectRepository.findById(projectForAssignment.id);
        expect(updatedProject!.memberIds, contains(user2.id));
      });

      test('POST /assign should return 404 if project not found', () async {
         final assignDto = ProjectAssignRequestDto(userId: user2.id);
         final response = await handler.post(
          '/projects/fake-project-id/assign',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $testUserToken'},
          body: jsonEncode(assignDto.toJson()),
        );
        expect(response.statusCode, 404);
      });
      
      test('POST /assign should return 404 if user to assign not found', () async {
         final assignDto = ProjectAssignRequestDto(userId: 'fake-user-id');
         final response = await handler.post(
          '/projects/${projectForAssignment.id}/assign',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $testUserToken'},
          body: jsonEncode(assignDto.toJson()),
        );
        expect(response.statusCode, 404);
      });

      test('POST /assign should return 409 if user already assigned', () async {
        // Assign user2 first
        await projectService.assignUserToProject(projectForAssignment.id, user2.id);
        
        final assignDto = ProjectAssignRequestDto(userId: user2.id);
        final response = await handler.post(
          '/projects/${projectForAssignment.id}/assign',
          headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $testUserToken'},
          body: jsonEncode(assignDto.toJson()),
        );
        expect(response.statusCode, 409);
      });
      
      test('DELETE /assign should remove a user from a project', () async {
        // Assign user2 first
        await projectService.assignUserToProject(projectForAssignment.id, user2.id);
        var updatedProject = await projectRepository.findById(projectForAssignment.id);
        expect(updatedProject!.memberIds, contains(user2.id));


        final response = await handler.delete(
          '/projects/${projectForAssignment.id}/assign/${user2.id}',
          headers: {'Authorization': 'Bearer $testUserToken'},
        );
        expect(response.statusCode, 204);

        updatedProject = await projectRepository.findById(projectForAssignment.id);
        expect(updatedProject!.memberIds, isNot(contains(user2.id)));
      });
      
      test('DELETE /assign should return 404 if user not on project (or project/user not found)', () async {
         final response = await handler.delete(
          '/projects/${projectForAssignment.id}/assign/non-member-user-id',
          headers: {'Authorization': 'Bearer $testUserToken'},
        );
        expect(response.statusCode, 404);
      });
    });

    group('GET /projects/<id>/users', () {
       late shared_models.Project projectWithMembers;
       setUp(() async {
        final createDto = shared_models.Project(id: 'members-proj', name: 'Members Test Project', description: '', creatorId: user1.id, memberIds: [user1.id]);
        await projectRepository.create(createDto);
        projectWithMembers = (await projectRepository.findById(createDto.id))!;
        await projectService.assignUserToProject(projectWithMembers.id, user2.id);
      });

      test('should return list of users for a project', () async {
        final response = await handler.get(
          '/projects/${projectWithMembers.id}/users',
          headers: {'Authorization': 'Bearer $testUserToken'},
        );
        expect(response.statusCode, 200);
        final users = (jsonDecode(await response.readAsString()) as List)
            .map((u) => shared_models.User.fromJson(u)) // Assuming UserPublicResponseDto is compatible
            .toList();
        expect(users.length, 2); // user1 (creator) and user2
        expect(users.any((u) => u.id == user1.id), isTrue);
        expect(users.any((u) => u.id == user2.id), isTrue);
      });
      
      test('should return 404 if project for users not found', () async {
        final response = await handler.get(
          '/projects/fake-project-id/users',
          headers: {'Authorization': 'Bearer $testUserToken'},
        );
        expect(response.statusCode, 404);
      });
    });

    group('GET /projects/user/<userId>', () {
      late shared_models.Project projectForUser1;
      late shared_models.Project projectForUser2;

      setUp(() async {
        // Project created by user1
        final p1Dto = shared_models.Project(id: 'p1-user-test', name: 'User1 Project', description: '', creatorId: user1.id, memberIds: [user1.id]);
        await projectRepository.create(p1Dto);
        projectForUser1 = (await projectRepository.findById(p1Dto.id))!;

        // Project created by user2, user1 is a member
        final p2Dto = shared_models.Project(id: 'p2-user-test', name: 'User2 Project with User1 member', description: '', creatorId: user2.id, memberIds: [user2.id, user1.id]);
        await projectRepository.create(p2Dto);
        projectForUser2 = (await projectRepository.findById(p2Dto.id))!;
      });

      test('should return projects for a given user (creator or member)', () async {
        final response = await handler.get(
          '/projects/user/${user1.id}',
          headers: {'Authorization': 'Bearer $testUserToken'}, // Auth user doesn't matter as much as path userId
        );
        expect(response.statusCode, 200);
        final projects = (jsonDecode(await response.readAsString()) as List)
            .map((p) => shared_models.Project.fromJson(p))
            .toList();
        expect(projects.length, 2);
        expect(projects.any((p) => p.id == projectForUser1.id), isTrue);
        expect(projects.any((p) => p.id == projectForUser2.id), isTrue);
      });
      
       test('should return empty list if user has no projects', () async {
         // Create a user3 who has no projects
        final user3 = await createTestUserInDb(authRepository, 'user3-id', 'User Three', 'user3@example.com', 'password123');
        final user3Token = jwtService.generateToken(user3);

        final response = await handler.get(
          '/projects/user/${user3.id}',
          headers: {'Authorization': 'Bearer $user3Token'},
        );
        expect(response.statusCode, 200);
        final projects = jsonDecode(await response.readAsString()) as List;
        expect(projects, isEmpty);
      });

      test('should return 404 if user for projects not found', () async {
        final response = await handler.get(
          '/projects/user/fake-user-id',
          headers: {'Authorization': 'Bearer $testUserToken'},
        );
        expect(response.statusCode, 404);
      });
    });

  });
}
