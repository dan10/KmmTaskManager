import 'package:freezed_annotation/freezed_annotation.dart';

part 'task_progress_dto.freezed.dart';
part 'task_progress_dto.g.dart';

/// DTO for task progress (completed vs total)
/// Matches KMM's TaskProgressResponse
@freezed
abstract class TaskProgress with _$TaskProgress {
  const factory TaskProgress({
    required int totalTasks,
    required int completedTasks,
  }) = _TaskProgress;

  factory TaskProgress.fromJson(Map<String, dynamic> json) =>
      _$TaskProgressFromJson(json);
}

