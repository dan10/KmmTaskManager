// dart format width=80
// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'task_assign_request_dto.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$TaskAssignRequestDto {
  String get assigneeId;

  /// Create a copy of TaskAssignRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  $TaskAssignRequestDtoCopyWith<TaskAssignRequestDto> get copyWith =>
      _$TaskAssignRequestDtoCopyWithImpl<TaskAssignRequestDto>(
          this as TaskAssignRequestDto, _$identity);

  /// Serializes this TaskAssignRequestDto to a JSON map.
  Map<String, dynamic> toJson();

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is TaskAssignRequestDto &&
            (identical(other.assigneeId, assigneeId) ||
                other.assigneeId == assigneeId));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, assigneeId);

  @override
  String toString() {
    return 'TaskAssignRequestDto(assigneeId: $assigneeId)';
  }
}

/// @nodoc
abstract mixin class $TaskAssignRequestDtoCopyWith<$Res> {
  factory $TaskAssignRequestDtoCopyWith(TaskAssignRequestDto value,
          $Res Function(TaskAssignRequestDto) _then) =
      _$TaskAssignRequestDtoCopyWithImpl;
  @useResult
  $Res call({String assigneeId});
}

/// @nodoc
class _$TaskAssignRequestDtoCopyWithImpl<$Res>
    implements $TaskAssignRequestDtoCopyWith<$Res> {
  _$TaskAssignRequestDtoCopyWithImpl(this._self, this._then);

  final TaskAssignRequestDto _self;
  final $Res Function(TaskAssignRequestDto) _then;

  /// Create a copy of TaskAssignRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? assigneeId = null,
  }) {
    return _then(_self.copyWith(
      assigneeId: null == assigneeId
          ? _self.assigneeId
          : assigneeId // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _TaskAssignRequestDto implements TaskAssignRequestDto {
  const _TaskAssignRequestDto({required this.assigneeId});
  factory _TaskAssignRequestDto.fromJson(Map<String, dynamic> json) =>
      _$TaskAssignRequestDtoFromJson(json);

  @override
  final String assigneeId;

  /// Create a copy of TaskAssignRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  _$TaskAssignRequestDtoCopyWith<_TaskAssignRequestDto> get copyWith =>
      __$TaskAssignRequestDtoCopyWithImpl<_TaskAssignRequestDto>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$TaskAssignRequestDtoToJson(
      this,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _TaskAssignRequestDto &&
            (identical(other.assigneeId, assigneeId) ||
                other.assigneeId == assigneeId));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, assigneeId);

  @override
  String toString() {
    return 'TaskAssignRequestDto(assigneeId: $assigneeId)';
  }
}

/// @nodoc
abstract mixin class _$TaskAssignRequestDtoCopyWith<$Res>
    implements $TaskAssignRequestDtoCopyWith<$Res> {
  factory _$TaskAssignRequestDtoCopyWith(_TaskAssignRequestDto value,
          $Res Function(_TaskAssignRequestDto) _then) =
      __$TaskAssignRequestDtoCopyWithImpl;
  @override
  @useResult
  $Res call({String assigneeId});
}

/// @nodoc
class __$TaskAssignRequestDtoCopyWithImpl<$Res>
    implements _$TaskAssignRequestDtoCopyWith<$Res> {
  __$TaskAssignRequestDtoCopyWithImpl(this._self, this._then);

  final _TaskAssignRequestDto _self;
  final $Res Function(_TaskAssignRequestDto) _then;

  /// Create a copy of TaskAssignRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $Res call({
    Object? assigneeId = null,
  }) {
    return _then(_TaskAssignRequestDto(
      assigneeId: null == assigneeId
          ? _self.assigneeId
          : assigneeId // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

// dart format on
