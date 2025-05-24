class Project {
  final String id;
  final String name;
  final String? description;
  final String ownerId;
  final DateTime? createdAt;
  final DateTime? updatedAt;
  final int completed;
  final int inProgress;
  final int total;

  const Project({
    required this.id,
    required this.name,
    this.description,
    required this.ownerId,
    this.createdAt,
    this.updatedAt,
    this.completed = 0,
    this.inProgress = 0,
    this.total = 0,
  });

  // Helper getter for backwards compatibility
  int get taskCount => total;

  Project copyWith({
    String? id,
    String? name,
    String? description,
    String? ownerId,
    DateTime? createdAt,
    DateTime? updatedAt,
    int? completed,
    int? inProgress,
    int? total,
  }) {
    return Project(
      id: id ?? this.id,
      name: name ?? this.name,
      description: description ?? this.description,
      ownerId: ownerId ?? this.ownerId,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      completed: completed ?? this.completed,
      inProgress: inProgress ?? this.inProgress,
      total: total ?? this.total,
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
    return 'Project{id: $id, name: $name, ownerId: $ownerId, completed: $completed, inProgress: $inProgress, total: $total}';
  }
} 