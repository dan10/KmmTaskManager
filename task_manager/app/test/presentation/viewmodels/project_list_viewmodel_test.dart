import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_shared/models.dart';

import '../../../lib/presentation/viewmodels/project_list_viewmodel.dart';
import '../../mocks/mock_project_repository.dart';

void main() {
  group('ProjectListViewModel', () {
    late ProjectListViewModel viewModel;
    late MockProjectRepository mockRepository;

    setUp(() {
      mockRepository = MockProjectRepository();
      viewModel = ProjectListViewModel(mockRepository);
    });

    tearDown(() {
      mockRepository.reset();
    });

    group('Initial State', () {
      test('should have correct initial state', () {
        expect(viewModel.state, ProjectListState.initial);
        expect(viewModel.projects, isEmpty);
        expect(viewModel.errorMessage, isNull);
        expect(viewModel.currentPage, 0);
        expect(viewModel.totalElements, 0);
        expect(viewModel.totalPages, 0);
        expect(viewModel.hasMorePages, true);
        expect(viewModel.searchQuery, '');
        expect(viewModel.totalProjects, 0);
        expect(viewModel.completedProjects, 0);
        expect(viewModel.averageCompletion, 0.0);
      });

      test('should have correct state checks', () {
        expect(viewModel.isLoading, false);
        expect(viewModel.isLoadingMore, false);
        expect(viewModel.isRefreshing, false);
        expect(viewModel.hasError, false);
        expect(viewModel.isEmpty, false);
        expect(viewModel.isNotEmpty, false);
      });
    });

    group('Initialize', () {
      test('should load projects on initialize', () async {
        final projects = [
          const Project(
            id: '1',
            name: 'Project 1',
            completed: 5,
            inProgress: 3,
            total: 10,
            memberIds: [],
          ),
        ];

        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: projects,
            page: 0,
            size: 10,
            total: 1,
            totalPages: 1,
          ),
        );

        await viewModel.initialize();

        expect(viewModel.state, ProjectListState.loaded);
        expect(viewModel.projects, projects);
        expect(viewModel.totalElements, 1);
        expect(viewModel.totalPages, 1);
        expect(viewModel.hasMorePages, false);
        expect(viewModel.currentPage, 1);
      });

      test('should handle error on initialize', () async {
        mockRepository.setShouldThrowError(true);

        await viewModel.initialize();

        expect(viewModel.state, ProjectListState.error);
        expect(viewModel.errorMessage, contains('Failed to load projects'));
        expect(viewModel.projects, isEmpty);
      });
    });

    group('Load Projects', () {
      test('should load first page successfully', () async {
        final projects = [
          const Project(
            id: '1',
            name: 'Project 1',
            completed: 2,
            inProgress: 1,
            total: 5,
            memberIds: [],
          ),
        ];

        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: projects,
            page: 0,
            size: 10,
            total: 1,
            totalPages: 1,
          ),
        );

        await viewModel.loadProjects(refresh: true);

        expect(viewModel.state, ProjectListState.loaded);
        expect(viewModel.projects, projects);
        expect(viewModel.totalProjects, 1);
        expect(viewModel.completedProjects, 0);
        expect(viewModel.averageCompletion, 0.4);
      });

      test('should append projects when loading more pages', () async {
        // First page
        final firstPageProjects = [
          const Project(
            id: '1',
            name: 'Project 1',
            completed: 0,
            inProgress: 0,
            total: 0,
            memberIds: [],
          ),
        ];

        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: firstPageProjects,
            page: 0,
            size: 1,
            total: 2,
            totalPages: 2,
          ),
        );

        await viewModel.loadProjects(refresh: true);

        expect(viewModel.projects.length, 1);
        expect(viewModel.hasMorePages, true);

        // Second page
        final secondPageProjects = [
          const Project(
            id: '2',
            name: 'Project 2',
            completed: 0,
            inProgress: 0,
            total: 0,
            memberIds: [],
          ),
        ];

        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: secondPageProjects,
            page: 1,
            size: 1,
            total: 2,
            totalPages: 2,
          ),
        );

        await viewModel.loadProjects();

        expect(viewModel.projects.length, 2);
        expect(viewModel.projects[0].id, '1');
        expect(viewModel.projects[1].id, '2');
        expect(viewModel.hasMorePages, false);
      });

      test('should not load more when already loading', () async {
        mockRepository.setDelayResponse(true);
        
        // Start loading
        final future1 = viewModel.loadProjects();
        
        // Try to load more while already loading
        await viewModel.loadMore();
        
        await future1;

        // Should only have made one call
        expect(mockRepository.lastProjectId, isNull);
      });

      test('should not load more when no more pages', () async {
        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: [],
            page: 0,
            size: 10,
            total: 0,
            totalPages: 0,
          ),
        );

        await viewModel.loadProjects(refresh: true);
        
        // Reset to track new calls
        mockRepository.reset();
        
        await viewModel.loadMore();

        // Should not have made any new calls
        expect(mockRepository.lastProjectId, isNull);
      });
    });

    group('Refresh', () {
      test('should refresh projects list', () async {
        // Initial load
        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: [
              const Project(
                id: '1',
                name: 'Project 1',
                completed: 0,
                inProgress: 0,
                total: 0,
                memberIds: [],
              ),
            ],
            page: 0,
            size: 10,
            total: 1,
            totalPages: 1,
          ),
        );

        await viewModel.initialize();
        expect(viewModel.projects.length, 1);

        // Refresh with new data
        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: [
              const Project(
                id: '2',
                name: 'Project 2',
                completed: 0,
                inProgress: 0,
                total: 0,
                memberIds: [],
              ),
            ],
            page: 0,
            size: 10,
            total: 1,
            totalPages: 1,
          ),
        );

        await viewModel.refresh();

        expect(viewModel.state, ProjectListState.loaded);
        expect(viewModel.projects.length, 1);
        expect(viewModel.projects[0].id, '2');
      });

      test('should set refreshing state during refresh', () async {
        // First load some projects
        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: [
              const Project(
                id: '1',
                name: 'Project 1',
                completed: 0,
                inProgress: 0,
                total: 0,
                memberIds: [],
              ),
            ],
            page: 0,
            size: 10,
            total: 1,
            totalPages: 1,
          ),
        );

        await viewModel.initialize();

        mockRepository.setDelayResponse(true);

        final future = viewModel.refresh();
        
        expect(viewModel.state, ProjectListState.refreshing);
        expect(viewModel.isRefreshing, true);

        await future;

        expect(viewModel.state, ProjectListState.loaded);
        expect(viewModel.isRefreshing, false);
      });
    });

    group('Search', () {
      test('should search projects with query', () async {
        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: [
              const Project(
                id: '1',
                name: 'Test Project',
                completed: 0,
                inProgress: 0,
                total: 0,
                memberIds: [],
              ),
            ],
            page: 0,
            size: 10,
            total: 1,
            totalPages: 1,
          ),
        );

        await viewModel.search('test');

        expect(viewModel.searchQuery, 'test');
        expect(viewModel.projects.length, 1);
        expect(viewModel.projects[0].name, 'Test Project');
      });

      test('should not search if query is the same', () async {
        await viewModel.search('test');
        
        mockRepository.reset();
        
        await viewModel.search('test');

        // Should not have made a new call
        expect(mockRepository.lastProjectId, isNull);
      });

      test('should clear search', () async {
        await viewModel.search('test');
        expect(viewModel.searchQuery, 'test');

        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: [],
            page: 0,
            size: 10,
            total: 0,
            totalPages: 0,
          ),
        );

        await viewModel.clearSearch();

        expect(viewModel.searchQuery, '');
      });

      test('should not clear search if already empty', () async {
        expect(viewModel.searchQuery, '');
        
        mockRepository.reset();
        
        await viewModel.clearSearch();

        // Should not have made a call
        expect(mockRepository.lastProjectId, isNull);
      });
    });

    group('Create Project', () {
      test('should create project successfully', () async {
        final newProject = const Project(
          id: 'new-id',
          name: 'New Project',
          description: 'New Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setCreateProjectResponse(newProject);

        final result = await viewModel.createProject(
          name: 'New Project',
          description: 'New Description',
        );

        expect(result, true);
        expect(viewModel.projects.first, newProject);
        expect(viewModel.totalElements, 1);
        expect(mockRepository.lastCreateRequest?.name, 'New Project');
        expect(mockRepository.lastCreateRequest?.description, 'New Description');
      });

      test('should handle empty description', () async {
        final newProject = const Project(
          id: 'new-id',
          name: 'New Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setCreateProjectResponse(newProject);

        final result = await viewModel.createProject(
          name: 'New Project',
          description: '',
        );

        expect(result, true);
        expect(mockRepository.lastCreateRequest?.description, isNull);
      });

      test('should handle create project error', () async {
        mockRepository.setShouldThrowError(true);

        final result = await viewModel.createProject(
          name: 'New Project',
        );

        expect(result, false);
        expect(viewModel.state, ProjectListState.error);
        expect(viewModel.errorMessage, contains('Failed to create project'));
      });
    });

    group('Update Project', () {
      test('should update project successfully', () async {
        // First load the project through the repository
        final initialProject = const Project(
          id: '1',
          name: 'Original Project',
          description: 'Original Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: [initialProject],
            page: 0,
            size: 10,
            total: 1,
            totalPages: 1,
          ),
        );

        await viewModel.initialize();

        final updatedProject = const Project(
          id: '1',
          name: 'Updated Project',
          description: 'Updated Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1'],
        );

        mockRepository.setUpdateProjectResponse(updatedProject);

        final result = await viewModel.updateProject(
          projectId: '1',
          name: 'Updated Project',
          description: 'Updated Description',
          memberIds: ['user1'],
        );

        expect(result, true);
        expect(viewModel.projects.first.name, 'Updated Project');
        expect(viewModel.projects.first.description, 'Updated Description');
        expect(viewModel.projects.first.memberIds, ['user1']);
      });

      test('should handle update project error', () async {
        mockRepository.setShouldThrowError(true);

        final result = await viewModel.updateProject(
          projectId: '1',
          name: 'Updated Project',
        );

        expect(result, false);
        expect(viewModel.state, ProjectListState.error);
        expect(viewModel.errorMessage, contains('Failed to update project'));
      });
    });

    group('Delete Project', () {
      test('should delete project successfully', () async {
        // Add initial project
        final project = const Project(
          id: '1',
          name: 'Project to Delete',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        // First load the project through the repository
        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: [project],
            page: 0,
            size: 10,
            total: 1,
            totalPages: 1,
          ),
        );

        await viewModel.initialize();
        expect(viewModel.totalElements, 1);

        final result = await viewModel.deleteProject('1');

        expect(result, true);
        expect(viewModel.projects, isEmpty);
        expect(viewModel.totalElements, 0);
        expect(mockRepository.lastDeleteProjectId, '1');
      });

      test('should handle delete project error', () async {
        mockRepository.setShouldThrowError(true);

        final result = await viewModel.deleteProject('1');

        expect(result, false);
        expect(viewModel.state, ProjectListState.error);
        expect(viewModel.errorMessage, contains('Failed to delete project'));
      });
    });

    group('Member Management', () {
      test('should add member to project successfully', () async {
        // First load the project through the repository
        final initialProject = const Project(
          id: '1',
          name: 'Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: [initialProject],
            page: 0,
            size: 10,
            total: 1,
            totalPages: 1,
          ),
        );

        await viewModel.initialize();

        final updatedProject = const Project(
          id: '1',
          name: 'Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1'],
        );

        mockRepository.setAddMemberResponse(updatedProject);

        final result = await viewModel.addMember('1', 'user1');

        expect(result, true);
        expect(viewModel.projects.first.memberIds, ['user1']);
        expect(mockRepository.lastAddMemberProjectId, '1');
        expect(mockRepository.lastAddMemberUserId, 'user1');
      });

      test('should remove member from project successfully', () async {
        // First load the project with member through the repository
        final initialProject = const Project(
          id: '1',
          name: 'Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1'],
        );

        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: [initialProject],
            page: 0,
            size: 10,
            total: 1,
            totalPages: 1,
          ),
        );

        await viewModel.initialize();

        final updatedProject = const Project(
          id: '1',
          name: 'Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        mockRepository.setRemoveMemberResponse(updatedProject);

        final result = await viewModel.removeMember('1', 'user1');

        expect(result, true);
        expect(viewModel.projects.first.memberIds, isEmpty);
        expect(mockRepository.lastRemoveMemberProjectId, '1');
        expect(mockRepository.lastRemoveMemberUserId, 'user1');
      });

      test('should handle add member error', () async {
        mockRepository.setShouldThrowError(true);

        final result = await viewModel.addMember('1', 'user1');

        expect(result, false);
        expect(viewModel.state, ProjectListState.error);
        expect(viewModel.errorMessage, contains('Failed to add member'));
      });

      test('should handle remove member error', () async {
        mockRepository.setShouldThrowError(true);

        final result = await viewModel.removeMember('1', 'user1');

        expect(result, false);
        expect(viewModel.state, ProjectListState.error);
        expect(viewModel.errorMessage, contains('Failed to remove member'));
      });
    });

    group('Get Project', () {
      test('should return project by ID', () async {
        final project = const Project(
          id: '1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
        );

        // First load the project through the repository
        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: [project],
            page: 0,
            size: 10,
            total: 1,
            totalPages: 1,
          ),
        );

        await viewModel.initialize();

        final result = viewModel.getProject('1');

        expect(result, project);
      });

      test('should return null for non-existent project', () {
        final result = viewModel.getProject('non-existent');

        expect(result, isNull);
      });
    });

    group('Statistics', () {
      test('should calculate statistics correctly', () async {
        final projects = [
          const Project(
            id: '1',
            name: 'Project 1',
            completed: 5,
            inProgress: 3,
            total: 10,
            memberIds: [],
          ),
          const Project(
            id: '2',
            name: 'Project 2',
            completed: 10,
            inProgress: 0,
            total: 10,
            memberIds: [],
          ),
          const Project(
            id: '3',
            name: 'Project 3',
            completed: 0,
            inProgress: 0,
            total: 0,
            memberIds: [],
          ),
        ];

        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: projects,
            page: 0,
            size: 10,
            total: 3,
            totalPages: 1,
          ),
        );

        await viewModel.initialize();

        expect(viewModel.totalProjects, 3);
        expect(viewModel.completedProjects, 1); // Only project 2 is completed
        expect(viewModel.averageCompletion, 0.75); // (0.5 + 1.0) / 2 = 0.75
      });

      test('should handle projects with no tasks', () async {
        final projects = [
          const Project(
            id: '1',
            name: 'Project 1',
            completed: 0,
            inProgress: 0,
            total: 0,
            memberIds: [],
          ),
        ];

        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: projects,
            page: 0,
            size: 10,
            total: 1,
            totalPages: 1,
          ),
        );

        await viewModel.initialize();

        expect(viewModel.totalProjects, 1);
        expect(viewModel.completedProjects, 0);
        expect(viewModel.averageCompletion, 0.0);
      });
    });

    group('Error Handling', () {
      test('should clear error', () async {
        // Trigger an error first
        mockRepository.setShouldThrowError(true);
        await viewModel.initialize();
        
        expect(viewModel.hasError, true);
        expect(viewModel.errorMessage, isNotNull);

        viewModel.clearError();

        expect(viewModel.hasError, false);
        expect(viewModel.errorMessage, isNull);
        expect(viewModel.state, ProjectListState.initial);
      });

      test('should clear error and set loaded state when projects exist', () async {
        // Load some projects first
        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: [
              const Project(
                id: '1',
                name: 'Project 1',
                completed: 0,
                inProgress: 0,
                total: 0,
                memberIds: [],
              ),
            ],
            page: 0,
            size: 10,
            total: 1,
            totalPages: 1,
          ),
        );

        await viewModel.initialize();

        // Trigger an error
        mockRepository.setShouldThrowError(true);
        await viewModel.createProject(name: 'Test');
        expect(viewModel.state, ProjectListState.error);

        viewModel.clearError();

        expect(viewModel.state, ProjectListState.loaded);
      });
    });

    group('Reset', () {
      test('should reset to initial state', () async {
        // Load some data first
        mockRepository.setGetProjectsResponse(
          PaginatedResponse<Project>(
            items: [
              const Project(
                id: '1',
                name: 'Project 1',
                completed: 0,
                inProgress: 0,
                total: 0,
                memberIds: [],
              ),
            ],
            page: 0,
            size: 10,
            total: 1,
            totalPages: 1,
          ),
        );

        await viewModel.initialize();
        
        // Verify data is loaded
        expect(viewModel.projects.isNotEmpty, true);

        viewModel.reset();

        expect(viewModel.state, ProjectListState.initial);
        expect(viewModel.projects, isEmpty);
        expect(viewModel.currentPage, 0);
        expect(viewModel.totalElements, 0);
        expect(viewModel.totalPages, 0);
        expect(viewModel.hasMorePages, true);
        expect(viewModel.searchQuery, '');
        expect(viewModel.errorMessage, isNull);
        expect(viewModel.totalProjects, 0);
        expect(viewModel.completedProjects, 0);
        expect(viewModel.averageCompletion, 0.0);
      });
    });
  });
} 