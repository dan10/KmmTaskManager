class GoogleLoginRequestDto {
  final String idToken;

  const GoogleLoginRequestDto({
    required this.idToken,
  });

  factory GoogleLoginRequestDto.fromJson(Map<String, dynamic> json) {
    return GoogleLoginRequestDto(
      idToken: json['idToken'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'idToken': idToken,
    };
  }

  Map<String, String> validate() {
    final details = <String, String>{};

    if (idToken.isEmpty) {
      details['idToken'] = 'ID token cannot be empty.';
    }
    // Further validation of idToken format could be done here if needed,
    // but actual token verification happens in the AuthService.

    return details;
  }

  bool get isValid => validate().isEmpty;
} 