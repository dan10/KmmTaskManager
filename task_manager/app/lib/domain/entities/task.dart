enum TaskStatus {
  todo,
  inProgress,
  completed,
}

enum TaskPriority {
  low,
  medium,
  high,
}

class Task {
  final String id;
  final String title;
  final String? description;
  final TaskStatus status;
  final TaskPriority priority;
  final String projectId;
  final String? assigneeId;
  final DateTime? dueDate;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  const Task({
    required this.id,
    required this.title,
    this.description,
    required this.status,
    required this.priority,
    required this.projectId,
    this.assigneeId,
    this.dueDate,
    this.createdAt,
    this.updatedAt,
  });

  Task copyWith({
    String? id,
    String? title,
    String? description,
    TaskStatus? status,
    TaskPriority? priority,
    String? projectId,
    String? assigneeId,
    DateTime? dueDate,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return Task(
      id: id ?? this.id,
      title: title ?? this.title,
      description: description ?? this.description,
      status: status ?? this.status,
      priority: priority ?? this.priority,
      projectId: projectId ?? this.projectId,
      assigneeId: assigneeId ?? this.assigneeId,
      dueDate: dueDate ?? this.dueDate,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  bool get isCompleted => status == TaskStatus.completed;
  bool get isOverdue => dueDate != null && DateTime.now().isAfter(dueDate!) && !isCompleted;

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is Task &&
          runtimeType == other.runtimeType &&
          id == other.id &&
          title == other.title &&
          projectId == other.projectId;

  @override
  int get hashCode => id.hashCode ^ title.hashCode ^ projectId.hashCode;

  @override
  String toString() {
    return 'Task{id: $id, title: $title, status: $status, priority: $priority}';
  }
} 