import 'package:freezed_annotation/freezed_annotation.dart';

part 'project.freezed.dart';
part 'project.g.dart';

@freezed
abstract class Project with _$Project {
  const factory Project({
    required String id,
    required String name,
    @Default(0) int completed,
    @Default(0) int inProgress,
    @Default(0) int total,
    String? description,
    // Additional fields for server-side operations
    String? creatorId,
    @Default([]) List<String> memberIds,
  }) = _Project;

  factory Project.fromJson(Map<String, dynamic> json) =>
      _$ProjectFromJson(json);
}
