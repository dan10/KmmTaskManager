import 'package:json_annotation/json_annotation.dart';
import '../models/task.dart';

class TaskStatusConverter implements JsonConverter<TaskStatus, String> {
  const TaskStatusConverter();

  @override
  TaskStatus fromJson(String json) {
    return TaskStatus.values.firstWhere(
      (e) => e.toString() == 'TaskStatus.${json.toLowerCase()}',
      orElse: () => TaskStatus.todo,
    );
  }

  @override
  String toJson(TaskStatus status) {
    return status.toString().split('.').last.toUpperCase();
  }
}
