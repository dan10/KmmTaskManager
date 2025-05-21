import 'package:test/test.dart';
import 'package:shared/models.dart' as shared_models;
import '../../lib/src/repositories/task_repository.dart';
import '../../lib/src/repositories/auth_repository.dart';
import '../../lib/src/repositories/project_repository.dart'; // For creating projects
import '../../lib/src/exceptions/custom_exceptions.dart';
import '../helpers/test_base.dart';

void main() {
  late TestBase testBase;
  late TaskRepository repository;
  late AuthRepository authRepository;
  late ProjectRepository projectRepository;
  late shared_models.User user1;
  late shared_models.User user2;
  late shared_models.Project project1;
  late shared_models.Project project2;

  setUpAll(() async {
    testBase = TestBase();
    await testBase.setUp();
    repository = TaskRepository(testBase.connection);
    authRepository = AuthRepository(testBase.connection);
    projectRepository = ProjectRepository(testBase.connection);

    user1 = shared_models.User(id: 'task-repo-user-1', name: 'Task User 1', email: 'truser1@example.com', passwordHash: 'hash');
    user2 = shared_models.User(id: 'task-repo-user-2', name: 'Task User 2', email: 'truser2@example.com', passwordHash: 'hash');
    await authRepository.createUser(user1);
    await authRepository.createUser(user2);

    project1 = shared_models.Project(id: 'task-repo-proj-1', name: 'TR Project 1', creatorId: user1.id, memberIds: [user1.id]);
    project2 = shared_models.Project(id: 'task-repo-proj-2', name: 'TR Project 2', creatorId: user2.id, memberIds: [user2.id]);
    await projectRepository.create(project1);
    await projectRepository.create(project2);
  });

  tearDownAll(() async {
    await testBase.tearDown();
  });

  setUp(() async {
    // Clear tasks before each test
    await testBase.connection.execute('DELETE FROM tasks');
  });
  
  final taskData1 = shared_models.Task(
    id: 'task-id-A', title: 'Alpha Task One', description: 'First task', 
    status: shared_models.TaskStatus.todo, priority: shared_models.Priority.medium,
    creatorId: user1.id, assigneeId: user1.id, projectId: project1.id,
    dueDate: DateTime.now().add(Duration(days: 5))
  );
  final taskData2 = shared_models.Task(
    id: 'task-id-B', title: 'Beta Task Two (search)', description: 'Second task', 
    status: shared_models.TaskStatus.inProgress, priority: shared_models.Priority.high,
    creatorId: user1.id, assigneeId: user2.id, projectId: project1.id,
    dueDate: DateTime.now().add(Duration(days: 2))
  );
   final taskData3 = shared_models.Task(
    id: 'task-id-C', title: 'Gamma Task Three', description: 'Third task (search)', 
    status: shared_models.TaskStatus.done, priority: shared_models.Priority.low,
    creatorId: user2.id, assigneeId: user2.id, projectId: project2.id,
    dueDate: DateTime.now().add(Duration(days: 10))
  );


  group('TaskRepository', () {
    test('create and findById should work', () async {
      final createdTask = await repository.create(taskData1);
      expect(createdTask.title, taskData1.title);
      expect(createdTask.status.name, taskData1.status.name); // Check enum storage

      final foundTask = await repository.findById(taskData1.id);
      expect(foundTask, isNotNull);
      expect(foundTask!.title, taskData1.title);
      expect(foundTask.status, taskData1.status);
      expect(foundTask.priority, taskData1.priority);
    });

    test('update should modify task details', () async {
      final task = await repository.create(taskData1);
      final updatedData = task.copyWith(
          title: 'Updated Title', status: shared_models.TaskStatus.done);
      
      final result = await repository.update(updatedData);
      expect(result.title, 'Updated Title');
      expect(result.status, shared_models.TaskStatus.done);

      final foundAgain = await repository.findById(task.id);
      expect(foundAgain!.title, 'Updated Title');
    });

    test('update should throw TaskNotFoundException if task does not exist', () {
      final nonExistentTask = taskData1.copyWith(id: 'fake-task-id');
      expect(() => repository.update(nonExistentTask), throwsA(isA<TaskNotFoundException>()));
    });

    test('delete should remove a task', () async {
      final task = await repository.create(taskData1);
      await repository.delete(task.id);
      final foundTask = await repository.findById(task.id);
      expect(foundTask, isNull);
    });

    test('delete should throw TaskNotFoundException if task does not exist', () {
      expect(() => repository.delete('fake-task-id'), throwsA(isA<TaskNotFoundException>()));
    });

    group('getTasks', () {
      setUp(() async {
        await repository.create(taskData1); // user1 creator, user1 assignee, project1
        await repository.create(taskData2); // user1 creator, user2 assignee, project1
        await repository.create(taskData3); // user2 creator, user2 assignee, project2
      });

      test('should filter by assigneeId', async () {
        final tasks = await repository.getTasks(assigneeId: user1.id);
        expect(tasks.length, 1);
        expect(tasks.first.id, taskData1.id);
      });
      test('should filter by creatorId', async () {
        final tasks = await repository.getTasks(creatorId: user1.id);
        expect(tasks.length, 2);
        expect(tasks.map((t)=>t.id), containsAll([taskData1.id, taskData2.id]));
      });
      test('should filter by projectId', async () {
        final tasks = await repository.getTasks(projectId: project1.id);
        expect(tasks.length, 2);
         expect(tasks.map((t)=>t.id), containsAll([taskData1.id, taskData2.id]));
      });
      test('should filter by query (title or description)', async () {
        var tasks = await repository.getTasks(query: 'Beta');
        expect(tasks.length, 1);
        expect(tasks.first.id, taskData2.id);
        
        tasks = await repository.getTasks(query: 'search'); // taskData2 and taskData3 have 'search' in title/desc
        expect(tasks.length, 2);
         expect(tasks.map((t)=>t.id), containsAll([taskData2.id, taskData3.id]));
      });
      test('should apply pagination (page 0, size 1, ordered by due_date ASC NULLS LAST, title ASC)', async () {
        // Order: taskData2 (due earliest), taskData1, taskData3
        final tasks = await repository.getTasks(size: 1, page: 0);
        expect(tasks.length, 1);
        expect(tasks.first.id, taskData2.id); 
      });
       test('should apply pagination (page 1, size 1)', async () {
        final tasks = await repository.getTasks(size: 1, page: 1);
        expect(tasks.length, 1);
        expect(tasks.first.id, taskData1.id);
      });
      test('should combine filters (projectId and query)', async () {
        final tasks = await repository.getTasks(projectId: project1.id, query: 'Alpha');
        expect(tasks.length, 1);
        expect(tasks.first.id, taskData1.id);
      });
    });
    
    group('assignTask', () {
      late shared_models.Task task;
      setUp(() async {
        task = await repository.create(taskData1); // Initially assigned to user1
      });
      test('should assign task to a different user', async () {
        final updatedTask = await repository.assignTask(task.id, user2.id);
        expect(updatedTask.assigneeId, user2.id);
        final foundTask = await repository.findById(task.id);
        expect(foundTask!.assigneeId, user2.id);
      });
      test('should throw TaskNotFoundException if task does not exist', () {
        expect(() => repository.assignTask('fake-task', user2.id), throwsA(isA<TaskNotFoundException>()));
      });
      test('should throw UserNotFoundException if assignee does not exist', () {
        expect(() => repository.assignTask(task.id, 'fake-user'), throwsA(isA<UserNotFoundException>()));
      });
    });

    group('changeTaskStatus', () {
       late shared_models.Task task;
      setUp(() async {
        task = await repository.create(taskData1); // Initial status: todo
      });
      test('should change task status', async () {
        final newStatus = shared_models.TaskStatus.done;
        final updatedTask = await repository.changeTaskStatus(task.id, newStatus);
        expect(updatedTask.status, newStatus);
        final foundTask = await repository.findById(task.id);
        expect(foundTask!.status, newStatus);
      });
       test('should throw TaskNotFoundException if task does not exist for status change', () {
        expect(() => repository.changeTaskStatus('fake-task', shared_models.TaskStatus.inProgress), 
          throwsA(isA<TaskNotFoundException>()));
      });
    });

  });
}
