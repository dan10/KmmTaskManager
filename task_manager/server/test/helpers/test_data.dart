import '../../lib/src/models/models.dart';

class TestData {
  static User createTestUser({
    String? id,
    String? name,
    String? email,
    String? passwordHash,
  }) {
    return User(
      id: id ?? 'test-user-id',
      name: name ?? 'Test User',
      email: email ?? 'test@example.com',
      passwordHash: passwordHash ?? 'hashed-password',
    );
  }

  static Task createTestTask({
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
    return Task(
      id: id ?? 'test-task-id',
      title: title ?? 'Test Task',
      description: description ?? 'Test Description',
      status: status ?? TaskStatus.TODO,
      priority: priority ?? Priority.MEDIUM,
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
