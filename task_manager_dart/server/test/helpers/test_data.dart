import 'package:task_manager_shared/models.dart';

class TestData {
  static User createTestUser({
    String? id,
    String? displayName,
    String? email,
    String? googleId,
  }) {
    return User(
      id: id ?? 'test-user-id',
      displayName: displayName ?? 'Test User',
      email: email ?? 'test@example.com',
      googleId: googleId ?? 'google-123',
      createdAt: DateTime.now().toIso8601String(),
    );
  }

  static TaskDto createTestTask({
    String? id,
    String? title,
    String? description,
    TaskStatus? status,
    Priority? priority,
    String? creatorId,
    String? projectId,
    String? assigneeId,
    DateTime? dueDate,
  }) {
    return TaskDto(
      id: id ?? 'test-task-id',
      title: title ?? 'Test Task',
      description: description ?? 'Test Description',
      status: status ?? TaskStatus.todo,
      priority: priority ?? Priority.medium,
      creatorId: creatorId ?? 'test-user-id',
      projectId: projectId,
      assigneeId: assigneeId,
      dueDate: dueDate,
    );
  }

  static Project createTestProject({
    String? id,
    String? name,
    String? description,
    String? creatorId,
    List<String>? memberIds,
  }) {
    return Project(
      id: id ?? 'test-project-id',
      name: name ?? 'Test Project',
      description: description ?? 'Test Description',
      creatorId: creatorId ?? 'test-user-id',
      memberIds: memberIds ?? ['test-user-id'],
    );
  }
}
