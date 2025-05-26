// dart format width=80
// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'error_response_dto.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$ErrorResponseDto {
  int get statusCode;
  String get error; // e.g., "Not Found", "Bad Request"
  String? get message;
  Map<String, dynamic>? get details;

  /// Create a copy of ErrorResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  $ErrorResponseDtoCopyWith<ErrorResponseDto> get copyWith =>
      _$ErrorResponseDtoCopyWithImpl<ErrorResponseDto>(
          this as ErrorResponseDto, _$identity);

  /// Serializes this ErrorResponseDto to a JSON map.
  Map<String, dynamic> toJson();

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is ErrorResponseDto &&
            (identical(other.statusCode, statusCode) ||
                other.statusCode == statusCode) &&
            (identical(other.error, error) || other.error == error) &&
            (identical(other.message, message) || other.message == message) &&
            const DeepCollectionEquality().equals(other.details, details));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, statusCode, error, message,
      const DeepCollectionEquality().hash(details));

  @override
  String toString() {
    return 'ErrorResponseDto(statusCode: $statusCode, error: $error, message: $message, details: $details)';
  }
}

/// @nodoc
abstract mixin class $ErrorResponseDtoCopyWith<$Res> {
  factory $ErrorResponseDtoCopyWith(
          ErrorResponseDto value, $Res Function(ErrorResponseDto) _then) =
      _$ErrorResponseDtoCopyWithImpl;
  @useResult
  $Res call(
      {int statusCode,
      String error,
      String? message,
      Map<String, dynamic>? details});
}

/// @nodoc
class _$ErrorResponseDtoCopyWithImpl<$Res>
    implements $ErrorResponseDtoCopyWith<$Res> {
  _$ErrorResponseDtoCopyWithImpl(this._self, this._then);

  final ErrorResponseDto _self;
  final $Res Function(ErrorResponseDto) _then;

  /// Create a copy of ErrorResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? statusCode = null,
    Object? error = null,
    Object? message = freezed,
    Object? details = freezed,
  }) {
    return _then(_self.copyWith(
      statusCode: null == statusCode
          ? _self.statusCode
          : statusCode // ignore: cast_nullable_to_non_nullable
              as int,
      error: null == error
          ? _self.error
          : error // ignore: cast_nullable_to_non_nullable
              as String,
      message: freezed == message
          ? _self.message
          : message // ignore: cast_nullable_to_non_nullable
              as String?,
      details: freezed == details
          ? _self.details
          : details // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _ErrorResponseDto implements ErrorResponseDto {
  const _ErrorResponseDto(
      {required this.statusCode,
      required this.error,
      this.message,
      final Map<String, dynamic>? details})
      : _details = details;
  factory _ErrorResponseDto.fromJson(Map<String, dynamic> json) =>
      _$ErrorResponseDtoFromJson(json);

  @override
  final int statusCode;
  @override
  final String error;
// e.g., "Not Found", "Bad Request"
  @override
  final String? message;
  final Map<String, dynamic>? _details;
  @override
  Map<String, dynamic>? get details {
    final value = _details;
    if (value == null) return null;
    if (_details is EqualUnmodifiableMapView) return _details;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(value);
  }

  /// Create a copy of ErrorResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  _$ErrorResponseDtoCopyWith<_ErrorResponseDto> get copyWith =>
      __$ErrorResponseDtoCopyWithImpl<_ErrorResponseDto>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$ErrorResponseDtoToJson(
      this,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _ErrorResponseDto &&
            (identical(other.statusCode, statusCode) ||
                other.statusCode == statusCode) &&
            (identical(other.error, error) || other.error == error) &&
            (identical(other.message, message) || other.message == message) &&
            const DeepCollectionEquality().equals(other._details, _details));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, statusCode, error, message,
      const DeepCollectionEquality().hash(_details));

  @override
  String toString() {
    return 'ErrorResponseDto(statusCode: $statusCode, error: $error, message: $message, details: $details)';
  }
}

/// @nodoc
abstract mixin class _$ErrorResponseDtoCopyWith<$Res>
    implements $ErrorResponseDtoCopyWith<$Res> {
  factory _$ErrorResponseDtoCopyWith(
          _ErrorResponseDto value, $Res Function(_ErrorResponseDto) _then) =
      __$ErrorResponseDtoCopyWithImpl;
  @override
  @useResult
  $Res call(
      {int statusCode,
      String error,
      String? message,
      Map<String, dynamic>? details});
}

/// @nodoc
class __$ErrorResponseDtoCopyWithImpl<$Res>
    implements _$ErrorResponseDtoCopyWith<$Res> {
  __$ErrorResponseDtoCopyWithImpl(this._self, this._then);

  final _ErrorResponseDto _self;
  final $Res Function(_ErrorResponseDto) _then;

  /// Create a copy of ErrorResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $Res call({
    Object? statusCode = null,
    Object? error = null,
    Object? message = freezed,
    Object? details = freezed,
  }) {
    return _then(_ErrorResponseDto(
      statusCode: null == statusCode
          ? _self.statusCode
          : statusCode // ignore: cast_nullable_to_non_nullable
              as int,
      error: null == error
          ? _self.error
          : error // ignore: cast_nullable_to_non_nullable
              as String,
      message: freezed == message
          ? _self.message
          : message // ignore: cast_nullable_to_non_nullable
              as String?,
      details: freezed == details
          ? _self._details
          : details // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
    ));
  }
}

// dart format on
