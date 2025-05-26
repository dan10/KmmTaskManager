import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_shared/models.dart';

import '../../../lib/data/repositories/task_repository.dart';
import '../../../lib/data/services/task_api_service.dart';
import '../../mocks/mock_task_api_service.dart';

void main() {
  group('TaskRepository', () {
    late TaskRepository repository;
    late MockTaskApiService mockApiService;

    setUp(() {
      mockApiService = MockTaskApiService();
      repository = TaskRepositoryImpl(mockApiService);
    });

    group('getTasks', () {
      test('should return paginated tasks successfully', () async {
        // Arrange
        final mockTasks = [
          TaskDto(
            id: '1',
            title: 'Test Task 1',
            description: 'Description 1',
            status: TaskStatus.todo,
            priority: Priority.high,
            creatorId: 'user1',
            createdAt: DateTime.now(),
            updatedAt: DateTime.now(),
          ),
          TaskDto(
            id: '2',
            title: 'Test Task 2',
            description: 'Description 2',
            status: TaskStatus.inProgress,
            priority: Priority.medium,
            creatorId: 'user1',
            createdAt: DateTime.now(),
            updatedAt: DateTime.now(),
          ),
        ];

        final mockResponse = PaginatedResponse<TaskDto>(
          items: mockTasks,
          total: 2,
          page: 0,
          size: 20,
          totalPages: 1,
        );

        mockApiService.setGetTasksResponse(mockResponse);

        // Act
        final result = await repository.getTasks();

        // Assert
        expect(result.items.length, 2);
        expect(result.total, 2);
        expect(result.page, 0);
        expect(result.totalPages, 1);
        expect(result.items[0].title, 'Test Task 1');
        expect(result.items[1].title, 'Test Task 2');
      });

      test('should handle pagination parameters correctly', () async {
        // Arrange
        final mockResponse = PaginatedResponse<TaskDto>(
          items: [],
          total: 0,
          page: 1,
          size: 10,
          totalPages: 0,
        );

        mockApiService.setGetTasksResponse(mockResponse);

        // Act
        await repository.getTasks(page: 1, size: 10, query: 'test', projectId: 'proj1');

        // Assert
        expect(mockApiService.lastGetTasksPage, 1);
        expect(mockApiService.lastGetTasksSize, 10);
        expect(mockApiService.lastGetTasksQuery, 'test');
        expect(mockApiService.lastGetTasksProjectId, 'proj1');
      });

      test('should throw exception when API call fails', () async {
        // Arrange
        mockApiService.setShouldThrowError(true);

        // Act & Assert
        expect(
          () => repository.getTasks(),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to load tasks'),
          )),
        );
      });
    });

    group('getTask', () {
      test('should return single task successfully', () async {
        // Arrange
        final mockTask = TaskDto(
          id: '1',
          title: 'Test Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.high,
          creatorId: 'user1',
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
        );

        mockApiService.setGetTaskResponse(mockTask);

        // Act
        final result = await repository.getTask('1');

        // Assert
        expect(result.id, '1');
        expect(result.title, 'Test Task');
        expect(result.status, TaskStatus.todo);
        expect(result.priority, Priority.high);
      });

      test('should throw exception when task not found', () async {
        // Arrange
        mockApiService.setShouldThrowError(true);

        // Act & Assert
        expect(
          () => repository.getTask('nonexistent'),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to load task'),
          )),
        );
      });
    });

    group('createTask', () {
      test('should create task successfully with valid request', () async {
        // Arrange
        final request = TaskCreateRequestDto(
          title: 'New Task',
          description: 'New Description',
          priority: Priority.medium,
          projectId: 'proj1',
        );

        final mockResponse = TaskDto(
          id: '1',
          title: 'New Task',
          description: 'New Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'proj1',
          creatorId: 'user1',
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
        );

        mockApiService.setCreateTaskResponse(mockResponse);

        // Act
        final result = await repository.createTask(request);

        // Assert
        expect(result.title, 'New Task');
        expect(result.description, 'New Description');
        expect(result.priority, Priority.medium);
        expect(result.projectId, 'proj1');
        expect(result.status, TaskStatus.todo);
      });

      test('should throw validation exception for invalid request', () async {
        // Arrange
        final request = TaskCreateRequestDto(
          title: 'A', // Too short
          description: 'Short', // Too short
          priority: Priority.medium,
        );

        // Act & Assert
        expect(
          () => repository.createTask(request),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Validation failed'),
          )),
        );
      });

      test('should throw exception when API call fails', () async {
        // Arrange
        final request = TaskCreateRequestDto(
          title: 'Valid Task Title',
          description: 'Valid task description that is long enough',
          priority: Priority.medium,
        );

        mockApiService.setShouldThrowError(true);

        // Act & Assert
        expect(
          () => repository.createTask(request),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to create task'),
          )),
        );
      });
    });

    group('updateTask', () {
      test('should update task successfully with valid request', () async {
        // Arrange
        final request = TaskUpdateRequestDto(
          title: 'Updated Task',
          description: 'Updated description that is long enough',
          priority: Priority.high,
        );

        final mockResponse = TaskDto(
          id: '1',
          title: 'Updated Task',
          description: 'Updated description that is long enough',
          status: TaskStatus.todo,
          priority: Priority.high,
          creatorId: 'user1',
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
        );

        mockApiService.setUpdateTaskResponse(mockResponse);

        // Act
        final result = await repository.updateTask('1', request);

        // Assert
        expect(result.title, 'Updated Task');
        expect(result.description, 'Updated description that is long enough');
        expect(result.priority, Priority.high);
      });

      test('should throw validation exception for invalid request', () async {
        // Arrange
        final request = TaskUpdateRequestDto(
          title: 'A', // Too short
        );

        // Act & Assert
        expect(
          () => repository.updateTask('1', request),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Validation failed'),
          )),
        );
      });

      test('should throw exception when no updates provided', () async {
        // Arrange
        final request = TaskUpdateRequestDto(); // No updates

        // Act & Assert
        expect(
          () => repository.updateTask('1', request),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('No updates provided'),
          )),
        );
      });

      test('should throw exception when API call fails', () async {
        // Arrange
        final request = TaskUpdateRequestDto(
          title: 'Valid Updated Title',
        );

        mockApiService.setShouldThrowError(true);

        // Act & Assert
        expect(
          () => repository.updateTask('1', request),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to update task'),
          )),
        );
      });
    });

    group('deleteTask', () {
      test('should delete task successfully', () async {
        // Arrange
        mockApiService.setDeleteTaskSuccess(true);

        // Act & Assert
        expect(() => repository.deleteTask('1'), returnsNormally);
      });

      test('should throw exception when API call fails', () async {
        // Arrange
        mockApiService.setShouldThrowError(true);

        // Act & Assert
        expect(
          () => repository.deleteTask('1'),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to delete task'),
          )),
        );
      });
    });

    group('changeTaskStatus', () {
      test('should change task status successfully', () async {
        // Arrange
        final mockResponse = TaskDto(
          id: '1',
          title: 'Test Task',
          description: 'Description',
          status: TaskStatus.done,
          priority: Priority.medium,
          creatorId: 'user1',
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
        );

        mockApiService.setChangeTaskStatusResponse(mockResponse);

        // Act
        final result = await repository.changeTaskStatus('1', TaskStatus.done);

        // Assert
        expect(result.status, TaskStatus.done);
        expect(mockApiService.lastStatusChangeRequest?.status, TaskStatus.done);
      });

      test('should throw exception when API call fails', () async {
        // Arrange
        mockApiService.setShouldThrowError(true);

        // Act & Assert
        expect(
          () => repository.changeTaskStatus('1', TaskStatus.done),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to change task status'),
          )),
        );
      });
    });

    group('assignTask', () {
      test('should assign task successfully', () async {
        // Arrange
        final mockResponse = TaskDto(
          id: '1',
          title: 'Test Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          assigneeId: 'user2',
          creatorId: 'user1',
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
        );

        mockApiService.setAssignTaskResponse(mockResponse);

        // Act
        final result = await repository.assignTask('1', 'user2');

        // Assert
        expect(result.assigneeId, 'user2');
        expect(mockApiService.lastAssignRequest?.assigneeId, 'user2');
      });

      test('should throw exception when API call fails', () async {
        // Arrange
        mockApiService.setShouldThrowError(true);

        // Act & Assert
        expect(
          () => repository.assignTask('1', 'user2'),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to assign task'),
          )),
        );
      });
    });
  });
} 