class TaskAssignRequestDto {
  final String assigneeId;

  const TaskAssignRequestDto({
    required this.assigneeId,
  });

  factory TaskAssignRequestDto.fromJson(Map<String, dynamic> json) {
    return TaskAssignRequestDto(
      assigneeId: json['assigneeId'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'assigneeId': assigneeId,
    };
  }

  Map<String, String> validate() {
    final details = <String, String>{};
    if (assigneeId.isEmpty) {
      details['assigneeId'] = 'Assignee ID cannot be empty.';
    }
    return details;
  }

  bool get isValid => validate().isEmpty;
} 