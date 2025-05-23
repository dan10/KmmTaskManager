import '../models/user.dart';

class UserPublicResponseDto {
  final String id;
  final String displayName;
  final String email;

  const UserPublicResponseDto({
    required this.id,
    required this.displayName,
    required this.email,
  });

  factory UserPublicResponseDto.fromJson(Map<String, dynamic> json) {
    return UserPublicResponseDto(
      id: json['id'] as String,
      displayName: json['displayName'] as String,
      email: json['email'] as String,
    );
  }

  factory UserPublicResponseDto.fromUser(User user) {
    return UserPublicResponseDto(
      id: user.id,
      displayName: user.displayName,
      email: user.email,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'displayName': displayName,
      'email': email,
    };
  }
} 