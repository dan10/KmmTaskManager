import 'package:test/test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:postgres/postgres.dart';
import 'package:shared/src/models/task.dart';
import '../lib/src/repositories/task_repository.dart';
import '../lib/src/repositories/task_repository_impl.dart';

@GenerateMocks([PostgreSQLConnection])
void main() {
  late TaskRepository repository;
  late MockPostgreSQLConnection mockDb;

  setUp(() {
    mockDb = MockPostgreSQLConnection();
    repository = TaskRepositoryImpl(mockDb);
  });

  group('TaskRepository', () {
    test('findAllByProjectId returns list of tasks', () async {
      final mockResults = [
        {
          'tasks': {
            'id': '1',
            'title': 'Test Task',
            'description': 'Test Description',
            'status': 'TODO',
            'priority': 0,
            'project_id': '1',
            'assignee_id': null,
            'creator_id': '1',
            'due_date': null,
          },
        },
      ];

      when(
        mockDb.mappedResultsQuery(
          any,
          substitutionValues: anyNamed('substitutionValues'),
        ),
      ).thenAnswer((_) async => mockResults);

      final tasks = await repository.findAllByProjectId('1');

      expect(tasks.length, 1);
      expect(tasks.first.title, 'Test Task');
      expect(tasks.first.description, 'Test Description');
      expect(tasks.first.isCompleted, false);
    });

    test('findById returns task when found', () async {
      final mockResults = [
        {
          'tasks': {
            'id': '1',
            'title': 'Test Task',
            'description': 'Test Description',
            'status': 'TODO',
            'priority': 0,
            'project_id': '1',
            'assignee_id': null,
            'creator_id': '1',
            'due_date': null,
          },
        },
      ];

      when(
        mockDb.mappedResultsQuery(
          any,
          substitutionValues: anyNamed('substitutionValues'),
        ),
      ).thenAnswer((_) async => mockResults);

      final task = await repository.findById('1');

      expect(task, isNotNull);
      expect(task!.title, 'Test Task');
      expect(task.description, 'Test Description');
    });

    test('findById returns null when not found', () async {
      when(
        mockDb.mappedResultsQuery(
          any,
          substitutionValues: anyNamed('substitutionValues'),
        ),
      ).thenAnswer((_) async => []);

      final task = await repository.findById('1');

      expect(task, isNull);
    });

    test('create returns created task', () async {
      final mockResults = [
        {
          'tasks': {
            'id': '1',
            'title': 'New Task',
            'description': 'New Description',
            'status': 'TODO',
            'priority': 0,
            'project_id': '1',
            'assignee_id': null,
            'creator_id': '1',
            'due_date': null,
          },
        },
      ];

      when(
        mockDb.mappedResultsQuery(
          any,
          substitutionValues: anyNamed('substitutionValues'),
        ),
      ).thenAnswer((_) async => mockResults);

      final task = Task(
        id: '1',
        title: 'New Task',
        description: 'New Description',
        creatorId: '1',
        projectId: '1',
      );

      final createdTask = await repository.create(task);

      expect(createdTask.title, 'New Task');
      expect(createdTask.description, 'New Description');
    });

    test('update returns updated task', () async {
      final mockResults = [
        {
          'tasks': {
            'id': '1',
            'title': 'Updated Task',
            'description': 'Updated Description',
            'status': 'DONE',
            'priority': 1,
            'project_id': '1',
            'assignee_id': '2',
            'creator_id': '1',
            'due_date': null,
          },
        },
      ];

      when(
        mockDb.mappedResultsQuery(
          any,
          substitutionValues: anyNamed('substitutionValues'),
        ),
      ).thenAnswer((_) async => mockResults);

      final task = Task(
        id: '1',
        title: 'Updated Task',
        description: 'Updated Description',
        isCompleted: true,
        priority: 1,
        creatorId: '1',
        projectId: '1',
        assigneeId: '2',
      );

      final updatedTask = await repository.update('1', task);

      expect(updatedTask, isNotNull);
      expect(updatedTask!.title, 'Updated Task');
      expect(updatedTask.description, 'Updated Description');
      expect(updatedTask.isCompleted, true);
      expect(updatedTask.priority, 1);
    });

    test('delete returns true when task is deleted', () async {
      when(
        mockDb.execute(any, substitutionValues: anyNamed('substitutionValues')),
      ).thenAnswer((_) async => 1);

      final result = await repository.delete('1');

      expect(result, true);
    });

    test('delete returns false when task is not found', () async {
      when(
        mockDb.execute(any, substitutionValues: anyNamed('substitutionValues')),
      ).thenAnswer((_) async => 0);

      final result = await repository.delete('1');

      expect(result, false);
    });
  });
}
