import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_shared/models.dart';

import '../../../lib/presentation/viewmodels/task_create_edit_viewmodel.dart';
import '../../mocks/mock_task_repository.dart';

void main() {
  group('TaskCreateEditViewModel', () {
    late TaskCreateEditViewModel viewModel;
    late MockTaskRepository mockRepository;

    setUp(() {
      mockRepository = MockTaskRepository();
      viewModel = TaskCreateEditViewModel(mockRepository);
    });

    tearDown(() {
      viewModel.dispose();
    });

    group('Initial State', () {
      test('should have correct initial state', () {
        expect(viewModel.state, TaskCreateEditState.initial);
        expect(viewModel.task, isNull);
        expect(viewModel.errorMessage, isNull);
        expect(viewModel.title, isEmpty);
        expect(viewModel.description, isEmpty);
        expect(viewModel.priority, Priority.medium);
        expect(viewModel.dueDate, isNull);
        expect(viewModel.projectId, isNull);
        expect(viewModel.assigneeId, isNull);
        
        // State checks
        expect(viewModel.isLoading, false);
        expect(viewModel.isSaving, false);
        expect(viewModel.isDeleting, false);
        expect(viewModel.hasTask, false);
        expect(viewModel.isEditing, false);
        expect(viewModel.isCreating, true);
        expect(viewModel.isValid, false);
        expect(viewModel.hasChanges, false);
      });
    });

    group('Initialize for Create', () {
      test('should initialize correctly for creating new task', () {
        viewModel.initializeForCreate();

        expect(viewModel.state, TaskCreateEditState.initial);
        expect(viewModel.task, isNull);
        expect(viewModel.isCreating, true);
        expect(viewModel.isEditing, false);
        expect(viewModel.projectId, isNull);
      });

      test('should initialize with project ID when provided', () {
        const projectId = 'project123';
        
        viewModel.initializeForCreate(projectId: projectId);

        expect(viewModel.state, TaskCreateEditState.initial);
        expect(viewModel.projectId, projectId);
        expect(viewModel.isCreating, true);
      });
    });

    group('Initialize for Edit', () {
      test('should load task successfully for editing', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Test Task',
          description: 'Test Description',
          status: TaskStatus.todo,
          priority: Priority.high,
          projectId: 'project1',
          assigneeId: 'user1',
          dueDate: DateTime(2024, 12, 31),
          creatorId: 'creator1',
        );

        mockRepository.setGetTaskResponse(task);

        // Act
        await viewModel.initializeForEdit('1');

        // Assert
        expect(viewModel.state, TaskCreateEditState.loaded);
        expect(viewModel.task, isNotNull);
        expect(viewModel.title, 'Test Task');
        expect(viewModel.description, 'Test Description');
        expect(viewModel.priority, Priority.high);
        expect(viewModel.projectId, 'project1');
        expect(viewModel.assigneeId, 'user1');
        expect(viewModel.dueDate, DateTime(2024, 12, 31));
        expect(viewModel.isEditing, true);
        expect(viewModel.isCreating, false);
      });

      test('should handle loading state correctly', () async {
        // Arrange
        mockRepository.setDelayResponse(true);
        
        // Act
        final future = viewModel.initializeForEdit('1');
        
        // Assert - Check loading state
        expect(viewModel.state, TaskCreateEditState.loading);
        expect(viewModel.isLoading, true);
        
        // Complete the operation
        await future;
        expect(viewModel.isLoading, false);
      });

      test('should handle error when loading task fails', () async {
        // Arrange
        mockRepository.setShouldThrowError(true);

        // Act
        await viewModel.initializeForEdit('1');

        // Assert
        expect(viewModel.state, TaskCreateEditState.error);
        expect(viewModel.errorMessage, isNotNull);
        expect(viewModel.errorMessage, contains('Failed to load task'));
      });
    });

    group('Form Field Updates', () {
      test('should update title correctly', () {
        const newTitle = 'New Task Title';
        
        viewModel.updateTitle(newTitle);

        expect(viewModel.title, newTitle);
      });

      test('should update description correctly', () {
        const newDescription = 'New task description';
        
        viewModel.updateDescription(newDescription);

        expect(viewModel.description, newDescription);
      });

      test('should update priority correctly', () {
        viewModel.updatePriority(Priority.high);

        expect(viewModel.priority, Priority.high);
      });

      test('should update due date correctly', () {
        final newDate = DateTime(2024, 12, 31);
        
        viewModel.updateDueDate(newDate);

        expect(viewModel.dueDate, newDate);
      });

      test('should update project ID correctly', () {
        const projectId = 'project123';
        
        viewModel.updateProjectId(projectId);

        expect(viewModel.projectId, projectId);
      });

      test('should update assignee ID correctly', () {
        const assigneeId = 'user123';
        
        viewModel.updateAssigneeId(assigneeId);

        expect(viewModel.assigneeId, assigneeId);
      });
    });

    group('Validation', () {
      test('should be invalid when title is empty', () {
        viewModel.updateTitle('');
        viewModel.updateProjectId('project1');

        expect(viewModel.isValid, false);
      });

      test('should be invalid when project ID is null', () {
        viewModel.updateTitle('Valid Title');
        viewModel.updateProjectId(null);

        expect(viewModel.isValid, false);
      });

      test('should be valid when title and project ID are provided', () {
        viewModel.updateTitle('Valid Title');
        viewModel.updateProjectId('project1');

        expect(viewModel.isValid, true);
      });

      test('should detect changes correctly for new task', () {
        expect(viewModel.hasChanges, false);

        viewModel.updateTitle('New Title');
        expect(viewModel.hasChanges, true);

        viewModel.updateTitle('');
        viewModel.updateDescription('Description');
        expect(viewModel.hasChanges, true);

        viewModel.updateDescription('');
        viewModel.updatePriority(Priority.high);
        expect(viewModel.hasChanges, true);
      });

      test('should detect changes correctly for existing task', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Original Title',
          description: 'Original Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project1',
          creatorId: 'creator1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.initializeForEdit('1');

        expect(viewModel.hasChanges, false);

        // Act & Assert
        viewModel.updateTitle('Modified Title');
        expect(viewModel.hasChanges, true);

        viewModel.updateTitle('Original Title');
        expect(viewModel.hasChanges, false);

        viewModel.updateDescription('Modified Description');
        expect(viewModel.hasChanges, true);
      });
    });

    group('Save Task', () {
      test('should create task successfully', () async {
        // Arrange
        viewModel.updateTitle('New Task');
        viewModel.updateDescription('Task description');
        viewModel.updateProjectId('project1');
        viewModel.updatePriority(Priority.high);

        final createdTask = TaskDto(
          id: '1',
          title: 'New Task',
          description: 'Task description',
          status: TaskStatus.todo,
          priority: Priority.high,
          projectId: 'project1',
          creatorId: 'creator1',
        );

        mockRepository.setCreateTaskResponse(createdTask);

        // Act
        final result = await viewModel.saveTask();

        // Assert
        expect(result, true);
        expect(viewModel.state, TaskCreateEditState.saved);
        expect(viewModel.task, isNotNull);
        expect(viewModel.task!.title, 'New Task');
      });

      test('should handle saving state correctly', () async {
        // Arrange
        viewModel.updateTitle('New Task');
        viewModel.updateProjectId('project1');
        
        mockRepository.setDelayResponse(true);
        mockRepository.setCreateTaskResponse(TaskDto(
          id: '1',
          title: 'New Task',
          description: '',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project1',
          creatorId: 'creator1',
        ));

        // Act
        final future = viewModel.saveTask();
        
        // Assert - Check saving state
        expect(viewModel.state, TaskCreateEditState.saving);
        expect(viewModel.isSaving, true);
        
        // Complete the operation
        final result = await future;
        expect(result, true);
        expect(viewModel.isSaving, false);
      });

      test('should update existing task successfully', () async {
        // Arrange
        final originalTask = TaskDto(
          id: '1',
          title: 'Original Task',
          description: 'Original Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project1',
          creatorId: 'creator1',
        );

        mockRepository.setGetTaskResponse(originalTask);
        await viewModel.initializeForEdit('1');

        viewModel.updateTitle('Updated Task');
        viewModel.updatePriority(Priority.high);

        final updatedTask = originalTask.copyWith(
          title: 'Updated Task',
          priority: Priority.high,
        );

        mockRepository.setUpdateTaskResponse(updatedTask);

        // Act
        final result = await viewModel.saveTask();

        // Assert
        expect(result, true);
        expect(viewModel.state, TaskCreateEditState.saved);
        expect(viewModel.task!.title, 'Updated Task');
        expect(viewModel.task!.priority, Priority.high);
      });

      test('should not update when no changes are made', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task Title',
          description: 'Task Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project1',
          creatorId: 'creator1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.initializeForEdit('1');

        // Act - No changes made
        final result = await viewModel.saveTask();

        // Assert
        expect(result, true);
        expect(viewModel.state, TaskCreateEditState.saved);
        expect(mockRepository.lastUpdateRequest, isNull); // No update request should be made
      });

      test('should fail validation when required fields are missing', () async {
        // Arrange - No title or project ID
        viewModel.updateTitle('');
        viewModel.updateProjectId(null);

        // Act
        final result = await viewModel.saveTask();

        // Assert
        expect(result, false);
        expect(viewModel.state, TaskCreateEditState.error);
        expect(viewModel.errorMessage, contains('required fields'));
      });

      test('should handle create error correctly', () async {
        // Arrange
        viewModel.updateTitle('New Task');
        viewModel.updateProjectId('project1');
        
        mockRepository.setShouldThrowError(true);

        // Act
        final result = await viewModel.saveTask();

        // Assert
        expect(result, false);
        expect(viewModel.state, TaskCreateEditState.error);
        expect(viewModel.errorMessage, contains('Failed to create task'));
      });

      test('should handle update error correctly', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Original Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project1',
          creatorId: 'creator1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.initializeForEdit('1');

        viewModel.updateTitle('Updated Task');
        mockRepository.setShouldThrowError(true);

        // Act
        final result = await viewModel.saveTask();

        // Assert
        expect(result, false);
        expect(viewModel.state, TaskCreateEditState.error);
        expect(viewModel.errorMessage, contains('Failed to update task'));
      });
    });

    group('Delete Task', () {
      test('should delete task successfully', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task to Delete',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project1',
          creatorId: 'creator1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.initializeForEdit('1');

        mockRepository.setDeleteTaskSuccess(true);

        // Act
        final result = await viewModel.deleteTask();

        // Assert
        expect(result, true);
        expect(viewModel.state, TaskCreateEditState.deleted);
      });

      test('should handle deleting state correctly', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project1',
          creatorId: 'creator1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.initializeForEdit('1');

        mockRepository.setDelayResponse(true);
        mockRepository.setDeleteTaskSuccess(true);

        // Act
        final future = viewModel.deleteTask();
        
        // Assert - Check deleting state
        expect(viewModel.state, TaskCreateEditState.deleting);
        expect(viewModel.isDeleting, true);
        
        // Complete the operation
        final result = await future;
        expect(result, true);
        expect(viewModel.isDeleting, false);
      });

      test('should not delete when no task is loaded', () async {
        // Act
        final result = await viewModel.deleteTask();

        // Assert
        expect(result, false);
        expect(mockRepository.lastDeleteTaskId, isNull);
      });

      test('should handle delete error correctly', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project1',
          creatorId: 'creator1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.initializeForEdit('1');

        mockRepository.setShouldThrowError(true);

        // Act
        final result = await viewModel.deleteTask();

        // Assert
        expect(result, false);
        expect(viewModel.state, TaskCreateEditState.error);
        expect(viewModel.errorMessage, contains('Failed to delete task'));
      });
    });

    group('Error Handling', () {
      test('should clear error correctly', () async {
        // Arrange
        mockRepository.setShouldThrowError(true);
        await viewModel.initializeForEdit('1');
        expect(viewModel.state, TaskCreateEditState.error);

        // Act
        viewModel.clearError();

        // Assert
        expect(viewModel.state, TaskCreateEditState.initial);
        expect(viewModel.errorMessage, isNull);
      });

      test('should clear error and return to loaded state when task exists', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task',
          description: 'Description',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project1',
          creatorId: 'creator1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.initializeForEdit('1');

        // Make changes to trigger an update
        viewModel.updateTitle('Modified Task');
        
        // Simulate error
        mockRepository.setShouldThrowError(true);
        await viewModel.saveTask();
        expect(viewModel.state, TaskCreateEditState.error);

        // Act
        viewModel.clearError();

        // Assert
        expect(viewModel.state, TaskCreateEditState.loaded);
        expect(viewModel.errorMessage, isNull);
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
          projectId: 'project1',
          creatorId: 'creator1',
        );

        mockRepository.setGetTaskResponse(task);
        await viewModel.initializeForEdit('1');

        viewModel.updateTitle('Modified Title');
        expect(viewModel.hasTask, true);
        expect(viewModel.hasChanges, true);

        // Act
        viewModel.reset();

        // Assert
        expect(viewModel.state, TaskCreateEditState.initial);
        expect(viewModel.task, isNull);
        expect(viewModel.title, isEmpty);
        expect(viewModel.description, isEmpty);
        expect(viewModel.priority, Priority.medium);
        expect(viewModel.dueDate, isNull);
        expect(viewModel.projectId, isNull);
        expect(viewModel.assigneeId, isNull);
        expect(viewModel.errorMessage, isNull);
        expect(viewModel.hasTask, false);
        expect(viewModel.hasChanges, false);
      });
    });

    group('Edge Cases', () {
      test('should handle empty description correctly', () {
        viewModel.updateTitle('Task Title');
        viewModel.updateDescription('   '); // Whitespace only
        viewModel.updateProjectId('project1');

        expect(viewModel.isValid, true);
        expect(viewModel.description, '   ');
      });

      test('should handle task with null description', () async {
        // Arrange
        final task = TaskDto(
          id: '1',
          title: 'Task',
          description: '',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project1',
          creatorId: 'creator1',
        );

        mockRepository.setGetTaskResponse(task);

        // Act
        await viewModel.initializeForEdit('1');

        // Assert
        expect(viewModel.description, isEmpty);
      });

      test('should handle task creation with minimum required fields', () async {
        // Arrange
        viewModel.updateTitle('Minimal Task');
        viewModel.updateProjectId('project1');
        // Leave description empty, no due date, no assignee

        final createdTask = TaskDto(
          id: '1',
          title: 'Minimal Task',
          description: '',
          status: TaskStatus.todo,
          priority: Priority.medium,
          projectId: 'project1',
          creatorId: 'creator1',
        );

        mockRepository.setCreateTaskResponse(createdTask);

        // Act
        final result = await viewModel.saveTask();

        // Assert
        expect(result, true);
        expect(viewModel.task!.title, 'Minimal Task');
        expect(viewModel.task!.description, '');
      });
    });
  });
} 