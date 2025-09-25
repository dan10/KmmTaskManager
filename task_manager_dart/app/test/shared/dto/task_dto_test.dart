import 'package:flutter_test/flutter_test.dart';
import 'package:task_manager_shared/models.dart';

void main() {
  group('Task DTOs', () {
    group('TaskCreateRequestDto', () {
      group('Validation', () {
        test('should be valid with correct data', () {
          final dto = TaskCreateRequestDto(
            title: 'Valid Task Title',
            description: 'This is a valid task description that is long enough',
            priority: Priority.medium,
          );

          expect(dto.isValid, true);
          expect(dto.validate(), isEmpty);
        });

        test('should validate title correctly', () {
          // Empty title
          var dto = TaskCreateRequestDto(
            title: '',
            description: 'Valid description that is long enough',
            priority: Priority.medium,
          );
          var errors = dto.validate();
          expect(errors['title'], 'Title cannot be empty.');

          // Title too short
          dto = TaskCreateRequestDto(
            title: 'AB',
            description: 'Valid description that is long enough',
            priority: Priority.medium,
          );
          errors = dto.validate();
          expect(errors['title'], 'Title must be at least 3 characters long.');

          // Title too long
          dto = TaskCreateRequestDto(
            title: 'A' * 101,
            description: 'Valid description that is long enough',
            priority: Priority.medium,
          );
          errors = dto.validate();
          expect(errors['title'], 'Title cannot exceed 100 characters.');

          // Valid title
          dto = TaskCreateRequestDto(
            title: 'Valid Title',
            description: 'Valid description that is long enough',
            priority: Priority.medium,
          );
          errors = dto.validate();
          expect(errors.containsKey('title'), false);
        });

        test('should validate description correctly', () {
          // Empty description
          var dto = TaskCreateRequestDto(
            title: 'Valid Title',
            description: '',
            priority: Priority.medium,
          );
          var errors = dto.validate();
          expect(errors['description'], 'Description cannot be empty.');

          // Description too short
          dto = TaskCreateRequestDto(
            title: 'Valid Title',
            description: 'Short',
            priority: Priority.medium,
          );
          errors = dto.validate();
          expect(errors['description'], 'Description must be at least 10 characters long.');

          // Description too long
          dto = TaskCreateRequestDto(
            title: 'Valid Title',
            description: 'A' * 501,
            priority: Priority.medium,
          );
          errors = dto.validate();
          expect(errors['description'], 'Description cannot exceed 500 characters.');

          // Valid description
          dto = TaskCreateRequestDto(
            title: 'Valid Title',
            description: 'This is a valid description',
            priority: Priority.medium,
          );
          errors = dto.validate();
          expect(errors.containsKey('description'), false);
        });

        test('should validate due date correctly', () {
          final pastDate = DateTime.now().subtract(const Duration(days: 1));
          final futureDate = DateTime.now().add(const Duration(days: 1));

          // Past due date
          var dto = TaskCreateRequestDto(
            title: 'Valid Title',
            description: 'Valid description that is long enough',
            priority: Priority.medium,
            dueDate: pastDate,
          );
          var errors = dto.validate();
          expect(errors['dueDate'], 'Due date cannot be in the past.');

          // Future due date
          dto = TaskCreateRequestDto(
            title: 'Valid Title',
            description: 'Valid description that is long enough',
            priority: Priority.medium,
            dueDate: futureDate,
          );
          errors = dto.validate();
          expect(errors.containsKey('dueDate'), false);

          // No due date
          dto = TaskCreateRequestDto(
            title: 'Valid Title',
            description: 'Valid description that is long enough',
            priority: Priority.medium,
          );
          errors = dto.validate();
          expect(errors.containsKey('dueDate'), false);
        });
      });

      group('Serialization', () {
        test('should serialize to JSON correctly', () {
          final dueDate = DateTime(2024, 12, 31, 23, 59, 59);
          final dto = TaskCreateRequestDto(
            title: 'Test Task',
            description: 'Test Description',
            priority: Priority.high,
            dueDate: dueDate,
            projectId: 'project1',
            assigneeId: 'user1',
          );

          final json = dto.toJson();

          expect(json['title'], 'Test Task');
          expect(json['description'], 'Test Description');
          expect(json['priority'], 'HIGH');
          expect(json['dueDate'], dueDate.toIso8601String());
          expect(json['projectId'], 'project1');
          expect(json['assigneeId'], 'user1');
        });

        test('should serialize without optional fields', () {
          final dto = TaskCreateRequestDto(
            title: 'Test Task',
            description: 'Test Description',
            priority: Priority.medium,
          );

          final json = dto.toJson();

          expect(json['title'], 'Test Task');
          expect(json['description'], 'Test Description');
          expect(json['priority'], 'MEDIUM');
          expect(json['dueDate'], isNull);
          expect(json['projectId'], isNull);
          expect(json['assigneeId'], isNull);
        });

        test('should handle all priority levels', () {
          final lowDto = TaskCreateRequestDto(
            title: 'Test Task',
            description: 'Test Description',
            priority: Priority.low,
          );
          expect(lowDto.toJson()['priority'], 'LOW');

          final mediumDto = TaskCreateRequestDto(
            title: 'Test Task',
            description: 'Test Description',
            priority: Priority.medium,
          );
          expect(mediumDto.toJson()['priority'], 'MEDIUM');

          final highDto = TaskCreateRequestDto(
            title: 'Test Task',
            description: 'Test Description',
            priority: Priority.high,
          );
          expect(highDto.toJson()['priority'], 'HIGH');
        });
      });

      group('Deserialization', () {
        test('should deserialize from JSON correctly', () {
          final json = {
            'title': 'Test Task',
            'description': 'Test Description',
            'priority': 'HIGH',
            'dueDate': '2024-12-31T23:59:59.000Z',
            'projectId': 'project1',
            'assigneeId': 'user1',
          };

          final dto = TaskCreateRequestDto.fromJson(json);

          expect(dto.title, 'Test Task');
          expect(dto.description, 'Test Description');
          expect(dto.priority, Priority.high);
          expect(dto.dueDate, DateTime.parse('2024-12-31T23:59:59.000Z'));
          expect(dto.projectId, 'project1');
          expect(dto.assigneeId, 'user1');
        });

        test('should handle missing optional fields', () {
          final json = {
            'title': 'Test Task',
            'description': 'Test Description',
            'priority': 'MEDIUM',
          };

          final dto = TaskCreateRequestDto.fromJson(json);

          expect(dto.title, 'Test Task');
          expect(dto.description, 'Test Description');
          expect(dto.priority, Priority.medium);
          expect(dto.dueDate, isNull);
          expect(dto.projectId, isNull);
          expect(dto.assigneeId, isNull);
        });

        test('should handle all priority levels', () {
          final lowJson = {
            'title': 'Test Task',
            'description': 'Test Description',
            'priority': 'LOW',
          };
          expect(TaskCreateRequestDto.fromJson(lowJson).priority, Priority.low);

          final mediumJson = {
            'title': 'Test Task',
            'description': 'Test Description',
            'priority': 'MEDIUM',
          };
          expect(TaskCreateRequestDto.fromJson(mediumJson).priority, Priority.medium);

          final highJson = {
            'title': 'Test Task',
            'description': 'Test Description',
            'priority': 'HIGH',
          };
          expect(TaskCreateRequestDto.fromJson(highJson).priority, Priority.high);
        });

        test('should handle valid priority values', () {
          final json = {
            'title': 'Test Task',
            'description': 'Test Description',
            'priority': 'HIGH',
          };

          final dto = TaskCreateRequestDto.fromJson(json);
          expect(dto.priority, Priority.high);
        });
      });
    });

    group('TaskUpdateRequestDto', () {
      group('Validation', () {
        test('should be valid with correct data', () {
          final dto = TaskUpdateRequestDto(
            title: 'Valid Task Title',
            description: 'This is a valid task description that is long enough',
            priority: Priority.medium,
          );

          expect(dto.isValid, true);
          expect(dto.validate(), isEmpty);
        });

        test('should be valid with no updates', () {
          final dto = TaskUpdateRequestDto();

          expect(dto.isValid, true);
          expect(dto.validate(), isEmpty);
          expect(dto.hasUpdates, false);
        });

        test('should validate title when provided', () {
          var dto = TaskUpdateRequestDto(title: 'A');
          var errors = dto.validate();
          expect(errors['title'], 'Title must be at least 3 characters long.');

          dto = TaskUpdateRequestDto(title: 'Valid Title');
          errors = dto.validate();
          expect(errors.containsKey('title'), false);
        });

        test('should validate description when provided', () {
          var dto = TaskUpdateRequestDto(description: 'Short');
          var errors = dto.validate();
          expect(errors['description'], 'Description must be at least 10 characters long.');

          dto = TaskUpdateRequestDto(description: 'Valid description');
          errors = dto.validate();
          expect(errors.containsKey('description'), false);
        });

        test('should detect updates correctly', () {
          expect(TaskUpdateRequestDto().hasUpdates, false);
          expect(TaskUpdateRequestDto(title: 'Title').hasUpdates, true);
          expect(TaskUpdateRequestDto(description: 'Description').hasUpdates, true);
          expect(TaskUpdateRequestDto(status: TaskStatus.done).hasUpdates, true);
          expect(TaskUpdateRequestDto(priority: Priority.high).hasUpdates, true);
          expect(TaskUpdateRequestDto(dueDate: DateTime.now()).hasUpdates, true);
          expect(TaskUpdateRequestDto(projectId: 'project1').hasUpdates, true);
          expect(TaskUpdateRequestDto(assigneeId: 'user1').hasUpdates, true);
        });
      });

      group('Serialization', () {
        test('should serialize only provided fields', () {
          final dto = TaskUpdateRequestDto(
            title: 'Updated Title',
            priority: Priority.high,
          );

          final json = dto.toJson();

          expect(json['title'], 'Updated Title');
          expect(json['priority'], 'HIGH');
          expect(json['description'], isNull);
          expect(json['status'], isNull);
          expect(json['dueDate'], isNull);
          expect(json['projectId'], isNull);
          expect(json['assigneeId'], isNull);
        });

        test('should handle all status levels', () {
          final todoDto = TaskUpdateRequestDto(status: TaskStatus.todo);
          expect(todoDto.toJson()['status'], 'TODO');

          final inProgressDto = TaskUpdateRequestDto(status: TaskStatus.inProgress);
          expect(inProgressDto.toJson()['status'], 'IN_PROGRESS');

          final doneDto = TaskUpdateRequestDto(status: TaskStatus.done);
          expect(doneDto.toJson()['status'], 'DONE');
        });
      });

      group('Deserialization', () {
        test('should deserialize from JSON correctly', () {
          final json = {
            'title': 'Updated Task',
            'status': 'IN_PROGRESS',
            'priority': 'LOW',
          };

          final dto = TaskUpdateRequestDto.fromJson(json);

          expect(dto.title, 'Updated Task');
          expect(dto.status, TaskStatus.inProgress);
          expect(dto.priority, Priority.low);
          expect(dto.description, isNull);
        });

        test('should handle all status levels', () {
          final todoJson = {'status': 'TODO'};
          expect(TaskUpdateRequestDto.fromJson(todoJson).status, TaskStatus.todo);

          final inProgressJson = {'status': 'IN_PROGRESS'};
          expect(TaskUpdateRequestDto.fromJson(inProgressJson).status, TaskStatus.inProgress);

          final doneJson = {'status': 'DONE'};
          expect(TaskUpdateRequestDto.fromJson(doneJson).status, TaskStatus.done);
        });
      });
    });

    group('TaskDto', () {
      group('Serialization', () {
        test('should serialize to JSON correctly', () {
          final createdAt = DateTime(2024, 1, 1, 12, 0, 0);
          final updatedAt = DateTime(2024, 1, 2, 12, 0, 0);
          final dueDate = DateTime(2024, 12, 31, 23, 59, 59);

          final dto = TaskDto(
            id: '1',
            title: 'Test Task',
            description: 'Test Description',
            status: TaskStatus.inProgress,
            priority: Priority.high,
            dueDate: dueDate,
            projectId: 'project1',
            assigneeId: 'user2',
            creatorId: 'user1',
            createdAt: createdAt,
            updatedAt: updatedAt,
          );

          final json = dto.toJson();

          expect(json['id'], '1');
          expect(json['title'], 'Test Task');
          expect(json['description'], 'Test Description');
          expect(json['status'], 'IN_PROGRESS');
          expect(json['priority'], 'HIGH');
          expect(json['dueDate'], dueDate.toIso8601String());
          expect(json['projectId'], 'project1');
          expect(json['assigneeId'], 'user2');
          expect(json['creatorId'], 'user1');
          expect(json['createdAt'], createdAt.toIso8601String());
          expect(json['updatedAt'], updatedAt.toIso8601String());
        });

        test('should handle optional fields correctly', () {
          final createdAt = DateTime(2024, 1, 1, 12, 0, 0);
          final updatedAt = DateTime(2024, 1, 2, 12, 0, 0);

          final dto = TaskDto(
            id: '1',
            title: 'Test Task',
            description: 'Test Description',
            status: TaskStatus.todo,
            priority: Priority.medium,
            creatorId: 'user1',
            createdAt: createdAt,
            updatedAt: updatedAt,
          );

          final json = dto.toJson();

          expect(json['id'], '1');
          expect(json['dueDate'], isNull);
          expect(json['projectId'], isNull);
          expect(json['assigneeId'], isNull);
        });
      });

      group('Deserialization', () {
        test('should deserialize from JSON correctly', () {
          final json = {
            'id': '1',
            'title': 'Test Task',
            'description': 'Test Description',
            'status': 'DONE',
            'priority': 'LOW',
            'dueDate': '2024-12-31T23:59:59.000Z',
            'projectId': 'project1',
            'assigneeId': 'user2',
            'creatorId': 'user1',
            'createdAt': '2024-01-01T12:00:00.000Z',
            'updatedAt': '2024-01-02T12:00:00.000Z',
          };

          final dto = TaskDto.fromJson(json);

          expect(dto.id, '1');
          expect(dto.title, 'Test Task');
          expect(dto.description, 'Test Description');
          expect(dto.status, TaskStatus.done);
          expect(dto.priority, Priority.low);
          expect(dto.dueDate, DateTime.parse('2024-12-31T23:59:59.000Z'));
          expect(dto.projectId, 'project1');
          expect(dto.assigneeId, 'user2');
          expect(dto.creatorId, 'user1');
          expect(dto.createdAt, DateTime.parse('2024-01-01T12:00:00.000Z'));
          expect(dto.updatedAt, DateTime.parse('2024-01-02T12:00:00.000Z'));
        });

        test('should handle valid enum values', () {
          final json = {
            'id': '1',
            'title': 'Test Task',
            'description': 'Test Description',
            'status': 'IN_PROGRESS',
            'priority': 'HIGH',
            'creatorId': 'user1',
            'createdAt': '2024-01-01T12:00:00.000Z',
            'updatedAt': '2024-01-02T12:00:00.000Z',
          };

          final dto = TaskDto.fromJson(json);

          expect(dto.status, TaskStatus.inProgress);
          expect(dto.priority, Priority.high);
        });
      });
    });
  });
} 