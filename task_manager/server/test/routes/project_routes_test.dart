import 'dart:convert';
import 'package:test/test.dart';
import 'package:shelf/shelf.dart';
import 'package:shared/models.dart';
import '../../lib/src/routes/project_routes.dart';
import '../../lib/src/services/project_service.dart';
import '../../lib/src/repositories/project_repository.dart';
import '../../lib/src/middleware/auth_middleware.dart';
import '../../lib/src/services/jwt_service.dart';
import '../helpers/test_base.dart';

void main() {
  group('ProjectRoutes Integration Tests', () {
    late TestBase testBase;
    late ProjectService projectService;
    late ProjectRepository projectRepository;
    late ProjectRoutes projectRoutes;
    
    const testUserId = 'test_user_123';

    setUpAll(() async {
      testBase = TestBase();
      await testBase.setUp();
      
      // Set up real services with container database
      projectRepository = ProjectRepositoryImpl(testBase.connection);
      projectService = ProjectServiceImpl(projectRepository);
      
      // Create a simple mock middleware for testing
      final mockJwtService = MockJwtService();
      final mockAuthMiddleware = AuthMiddleware(mockJwtService);
      
      projectRoutes = ProjectRoutes(projectService, mockAuthMiddleware);
    });

    tearDownAll(() async {
      await testBase.tearDown();
    });

    setUp(() async {
      // Clear all tables before each test
      await testBase.connection.execute('DELETE FROM project_members');
      await testBase.connection.execute('DELETE FROM projects');
      await testBase.connection.execute('DELETE FROM users');
      
      // Create test users
      await testBase.connection.execute('''
        INSERT INTO users (id, display_name, email, created_at)
        VALUES 
          ('$testUserId', 'Test User', 'test@example.com', NOW()::TEXT),
          ('other_user', 'Other User', 'other@example.com', NOW()::TEXT)
      ''');
    });

    group('GET /projects', () {
      test('should return empty list when no projects exist', () async {
        final request = Request(
          'GET', 
          Uri.parse('http://localhost/projects'),
          headers: {'Authorization': 'Bearer mock_token'},
        );
        final response = await projectRoutes.router.call(request);
        
        expect(response.statusCode, equals(200));
        final body = await response.readAsString();
        final List<dynamic> projects = jsonDecode(body);
        expect(projects, isEmpty);
      });

      test('should return projects when they exist', () async {
        // Create test projects and memberships
        await testBase.connection.execute('''
          INSERT INTO projects (id, name, description, creator_id)
          VALUES 
            ('1', 'Test Project 1', 'Description 1', '$testUserId'),
            ('2', 'Test Project 2', 'Description 2', 'other_user')
        ''');
        
        await testBase.connection.execute('''
          INSERT INTO project_members (project_id, user_id)
          VALUES 
            ('1', '$testUserId'),
            ('2', 'other_user')
        ''');

        final request = Request(
          'GET', 
          Uri.parse('http://localhost/projects'),
          headers: {'Authorization': 'Bearer mock_token'},
        );
        final response = await projectRoutes.router.call(request);
        
        expect(response.statusCode, equals(200));
        final body = await response.readAsString();
        final List<dynamic> projects = jsonDecode(body);
        expect(projects, hasLength(2));
      });
    });

    group('POST /projects', () {
      test('should create project successfully', () async {
        final projectData = {
          'name': 'New Project',
          'description': 'A new project description',
        };

        final request = Request(
          'POST',
          Uri.parse('http://localhost/projects'),
          body: jsonEncode(projectData),
          headers: {
            'content-type': 'application/json',
            'Authorization': 'Bearer mock_token',
          },
        );
        final response = await projectRoutes.router.call(request);
        
        expect(response.statusCode, equals(200));
        final body = await response.readAsString();
        final Map<String, dynamic> createdProject = jsonDecode(body);
        expect(createdProject['name'], equals('New Project'));
        expect(createdProject['description'], equals('A new project description'));
        expect(createdProject['creatorId'], equals(testUserId));
      });
    });

    group('GET /projects/<id>', () {
      test('should return project when user has access', () async {
        await testBase.connection.execute('''
          INSERT INTO projects (id, name, description, creator_id)
          VALUES ('test_project_id', 'Accessible Project', 'User has access', '$testUserId')
        ''');
        
        await testBase.connection.execute('''
          INSERT INTO project_members (project_id, user_id)
          VALUES ('test_project_id', '$testUserId')
        ''');

        final request = Request(
          'GET', 
          Uri.parse('http://localhost/projects/test_project_id'),
          headers: {'Authorization': 'Bearer mock_token'},
        );
        final response = await projectRoutes.router.call(request);
        
        expect(response.statusCode, equals(200));
        final body = await response.readAsString();
        final Map<String, dynamic> project = jsonDecode(body);
        expect(project['id'], equals('test_project_id'));
        expect(project['name'], equals('Accessible Project'));
      });

      test('should return 404 when project not found', () async {
        final request = Request(
          'GET', 
          Uri.parse('http://localhost/projects/nonexistent_id'),
          headers: {'Authorization': 'Bearer mock_token'},
        );
        final response = await projectRoutes.router.call(request);
        
        expect(response.statusCode, equals(404));
      });
    });

    group('Error handling', () {
      test('should handle malformed JSON in POST request', () async {
        final request = Request(
          'POST',
          Uri.parse('http://localhost/projects'),
          body: 'invalid json',
          headers: {
            'content-type': 'application/json',
            'Authorization': 'Bearer mock_token',
          },
        );
        final response = await projectRoutes.router.call(request);
        
        expect(response.statusCode, equals(500));
      });
    });
  });
}

// Simple mock JWT service for testing
class MockJwtService implements JwtService {
  @override
  String generateToken(User user) => 'mock_token_${user.id}';

  @override
  Map<String, dynamic>? validateToken(String token) {
    if (token == 'mock_token') {
      return {
        'sub': 'test_user_123',
        'email': 'test@example.com',
        'displayName': 'Test User',
      };
    }
    return null;
  }

  @override
  String? getUserIdFromToken(String token) {
    final payload = validateToken(token);
    return payload?['sub'] as String?;
  }
} 