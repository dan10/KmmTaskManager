import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_shared/models.dart';

import '../../../lib/data/repositories/project_repository.dart';
import '../../mocks/mock_project_api_service.dart';

void main() {
  group('ProjectRepositoryImpl', () {
    late ProjectRepository repository;
    late MockProjectApiService mockApiService;

    setUp(() {
      mockApiService = MockProjectApiService();
      repository = ProjectRepositoryImpl(mockApiService);
    });

    tearDown(() {
      mockApiService.reset();
    });

    group('Get Projects', () {
      test('should return paginated projects successfully', () async {
        final projectDtos = [
          const ProjectResponseDto(
            id: 'project1',
            name: 'Project 1',
            description: 'Description 1',
            completed: 5,
            inProgress: 3,
            total: 10,
            memberIds: ['user1'],
            members: [],
          ),
          const ProjectResponseDto(
            id: 'project2',
            name: 'Project 2',
            completed: 0,
            inProgress: 2,
            total: 5,
            memberIds: [],
            members: [],
          ),
        ];

        final paginatedResponse = PaginatedResponse<ProjectResponseDto>(
          items: projectDtos,
          page: 0,
          size: 10,
          total: 2,
          totalPages: 1,
        );

        mockApiService.setGetProjectsResponse(paginatedResponse);

        final result = await repository.getProjects(page: 0, size: 10);

        expect(result.items.length, 2);
        expect(result.items[0].id, 'project1');
        expect(result.items[0].name, 'Project 1');
        expect(result.items[1].id, 'project2');
        expect(result.page, 0);
        expect(result.size, 10);
        expect(result.total, 2);
        expect(result.totalPages, 1);
      });

      test('should return projects with search query', () async {
        final projectDtos = [
          const ProjectResponseDto(
            id: 'project1',
            name: 'Test Project',
            completed: 0,
            inProgress: 0,
            total: 0,
            memberIds: [],
            members: [],
          ),
        ];

        final paginatedResponse = PaginatedResponse<ProjectResponseDto>(
          items: projectDtos,
          page: 0,
          size: 10,
          total: 1,
          totalPages: 1,
        );

        mockApiService.setGetProjectsResponse(paginatedResponse);

        final result = await repository.getProjects(
          page: 0,
          size: 10,
          query: 'test',
        );

        expect(result.items.length, 1);
        expect(result.items[0].name, 'Test Project');
      });

      test('should handle get projects error', () async {
        mockApiService.setShouldThrowError(true);

        expect(
          () => repository.getProjects(),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to fetch projects'),
          )),
        );
      });
    });

    group('Get Project', () {
      test('should return single project successfully', () async {
        const projectDto = ProjectResponseDto(
          id: 'project1',
          name: 'Test Project',
          description: 'Test Description',
          completed: 5,
          inProgress: 3,
          total: 10,
          memberIds: ['user1', 'user2'],
          members: [],
        );

        mockApiService.setGetProjectResponse(projectDto);

        final result = await repository.getProject('project1');

        expect(result.id, 'project1');
        expect(result.name, 'Test Project');
        expect(result.description, 'Test Description');
        expect(result.completed, 5);
        expect(result.inProgress, 3);
        expect(result.total, 10);
        expect(result.memberIds, ['user1', 'user2']);
        expect(mockApiService.lastProjectId, 'project1');
      });

      test('should validate project ID', () async {
        expect(
          () => repository.getProject(''),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Project ID cannot be empty'),
          )),
        );

        expect(
          () => repository.getProject('   '),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Project ID cannot be empty'),
          )),
        );
      });

      test('should handle get project error', () async {
        mockApiService.setShouldThrowError(true);

        expect(
          () => repository.getProject('project1'),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to fetch project'),
          )),
        );
      });
    });

    group('Create Project', () {
      test('should create project successfully', () async {
        const request = CreateProjectRequestDto(
          name: 'New Project',
          description: 'New Description',
        );

        const projectDto = ProjectResponseDto(
          id: 'new-id',
          name: 'New Project',
          description: 'New Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
          members: [],
        );

        mockApiService.setCreateProjectResponse(projectDto);

        final result = await repository.createProject(request);

        expect(result.id, 'new-id');
        expect(result.name, 'New Project');
        expect(result.description, 'New Description');
        expect(mockApiService.lastCreateRequest, request);
      });

      test('should validate create request', () async {
        const invalidRequest = CreateProjectRequestDto(
          name: '', // Invalid: empty name
          description: 'Valid description',
        );

        expect(
          () => repository.createProject(invalidRequest),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Validation failed'),
          )),
        );
      });

      test('should handle create project error', () async {
        const request = CreateProjectRequestDto(
          name: 'Valid Project',
        );

        mockApiService.setShouldThrowError(true);

        expect(
          () => repository.createProject(request),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to create project'),
          )),
        );
      });
    });

    group('Update Project', () {
      test('should update project successfully', () async {
        const request = ProjectUpdateRequestDto(
          name: 'Updated Project',
          description: 'Updated Description',
          memberIds: ['user1', 'user2'],
        );

        const projectDto = ProjectResponseDto(
          id: 'project1',
          name: 'Updated Project',
          description: 'Updated Description',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1', 'user2'],
          members: [],
        );

        mockApiService.setUpdateProjectResponse(projectDto);

        final result = await repository.updateProject('project1', request);

        expect(result.id, 'project1');
        expect(result.name, 'Updated Project');
        expect(result.description, 'Updated Description');
        expect(result.memberIds, ['user1', 'user2']);
        expect(mockApiService.lastProjectId, 'project1');
        expect(mockApiService.lastUpdateRequest, request);
      });

      test('should validate project ID', () async {
        const request = ProjectUpdateRequestDto(name: 'Updated Project');

        expect(
          () => repository.updateProject('', request),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Project ID cannot be empty'),
          )),
        );
      });

      test('should validate update request', () async {
        const invalidRequest = ProjectUpdateRequestDto(
          name: 'AB', // Invalid: too short
        );

        expect(
          () => repository.updateProject('project1', invalidRequest),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Validation failed'),
          )),
        );
      });

      test('should validate that updates are provided', () async {
        const emptyRequest = ProjectUpdateRequestDto();

        expect(
          () => repository.updateProject('project1', emptyRequest),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('No updates provided'),
          )),
        );
      });

      test('should handle update project error', () async {
        const request = ProjectUpdateRequestDto(name: 'Updated Project');

        mockApiService.setShouldThrowError(true);

        expect(
          () => repository.updateProject('project1', request),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to update project'),
          )),
        );
      });
    });

    group('Delete Project', () {
      test('should delete project successfully', () async {
        await repository.deleteProject('project1');

        expect(mockApiService.lastDeleteProjectId, 'project1');
      });

      test('should validate project ID', () async {
        expect(
          () => repository.deleteProject(''),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Project ID cannot be empty'),
          )),
        );
      });

      test('should handle delete project error', () async {
        mockApiService.setShouldThrowError(true);

        expect(
          () => repository.deleteProject('project1'),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to delete project'),
          )),
        );
      });
    });

    group('Add Member', () {
      test('should add member successfully', () async {
        const projectDto = ProjectResponseDto(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: ['user1'],
          members: [],
        );

        mockApiService.setAddMemberResponse(projectDto);

        final result = await repository.addMember('project1', 'user1');

        expect(result.id, 'project1');
        expect(result.memberIds, ['user1']);
        expect(mockApiService.lastAddMemberProjectId, 'project1');
        expect(mockApiService.lastAddMemberUserId, 'user1');
      });

      test('should validate project ID', () async {
        expect(
          () => repository.addMember('', 'user1'),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Project ID cannot be empty'),
          )),
        );
      });

      test('should validate user ID', () async {
        expect(
          () => repository.addMember('project1', ''),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('User ID cannot be empty'),
          )),
        );
      });

      test('should handle add member error', () async {
        mockApiService.setShouldThrowError(true);

        expect(
          () => repository.addMember('project1', 'user1'),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to add member'),
          )),
        );
      });
    });

    group('Remove Member', () {
      test('should remove member successfully', () async {
        const projectDto = ProjectResponseDto(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
          members: [],
        );

        mockApiService.setRemoveMemberResponse(projectDto);

        final result = await repository.removeMember('project1', 'user1');

        expect(result.id, 'project1');
        expect(result.memberIds, isEmpty);
        expect(mockApiService.lastRemoveMemberProjectId, 'project1');
        expect(mockApiService.lastRemoveMemberUserId, 'user1');
      });

      test('should validate project ID', () async {
        expect(
          () => repository.removeMember('', 'user1'),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Project ID cannot be empty'),
          )),
        );
      });

      test('should validate user ID', () async {
        expect(
          () => repository.removeMember('project1', ''),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('User ID cannot be empty'),
          )),
        );
      });

      test('should handle remove member error', () async {
        mockApiService.setShouldThrowError(true);

        expect(
          () => repository.removeMember('project1', 'user1'),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to remove member'),
          )),
        );
      });
    });

    group('Get Project Stats', () {
      test('should get project statistics successfully', () async {
        final stats = {
          'totalTasks': 10,
          'completedTasks': 5,
          'inProgressTasks': 3,
          'overdueTasks': 1,
        };

        mockApiService.setProjectStatsResponse(stats);

        final result = await repository.getProjectStats('project1');

        expect(result, stats);
        expect(mockApiService.lastStatsProjectId, 'project1');
      });

      test('should validate project ID', () async {
        expect(
          () => repository.getProjectStats(''),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Project ID cannot be empty'),
          )),
        );
      });

      test('should handle get project stats error', () async {
        mockApiService.setShouldThrowError(true);

        expect(
          () => repository.getProjectStats('project1'),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to fetch project statistics'),
          )),
        );
      });
    });

    group('DTO to Model Conversion', () {
      test('should convert ProjectResponseDto to Project correctly', () async {
        const projectDto = ProjectResponseDto(
          id: 'project1',
          name: 'Test Project',
          description: 'Test Description',
          completed: 5,
          inProgress: 3,
          total: 10,
          creatorId: 'creator1',
          memberIds: ['user1', 'user2'],
          members: [],
          createdAt: null,
          updatedAt: null,
        );

        mockApiService.setGetProjectResponse(projectDto);

        final result = await repository.getProject('project1');

        expect(result.id, 'project1');
        expect(result.name, 'Test Project');
        expect(result.description, 'Test Description');
        expect(result.completed, 5);
        expect(result.inProgress, 3);
        expect(result.total, 10);
        expect(result.creatorId, 'creator1');
        expect(result.memberIds, ['user1', 'user2']);
      });

      test('should handle null values in DTO conversion', () async {
        const projectDto = ProjectResponseDto(
          id: 'project1',
          name: 'Test Project',
          completed: 0,
          inProgress: 0,
          total: 0,
          memberIds: [],
          members: [],
        );

        mockApiService.setGetProjectResponse(projectDto);

        final result = await repository.getProject('project1');

        expect(result.description, isNull);
        expect(result.creatorId, isNull);
        expect(result.memberIds, isEmpty);
      });
    });

    group('Error Message Wrapping', () {
      test('should wrap API errors with repository context', () async {
        mockApiService.setShouldThrowError(true);

        try {
          await repository.getProjects();
          fail('Expected exception');
        } catch (e) {
          expect(e.toString(), contains('Failed to fetch projects'));
          expect(e.toString(), contains('Mock error'));
        }

        try {
          await repository.getProject('project1');
          fail('Expected exception');
        } catch (e) {
          expect(e.toString(), contains('Failed to fetch project'));
          expect(e.toString(), contains('Mock error'));
        }

        try {
          await repository.createProject(const CreateProjectRequestDto(name: 'Test'));
          fail('Expected exception');
        } catch (e) {
          expect(e.toString(), contains('Failed to create project'));
          expect(e.toString(), contains('Mock error'));
        }

        try {
          await repository.updateProject('project1', const ProjectUpdateRequestDto(name: 'Test'));
          fail('Expected exception');
        } catch (e) {
          expect(e.toString(), contains('Failed to update project'));
          expect(e.toString(), contains('Mock error'));
        }

        try {
          await repository.deleteProject('project1');
          fail('Expected exception');
        } catch (e) {
          expect(e.toString(), contains('Failed to delete project'));
          expect(e.toString(), contains('Mock error'));
        }

        try {
          await repository.addMember('project1', 'user1');
          fail('Expected exception');
        } catch (e) {
          expect(e.toString(), contains('Failed to add member'));
          expect(e.toString(), contains('Mock error'));
        }

        try {
          await repository.removeMember('project1', 'user1');
          fail('Expected exception');
        } catch (e) {
          expect(e.toString(), contains('Failed to remove member'));
          expect(e.toString(), contains('Mock error'));
        }

        try {
          await repository.getProjectStats('project1');
          fail('Expected exception');
        } catch (e) {
          expect(e.toString(), contains('Failed to fetch project statistics'));
          expect(e.toString(), contains('Mock error'));
        }
      });
    });
  });
} 