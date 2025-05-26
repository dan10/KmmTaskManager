import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_shared/models.dart';

import '../../../lib/presentation/viewmodels/project_create_edit_viewmodel.dart';
import '../../mocks/mock_project_repository.dart';

void main() {
  group('ProjectCreateEditViewModel', () {
    late ProjectCreateEditViewModel viewModel;
    late MockProjectRepository mockRepository;

    setUp(() {
      mockRepository = MockProjectRepository();
      viewModel = ProjectCreateEditViewModel(mockRepository);
    });

    tearDown(() {
      mockRepository.reset();
    });

    group('Initial State', () {
      test('should have correct initial state', () {
        expect(viewModel.state, ProjectCreateEditState.initial);
        expect(viewModel.project, isNull);
        expect(viewModel.errorMessage, isNull);
        expect(viewModel.name, '');
        expect(viewModel.description, '');
        expect(viewModel.memberIds, isEmpty);
      });

      test('should have correct state checks', () {
        expect(viewModel.isLoading, false);
        expect(viewModel.isSaving, false);
        expect(viewModel.isDeleting, false);
        expect(viewModel.hasProject, false);
        expect(viewModel.isEditing, false);
        expect(viewModel.isCreating, true);
        expect(viewModel.isValid, false);
        expect(viewModel.hasChanges, false);
      });
    });

    group('Initialize for Create', () {
      test('should initialize for creating new project', () {
        viewModel.initializeForCreate();

        expect(viewModel.state, ProjectCreateEditState.initial);
        expect(viewModel.project, isNull);
        expect(viewModel.name, '');
        expect(viewModel.description, '');
        expect(viewModel.memberIds, isEmpty);
        expect(viewModel.isCreating, true);
        expect(viewModel.isEditing, false);
      });
    });

    group('Initialize for Edit', () {
      test('should initialize for editing existing project', () async {
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          description: 'Test Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1', 'user2'],
        );

        mockRepository.setGetProjectResponse(project);

        await viewModel.initializeForEdit('project1');

        expect(viewModel.state, ProjectCreateEditState.loaded);
        expect(viewModel.project, project);
        expect(viewModel.name, 'Test Project');
        expect(viewModel.description, 'Test Description');
        expect(viewModel.memberIds, ['user1', 'user2']);
        expect(viewModel.isCreating, false);
        expect(viewModel.isEditing, true);
        expect(viewModel.hasChanges, false);
      });

      test('should handle error when loading project for edit', () async {
        mockRepository.setShouldThrowError(true);

        await viewModel.initializeForEdit('project1');

        expect(viewModel.state, ProjectCreateEditState.error);
        expect(viewModel.errorMessage, contains('Failed to load project'));
        expect(viewModel.project, isNull);
      });
    });

    group('Form Field Updates', () {
      test('should update name field', () {
        viewModel.updateName('New Project Name');

        expect(viewModel.name, 'New Project Name');
        expect(viewModel.isValid, true);
        expect(viewModel.hasChanges, true);
      });

      test('should update description field', () {
        viewModel.updateDescription('New Description');

        expect(viewModel.description, 'New Description');
        expect(viewModel.hasChanges, true);
      });

      test('should update member IDs', () {
        viewModel.updateMemberIds(['user1', 'user2']);

        expect(viewModel.memberIds, ['user1', 'user2']);
        expect(viewModel.hasChanges, true);
      });

      test('should add member', () {
        viewModel.addMember('user1');
        expect(viewModel.memberIds, ['user1']);

        viewModel.addMember('user2');
        expect(viewModel.memberIds, ['user1', 'user2']);

        // Should not add duplicate
        viewModel.addMember('user1');
        expect(viewModel.memberIds, ['user1', 'user2']);
      });

      test('should remove member', () {
        viewModel.updateMemberIds(['user1', 'user2', 'user3']);

        viewModel.removeMember('user2');
        expect(viewModel.memberIds, ['user1', 'user3']);

        viewModel.removeMember('nonexistent');
        expect(viewModel.memberIds, ['user1', 'user3']);
      });
    });

    group('Validation', () {
      test('should validate name correctly', () {
        expect(viewModel.validateName(null), 'Project name is required');
        expect(viewModel.validateName(''), 'Project name is required');
        expect(viewModel.validateName('   '), 'Project name is required');
        expect(viewModel.validateName('AB'), 'Project name must be at least 3 characters');
        expect(viewModel.validateName('A' * 101), 'Project name cannot exceed 100 characters');
        expect(viewModel.validateName('Valid Name'), isNull);
      });

      test('should validate description correctly', () {
        expect(viewModel.validateDescription(null), isNull);
        expect(viewModel.validateDescription(''), isNull);
        expect(viewModel.validateDescription('Valid description'), isNull);
        expect(viewModel.validateDescription('A' * 501), 'Description cannot exceed 500 characters');
      });

      test('should get validation errors', () {
        viewModel.updateName('AB');
        viewModel.updateDescription('A' * 501);

        final errors = viewModel.getValidationErrors();

        expect(errors['name'], 'Project name must be at least 3 characters');
        expect(errors['description'], 'Description cannot exceed 500 characters');
      });

      test('should return empty errors for valid form', () {
        viewModel.updateName('Valid Project Name');
        viewModel.updateDescription('Valid description');

        final errors = viewModel.getValidationErrors();

        expect(errors, isEmpty);
      });

      test('should check form validity', () {
        expect(viewModel.isValid, false);

        viewModel.updateName('Valid Name');
        expect(viewModel.isValid, true);

        viewModel.updateName('');
        expect(viewModel.isValid, false);
      });
    });

    group('Save Project - Create Mode', () {
      test('should create project successfully', () async {
        final createdProject = const Project(
          id: 'new-id',
          name: 'New Project',
          description: 'New Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setCreateProjectResponse(createdProject);

        viewModel.updateName('New Project');
        viewModel.updateDescription('New Description');

        final result = await viewModel.saveProject();

        expect(result, true);
        expect(viewModel.state, ProjectCreateEditState.saved);
        expect(viewModel.project, createdProject);
        expect(mockRepository.lastCreateRequest?.name, 'New Project');
        expect(mockRepository.lastCreateRequest?.description, 'New Description');
      });

      test('should create project with members', () async {
        final createdProject = const Project(
          id: 'new-id',
          name: 'New Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        final updatedProject = const Project(
          id: 'new-id',
          name: 'New Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1', 'user2'],
        );

        mockRepository.setCreateProjectResponse(createdProject);
        mockRepository.setUpdateProjectResponse(updatedProject);

        viewModel.updateName('New Project');
        viewModel.addMember('user1');
        viewModel.addMember('user2');

        final result = await viewModel.saveProject();

        expect(result, true);
        expect(viewModel.project, updatedProject);
        expect(mockRepository.lastUpdateRequest?.memberIds, ['user1', 'user2']);
      });

      test('should handle empty description in create mode', () async {
        final createdProject = const Project(
          id: 'new-id',
          name: 'New Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setCreateProjectResponse(createdProject);

        viewModel.updateName('New Project');
        viewModel.updateDescription('');

        final result = await viewModel.saveProject();

        expect(result, true);
        expect(mockRepository.lastCreateRequest?.description, isNull);
      });

      test('should not save invalid project', () async {
        viewModel.updateName('');

        final result = await viewModel.saveProject();

        expect(result, false);
        expect(viewModel.state, ProjectCreateEditState.error);
        expect(viewModel.errorMessage, 'Please fill in all required fields');
      });

      test('should handle create project error', () async {
        mockRepository.setShouldThrowError(true);

        viewModel.updateName('New Project');

        final result = await viewModel.saveProject();

        expect(result, false);
        expect(viewModel.state, ProjectCreateEditState.error);
        expect(viewModel.errorMessage, contains('Failed to create project'));
      });
    });

    group('Save Project - Edit Mode', () {
      test('should update project successfully', () async {
        // Initialize for edit
        final originalProject = const Project(
          id: 'project1',
          name: 'Original Name',
          description: 'Original Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1'],
        );

        mockRepository.setGetProjectResponse(originalProject);
        await viewModel.initializeForEdit('project1');

        // Update fields
        viewModel.updateName('Updated Name');
        viewModel.updateDescription('Updated Description');
        viewModel.addMember('user2');

        final updatedProject = const Project(
          id: 'project1',
          name: 'Updated Name',
          description: 'Updated Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1', 'user2'],
        );

        mockRepository.setUpdateProjectResponse(updatedProject);

        final result = await viewModel.saveProject();

        expect(result, true);
        expect(viewModel.state, ProjectCreateEditState.saved);
        expect(viewModel.project, updatedProject);
        expect(mockRepository.lastUpdateRequest?.name, 'Updated Name');
        expect(mockRepository.lastUpdateRequest?.description, 'Updated Description');
        expect(mockRepository.lastUpdateRequest?.memberIds, ['user1', 'user2']);
      });

      test('should only update changed fields', () async {
        // Initialize for edit
        final originalProject = const Project(
          id: 'project1',
          name: 'Original Name',
          description: 'Original Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1'],
        );

        mockRepository.setGetProjectResponse(originalProject);
        await viewModel.initializeForEdit('project1');

        // Only update name
        viewModel.updateName('Updated Name');

        final updatedProject = const Project(
          id: 'project1',
          name: 'Updated Name',
          description: 'Original Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1'],
        );

        mockRepository.setUpdateProjectResponse(updatedProject);

        final result = await viewModel.saveProject();

        expect(result, true);
        expect(mockRepository.lastUpdateRequest?.name, 'Updated Name');
        expect(mockRepository.lastUpdateRequest?.description, isNull);
        expect(mockRepository.lastUpdateRequest?.memberIds, isNull);
      });

      test('should handle no changes in edit mode', () async {
        // Initialize for edit
        final originalProject = const Project(
          id: 'project1',
          name: 'Original Name',
          description: 'Original Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1'],
        );

        mockRepository.setGetProjectResponse(originalProject);
        await viewModel.initializeForEdit('project1');

        // Don't change anything
        final result = await viewModel.saveProject();

        expect(result, true);
        expect(viewModel.state, ProjectCreateEditState.saved);
        // Should not have made an update request
        expect(mockRepository.lastUpdateRequest, isNull);
      });

      test('should handle update project error', () async {
        // Initialize for edit
        final originalProject = const Project(
          id: 'project1',
          name: 'Original Name',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(originalProject);
        await viewModel.initializeForEdit('project1');

        viewModel.updateName('Updated Name');

        mockRepository.setShouldThrowError(true);

        final result = await viewModel.saveProject();

        expect(result, false);
        expect(viewModel.state, ProjectCreateEditState.error);
        expect(viewModel.errorMessage, contains('Failed to update project'));
      });
    });

    group('Delete Project', () {
      test('should delete project successfully', () async {
        // Initialize for edit
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(project);
        await viewModel.initializeForEdit('project1');

        final result = await viewModel.deleteProject();

        expect(result, true);
        expect(viewModel.state, ProjectCreateEditState.deleted);
        expect(mockRepository.lastDeleteProjectId, 'project1');
      });

      test('should not delete when no project loaded', () async {
        final result = await viewModel.deleteProject();

        expect(result, false);
        expect(viewModel.project, isNull);
      });

      test('should handle delete project error', () async {
        // Initialize for edit
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(project);
        await viewModel.initializeForEdit('project1');

        mockRepository.setShouldThrowError(true);

        final result = await viewModel.deleteProject();

        expect(result, false);
        expect(viewModel.state, ProjectCreateEditState.error);
        expect(viewModel.errorMessage, contains('Failed to delete project'));
      });
    });

    group('Change Detection', () {
      test('should detect changes in create mode', () {
        expect(viewModel.hasChanges, false);

        viewModel.updateName('Test');
        expect(viewModel.hasChanges, true);

        viewModel.updateName('');
        viewModel.updateDescription('Test Description');
        expect(viewModel.hasChanges, true);

        viewModel.updateDescription('');
        viewModel.addMember('user1');
        expect(viewModel.hasChanges, true);
      });

      test('should detect changes in edit mode', () async {
        // Initialize for edit
        final project = const Project(
          id: 'project1',
          name: 'Original Name',
          description: 'Original Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1'],
        );

        mockRepository.setGetProjectResponse(project);
        await viewModel.initializeForEdit('project1');

        expect(viewModel.hasChanges, false);

        viewModel.updateName('Updated Name');
        expect(viewModel.hasChanges, true);

        viewModel.updateName('Original Name');
        expect(viewModel.hasChanges, false);

        viewModel.updateDescription('Updated Description');
        expect(viewModel.hasChanges, true);

        viewModel.updateDescription('Original Description');
        viewModel.addMember('user2');
        expect(viewModel.hasChanges, true);

        viewModel.removeMember('user2');
        expect(viewModel.hasChanges, false);
      });
    });

    group('Error Handling', () {
      test('should clear error', () async {
        // Trigger an error
        mockRepository.setShouldThrowError(true);
        await viewModel.initializeForEdit('project1');

        expect(viewModel.state, ProjectCreateEditState.error);
        expect(viewModel.errorMessage, isNotNull);

        viewModel.clearError();

        expect(viewModel.errorMessage, isNull);
        expect(viewModel.state, ProjectCreateEditState.initial);
      });

      test('should clear error and set loaded state when project exists', () async {
        // Load project first
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(project);
        await viewModel.initializeForEdit('project1');

        // Trigger an error
        mockRepository.setShouldThrowError(true);
        viewModel.updateName('Updated Name');
        await viewModel.saveProject();
        expect(viewModel.state, ProjectCreateEditState.error);

        viewModel.clearError();

        expect(viewModel.state, ProjectCreateEditState.loaded);
      });
    });

    group('Reset', () {
      test('should reset to initial state', () async {
        // Load some data first
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          description: 'Test Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1'],
        );

        mockRepository.setGetProjectResponse(project);
        await viewModel.initializeForEdit('project1');

        viewModel.updateName('Updated Name');

        expect(viewModel.hasProject, true);
        expect(viewModel.hasChanges, true);

        viewModel.reset();

        expect(viewModel.state, ProjectCreateEditState.initial);
        expect(viewModel.project, isNull);
        expect(viewModel.name, '');
        expect(viewModel.description, '');
        expect(viewModel.memberIds, isEmpty);
        expect(viewModel.errorMessage, isNull);
        expect(viewModel.hasProject, false);
        expect(viewModel.hasChanges, false);
      });
    });

    group('State Transitions', () {
      test('should set saving state during save', () async {
        mockRepository.setDelayResponse(true);

        viewModel.updateName('Test Project');

        final future = viewModel.saveProject();

        expect(viewModel.state, ProjectCreateEditState.saving);
        expect(viewModel.isSaving, true);

        await future;

        expect(viewModel.state, ProjectCreateEditState.saved);
        expect(viewModel.isSaving, false);
      });

      test('should set deleting state during delete', () async {
        // Initialize for edit
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(project);
        await viewModel.initializeForEdit('project1');

        mockRepository.setDelayResponse(true);

        final future = viewModel.deleteProject();

        expect(viewModel.state, ProjectCreateEditState.deleting);
        expect(viewModel.isDeleting, true);

        await future;

        expect(viewModel.state, ProjectCreateEditState.deleted);
        expect(viewModel.isDeleting, false);
      });
    });
  });
} 