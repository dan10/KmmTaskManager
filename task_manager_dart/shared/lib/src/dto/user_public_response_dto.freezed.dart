// dart format width=80
// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'user_public_response_dto.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$UserPublicResponseDto {
  String get id;
  String get displayName;
  String get email;

  /// Create a copy of UserPublicResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  $UserPublicResponseDtoCopyWith<UserPublicResponseDto> get copyWith =>
      _$UserPublicResponseDtoCopyWithImpl<UserPublicResponseDto>(
          this as UserPublicResponseDto, _$identity);

  /// Serializes this UserPublicResponseDto to a JSON map.
  Map<String, dynamic> toJson();

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is UserPublicResponseDto &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.displayName, displayName) ||
                other.displayName == displayName) &&
            (identical(other.email, email) || other.email == email));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, id, displayName, email);

  @override
  String toString() {
    return 'UserPublicResponseDto(id: $id, displayName: $displayName, email: $email)';
  }
}

/// @nodoc
abstract mixin class $UserPublicResponseDtoCopyWith<$Res> {
  factory $UserPublicResponseDtoCopyWith(UserPublicResponseDto value,
          $Res Function(UserPublicResponseDto) _then) =
      _$UserPublicResponseDtoCopyWithImpl;
  @useResult
  $Res call({String id, String displayName, String email});
}

/// @nodoc
class _$UserPublicResponseDtoCopyWithImpl<$Res>
    implements $UserPublicResponseDtoCopyWith<$Res> {
  _$UserPublicResponseDtoCopyWithImpl(this._self, this._then);

  final UserPublicResponseDto _self;
  final $Res Function(UserPublicResponseDto) _then;

  /// Create a copy of UserPublicResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? displayName = null,
    Object? email = null,
  }) {
    return _then(_self.copyWith(
      id: null == id
          ? _self.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      displayName: null == displayName
          ? _self.displayName
          : displayName // ignore: cast_nullable_to_non_nullable
              as String,
      email: null == email
          ? _self.email
          : email // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _UserPublicResponseDto implements UserPublicResponseDto {
  const _UserPublicResponseDto(
      {required this.id, required this.displayName, required this.email});
  factory _UserPublicResponseDto.fromJson(Map<String, dynamic> json) =>
      _$UserPublicResponseDtoFromJson(json);

  @override
  final String id;
  @override
  final String displayName;
  @override
  final String email;

  /// Create a copy of UserPublicResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  _$UserPublicResponseDtoCopyWith<_UserPublicResponseDto> get copyWith =>
      __$UserPublicResponseDtoCopyWithImpl<_UserPublicResponseDto>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$UserPublicResponseDtoToJson(
      this,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _UserPublicResponseDto &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.displayName, displayName) ||
                other.displayName == displayName) &&
            (identical(other.email, email) || other.email == email));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, id, displayName, email);

  @override
  String toString() {
    return 'UserPublicResponseDto(id: $id, displayName: $displayName, email: $email)';
  }
}

/// @nodoc
abstract mixin class _$UserPublicResponseDtoCopyWith<$Res>
    implements $UserPublicResponseDtoCopyWith<$Res> {
  factory _$UserPublicResponseDtoCopyWith(_UserPublicResponseDto value,
          $Res Function(_UserPublicResponseDto) _then) =
      __$UserPublicResponseDtoCopyWithImpl;
  @override
  @useResult
  $Res call({String id, String displayName, String email});
}

/// @nodoc
class __$UserPublicResponseDtoCopyWithImpl<$Res>
    implements _$UserPublicResponseDtoCopyWith<$Res> {
  __$UserPublicResponseDtoCopyWithImpl(this._self, this._then);

  final _UserPublicResponseDto _self;
  final $Res Function(_UserPublicResponseDto) _then;

  /// Create a copy of UserPublicResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $Res call({
    Object? id = null,
    Object? displayName = null,
    Object? email = null,
  }) {
    return _then(_UserPublicResponseDto(
      id: null == id
          ? _self.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      displayName: null == displayName
          ? _self.displayName
          : displayName // ignore: cast_nullable_to_non_nullable
              as String,
      email: null == email
          ? _self.email
          : email // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

// dart format on
