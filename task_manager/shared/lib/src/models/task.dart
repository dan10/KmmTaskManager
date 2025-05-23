import 'package:freezed_annotation/freezed_annotation.dart';

import '../converters/task_status_converter.dart';

part 'task.freezed.dart';
part 'task.g.dart';

enum TaskStatus {
  @JsonKey(name: 'TODO')
  todo,
  @JsonKey(name: 'IN_PROGRESS')
  inProgress,
  @JsonKey(name: 'DONE')
  done
}

enum Priority {
  @JsonKey(name: 'LOW')
  low,
  @JsonKey(name: 'MEDIUM')
  medium,
  @JsonKey(name: 'HIGH')
  high
}

@freezed
abstract class Task with _$Task {
  @JsonSerializable()
  const factory Task({
    required String id,
    required String title,
    required String description,
    @TaskStatusConverter() required TaskStatus status,
    required Priority priority,
    DateTime? dueDate,
    String? projectId,
    String? assigneeId,
    required String creatorId,
  }) = _Task;

  factory Task.fromJson(Map<String, dynamic> json) => _$TaskFromJson(json);
}
