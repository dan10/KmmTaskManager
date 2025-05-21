import 'package:freezed_annotation/freezed_annotation.dart';

part 'project_assign_request_dto.freezed.dart';
part 'project_assign_request_dto.g.dart';

import '../../../src/exceptions/custom_exceptions.dart';


@freezed
class ProjectAssignRequestDto with _$ProjectAssignRequestDto {
  const factory ProjectAssignRequestDto({
    required String userId,
  }) = _ProjectAssignRequestDto;

  factory ProjectAssignRequestDto.fromJson(Map<String, dynamic> json) =>
      _$ProjectAssignRequestDtoFromJson(json);

  void validateOrThrow() {
    final details = <String, String>{};
    if (userId.isEmpty) {
      details['userId'] = 'User ID cannot be empty.';
    }
    if (details.isNotEmpty) {
      throw ValidationException(
        message: 'Project assignment validation failed.',
        details: details,
      );
    }
  }
}
