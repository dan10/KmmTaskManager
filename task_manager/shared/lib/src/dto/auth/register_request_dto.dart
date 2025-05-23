class RegisterRequestDto {
  final String displayName;
  final String email;
  final String password;

  const RegisterRequestDto({
    required this.displayName,
    required this.email,
    required this.password,
  });

  factory RegisterRequestDto.fromJson(Map<String, dynamic> json) {
    return RegisterRequestDto(
      displayName: json['displayName'] as String,
      email: json['email'] as String,
      password: json['password'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'displayName': displayName,
      'email': email,
      'password': password,
    };
  }

  Map<String, String> validate() {
    final details = <String, String>{};

    if (displayName.isEmpty) {
      details['displayName'] = 'Display name cannot be empty.';
    }

    if (email.isEmpty) {
      details['email'] = 'Email cannot be empty.';
    } else if (!email.contains('@')) {
      details['email'] = 'Invalid email format.';
    }

    if (password.isEmpty) {
      details['password'] = 'Password cannot be empty.';
    } else if (password.length < 6) {
      details['password'] = 'Password must be at least 6 characters long.';
    }

    return details;
  }

  bool get isValid => validate().isEmpty;
} 