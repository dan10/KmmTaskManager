import 'package:freezed_annotation/freezed_annotation.dart';

part 'project_update_request_dto.freezed.dart';
part 'project_update_request_dto.g.dart';

@freezed
abstract class ProjectUpdateRequestDto with _$ProjectUpdateRequestDto {
  const factory ProjectUpdateRequestDto({
    String? name,
    String? description,
    List<String>? memberIds,
  }) = _ProjectUpdateRequestDto;

  factory ProjectUpdateRequestDto.fromJson(Map<String, dynamic> json) =>
      _$ProjectUpdateRequestDtoFromJson(json);
}

extension ProjectUpdateRequestDtoExtension on ProjectUpdateRequestDto {
  Map<String, dynamic> toJsonWithNulls() {
    final result = <String, dynamic>{};
    
    if (name != null) result['name'] = name;
    if (description != null) result['description'] = description;
    if (memberIds != null) result['memberIds'] = memberIds;
    
    return result;
  }

  Map<String, String> validate() {
    final details = <String, String>{};

    if (name != null) {
      if (name!.trim().isEmpty) {
        details['name'] = 'Project name cannot be empty.';
      } else if (name!.trim().length < 3) {
        details['name'] = 'Project name must be at least 3 characters long.';
      } else if (name!.trim().length > 100) {
        details['name'] = 'Project name cannot exceed 100 characters.';
      }
    }

    if (description != null) {
      if (description!.trim().isEmpty) {
        details['description'] = 'Description cannot be empty.';
      } else if (description!.trim().length > 500) {
        details['description'] = 'Description cannot exceed 500 characters.';
      }
    }

    return details;
  }

  bool get isValid => validate().isEmpty;
  
  bool get hasUpdates => name != null || 
                        description != null || 
                        memberIds != null;
} 