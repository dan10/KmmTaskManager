enum TaskStatus {
  TODO,
  IN_PROGRESS,
  DONE,
}

enum Priority {
  LOW,
  MEDIUM,
  HIGH,
}

class User {
  final String id;
  final String name;
  final String email;
  final String passwordHash;

  User({
    required this.id,
    required this.name,
    required this.email,
    required this.passwordHash,
  });

  User copyWith({
    String? id,
    String? name,
    String? email,
    String? passwordHash,
  }) {
    return User(
      id: id ?? this.id,
      name: name ?? this.name,
      email: email ?? this.email,
      passwordHash: passwordHash ?? this.passwordHash,
    );
  }
}

class Task {
  final String id;
  final String title;
  final String description;
  final TaskStatus status;
  final Priority priority;
  final String creatorId;
  final String? projectId;
  final String? assigneeId;
  final DateTime? dueDate;

  Task({
    required this.id,
    required this.title,
    required this.description,
    required this.status,
    required this.priority,
    required this.creatorId,
    this.projectId,
    this.assigneeId,
    this.dueDate,
  });

  Task copyWith({
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
      id: id ?? this.id,
      title: title ?? this.title,
      description: description ?? this.description,
      status: status ?? this.status,
      priority: priority ?? this.priority,
      creatorId: creatorId ?? this.creatorId,
      projectId: projectId ?? this.projectId,
      assigneeId: assigneeId ?? this.assigneeId,
      dueDate: dueDate ?? this.dueDate,
    );
  }
}

class Project {
  final String id;
  final String name;
  final String description;
  final String creatorId;
  final List<String> memberIds;

  Project({
    required this.id,
    required this.name,
    required this.description,
    required this.creatorId,
    required this.memberIds,
  });

  Project copyWith({
    String? id,
    String? name,
    String? description,
    String? creatorId,
    List<String>? memberIds,
  }) {
    return Project(
      id: id ?? this.id,
      name: name ?? this.name,
      description: description ?? this.description,
      creatorId: creatorId ?? this.creatorId,
      memberIds: memberIds ?? this.memberIds,
    );
  }
}
