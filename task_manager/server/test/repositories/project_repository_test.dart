import 'package:test/test.dart';
import 'package:shared/src/models/project.dart' as shared;
import '../../lib/src/data/in_memory_database.dart';
import '../../lib/src/repositories/project_repository.dart';

void main() {
  late InMemoryDatabase db;
  late ProjectRepository repository;

  setUp(() {
    db = InMemoryDatabase();
    repository = ProjectRepositoryImpl(db);
  });

  group('ProjectRepository', () {
    test('creates a new project', () async {
      final project = shared.Project(
        id: '1',
        name: 'Test Project',
        description: 'Test Description',
        creatorId: 'user1',
        memberIds: ['user1', 'user2'],
      );

      final createdProject = await repository.create(project);
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

      await repository.create(project);
      final foundProject = await repository.findById(project.id);
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

      await repository.create(project);
      final updatedProject = shared.Project(
        id: project.id,
        name: 'Updated Project',
        description: 'Updated Description',
        creatorId: 'user1',
        memberIds: ['user1', 'user3'],
      );

      final result = await repository.update(updatedProject);
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

      await repository.create(project);
      await repository.delete(project.id);
      final foundProject = await repository.findById(project.id);
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
      await repository.create(project1);
      await repository.create(project2);
      final allProjects = await repository.findAll();
      expect(allProjects.length, equals(2));
      expect(allProjects.map((p) => p.id), containsAll(['1', '2']));
    });
  });
}
