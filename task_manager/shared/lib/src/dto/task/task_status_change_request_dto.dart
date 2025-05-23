import '../../../models.dart';

class TaskStatusChangeRequestDto {
  final TaskStatus status;

  const TaskStatusChangeRequestDto({
    required this.status,
  });

  factory TaskStatusChangeRequestDto.fromJson(Map<String, dynamic> json) {
    final statusString = json['status'] as String;
    TaskStatus status;
    switch (statusString.toUpperCase()) {
      case 'TODO':
        status = TaskStatus.todo;
        break;
      case 'IN_PROGRESS':
        status = TaskStatus.inProgress;
        break;
      case 'DONE':
        status = TaskStatus.done;
        break;
      default:
        status = TaskStatus.todo; // Default fallback
    }
    return TaskStatusChangeRequestDto(status: status);
  }

  Map<String, dynamic> toJson() {
    String statusString;
    switch (status) {
      case TaskStatus.todo:
        statusString = 'TODO';
        break;
      case TaskStatus.inProgress:
        statusString = 'IN_PROGRESS';
        break;
      case TaskStatus.done:
        statusString = 'DONE';
        break;
    }
    return {
      'status': statusString,
    };
  }

  Map<String, String> validate() {
    // TaskStatus is an enum, so it's already validated
    return {};
  }

  bool get isValid => true;
} 