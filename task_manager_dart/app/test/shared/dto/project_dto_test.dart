import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_shared/models.dart';

void main() {
  group('CreateProjectRequestDto', () {
    test('should create instance with required fields', () {
      const dto = CreateProjectRequestDto(
        name: 'Test Project',
        description: 'Test Description',
      );

      expect(dto.name, 'Test Project');
      expect(dto.description, 'Test Description');
    });

    test('should create instance with null description', () {
      const dto = CreateProjectRequestDto(
        name: 'Test Project',
      );

      expect(dto.name, 'Test Project');
      expect(dto.description, isNull);
    });

    test('should serialize to JSON correctly', () {
      const dto = CreateProjectRequestDto(
        name: 'Test Project',
        description: 'Test Description',
      );

      final json = dto.toJson();

      expect(json['name'], 'Test Project');
      expect(json['description'], 'Test Description');
    });

    test('should serialize to JSON with null description', () {
      const dto = CreateProjectRequestDto(
        name: 'Test Project',
      );

      final json = dto.toJson();

      expect(json['name'], 'Test Project');
      expect(json['description'], isNull);
    });

    test('should deserialize from JSON correctly', () {
      final json = {
        'name': 'Test Project',
        'description': 'Test Description',
      };

      final dto = CreateProjectRequestDto.fromJson(json);

      expect(dto.name, 'Test Project');
      expect(dto.description, 'Test Description');
    });

    test('should validate successfully with valid data', () {
      const dto = CreateProjectRequestDto(
        name: 'Valid Project Name',
        description: 'Valid description',
      );

      expect(dto.isValid, true);
      expect(dto.validate(), isEmpty);
    });

    test('should fail validation with empty name', () {
      const dto = CreateProjectRequestDto(
        name: '',
        description: 'Valid description',
      );

      expect(dto.isValid, false);
      final errors = dto.validate();
      expect(errors, isNotEmpty);
      expect(errors['name'], 'Project name cannot be empty.');
    });

    test('should fail validation with whitespace-only name', () {
      const dto = CreateProjectRequestDto(
        name: '   ',
        description: 'Valid description',
      );

      expect(dto.isValid, false);
      final errors = dto.validate();
      expect(errors, isNotEmpty);
      expect(errors['name'], 'Project name cannot be empty.');
    });
  });

  group('ProjectUpdateRequestDto', () {
    test('should create instance with all fields', () {
      const dto = ProjectUpdateRequestDto(
        name: 'Updated Project',
        description: 'Updated Description',
        memberIds: ['user1', 'user2'],
      );

      expect(dto.name, 'Updated Project');
      expect(dto.description, 'Updated Description');
      expect(dto.memberIds, ['user1', 'user2']);
    });

    test('should create instance with null fields', () {
      const dto = ProjectUpdateRequestDto();

      expect(dto.name, isNull);
      expect(dto.description, isNull);
      expect(dto.memberIds, isNull);
    });

    test('should serialize to JSON correctly', () {
      const dto = ProjectUpdateRequestDto(
        name: 'Updated Project',
        description: 'Updated Description',
        memberIds: ['user1', 'user2'],
      );

      final json = dto.toJson();

      expect(json['name'], 'Updated Project');
      expect(json['description'], 'Updated Description');
      expect(json['memberIds'], ['user1', 'user2']);
    });

    test('should serialize to JSON with only non-null fields', () {
      const dto = ProjectUpdateRequestDto(
        name: 'Updated Project',
      );

      final json = dto.toJsonWithNulls();

      expect(json['name'], 'Updated Project');
      expect(json.containsKey('description'), false);
      expect(json.containsKey('memberIds'), false);
    });

    test('should deserialize from JSON correctly', () {
      final json = {
        'name': 'Updated Project',
        'description': 'Updated Description',
        'memberIds': ['user1', 'user2'],
      };

      final dto = ProjectUpdateRequestDto.fromJson(json);

      expect(dto.name, 'Updated Project');
      expect(dto.description, 'Updated Description');
      expect(dto.memberIds, ['user1', 'user2']);
    });

    test('should validate successfully with valid data', () {
      const dto = ProjectUpdateRequestDto(
        name: 'Valid Project Name',
        description: 'Valid description',
      );

      expect(dto.isValid, true);
      expect(dto.validate(), isEmpty);
    });

    test('should fail validation with short name', () {
      const dto = ProjectUpdateRequestDto(
        name: 'AB',
      );

      expect(dto.isValid, false);
      final errors = dto.validate();
      expect(errors, isNotEmpty);
      expect(errors['name'], 'Project name must be at least 3 characters long.');
    });

    test('should fail validation with long name', () {
      final longName = 'A' * 101;
      final dto = ProjectUpdateRequestDto(
        name: longName,
      );

      expect(dto.isValid, false);
      final errors = dto.validate();
      expect(errors, isNotEmpty);
      expect(errors['name'], 'Project name cannot exceed 100 characters.');
    });

    test('should fail validation with long description', () {
      final longDescription = 'A' * 501;
      final dto = ProjectUpdateRequestDto(
        description: longDescription,
      );

      expect(dto.isValid, false);
      final errors = dto.validate();
      expect(errors, isNotEmpty);
      expect(errors['description'], 'Description cannot exceed 500 characters.');
    });

    test('should detect updates correctly', () {
      const dtoWithUpdates = ProjectUpdateRequestDto(
        name: 'Updated Project',
      );

      const dtoWithoutUpdates = ProjectUpdateRequestDto();

      expect(dtoWithUpdates.hasUpdates, true);
      expect(dtoWithoutUpdates.hasUpdates, false);
    });
  });

  group('ProjectResponseDto', () {
    test('should create instance with all fields', () {
      final creator = User(
        id: 'creator1',
        displayName: 'Creator User',
        email: 'creator@example.com',
        createdAt: '2024-01-01T00:00:00.000Z',
      );

      final member = User(
        id: 'member1',
        displayName: 'Member User',
        email: 'member@example.com',
        createdAt: '2024-01-01T00:00:00.000Z',
      );

      final dto = ProjectResponseDto(
        id: 'project1',
        name: 'Test Project',
        description: 'Test Description',
        completed: 5,
        inProgress: 3,
        total: 10,
        creatorId: 'creator1',
        memberIds: ['member1'],
        creator: creator,
        members: [member],
        createdAt: DateTime(2024, 1, 1),
        updatedAt: DateTime(2024, 1, 2),
      );

      expect(dto.id, 'project1');
      expect(dto.name, 'Test Project');
      expect(dto.description, 'Test Description');
      expect(dto.completed, 5);
      expect(dto.inProgress, 3);
      expect(dto.total, 10);
      expect(dto.creatorId, 'creator1');
      expect(dto.memberIds, ['member1']);
      expect(dto.creator, creator);
      expect(dto.members, [member]);
      expect(dto.createdAt, DateTime(2024, 1, 1));
      expect(dto.updatedAt, DateTime(2024, 1, 2));
    });

    test('should serialize to JSON correctly', () {
      final creator = User(
        id: 'creator1',
        displayName: 'Creator User',
        email: 'creator@example.com',
        createdAt: '2024-01-01T00:00:00.000Z',
      );

      final dto = ProjectResponseDto(
        id: 'project1',
        name: 'Test Project',
        description: 'Test Description',
        completed: 5,
        inProgress: 3,
        total: 10,
        creatorId: 'creator1',
        memberIds: ['member1'],
        creator: creator,
        members: [],
        createdAt: DateTime(2024, 1, 1),
        updatedAt: DateTime(2024, 1, 2),
      );

      final json = dto.toJson();

      expect(json['id'], 'project1');
      expect(json['name'], 'Test Project');
      expect(json['description'], 'Test Description');
      expect(json['completed'], 5);
      expect(json['inProgress'], 3);
      expect(json['total'], 10);
      expect(json['creatorId'], 'creator1');
      expect(json['memberIds'], ['member1']);
      expect(json['creator'], isNotNull);
      expect(json['members'], isEmpty);
      expect(json['createdAt'], '2024-01-01T00:00:00.000');
      expect(json['updatedAt'], '2024-01-02T00:00:00.000');
    });

    test('should convert to Project model correctly', () {
      final dto = ProjectResponseDto(
        id: 'project1',
        name: 'Test Project',
        description: 'Test Description',
        completed: 5,
        inProgress: 3,
        total: 10,
        creatorId: 'creator1',
        memberIds: ['member1'],
        members: [],
      );

      final project = dto.toProject();

      expect(project.id, 'project1');
      expect(project.name, 'Test Project');
      expect(project.description, 'Test Description');
      expect(project.completed, 5);
      expect(project.inProgress, 3);
      expect(project.total, 10);
      expect(project.creatorId, 'creator1');
      expect(project.memberIds, ['member1']);
    });

    test('should handle null values correctly', () {
      const dto = ProjectResponseDto(
        id: 'project1',
        name: 'Test Project',
        completed: 0,
        inProgress: 0,
        total: 0,
        memberIds: [],
        members: [],
      );

      expect(dto.description, isNull);
      expect(dto.creatorId, isNull);
      expect(dto.creator, isNull);
      expect(dto.createdAt, isNull);
      expect(dto.updatedAt, isNull);

      final project = dto.toProject();
      expect(project.description, isNull);
      expect(project.creatorId, isNull);
    });
  });
} 