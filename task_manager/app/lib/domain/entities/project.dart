class Project {
  final String id;
  final String name;
  final String? description;
  final String ownerId;
  final DateTime? createdAt;
  final DateTime? updatedAt;
  final int taskCount;

  const Project({
    required this.id,
    required this.name,
    this.description,
    required this.ownerId,
    this.createdAt,
    this.updatedAt,
    this.taskCount = 0,
  });

  Project copyWith({
    String? id,
    String? name,
    String? description,
    String? ownerId,
    DateTime? createdAt,
    DateTime? updatedAt,
    int? taskCount,
  }) {
    return Project(
      id: id ?? this.id,
      name: name ?? this.name,
      description: description ?? this.description,
      ownerId: ownerId ?? this.ownerId,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      taskCount: taskCount ?? this.taskCount,
    );
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is Project &&
          runtimeType == other.runtimeType &&
          id == other.id &&
          name == other.name &&
          ownerId == other.ownerId;

  @override
  int get hashCode => id.hashCode ^ name.hashCode ^ ownerId.hashCode;

  @override
  String toString() {
    return 'Project{id: $id, name: $name, ownerId: $ownerId, taskCount: $taskCount}';
  }
} 