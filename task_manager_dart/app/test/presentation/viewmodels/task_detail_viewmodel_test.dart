import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_shared/models.dart';

import '../../../lib/presentation/viewmodels/task_detail_viewmodel.dart';
import '../../mocks/mock_task_repository.dart';

void main() {
  group('TaskDetailViewModel', () {
    late TaskDetailViewModel viewModel;
    late MockTaskRepository mockRepository;

    setUp(() {
      mockRepository = MockTaskRepository();
      viewModel = TaskDetailViewModel(mockRepository);
    });

    tearDown(() {
      viewModel.dispose();
    });

    group('Initial State', () {
      test('should have correct initial state', () {
        expect(viewModel.state, TaskDetailState.initial);
        expect(viewModel.task, isNull);
        expect(viewModel.errorMessage, isNull);
        expect(viewModel.isLoading, false);
        expect(viewModel.isUpdating, false);
        expect(viewModel.isDeleting, false);
        expect(viewModel.hasTask, false);
        
        // Convenience getters should return defaults
        expect(viewModel.taskTitle, isEmpty);
        expect(viewModel.taskDescription, isEmpty);
        expect(viewModel.taskStatus, TaskStatus.todo);
        expect(viewModel.taskPriority, Priority.medium);
        expect(viewModel.taskDueDate, isNull);
        expect(viewModel.taskProjectId, isNull);
        expect(viewModel.taskAssigneeId, isNull);
        expect(viewModel.taskCreatorId, isEmpty);
        
        // Status checks
        expect(viewModel.isTaskTodo, false);
        expect(viewModel.isTaskInProgress, false);
        expect(viewModel.isTaskDone, false);
        
        // Priority checks
        expect(viewModel.isHighPriority, false);
        expect(viewModel.isMediumPriority, false);
        expect(viewModel.isLowPriority, false);
        
        // State checks
        expect(viewModel.isOverdue, false);
        expect(viewModel.isAssigned, false);
      });
    });

    group('loadTask', () {
      test('should load task successfully', () async {
        // Arrange
        final mockTask = TaskDto(
          id: '1',
          title: 'Test Task',
          description: 'Test Description',
          status: TaskStatus.inProgress,
          priority: Priority.high,
          dueDate: DateTime.now().add(const Duration(days: 1)),
          projectId: 'project1',
          assigneeId: 'user2',
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(mockTask);

        // Act
        await viewModel.loadTask('1');

        // Assert
        expect(viewModel.state, TaskDetailState.loaded);
        expect(viewModel.task, isNotNull);
        expect(viewModel.hasTask, true);
        expect(viewModel.taskTitle, 'Test Task');
        expect(viewModel.taskDescription, 'Test Description');
        expect(viewModel.taskStatus, TaskStatus.inProgress);
        expect(viewModel.taskPriority, Priority.high);
        expect(viewModel.taskProjectId, 'project1');
        expect(viewModel.taskAssigneeId, 'user2');
        expect(viewModel.taskCreatorId, 'user1');
        
        // Status checks
        expect(viewModel.isTaskTodo, false);
        expect(viewModel.isTaskInProgress, true);
        expect(viewModel.isTaskDone, false);
        
        // Priority checks
        expect(viewModel.isHighPriority, true);
        expect(viewModel.isMediumPriority, false);
        expect(viewModel.isLowPriority, false);
        
        // State checks
        expect(viewModel.isOverdue, false);
        expect(viewModel.isAssigned, true);
      });

      test('should handle loading state correctly', () async {
        // Arrange
        mockRepository.setDelayResponse(true);
        
        // Act
        final future = viewModel.loadTask('1');
        
        // Assert - Check loading state
        expect(viewModel.state, TaskDetailState.loading);
        expect(viewModel.isLoading, true);
        
        // Complete the operation
        await future;
        expect(viewModel.state, TaskDetailState.loaded);
        expect(viewModel.isLoading, false);
      });

      test('should handle error correctly', () async {
        // Arrange
        mockRepository.setShouldThrowError(true);

        // Act
        await viewModel.loadTask('1');

        // Assert
        expect(viewModel.state, TaskDetailState.error);
        expect(viewModel.errorMessage, isNotNull);
        expect(viewModel.errorMessage, contains('Failed to load task'));
        expect(viewModel.hasTask, false);
      });

      test('should identify overdue task correctly', () async {
        // Arrange
        final pastDate = DateTime.now().subtract(const Duration(days: 1));
        final overdueTask = TaskDto(
          id: '1',
          title: 'Overdue Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          dueDate: pastDate,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(overdueTask);

        // Act
        await viewModel.loadTask('1');

        // Assert
        expect(viewModel.isOverdue, true);
      });

      test('should not consider completed task as overdue', () async {
        // Arrange
        final pastDate = DateTime.now().subtract(const Duration(days: 1));
        final completedTask = TaskDto(
          id: '1',
          title: 'Completed Task',
          description: 'Description',
          status: TaskStatus.done,
          priority: Priority.medium,
          dueDate: pastDate,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(completedTask);

        // Act
        await viewModel.loadTask('1');

        // Assert
        expect(viewModel.isOverdue, false);
        expect(viewModel.isTaskDone, true);
      });
    });

    group('updateTask', () {
      test('should update task successfully', () async {
        // Arrange - Load initial task
        final initialTask = TaskDto(
          id: '1',
          title: 'Original Task',
          description: 'Original Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(initialTask);
        await viewModel.loadTask('1');

        // Arrange - Update
        final request = TaskUpdateRequestDto(
          title: 'Updated Task',
          description: 'Updated Description',
          priority: Priority.high,
        );

        final updatedTask = TaskDto(
          id: '1',
          title: 'Updated Task',
          description: 'Updated Description',
          status: TaskStatus.todo,
          priority: Priority.high,
          creatorId: 'user1',
        );

        mockRepository.setUpdateTaskResponse(updatedTask);

        // Act
        await viewModel.updateTask('1', request);

        // Assert
        expect(viewModel.state, TaskDetailState.loaded);
        expect(viewModel.taskTitle, 'Updated Task');
        expect(viewModel.taskDescription, 'Updated Description');
        expect(viewModel.taskPriority, Priority.high);
        expect(viewModel.isHighPriority, true);
      });

      test('should handle updating state correctly', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.loadTask('1');

        mockRepository.setDelayResponse(true);
        mockRepository.setUpdateTaskResponse(task);

        // Act
        final future = viewModel.updateTask('1', TaskUpdateRequestDto(title: 'Updated'));
        
        // Assert - Check updating state
        expect(viewModel.state, TaskDetailState.updating);
        expect(viewModel.isUpdating, true);
        
        // Complete the operation
        await future;
        expect(viewModel.state, TaskDetailState.loaded);
        expect(viewModel.isUpdating, false);
      });

      test('should handle update error correctly', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.loadTask('1');

        mockRepository.setShouldThrowError(true);

        // Act
        await viewModel.updateTask('1', TaskUpdateRequestDto(title: 'Updated'));

        // Assert
        expect(viewModel.state, TaskDetailState.error);
        expect(viewModel.errorMessage, isNotNull);
        expect(viewModel.errorMessage, contains('Failed to update task'));
      });
    });

    group('deleteTask', () {
      test('should delete task successfully', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task to Delete',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.loadTask('1');

        mockRepository.setDeleteTaskSuccess(true);

        // Act
        final result = await viewModel.deleteTask('1');

        // Assert
        expect(result, true);
        expect(viewModel.state, TaskDetailState.deleted);
        expect(viewModel.task, isNull);
        expect(viewModel.hasTask, false);
      });

      test('should handle deleting state correctly', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.loadTask('1');

        mockRepository.setDelayResponse(true);
        mockRepository.setDeleteTaskSuccess(true);

        // Act
        final future = viewModel.deleteTask('1');
        
        // Assert - Check deleting state
        expect(viewModel.state, TaskDetailState.deleting);
        expect(viewModel.isDeleting, true);
        
        // Complete the operation
        final result = await future;
        expect(result, true);
        expect(viewModel.state, TaskDetailState.deleted);
        expect(viewModel.isDeleting, false);
      });

      test('should handle delete error correctly', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.loadTask('1');

        mockRepository.setShouldThrowError(true);

        // Act
        final result = await viewModel.deleteTask('1');

        // Assert
        expect(result, false);
        expect(viewModel.state, TaskDetailState.error);
        expect(viewModel.errorMessage, isNotNull);
        expect(viewModel.errorMessage, contains('Failed to delete task'));
        expect(viewModel.task, isNotNull); // Task should still be there
      });
    });

    group('changeTaskStatus', () {
      test('should change task status successfully', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.loadTask('1');

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
        expect(viewModel.state, TaskDetailState.loaded);
        expect(viewModel.taskStatus, TaskStatus.done);
        expect(viewModel.isTaskDone, true);
        expect(viewModel.isTaskTodo, false);
      });

      test('should handle status change error correctly', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.loadTask('1');

        mockRepository.setShouldThrowError(true);

        // Act
        await viewModel.changeTaskStatus('1', TaskStatus.done);

        // Assert
        expect(viewModel.state, TaskDetailState.error);
        expect(viewModel.errorMessage, isNotNull);
        expect(viewModel.taskStatus, TaskStatus.todo); // Should remain unchanged
      });

      test('should not change status when no task loaded', () async {
        // Act
        await viewModel.changeTaskStatus('1', TaskStatus.done);

        // Assert
        expect(viewModel.state, TaskDetailState.initial);
        expect(viewModel.task, isNull);
        expect(mockRepository.lastStatusChangeTaskId, isNull);
      });
    });

    group('assignTask', () {
      test('should assign task successfully', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.loadTask('1');

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
        expect(viewModel.state, TaskDetailState.loaded);
        expect(viewModel.taskAssigneeId, 'user2');
        expect(viewModel.isAssigned, true);
      });

      test('should handle assignment error correctly', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.loadTask('1');

        mockRepository.setShouldThrowError(true);

        // Act
        await viewModel.assignTask('1', 'user2');

        // Assert
        expect(viewModel.state, TaskDetailState.error);
        expect(viewModel.errorMessage, isNotNull);
        expect(viewModel.taskAssigneeId, isNull); // Should remain unchanged
      });

      test('should not assign when no task loaded', () async {
        // Act
        await viewModel.assignTask('1', 'user2');

        // Assert
        expect(viewModel.state, TaskDetailState.initial);
        expect(viewModel.task, isNull);
        expect(mockRepository.lastAssignTaskId, isNull);
      });
    });

    group('Error Handling', () {
      test('should clear error correctly', () async {
        // Arrange
        mockRepository.setShouldThrowError(true);
        await viewModel.loadTask('1');
        expect(viewModel.state, TaskDetailState.error);

        // Act
        viewModel.clearError();

        // Assert
        expect(viewModel.state, TaskDetailState.initial);
        expect(viewModel.errorMessage, isNull);
      });

      test('should clear error when task is loaded', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.loadTask('1');

        // Simulate error
        mockRepository.setShouldThrowError(true);
        await viewModel.updateTask('1', TaskUpdateRequestDto(title: 'Updated'));
        expect(viewModel.state, TaskDetailState.error);

        // Reset error flag
        mockRepository.setShouldThrowError(false);

        // Act
        viewModel.clearError();

        // Assert
        expect(viewModel.state, TaskDetailState.loaded);
        expect(viewModel.errorMessage, isNull);
        expect(viewModel.hasTask, true);
      });
    });

    group('Reset', () {
      test('should reset viewmodel correctly', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.loadTask('1');
        expect(viewModel.hasTask, true);

        // Act
        viewModel.reset();

        // Assert
        expect(viewModel.state, TaskDetailState.initial);
        expect(viewModel.task, isNull);
        expect(viewModel.errorMessage, isNull);
        expect(viewModel.hasTask, false);
      });
    });

    group('Priority Checks', () {
      test('should correctly identify task priorities', () async {
        // Test High Priority
        final highPriorityTask = TaskDto(
          id: '1',
          title: 'High Priority Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.high,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(highPriorityTask);
        await viewModel.loadTask('1');

        expect(viewModel.isHighPriority, true);
        expect(viewModel.isMediumPriority, false);
        expect(viewModel.isLowPriority, false);

        // Test Medium Priority
        final mediumPriorityTask = TaskDto(
          id: '2',
          title: 'Medium Priority Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(mediumPriorityTask);
        await viewModel.loadTask('2');

        expect(viewModel.isHighPriority, false);
        expect(viewModel.isMediumPriority, true);
        expect(viewModel.isLowPriority, false);

        // Test Low Priority
        final lowPriorityTask = TaskDto(
          id: '3',
          title: 'Low Priority Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.low,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(lowPriorityTask);
        await viewModel.loadTask('3');

        expect(viewModel.isHighPriority, false);
        expect(viewModel.isMediumPriority, false);
        expect(viewModel.isLowPriority, true);
      });
    });

    group('Status Checks', () {
      test('should correctly identify task statuses', () async {
        // Test Todo Status
        final todoTask = TaskDto(
          id: '1',
          title: 'Todo Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(todoTask);
        await viewModel.loadTask('1');

        expect(viewModel.isTaskTodo, true);
        expect(viewModel.isTaskInProgress, false);
        expect(viewModel.isTaskDone, false);

        // Test In Progress Status
        final inProgressTask = TaskDto(
          id: '2',
          title: 'In Progress Task',
          description: 'Description',
          status: TaskStatus.inProgress,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(inProgressTask);
        await viewModel.loadTask('2');

        expect(viewModel.isTaskTodo, false);
        expect(viewModel.isTaskInProgress, true);
        expect(viewModel.isTaskDone, false);

        // Test Done Status
        final doneTask = TaskDto(
          id: '3',
          title: 'Done Task',
          description: 'Description',
          status: TaskStatus.done,
          priority: Priority.medium,
          creatorId: 'user1',
        );

        mockRepository.setGetTaskResponse(doneTask);
        await viewModel.loadTask('3');

        expect(viewModel.isTaskTodo, false);
        expect(viewModel.isTaskInProgress, false);
        expect(viewModel.isTaskDone, true);
      });
    });
  });
} 