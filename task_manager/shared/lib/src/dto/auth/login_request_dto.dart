class LoginRequestDto {
  final String email;
  final String password;

  const LoginRequestDto({
    required this.email,
    required this.password,
  });

  factory LoginRequestDto.fromJson(Map<String, dynamic> json) {
    return LoginRequestDto(
      email: json['email'] as String,
      password: json['password'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'email': email,
      'password': password,
    };
  }

  Map<String, String> validate() {
    final details = <String, String>{};

    if (email.isEmpty) {
      details['email'] = 'Email cannot be empty.';
    }

    if (password.isEmpty) {
      details['password'] = 'Password cannot be empty.';
    }

    return details;
  }

  bool get isValid => validate().isEmpty;
} 