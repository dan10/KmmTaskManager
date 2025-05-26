import 'package:freezed_annotation/freezed_annotation.dart';

import '../../models/project.dart';
import '../../models/user.dart';

part 'project_response_dto.freezed.dart';
part 'project_response_dto.g.dart';

@freezed
abstract class ProjectResponseDto with _$ProjectResponseDto {
  const factory ProjectResponseDto({
    required String id,
    required String name,
    String? description,
    @Default(0) int completed,
    @Default(0) int inProgress,
    @Default(0) int total,
    String? creatorId,
    @Default([]) List<String> memberIds,
    User? creator,
    @Default([]) List<User> members,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) = _ProjectResponseDto;

  factory ProjectResponseDto.fromJson(Map<String, dynamic> json) =>
      _$ProjectResponseDtoFromJson(json);
}

extension ProjectResponseDtoExtension on ProjectResponseDto {
  Project toProject() {
    return Project(
      id: id,
      name: name,
      description: description,
      completed: completed,
      inProgress: inProgress,
      total: total,
      creatorId: creatorId,
      memberIds: memberIds,
    );
  }
} 