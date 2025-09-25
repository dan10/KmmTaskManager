import 'package:freezed_annotation/freezed_annotation.dart';

part 'create_project_request_dto.freezed.dart';
part 'create_project_request_dto.g.dart';

@freezed
abstract class CreateProjectRequestDto with _$CreateProjectRequestDto {
  const factory CreateProjectRequestDto({
    required String name,
    String? description,
  }) = _CreateProjectRequestDto;

  factory CreateProjectRequestDto.fromJson(Map<String, dynamic> json) =>
      _$CreateProjectRequestDtoFromJson(json);
}

extension CreateProjectRequestDtoExtension on CreateProjectRequestDto {
  Map<String, String> validate() {
    final details = <String, String>{};

    if (name.trim().isEmpty) {
      details['name'] = 'Project name cannot be empty.';
    } else if (name.trim().length < 3) {
      details['name'] = 'Project name must be at least 3 characters long.';
    } else if (name.trim().length > 100) {
      details['name'] = 'Project name cannot exceed 100 characters.';
    }

    return details;
  }

  bool get isValid => validate().isEmpty;
} 