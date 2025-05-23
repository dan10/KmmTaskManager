class CreateProjectRequestDto {
  final String name;
  final String? description;

  const CreateProjectRequestDto({
    required this.name,
    this.description,
  });

  factory CreateProjectRequestDto.fromJson(Map<String, dynamic> json) {
    return CreateProjectRequestDto(
      name: json['name'] as String,
      description: json['description'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'description': description,
    };
  }

  Map<String, String> validate() {
    final details = <String, String>{};

    if (name.isEmpty) {
      details['name'] = 'Project name cannot be empty.';
    }

    return details;
  }

  bool get isValid => validate().isEmpty;
} 