import 'dart:convert';
import 'package:test/test.dart';
import 'package:shelf/shelf.dart';
import 'package:shared/src/models/project.dart' as shared;
import '../../lib/src/data/in_memory_database.dart';
import '../../lib/src/repositories/project_repository.dart';
import '../../lib/src/services/project_service.dart';
import '../../lib/src/routes/project_routes.dart';

void main() {
  late InMemoryDatabase db;
  late ProjectRepository repository;
  late ProjectService service;
  late ProjectRoutes routes;
  late Handler handler;

  setUp(() {
    db = InMemoryDatabase();
    repository = ProjectRepositoryImpl(db);
    service = ProjectServiceImpl(repository);
    routes = ProjectRoutes(service);
    handler = routes.router;
  });

  group('ProjectRoutes', () {
    test('GET /projects returns all projects', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: 'user1',
        memberIds: ['user1', 'user2'],
      );

      await service.createProject(project);

      final request = Request('GET', Uri.parse('http://localhost/projects'));
      final response = await handler(request);
      expect(response.statusCode, equals(200));
      expect(response.headers['content-type'], equals('application/json'));
      final body = await response.readAsString();
      final projects = (jsonDecode(body) as List)
          .map((p) => shared.Project.fromJson(p))
          .toList();
      expect(projects.length, equals(1));
      expect(projects.first.id, equals(project.id));
    });

    test('GET /projects/<id> returns a project by ID', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: 'user1',
        memberIds: ['user1', 'user2'],
      );

      await service.createProject(project);

      final request = Request('GET', Uri.parse('http://localhost/projects/1'));
      final response = await handler(request);
      expect(response.statusCode, equals(200));
      expect(response.headers['content-type'], equals('application/json'));
      final body = await response.readAsString();
      final foundProject = shared.Project.fromJson(jsonDecode(body));
      expect(foundProject.id, equals(project.id));
    });

    test('POST /projects creates a new project', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: 'user1',
        memberIds: ['user1', 'user2'],
      );

      final request = Request(
        'POST',
        Uri.parse('http://localhost/projects'),
        body: jsonEncode(project.toJson()),
        headers: {'content-type': 'application/json'},
      );
      final response = await handler(request);

      expect(response.statusCode, equals(200));
      expect(response.headers['content-type'], equals('application/json'));
      final body = await response.readAsString();
      final createdProject = shared.Project.fromJson(jsonDecode(body));
      expect(createdProject.id, equals(project.id));
    });

    test('PUT /projects/<id> updates a project', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: 'user1',
        memberIds: ['user1', 'user2'],
      );

      await service.createProject(project);

      final updatedProject = shared.Project(
        id: project.id,
        name: 'Updated Project',
        description: 'Updated Description',
        creatorId: 'user1',
        memberIds: ['user1', 'user3'],
      );

      final request = Request(
        'PUT',
        Uri.parse('http://localhost/projects/1'),
        body: jsonEncode(updatedProject.toJson()),
        headers: {'content-type': 'application/json'},
      );
      final response = await handler(request);

      expect(response.statusCode, equals(200));
      expect(response.headers['content-type'], equals('application/json'));
      final body = await response.readAsString();
      final result = shared.Project.fromJson(jsonDecode(body));
      expect(result.name, equals(updatedProject.name));
    });

    test('DELETE /projects/<id> deletes a project', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: 'user1',
        memberIds: ['user1', 'user2'],
      );

      await service.createProject(project);

      final request =
          Request('DELETE', Uri.parse('http://localhost/projects/1'));
      final response = await handler(request);
      expect(response.statusCode, equals(200));
      expect(response.readAsString(), completion(equals('Project deleted')));
    });
  });
}
