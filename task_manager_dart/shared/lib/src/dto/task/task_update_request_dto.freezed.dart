// dart format width=80
// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'task_update_request_dto.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$TaskUpdateRequestDto {
  String? get title;
  String? get description;
  TaskStatus? get status;
  Priority? get priority;
  DateTime? get dueDate;
  String? get projectId;
  String? get assigneeId;

  /// Create a copy of TaskUpdateRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  $TaskUpdateRequestDtoCopyWith<TaskUpdateRequestDto> get copyWith =>
      _$TaskUpdateRequestDtoCopyWithImpl<TaskUpdateRequestDto>(
          this as TaskUpdateRequestDto, _$identity);

  /// Serializes this TaskUpdateRequestDto to a JSON map.
  Map<String, dynamic> toJson();

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is TaskUpdateRequestDto &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.description, description) ||
                other.description == description) &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.priority, priority) ||
                other.priority == priority) &&
            (identical(other.dueDate, dueDate) || other.dueDate == dueDate) &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            (identical(other.assigneeId, assigneeId) ||
                other.assigneeId == assigneeId));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, title, description, status,
      priority, dueDate, projectId, assigneeId);

  @override
  String toString() {
    return 'TaskUpdateRequestDto(title: $title, description: $description, status: $status, priority: $priority, dueDate: $dueDate, projectId: $projectId, assigneeId: $assigneeId)';
  }
}

/// @nodoc
abstract mixin class $TaskUpdateRequestDtoCopyWith<$Res> {
  factory $TaskUpdateRequestDtoCopyWith(TaskUpdateRequestDto value,
          $Res Function(TaskUpdateRequestDto) _then) =
      _$TaskUpdateRequestDtoCopyWithImpl;
  @useResult
  $Res call(
      {String? title,
      String? description,
      TaskStatus? status,
      Priority? priority,
      DateTime? dueDate,
      String? projectId,
      String? assigneeId});
}

/// @nodoc
class _$TaskUpdateRequestDtoCopyWithImpl<$Res>
    implements $TaskUpdateRequestDtoCopyWith<$Res> {
  _$TaskUpdateRequestDtoCopyWithImpl(this._self, this._then);

  final TaskUpdateRequestDto _self;
  final $Res Function(TaskUpdateRequestDto) _then;

  /// Create a copy of TaskUpdateRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? title = freezed,
    Object? description = freezed,
    Object? status = freezed,
    Object? priority = freezed,
    Object? dueDate = freezed,
    Object? projectId = freezed,
    Object? assigneeId = freezed,
  }) {
    return _then(_self.copyWith(
      title: freezed == title
          ? _self.title
          : title // ignore: cast_nullable_to_non_nullable
              as String?,
      description: freezed == description
          ? _self.description
          : description // ignore: cast_nullable_to_non_nullable
              as String?,
      status: freezed == status
          ? _self.status
          : status // ignore: cast_nullable_to_non_nullable
              as TaskStatus?,
      priority: freezed == priority
          ? _self.priority
          : priority // ignore: cast_nullable_to_non_nullable
              as Priority?,
      dueDate: freezed == dueDate
          ? _self.dueDate
          : dueDate // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      projectId: freezed == projectId
          ? _self.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String?,
      assigneeId: freezed == assigneeId
          ? _self.assigneeId
          : assigneeId // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _TaskUpdateRequestDto implements TaskUpdateRequestDto {
  const _TaskUpdateRequestDto(
      {this.title,
      this.description,
      this.status,
      this.priority,
      this.dueDate,
      this.projectId,
      this.assigneeId});
  factory _TaskUpdateRequestDto.fromJson(Map<String, dynamic> json) =>
      _$TaskUpdateRequestDtoFromJson(json);

  @override
  final String? title;
  @override
  final String? description;
  @override
  final TaskStatus? status;
  @override
  final Priority? priority;
  @override
  final DateTime? dueDate;
  @override
  final String? projectId;
  @override
  final String? assigneeId;

  /// Create a copy of TaskUpdateRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  _$TaskUpdateRequestDtoCopyWith<_TaskUpdateRequestDto> get copyWith =>
      __$TaskUpdateRequestDtoCopyWithImpl<_TaskUpdateRequestDto>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$TaskUpdateRequestDtoToJson(
      this,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _TaskUpdateRequestDto &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.description, description) ||
                other.description == description) &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.priority, priority) ||
                other.priority == priority) &&
            (identical(other.dueDate, dueDate) || other.dueDate == dueDate) &&
            (identical(other.projectId, projectId) ||
                other.projectId == projectId) &&
            (identical(other.assigneeId, assigneeId) ||
                other.assigneeId == assigneeId));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, title, description, status,
      priority, dueDate, projectId, assigneeId);

  @override
  String toString() {
    return 'TaskUpdateRequestDto(title: $title, description: $description, status: $status, priority: $priority, dueDate: $dueDate, projectId: $projectId, assigneeId: $assigneeId)';
  }
}

/// @nodoc
abstract mixin class _$TaskUpdateRequestDtoCopyWith<$Res>
    implements $TaskUpdateRequestDtoCopyWith<$Res> {
  factory _$TaskUpdateRequestDtoCopyWith(_TaskUpdateRequestDto value,
          $Res Function(_TaskUpdateRequestDto) _then) =
      __$TaskUpdateRequestDtoCopyWithImpl;
  @override
  @useResult
  $Res call(
      {String? title,
      String? description,
      TaskStatus? status,
      Priority? priority,
      DateTime? dueDate,
      String? projectId,
      String? assigneeId});
}

/// @nodoc
class __$TaskUpdateRequestDtoCopyWithImpl<$Res>
    implements _$TaskUpdateRequestDtoCopyWith<$Res> {
  __$TaskUpdateRequestDtoCopyWithImpl(this._self, this._then);

  final _TaskUpdateRequestDto _self;
  final $Res Function(_TaskUpdateRequestDto) _then;

  /// Create a copy of TaskUpdateRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $Res call({
    Object? title = freezed,
    Object? description = freezed,
    Object? status = freezed,
    Object? priority = freezed,
    Object? dueDate = freezed,
    Object? projectId = freezed,
    Object? assigneeId = freezed,
  }) {
    return _then(_TaskUpdateRequestDto(
      title: freezed == title
          ? _self.title
          : title // ignore: cast_nullable_to_non_nullable
              as String?,
      description: freezed == description
          ? _self.description
          : description // ignore: cast_nullable_to_non_nullable
              as String?,
      status: freezed == status
          ? _self.status
          : status // ignore: cast_nullable_to_non_nullable
              as TaskStatus?,
      priority: freezed == priority
          ? _self.priority
          : priority // ignore: cast_nullable_to_non_nullable
              as Priority?,
      dueDate: freezed == dueDate
          ? _self.dueDate
          : dueDate // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      projectId: freezed == projectId
          ? _self.projectId
          : projectId // ignore: cast_nullable_to_non_nullable
              as String?,
      assigneeId: freezed == assigneeId
          ? _self.assigneeId
          : assigneeId // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

// dart format on
