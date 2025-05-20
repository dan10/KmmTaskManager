import 'package:test/test.dart';
import 'package:shared/src/models/project.dart' as shared;
import '../../lib/src/data/in_memory_database.dart';
import '../../lib/src/repositories/project_repository.dart';
import '../../lib/src/services/project_service.dart';

void main() {
  late InMemoryDatabase db;
  late ProjectRepository repository;
  late ProjectService service;

  setUp(() {
    db = InMemoryDatabase();
    repository = ProjectRepositoryImpl(db);
    service = ProjectServiceImpl(repository);
  });

  group('ProjectService', () {
    test('creates a new project', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: 'user1',
        memberIds: ['user1', 'user2'],
      );

      final createdProject = await service.createProject(project);
      expect(createdProject.id, equals(project.id));
      expect(createdProject.name, equals(project.name));
      expect(createdProject.memberIds, containsAll(['user1', 'user2']));
    });

    test('finds a project by id', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: 'user1',
        memberIds: ['user1', 'user2'],
      );

      await service.createProject(project);
      final foundProject = await service.getProjectById(project.id);
      expect(foundProject, isNotNull);
      expect(foundProject?.id, equals(project.id));
    });

    test('updates a project', () async {
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

      final result = await service.updateProject(updatedProject);
      expect(result.name, equals(updatedProject.name));
      expect(result.memberIds, contains('user3'));
    });

    test('deletes a project', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: 'user1',
        memberIds: ['user1', 'user2'],
      );

      await service.createProject(project);
      await service.deleteProject(project.id);
      final foundProject = await service.getProjectById(project.id);
      expect(foundProject, isNull);
    });

    test('finds all projects', () async {
      final project1 = shared.Project(
        id: '1',
        name: 'Project 1',
        description: 'Desc 1',
        creatorId: 'user1',
        memberIds: ['user1'],
      );
      final project2 = shared.Project(
        id: '2',
        name: 'Project 2',
        description: 'Desc 2',
        creatorId: 'user2',
        memberIds: ['user2'],
      );
      await service.createProject(project1);
      await service.createProject(project2);
      final allProjects = await service.getAllProjects();
      expect(allProjects.length, equals(2));
      expect(allProjects.map((p) => p.id), containsAll(['1', '2']));
    });
  });
}
