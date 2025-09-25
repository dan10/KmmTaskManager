// dart format width=80
// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'create_project_request_dto.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$CreateProjectRequestDto {
  String get name;
  String? get description;

  /// Create a copy of CreateProjectRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  $CreateProjectRequestDtoCopyWith<CreateProjectRequestDto> get copyWith =>
      _$CreateProjectRequestDtoCopyWithImpl<CreateProjectRequestDto>(
          this as CreateProjectRequestDto, _$identity);

  /// Serializes this CreateProjectRequestDto to a JSON map.
  Map<String, dynamic> toJson();

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is CreateProjectRequestDto &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.description, description) ||
                other.description == description));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, name, description);

  @override
  String toString() {
    return 'CreateProjectRequestDto(name: $name, description: $description)';
  }
}

/// @nodoc
abstract mixin class $CreateProjectRequestDtoCopyWith<$Res> {
  factory $CreateProjectRequestDtoCopyWith(CreateProjectRequestDto value,
          $Res Function(CreateProjectRequestDto) _then) =
      _$CreateProjectRequestDtoCopyWithImpl;
  @useResult
  $Res call({String name, String? description});
}

/// @nodoc
class _$CreateProjectRequestDtoCopyWithImpl<$Res>
    implements $CreateProjectRequestDtoCopyWith<$Res> {
  _$CreateProjectRequestDtoCopyWithImpl(this._self, this._then);

  final CreateProjectRequestDto _self;
  final $Res Function(CreateProjectRequestDto) _then;

  /// Create a copy of CreateProjectRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? name = null,
    Object? description = freezed,
  }) {
    return _then(_self.copyWith(
      name: null == name
          ? _self.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      description: freezed == description
          ? _self.description
          : description // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _CreateProjectRequestDto implements CreateProjectRequestDto {
  const _CreateProjectRequestDto({required this.name, this.description});
  factory _CreateProjectRequestDto.fromJson(Map<String, dynamic> json) =>
      _$CreateProjectRequestDtoFromJson(json);

  @override
  final String name;
  @override
  final String? description;

  /// Create a copy of CreateProjectRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  _$CreateProjectRequestDtoCopyWith<_CreateProjectRequestDto> get copyWith =>
      __$CreateProjectRequestDtoCopyWithImpl<_CreateProjectRequestDto>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$CreateProjectRequestDtoToJson(
      this,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _CreateProjectRequestDto &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.description, description) ||
                other.description == description));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, name, description);

  @override
  String toString() {
    return 'CreateProjectRequestDto(name: $name, description: $description)';
  }
}

/// @nodoc
abstract mixin class _$CreateProjectRequestDtoCopyWith<$Res>
    implements $CreateProjectRequestDtoCopyWith<$Res> {
  factory _$CreateProjectRequestDtoCopyWith(_CreateProjectRequestDto value,
          $Res Function(_CreateProjectRequestDto) _then) =
      __$CreateProjectRequestDtoCopyWithImpl;
  @override
  @useResult
  $Res call({String name, String? description});
}

/// @nodoc
class __$CreateProjectRequestDtoCopyWithImpl<$Res>
    implements _$CreateProjectRequestDtoCopyWith<$Res> {
  __$CreateProjectRequestDtoCopyWithImpl(this._self, this._then);

  final _CreateProjectRequestDto _self;
  final $Res Function(_CreateProjectRequestDto) _then;

  /// Create a copy of CreateProjectRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $Res call({
    Object? name = null,
    Object? description = freezed,
  }) {
    return _then(_CreateProjectRequestDto(
      name: null == name
          ? _self.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      description: freezed == description
          ? _self.description
          : description // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

// dart format on
