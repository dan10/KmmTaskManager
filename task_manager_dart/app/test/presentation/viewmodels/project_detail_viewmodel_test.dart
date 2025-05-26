import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_shared/models.dart';

import '../../../lib/presentation/viewmodels/project_detail_viewmodel.dart';
import '../../mocks/mock_project_repository.dart';

void main() {
  group('ProjectDetailViewModel', () {
    late ProjectDetailViewModel viewModel;
    late MockProjectRepository mockRepository;

    setUp(() {
      mockRepository = MockProjectRepository();
      viewModel = ProjectDetailViewModel(mockRepository);
    });

    tearDown(() {
      mockRepository.reset();
    });

    group('Initial State', () {
      test('should have correct initial state', () {
        expect(viewModel.state, ProjectDetailState.initial);
        expect(viewModel.project, isNull);
        expect(viewModel.errorMessage, isNull);
        expect(viewModel.statistics, isNull);
      });

      test('should have correct state checks', () {
        expect(viewModel.isLoading, false);
        expect(viewModel.isUpdating, false);
        expect(viewModel.isDeleting, false);
        expect(viewModel.hasError, false);
        expect(viewModel.hasProject, false);
      });

      test('should have correct default property values', () {
        expect(viewModel.projectId, '');
        expect(viewModel.projectName, '');
        expect(viewModel.projectDescription, '');
        expect(viewModel.memberIds, isEmpty);
        expect(viewModel.completedTasks, 0);
        expect(viewModel.inProgressTasks, 0);
        expect(viewModel.totalTasks, 0);
        expect(viewModel.completionPercentage, 0.0);
        expect(viewModel.isCompleted, false);
        expect(viewModel.hasMembers, false);
        expect(viewModel.memberCount, 0);
      });
    });

    group('Load Project', () {
      test('should load project successfully', () async {
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          description: 'Test Description',
          completed: 5,
          inProgress: 3,
          total: 10,
          memberIds: ['user1', 'user2'],
        );

        final stats = {
          'totalTasks': 10,
          'completedTasks': 5,
          'inProgressTasks': 3,
          'overdueTasks': 1,
        };

        mockRepository.setGetProjectResponse(project);
        mockRepository.setProjectStatsResponse(stats);

        await viewModel.loadProject('project1');

        expect(viewModel.state, ProjectDetailState.loaded);
        expect(viewModel.project, project);
        expect(viewModel.statistics, stats);
        expect(viewModel.projectId, 'project1');
        expect(viewModel.projectName, 'Test Project');
        expect(viewModel.projectDescription, 'Test Description');
        expect(viewModel.memberIds, ['user1', 'user2']);
        expect(viewModel.completedTasks, 5);
        expect(viewModel.inProgressTasks, 3);
        expect(viewModel.totalTasks, 10);
        expect(viewModel.completionPercentage, 50.0);
        expect(viewModel.isCompleted, false);
        expect(viewModel.hasMembers, true);
        expect(viewModel.memberCount, 2);
      });

      test('should handle completed project', () async {
        final project = const Project(
          id: 'project1',
          name: 'Completed Project',
          completed: 10,
          inProgress: 0,
          total: 10,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(project);

        await viewModel.loadProject('project1');

        expect(viewModel.completionPercentage, 100.0);
        expect(viewModel.isCompleted, true);
        expect(viewModel.hasMembers, false);
      });

      test('should handle project with no tasks', () async {
        final project = const Project(
          id: 'project1',
          name: 'Empty Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(project);

        await viewModel.loadProject('project1');

        expect(viewModel.completionPercentage, 0.0);
        expect(viewModel.isCompleted, false);
      });

      test('should handle load project error', () async {
        mockRepository.setShouldThrowError(true);

        await viewModel.loadProject('project1');

        expect(viewModel.state, ProjectDetailState.error);
        expect(viewModel.errorMessage, contains('Failed to load project'));
        expect(viewModel.project, isNull);
      });

      test('should handle statistics loading error gracefully', () async {
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(project);
        // Set stats to throw error specifically
        mockRepository.setShouldThrowStatsError(true);

        await viewModel.loadProject('project1');

        expect(viewModel.state, ProjectDetailState.loaded);
        expect(viewModel.project, project);
        // Statistics should be null when there's an error
        expect(viewModel.statistics, isNull);
      });
    });

    group('Update Project', () {
      test('should update project successfully', () async {
        // Load initial project
        final initialProject = const Project(
          id: 'project1',
          name: 'Original Name',
          description: 'Original Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(initialProject);
        await viewModel.loadProject('project1');

        // Update project
        final updatedProject = const Project(
          id: 'project1',
          name: 'Updated Name',
          description: 'Updated Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1'],
        );

        mockRepository.setUpdateProjectResponse(updatedProject);

        final result = await viewModel.updateProject(
          name: 'Updated Name',
          description: 'Updated Description',
          memberIds: ['user1'],
        );

        expect(result, true);
        expect(viewModel.state, ProjectDetailState.loaded);
        expect(viewModel.project, updatedProject);
        expect(viewModel.projectName, 'Updated Name');
        expect(viewModel.projectDescription, 'Updated Description');
        expect(viewModel.memberIds, ['user1']);
      });

      test('should not update when no project loaded', () async {
        final result = await viewModel.updateProject(name: 'Test');

        expect(result, false);
        expect(viewModel.project, isNull);
      });

      test('should handle update project error', () async {
        // Load initial project
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(project);
        await viewModel.loadProject('project1');

        // Simulate error
        mockRepository.setShouldThrowError(true);

        final result = await viewModel.updateProject(name: 'Updated Name');

        expect(result, false);
        expect(viewModel.state, ProjectDetailState.error);
        expect(viewModel.errorMessage, contains('Failed to update project'));
      });
    });

    group('Delete Project', () {
      test('should delete project successfully', () async {
        // Load initial project
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(project);
        await viewModel.loadProject('project1');

        final result = await viewModel.deleteProject();

        expect(result, true);
        expect(viewModel.state, ProjectDetailState.initial);
        expect(viewModel.project, isNull);
        expect(viewModel.statistics, isNull);
        expect(mockRepository.lastDeleteProjectId, 'project1');
      });

      test('should not delete when no project loaded', () async {
        final result = await viewModel.deleteProject();

        expect(result, false);
        expect(viewModel.project, isNull);
      });

      test('should handle delete project error', () async {
        // Load initial project
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(project);
        await viewModel.loadProject('project1');

        // Simulate error
        mockRepository.setShouldThrowError(true);

        final result = await viewModel.deleteProject();

        expect(result, false);
        expect(viewModel.state, ProjectDetailState.error);
        expect(viewModel.errorMessage, contains('Failed to delete project'));
      });
    });

    group('Member Management', () {
      test('should add member successfully', () async {
        // Load initial project
        final initialProject = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(initialProject);
        await viewModel.loadProject('project1');

        // Add member
        final updatedProject = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1'],
        );

        mockRepository.setAddMemberResponse(updatedProject);

        final result = await viewModel.addMember('user1');

        expect(result, true);
        expect(viewModel.project, updatedProject);
        expect(viewModel.memberIds, ['user1']);
        expect(mockRepository.lastAddMemberProjectId, 'project1');
        expect(mockRepository.lastAddMemberUserId, 'user1');
      });

      test('should remove member successfully', () async {
        // Load initial project with member
        final initialProject = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1'],
        );

        mockRepository.setGetProjectResponse(initialProject);
        await viewModel.loadProject('project1');

        // Remove member
        final updatedProject = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setRemoveMemberResponse(updatedProject);

        final result = await viewModel.removeMember('user1');

        expect(result, true);
        expect(viewModel.project, updatedProject);
        expect(viewModel.memberIds, isEmpty);
        expect(mockRepository.lastRemoveMemberProjectId, 'project1');
        expect(mockRepository.lastRemoveMemberUserId, 'user1');
      });

      test('should not add member when no project loaded', () async {
        final result = await viewModel.addMember('user1');

        expect(result, false);
        expect(viewModel.project, isNull);
      });

      test('should not remove member when no project loaded', () async {
        final result = await viewModel.removeMember('user1');

        expect(result, false);
        expect(viewModel.project, isNull);
      });

      test('should handle add member error', () async {
        // Load initial project
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(project);
        await viewModel.loadProject('project1');

        // Simulate error
        mockRepository.setShouldThrowError(true);

        final result = await viewModel.addMember('user1');

        expect(result, false);
        expect(viewModel.state, ProjectDetailState.error);
        expect(viewModel.errorMessage, contains('Failed to add member'));
      });

      test('should handle remove member error', () async {
        // Load initial project
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1'],
        );

        mockRepository.setGetProjectResponse(project);
        await viewModel.loadProject('project1');

        // Simulate error
        mockRepository.setShouldThrowError(true);

        final result = await viewModel.removeMember('user1');

        expect(result, false);
        expect(viewModel.state, ProjectDetailState.error);
        expect(viewModel.errorMessage, contains('Failed to remove member'));
      });
    });

    group('Refresh', () {
      test('should refresh project data', () async {
        // Load initial project
        final initialProject = const Project(
          id: 'project1',
          name: 'Original Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(initialProject);
        await viewModel.loadProject('project1');

        expect(viewModel.projectName, 'Original Project');

        // Update mock to return different data
        final updatedProject = const Project(
          id: 'project1',
          name: 'Refreshed Project',
          completed: 5,
          inProgress: 3,
          total: 10,
          memberIds: ['user1'],
        );

        mockRepository.setGetProjectResponse(updatedProject);

        await viewModel.refresh();

        expect(viewModel.state, ProjectDetailState.loaded);
        expect(viewModel.projectName, 'Refreshed Project');
        expect(viewModel.completedTasks, 5);
        expect(viewModel.memberIds, ['user1']);
      });

      test('should not refresh when no project loaded', () async {
        await viewModel.refresh();

        expect(viewModel.project, isNull);
        expect(mockRepository.lastProjectId, isNull);
      });
    });

    group('Utility Methods', () {
      test('should check if user is member', () async {
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1', 'user2'],
        );

        mockRepository.setGetProjectResponse(project);
        await viewModel.loadProject('project1');

        expect(viewModel.isMember('user1'), true);
        expect(viewModel.isMember('user2'), true);
        expect(viewModel.isMember('user3'), false);
      });

      test('should return false for membership when no project loaded', () {
        expect(viewModel.isMember('user1'), false);
      });

      test('should get statistics value safely', () async {
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        final stats = {
          'totalTasks': 10,
          'completedTasks': 5,
          'stringValue': 'test',
        };

        mockRepository.setGetProjectResponse(project);
        mockRepository.setProjectStatsResponse(stats);
        await viewModel.loadProject('project1');

        expect(viewModel.getStatistic<int>('totalTasks'), 10);
        expect(viewModel.getStatistic<int>('completedTasks'), 5);
        expect(viewModel.getStatistic<String>('stringValue'), 'test');
        expect(viewModel.getStatistic<int>('nonExistent'), isNull);
      });

      test('should return null for statistics when no stats loaded', () {
        expect(viewModel.getStatistic<int>('totalTasks'), isNull);
      });
    });

    group('Error Handling', () {
      test('should clear error', () async {
        // Trigger an error
        mockRepository.setShouldThrowError(true);
        await viewModel.loadProject('project1');

        expect(viewModel.hasError, true);
        expect(viewModel.errorMessage, isNotNull);

        viewModel.clearError();

        expect(viewModel.hasError, false);
        expect(viewModel.errorMessage, isNull);
        expect(viewModel.state, ProjectDetailState.initial);
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
        await viewModel.loadProject('project1');

        // Trigger an error
        mockRepository.setShouldThrowError(true);
        await viewModel.updateProject(name: 'Test');
        expect(viewModel.state, ProjectDetailState.error);

        viewModel.clearError();

        expect(viewModel.state, ProjectDetailState.loaded);
      });
    });

    group('Reset', () {
      test('should reset to initial state', () async {
        // Load some data first
        final project = const Project(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectResponse(project);
        await viewModel.loadProject('project1');

        expect(viewModel.hasProject, true);

        viewModel.reset();

        expect(viewModel.state, ProjectDetailState.initial);
        expect(viewModel.project, isNull);
        expect(viewModel.statistics, isNull);
        expect(viewModel.errorMessage, isNull);
        expect(viewModel.hasProject, false);
      });
    });
  });
} 