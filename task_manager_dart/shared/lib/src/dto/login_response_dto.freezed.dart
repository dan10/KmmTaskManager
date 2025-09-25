// dart format width=80
// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'login_response_dto.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$LoginResponseDto {
  String get token;
  UserPublicResponseDto get user;

  /// Create a copy of LoginResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  $LoginResponseDtoCopyWith<LoginResponseDto> get copyWith =>
      _$LoginResponseDtoCopyWithImpl<LoginResponseDto>(
          this as LoginResponseDto, _$identity);

  /// Serializes this LoginResponseDto to a JSON map.
  Map<String, dynamic> toJson();

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is LoginResponseDto &&
            (identical(other.token, token) || other.token == token) &&
            (identical(other.user, user) || other.user == user));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, token, user);

  @override
  String toString() {
    return 'LoginResponseDto(token: $token, user: $user)';
  }
}

/// @nodoc
abstract mixin class $LoginResponseDtoCopyWith<$Res> {
  factory $LoginResponseDtoCopyWith(
          LoginResponseDto value, $Res Function(LoginResponseDto) _then) =
      _$LoginResponseDtoCopyWithImpl;
  @useResult
  $Res call({String token, UserPublicResponseDto user});

  $UserPublicResponseDtoCopyWith<$Res> get user;
}

/// @nodoc
class _$LoginResponseDtoCopyWithImpl<$Res>
    implements $LoginResponseDtoCopyWith<$Res> {
  _$LoginResponseDtoCopyWithImpl(this._self, this._then);

  final LoginResponseDto _self;
  final $Res Function(LoginResponseDto) _then;

  /// Create a copy of LoginResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? token = null,
    Object? user = null,
  }) {
    return _then(_self.copyWith(
      token: null == token
          ? _self.token
          : token // ignore: cast_nullable_to_non_nullable
              as String,
      user: null == user
          ? _self.user
          : user // ignore: cast_nullable_to_non_nullable
              as UserPublicResponseDto,
    ));
  }

  /// Create a copy of LoginResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $UserPublicResponseDtoCopyWith<$Res> get user {
    return $UserPublicResponseDtoCopyWith<$Res>(_self.user, (value) {
      return _then(_self.copyWith(user: value));
    });
  }
}

/// @nodoc
@JsonSerializable()
class _LoginResponseDto implements LoginResponseDto {
  const _LoginResponseDto({required this.token, required this.user});
  factory _LoginResponseDto.fromJson(Map<String, dynamic> json) =>
      _$LoginResponseDtoFromJson(json);

  @override
  final String token;
  @override
  final UserPublicResponseDto user;

  /// Create a copy of LoginResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  _$LoginResponseDtoCopyWith<_LoginResponseDto> get copyWith =>
      __$LoginResponseDtoCopyWithImpl<_LoginResponseDto>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$LoginResponseDtoToJson(
      this,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _LoginResponseDto &&
            (identical(other.token, token) || other.token == token) &&
            (identical(other.user, user) || other.user == user));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, token, user);

  @override
  String toString() {
    return 'LoginResponseDto(token: $token, user: $user)';
  }
}

/// @nodoc
abstract mixin class _$LoginResponseDtoCopyWith<$Res>
    implements $LoginResponseDtoCopyWith<$Res> {
  factory _$LoginResponseDtoCopyWith(
          _LoginResponseDto value, $Res Function(_LoginResponseDto) _then) =
      __$LoginResponseDtoCopyWithImpl;
  @override
  @useResult
  $Res call({String token, UserPublicResponseDto user});

  @override
  $UserPublicResponseDtoCopyWith<$Res> get user;
}

/// @nodoc
class __$LoginResponseDtoCopyWithImpl<$Res>
    implements _$LoginResponseDtoCopyWith<$Res> {
  __$LoginResponseDtoCopyWithImpl(this._self, this._then);

  final _LoginResponseDto _self;
  final $Res Function(_LoginResponseDto) _then;

  /// Create a copy of LoginResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $Res call({
    Object? token = null,
    Object? user = null,
  }) {
    return _then(_LoginResponseDto(
      token: null == token
          ? _self.token
          : token // ignore: cast_nullable_to_non_nullable
              as String,
      user: null == user
          ? _self.user
          : user // ignore: cast_nullable_to_non_nullable
              as UserPublicResponseDto,
    ));
  }

  /// Create a copy of LoginResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $UserPublicResponseDtoCopyWith<$Res> get user {
    return $UserPublicResponseDtoCopyWith<$Res>(_self.user, (value) {
      return _then(_self.copyWith(user: value));
    });
  }
}

// dart format on
