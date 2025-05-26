import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_shared/models.dart';

import '../../../lib/data/repositories/task_repository.dart';
import '../../../lib/presentation/viewmodels/task_list_viewmodel.dart';
import '../../mocks/mock_task_repository.dart';

void main() {
  group('TaskListViewModel', () {
    late TaskListViewModel viewModel;
    late MockTaskRepository mockRepository;

    setUp(() {
      mockRepository = MockTaskRepository();
      viewModel = TaskListViewModel(mockRepository);
    });

    tearDown(() {
      viewModel.dispose();
    });

    group('Initial State', () {
      test('should have correct initial state', () {
        expect(viewModel.state, TaskListState.initial);
        expect(viewModel.tasks, isEmpty);
        expect(viewModel.errorMessage, isNull);
        expect(viewModel.searchQuery, isEmpty);
        expect(viewModel.projectId, isNull);
        expect(viewModel.isLoading, false);
        expect(viewModel.isLoadingMore, false);
        expect(viewModel.isRefreshing, false);
        expect(viewModel.hasTasks, false);
        expect(viewModel.hasMorePages, false);
        expect(viewModel.currentPage, 0);
        expect(viewModel.totalPages, 0);
        expect(viewModel.totalItems, 0);
        expect(viewModel.pageSize, 20);
      });
    });

    group('loadTasks', () {
      test('should load tasks successfully', () async {
        // Arrange
        final mockTasks = [
          TaskDto(
            id: '1',
            title: 'Task 1',
            description: 'Description 1',
            status: TaskStatus.todo,
            priority: Priority.high,
            creatorId: 'user1',
          ),
          TaskDto(
            id: '2',
            title: 'Task 2',
            description: 'Description 2',
            status: TaskStatus.inProgress,
            priority: Priority.medium,
            creatorId: 'user1',
          ),
        ];

        final mockResponse = PaginatedResponse<TaskDto>(
          items: mockTasks,
          total: 2,
          page: 0,
          size: 20,
          totalPages: 1,
        );

        mockRepository.setGetTasksResponse(mockResponse);

        // Act
        await viewModel.loadTasks();

        // Assert
        expect(viewModel.state, TaskListState.loaded);
        expect(viewModel.tasks.length, 2);
        expect(viewModel.totalItems, 2);
        expect(viewModel.totalPages, 1);
        expect(viewModel.hasMorePages, false);
        expect(viewModel.tasks[0].title, 'Task 1');
        expect(viewModel.tasks[1].title, 'Task 2');
      });

      test('should handle loading state correctly', () async {
        // Arrange
        mockRepository.setDelayResponse(true);
        
        // Act
        final future = viewModel.loadTasks();
        
        // Assert - Check loading state
        expect(viewModel.state, TaskListState.loading);
        expect(viewModel.isLoading, true);
        
        // Complete the operation
        await future;
        expect(viewModel.state, TaskListState.loaded);
        expect(viewModel.isLoading, false);
      });

      test('should handle refresh correctly', () async {
        // Arrange
        final initialTasks = [
          TaskDto(
            id: '1',
            title: 'Task 1',
            description: 'Description 1',
            status: TaskStatus.todo,
            priority: Priority.high,
            creatorId: 'user1',
          ),
        ];

        final refreshedTasks = [
          TaskDto(
            id: '2',
            title: 'Task 2',
            description: 'Description 2',
            status: TaskStatus.todo,
            priority: Priority.medium,
            creatorId: 'user1',
          ),
        ];

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: initialTasks,
          total: 1,
          page: 0,
          size: 20,
          totalPages: 1,
        ));

        await viewModel.loadTasks();
        expect(viewModel.tasks.length, 1);

        // Change response for refresh
        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: refreshedTasks,
          total: 1,
          page: 0,
          size: 20,
          totalPages: 1,
        ));

        // Act
        await viewModel.loadTasks(refresh: true);

        // Assert
        expect(viewModel.state, TaskListState.loaded);
        expect(viewModel.tasks.length, 1);
        expect(viewModel.tasks[0].title, 'Task 2'); // Should be refreshed data
      });

      test('should handle project filter correctly', () async {
        // Arrange
        final mockResponse = PaginatedResponse<TaskDto>(
          items: [],
          total: 0,
          page: 0,
          size: 20,
          totalPages: 0,
        );

        mockRepository.setGetTasksResponse(mockResponse);

        // Act
        await viewModel.loadTasks(projectId: 'project1');

        // Assert
        expect(viewModel.projectId, 'project1');
        expect(mockRepository.lastGetTasksProjectId, 'project1');
      });

      test('should handle error correctly', () async {
        // Arrange
        mockRepository.setShouldThrowError(true);

        // Act
        await viewModel.loadTasks();

        // Assert
        expect(viewModel.state, TaskListState.error);
        expect(viewModel.errorMessage, isNotNull);
        expect(viewModel.errorMessage, contains('Failed to load tasks'));
      });
    });

    group('loadMoreTasks', () {
      test('should load more tasks when has more pages', () async {
        // Arrange - Initial load
        final initialTasks = [
          TaskDto(
            id: '1',
            title: 'Task 1',
            description: 'Description 1',
            status: TaskStatus.todo,
            priority: Priority.high,
            creatorId: 'user1',
          ),
        ];

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: initialTasks,
          total: 3,
          page: 0,
          size: 1,
          totalPages: 3,
        ));

        await viewModel.loadTasks();
        expect(viewModel.hasMorePages, true);
        expect(viewModel.currentPage, 0);

        // Arrange - Load more
        final moreTasks = [
          TaskDto(
            id: '2',
            title: 'Task 2',
            description: 'Description 2',
            status: TaskStatus.inProgress,
            priority: Priority.medium,
            creatorId: 'user1',
          ),
        ];

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: moreTasks,
          total: 3,
          page: 1,
          size: 1,
          totalPages: 3,
        ));

        // Act
        await viewModel.loadMoreTasks();

        // Assert
        expect(viewModel.state, TaskListState.loaded);
        expect(viewModel.tasks.length, 2); // Should have both tasks
        expect(viewModel.currentPage, 1);
        expect(viewModel.hasMorePages, true);
        expect(viewModel.tasks[0].title, 'Task 1');
        expect(viewModel.tasks[1].title, 'Task 2');
      });

      test('should not load more when no more pages', () async {
        // Arrange
        final mockTasks = [
          TaskDto(
            id: '1',
            title: 'Task 1',
            description: 'Description 1',
            status: TaskStatus.todo,
            priority: Priority.high,
            creatorId: 'user1',
          ),
        ];

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: mockTasks,
          total: 1,
          page: 0,
          size: 20,
          totalPages: 1,
        ));

        await viewModel.loadTasks();
        expect(viewModel.hasMorePages, false);

        // Act
        await viewModel.loadMoreTasks();

        // Assert
        expect(viewModel.currentPage, 0); // Should not increment
        expect(mockRepository.getTasksCallCount, 1); // Should not call API again
      });

      test('should handle loadMore error correctly', () async {
        // Arrange - Initial successful load
        final initialTasks = [
          TaskDto(
            id: '1',
            title: 'Task 1',
            description: 'Description 1',
            status: TaskStatus.todo,
            priority: Priority.high,
            creatorId: 'user1',
          ),
        ];

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: initialTasks,
          total: 2,
          page: 0,
          size: 1,
          totalPages: 2,
        ));

        await viewModel.loadTasks();
        expect(viewModel.hasMorePages, true);

        // Arrange - Error on load more
        mockRepository.setShouldThrowError(true);

        // Act
        await viewModel.loadMoreTasks();

        // Assert
        expect(viewModel.state, TaskListState.error);
        expect(viewModel.currentPage, 0); // Should revert page increment
        expect(viewModel.tasks.length, 1); // Should keep existing tasks
      });
    });

    group('searchTasks', () {
      test('should search tasks correctly', () async {
        // Arrange
        final searchResults = [
          TaskDto(
            id: '1',
            title: 'Search Result',
            description: 'Found task',
            status: TaskStatus.todo,
            priority: Priority.medium,
            creatorId: 'user1',
          ),
        ];

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: searchResults,
          total: 1,
          page: 0,
          size: 20,
          totalPages: 1,
        ));

        // Act
        await viewModel.searchTasks('search query');

        // Assert
        expect(viewModel.searchQuery, 'search query');
        expect(viewModel.currentPage, 0); // Should reset page
        expect(viewModel.tasks.length, 1);
        expect(mockRepository.lastGetTasksQuery, 'search query');
      });

      test('should clear search correctly', () async {
        // Arrange
        await viewModel.searchTasks('test query');
        expect(viewModel.searchQuery, 'test query');

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: [],
          total: 0,
          page: 0,
          size: 20,
          totalPages: 0,
        ));

        // Act
        viewModel.clearSearch();

        // Assert
        expect(viewModel.searchQuery, isEmpty);
      });
    });

    group('CRUD Operations', () {
      test('should create task successfully', () async {
        // Arrange
        final request = TaskCreateRequestDto(
          title: 'New Task',
          description: 'New task description',
          priority: Priority.high,
        );

        final newTask = TaskDto(
          id: '1',
          title: 'New Task',
          description: 'New task description',
          status: TaskStatus.todo,
          priority: Priority.high,
          creatorId: 'user1',
        );

        mockRepository.setCreateTaskResponse(newTask);

        // Act
        await viewModel.createTask(request);

        // Assert
        expect(viewModel.tasks.length, 1);
        expect(viewModel.tasks[0].title, 'New Task');
        expect(viewModel.totalItems, 1);
      });

      test('should update task successfully', () async {
        // Arrange - Add initial task
        final initialTask = TaskDto(
          id: '1',
          title: 'Original Task',
          description: 'Original description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: [initialTask],
          total: 1,
          page: 0,
          size: 20,
          totalPages: 1,
        ));

        await viewModel.loadTasks();

        // Arrange - Update
        final request = TaskUpdateRequestDto(
          title: 'Updated Task',
          description: 'Updated description',
        );

        final updatedTask = TaskDto(
          id: '1',
          title: 'Updated Task',
          description: 'Updated description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setUpdateTaskResponse(updatedTask);

        // Act
        await viewModel.updateTask('1', request);

        // Assert
        expect(viewModel.tasks[0].title, 'Updated Task');
        expect(viewModel.tasks[0].description, 'Updated description');
      });

      test('should delete task successfully', () async {
        // Arrange - Add initial task
        final initialTask = TaskDto(
          id: '1',
          title: 'Task to Delete',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: [initialTask],
          total: 1,
          page: 0,
          size: 20,
          totalPages: 1,
        ));

        await viewModel.loadTasks();
        expect(viewModel.tasks.length, 1);

        mockRepository.setDeleteTaskSuccess(true);

        // Act
        await viewModel.deleteTask('1');

        // Assert
        expect(viewModel.tasks.length, 0);
        expect(viewModel.totalItems, 0);
      });

      test('should change task status successfully', () async {
        // Arrange - Add initial task
        final initialTask = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: [initialTask],
          total: 1,
          page: 0,
          size: 20,
          totalPages: 1,
        ));

        await viewModel.loadTasks();

        // Arrange - Status change
        final updatedTask = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.done,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setChangeTaskStatusResponse(updatedTask);

        // Act
        await viewModel.changeTaskStatus('1', TaskStatus.done);

        // Assert
        expect(viewModel.tasks[0].status, TaskStatus.done);
      });

      test('should assign task successfully', () async {
        // Arrange - Add initial task
        final initialTask = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: [initialTask],
          total: 1,
          page: 0,
          size: 20,
          totalPages: 1,
        ));

        await viewModel.loadTasks();

        // Arrange - Assignment
        final assignedTask = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          assigneeId: 'user2',
          creatorId: 'user1',
        );

        mockRepository.setAssignTaskResponse(assignedTask);

        // Act
        await viewModel.assignTask('1', 'user2');

        // Assert
        expect(viewModel.tasks[0].assigneeId, 'user2');
      });
    });

    group('Task Filtering', () {
      test('should filter tasks by status correctly', () async {
        // Arrange
        final mockTasks = [
          TaskDto(
            id: '1',
            title: 'Todo Task',
            description: 'Description',
            status: TaskStatus.todo,
            priority: Priority.medium,
            creatorId: 'user1',
          ),
          TaskDto(
            id: '2',
            title: 'In Progress Task',
            description: 'Description',
            status: TaskStatus.inProgress,
            priority: Priority.medium,
            creatorId: 'user1',
          ),
          TaskDto(
            id: '3',
            title: 'Done Task',
            description: 'Description',
            status: TaskStatus.done,
            priority: Priority.medium,
            creatorId: 'user1',
          ),
        ];

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: mockTasks,
          total: 3,
          page: 0,
          size: 20,
          totalPages: 1,
        ));

        await viewModel.loadTasks();

        // Act & Assert
        expect(viewModel.todoTasks.length, 1);
        expect(viewModel.inProgressTasks.length, 1);
        expect(viewModel.doneTasks.length, 1);
        expect(viewModel.todoTasks[0].title, 'Todo Task');
        expect(viewModel.inProgressTasks[0].title, 'In Progress Task');
        expect(viewModel.doneTasks[0].title, 'Done Task');
      });

      test('should filter tasks by priority correctly', () async {
        // Arrange
        final mockTasks = [
          TaskDto(
            id: '1',
            title: 'High Priority Task',
            description: 'Description',
            status: TaskStatus.todo,
            priority: Priority.high,
            creatorId: 'user1',
          ),
          TaskDto(
            id: '2',
            title: 'Medium Priority Task',
            description: 'Description',
            status: TaskStatus.todo,
            priority: Priority.medium,
            creatorId: 'user1',
          ),
          TaskDto(
            id: '3',
            title: 'Low Priority Task',
            description: 'Description',
            status: TaskStatus.todo,
            priority: Priority.low,
            creatorId: 'user1',
          ),
        ];

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: mockTasks,
          total: 3,
          page: 0,
          size: 20,
          totalPages: 1,
        ));

        await viewModel.loadTasks();

        // Act & Assert
        expect(viewModel.highPriorityTasks.length, 1);
        expect(viewModel.mediumPriorityTasks.length, 1);
        expect(viewModel.lowPriorityTasks.length, 1);
        expect(viewModel.highPriorityTasks[0].title, 'High Priority Task');
        expect(viewModel.mediumPriorityTasks[0].title, 'Medium Priority Task');
        expect(viewModel.lowPriorityTasks[0].title, 'Low Priority Task');
      });

      test('should identify overdue tasks correctly', () async {
        // Arrange
        final pastDate = DateTime.now().subtract(const Duration(days: 1));
        final futureDate = DateTime.now().add(const Duration(days: 1));

        final mockTasks = [
          TaskDto(
            id: '1',
            title: 'Overdue Task',
            description: 'Description',
            status: TaskStatus.todo,
            priority: Priority.medium,
            dueDate: pastDate,
            creatorId: 'user1',
          ),
          TaskDto(
            id: '2',
            title: 'Not Overdue Task',
            description: 'Description',
            status: TaskStatus.todo,
            priority: Priority.medium,
            dueDate: futureDate,
            creatorId: 'user1',
          ),
          TaskDto(
            id: '3',
            title: 'Completed Overdue Task',
            description: 'Description',
            status: TaskStatus.done,
            priority: Priority.medium,
            dueDate: pastDate,
            creatorId: 'user1',
          ),
        ];

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: mockTasks,
          total: 3,
          page: 0,
          size: 20,
          totalPages: 1,
        ));

        await viewModel.loadTasks();

        // Act & Assert
        expect(viewModel.overdueTasks.length, 1);
        expect(viewModel.overdueTasks[0].title, 'Overdue Task');
      });
    });

    group('Task Statistics', () {
      test('should calculate task statistics correctly', () async {
        // Arrange
        final mockTasks = [
          TaskDto(
            id: '1',
            title: 'Todo Task',
            description: 'Description',
            status: TaskStatus.todo,
            priority: Priority.medium,
            creatorId: 'user1',
          ),
          TaskDto(
            id: '2',
            title: 'In Progress Task',
            description: 'Description',
            status: TaskStatus.inProgress,
            priority: Priority.medium,
            creatorId: 'user1',
          ),
          TaskDto(
            id: '3',
            title: 'Done Task 1',
            description: 'Description',
            status: TaskStatus.done,
            priority: Priority.medium,
            creatorId: 'user1',
          ),
          TaskDto(
            id: '4',
            title: 'Done Task 2',
            description: 'Description',
            status: TaskStatus.done,
            priority: Priority.medium,
            creatorId: 'user1',
          ),
        ];

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: mockTasks,
          total: 4,
          page: 0,
          size: 20,
          totalPages: 1,
        ));

        await viewModel.loadTasks();

        // Act & Assert
        expect(viewModel.totalTasksCount, 4);
        expect(viewModel.todoTasksCount, 1);
        expect(viewModel.inProgressTasksCount, 1);
        expect(viewModel.doneTasksCount, 2);
        expect(viewModel.completionRate, 0.5); // 2 done out of 4 total
      });
    });

    group('Error Handling', () {
      test('should clear error correctly', () async {
        // Arrange
        mockRepository.setShouldThrowError(true);
        await viewModel.loadTasks();
        expect(viewModel.state, TaskListState.error);

        // Act
        viewModel.clearError();

        // Assert
        expect(viewModel.state, TaskListState.initial);
        expect(viewModel.errorMessage, isNull);
      });

      test('should handle CRUD operation errors', () async {
        // Arrange
        mockRepository.setShouldThrowError(true);

        // Act & Assert - Create error
        await viewModel.createTask(TaskCreateRequestDto(
          title: 'Test Task',
          description: 'Test Description',
          priority: Priority.medium,
        ));
        expect(viewModel.state, TaskListState.error);

        // Reset
        viewModel.clearError();

        // Act & Assert - Update error
        await viewModel.updateTask('1', TaskUpdateRequestDto(title: 'Updated'));
        expect(viewModel.state, TaskListState.error);

        // Reset
        viewModel.clearError();

        // Act & Assert - Delete error
        await viewModel.deleteTask('1');
        expect(viewModel.state, TaskListState.error);
      });
    });

    group('Utility Methods', () {
      test('should get task by id correctly', () async {
        // Arrange
        final mockTasks = [
          TaskDto(
            id: '1',
            title: 'Task 1',
            description: 'Description 1',
            status: TaskStatus.todo,
            priority: Priority.medium,
            creatorId: 'user1',
          ),
          TaskDto(
            id: '2',
            title: 'Task 2',
            description: 'Description 2',
            status: TaskStatus.todo,
            priority: Priority.medium,
            creatorId: 'user1',
          ),
        ];

        mockRepository.setGetTasksResponse(PaginatedResponse<TaskDto>(
          items: mockTasks,
          total: 2,
          page: 0,
          size: 20,
          totalPages: 1,
        ));

        await viewModel.loadTasks();

        // Act & Assert
        final task1 = viewModel.getTask('1');
        final task2 = viewModel.getTask('2');
        final nonExistentTask = viewModel.getTask('999');

        expect(task1?.title, 'Task 1');
        expect(task2?.title, 'Task 2');
        expect(nonExistentTask, isNull);
      });
    });
  });
} 