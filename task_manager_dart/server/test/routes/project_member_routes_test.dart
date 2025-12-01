import 'package:postgres/postgres.dart';
import 'package:test/test.dart';
import 'package:shelf/shelf.dart';
import '../../lib/src/routes/project_member_routes.dart';
import '../../lib/src/services/project_service.dart';
import '../../lib/src/services/jwt_service.dart';
import '../../lib/src/repositories/project_repository.dart';
import '../../lib/src/repositories/auth_repository.dart';
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
  late String otherUserId;

  setUpAll(() async {
    testBase = TestBase();
    await testBase.setUp();

    // Create a mock JWT service that always validates
    final jwtService = _MockJwtService();
    final authMiddleware = AuthMiddleware(jwtService);
    final projectRepository = ProjectRepositoryImpl(testBase.connection);
    final projectService = ProjectServiceImpl(projectRepository);

    final projectMemberRoutes = ProjectMemberRoutes(projectService, authMiddleware);
    
    // Wrap router with error handling middleware like in production
    handler = Pipeline()
        .addMiddleware(errorHandlingMiddleware())
        .addHandler(projectMemberRoutes.router.call);
    
    // Create test users
    testUserId = 'test_user_id';
    otherUserId = 'other_user_id';
    final authRepository = AuthRepository(testBase.connection);
    
    await testBase.connection.execute('''
      INSERT INTO users (id, display_name, email, password_hash, created_at)
      VALUES 
        ('$testUserId', 'Test User', 'test@example.com', 'hash', CURRENT_TIMESTAMP),
        ('$otherUserId', 'Other User', 'other@example.com', 'hash', CURRENT_TIMESTAMP)
    ''');
  });

  tearDownAll(() async {
    await testBase.tearDown();
  });

  setUp(() async {
    // Clear project data before each test
    await testBase.connection.execute('DELETE FROM project_assignments');
    await testBase.connection.execute('DELETE FROM projects');
  });

  group('ProjectMemberRoutes Integration Tests', () {
    test('GET /<projectId>/users should return project members', () async {
      // Create a test project
      await testBase.connection.execute('''
        INSERT INTO projects (id, name, description, creator_id)
        VALUES ('test_project', 'Test Project', 'Description', '$testUserId')
      ''');

      await testBase.connection.execute('''
        INSERT INTO project_assignments (id, project_id, user_id, assigned_by)
        VALUES 
          ('assign-1', 'test_project', '$testUserId', '$testUserId'),
          ('assign-2', 'test_project', '$otherUserId', '$testUserId')
      ''');

      final request = Request(
        'GET',
        Uri.parse('http://localhost/test_project/users'),
        headers: {'Authorization': 'Bearer mock_token'},
        context: {'userId': testUserId},
      );

      final response = await handler(request);

      expect(response.statusCode, 200);
      final body = await response.readAsString();
      final users = jsonDecode(body) as List;
      expect(users.length, 2);
    });

    test('POST /<projectId>/assign should assign user to project', () async {
      // Create a test project
      await testBase.connection.execute('''
        INSERT INTO projects (id, name, description, creator_id)
        VALUES ('test_project', 'Test Project', 'Description', '$testUserId')
      ''');

      await testBase.connection.execute('''
        INSERT INTO project_assignments (id, project_id, user_id, assigned_by)
        VALUES ('assign-1', 'test_project', '$testUserId', '$testUserId')
      ''');

      final request = Request(
        'POST',
        Uri.parse('http://localhost/test_project/assign'),
        headers: {
          'Authorization': 'Bearer mock_token',
          'content-type': 'application/json',
        },
        body: jsonEncode({'userId': otherUserId}),
        context: {'userId': testUserId},
      );

      final response = await handler(request);

      expect(response.statusCode, 201);
      
      // Verify assignment was created
      final result = await testBase.connection.execute(
        Sql.named('SELECT * FROM project_assignments WHERE project_id = @projectId AND user_id = @userId'),
        parameters: {
          'projectId': 'test_project',
          'userId': otherUserId,
        },
      );
      expect(result.isNotEmpty, isTrue);
    });

    test('DELETE /<projectId>/assign/<userId> should remove user from project', () async {
      // Create a test project with two members
      await testBase.connection.execute('''
        INSERT INTO projects (id, name, description, creator_id)
        VALUES ('test_project', 'Test Project', 'Description', '$testUserId')
      ''');

      await testBase.connection.execute('''
        INSERT INTO project_assignments (id, project_id, user_id, assigned_by)
        VALUES 
          ('assign-1', 'test_project', '$testUserId', '$testUserId'),
          ('assign-2', 'test_project', '$otherUserId', '$testUserId')
      ''');

      final request = Request(
        'DELETE',
        Uri.parse('http://localhost/test_project/assign/$otherUserId'),
        headers: {'Authorization': 'Bearer mock_token'},
        context: {'userId': testUserId},
      );

      final response = await handler(request);

      expect(response.statusCode, 204);
      
      // Verify assignment was removed
      final result = await testBase.connection.execute(
        Sql.named('SELECT * FROM project_assignments WHERE project_id = @projectId AND user_id = @userId'),
        parameters: {
          'projectId': 'test_project',
          'userId': otherUserId,
        },
      );
      expect(result.isEmpty, isTrue);
    });

    test('POST /<projectId>/assign should fail for unauthorized user', () async {
      // Create a test project owned by otherUser
      await testBase.connection.execute('''
        INSERT INTO projects (id, name, description, creator_id)
        VALUES ('test_project', 'Test Project', 'Description', '$otherUserId')
      ''');

      await testBase.connection.execute('''
        INSERT INTO project_assignments (id, project_id, user_id, assigned_by)
        VALUES ('assign-1', 'test_project', '$otherUserId', '$otherUserId')
      ''');

      final request = Request(
        'POST',
        Uri.parse('http://localhost/test_project/assign'),
        headers: {
          'Authorization': 'Bearer mock_token',
          'content-type': 'application/json',
        },
        body: jsonEncode({'userId': testUserId}),
        context: {'userId': testUserId}, // testUser is not a member
      );

      final response = await handler(request);

      expect(response.statusCode, 403);
    });
  });
}

