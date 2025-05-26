// dart format width=80
// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'task_create_request_dto.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$TaskCreateRequestDto {
  String get title;
  String get description;
  Priority get priority;
  DateTime? get dueDate;
  String? get projectId;
  String? get assigneeId;

  /// Create a copy of TaskCreateRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  $TaskCreateRequestDtoCopyWith<TaskCreateRequestDto> get copyWith =>
      _$TaskCreateRequestDtoCopyWithImpl<TaskCreateRequestDto>(
          this as TaskCreateRequestDto, _$identity);

  /// Serializes this TaskCreateRequestDto to a JSON map.
  Map<String, dynamic> toJson();

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is TaskCreateRequestDto &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.description, description) ||
                other.description == description) &&
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
  int get hashCode => Object.hash(runtimeType, title, description, priority,
      dueDate, projectId, assigneeId);

  @override
  String toString() {
    return 'TaskCreateRequestDto(title: $title, description: $description, priority: $priority, dueDate: $dueDate, projectId: $projectId, assigneeId: $assigneeId)';
  }
}

/// @nodoc
abstract mixin class $TaskCreateRequestDtoCopyWith<$Res> {
  factory $TaskCreateRequestDtoCopyWith(TaskCreateRequestDto value,
          $Res Function(TaskCreateRequestDto) _then) =
      _$TaskCreateRequestDtoCopyWithImpl;
  @useResult
  $Res call(
      {String title,
      String description,
      Priority priority,
      DateTime? dueDate,
      String? projectId,
      String? assigneeId});
}

/// @nodoc
class _$TaskCreateRequestDtoCopyWithImpl<$Res>
    implements $TaskCreateRequestDtoCopyWith<$Res> {
  _$TaskCreateRequestDtoCopyWithImpl(this._self, this._then);

  final TaskCreateRequestDto _self;
  final $Res Function(TaskCreateRequestDto) _then;

  /// Create a copy of TaskCreateRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? title = null,
    Object? description = null,
    Object? priority = null,
    Object? dueDate = freezed,
    Object? projectId = freezed,
    Object? assigneeId = freezed,
  }) {
    return _then(_self.copyWith(
      title: null == title
          ? _self.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      description: null == description
          ? _self.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
      priority: null == priority
          ? _self.priority
          : priority // ignore: cast_nullable_to_non_nullable
              as Priority,
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
class _TaskCreateRequestDto implements TaskCreateRequestDto {
  const _TaskCreateRequestDto(
      {required this.title,
      required this.description,
      required this.priority,
      this.dueDate,
      this.projectId,
      this.assigneeId});
  factory _TaskCreateRequestDto.fromJson(Map<String, dynamic> json) =>
      _$TaskCreateRequestDtoFromJson(json);

  @override
  final String title;
  @override
  final String description;
  @override
  final Priority priority;
  @override
  final DateTime? dueDate;
  @override
  final String? projectId;
  @override
  final String? assigneeId;

  /// Create a copy of TaskCreateRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  _$TaskCreateRequestDtoCopyWith<_TaskCreateRequestDto> get copyWith =>
      __$TaskCreateRequestDtoCopyWithImpl<_TaskCreateRequestDto>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$TaskCreateRequestDtoToJson(
      this,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _TaskCreateRequestDto &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.description, description) ||
                other.description == description) &&
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
  int get hashCode => Object.hash(runtimeType, title, description, priority,
      dueDate, projectId, assigneeId);

  @override
  String toString() {
    return 'TaskCreateRequestDto(title: $title, description: $description, priority: $priority, dueDate: $dueDate, projectId: $projectId, assigneeId: $assigneeId)';
  }
}

/// @nodoc
abstract mixin class _$TaskCreateRequestDtoCopyWith<$Res>
    implements $TaskCreateRequestDtoCopyWith<$Res> {
  factory _$TaskCreateRequestDtoCopyWith(_TaskCreateRequestDto value,
          $Res Function(_TaskCreateRequestDto) _then) =
      __$TaskCreateRequestDtoCopyWithImpl;
  @override
  @useResult
  $Res call(
      {String title,
      String description,
      Priority priority,
      DateTime? dueDate,
      String? projectId,
      String? assigneeId});
}

/// @nodoc
class __$TaskCreateRequestDtoCopyWithImpl<$Res>
    implements _$TaskCreateRequestDtoCopyWith<$Res> {
  __$TaskCreateRequestDtoCopyWithImpl(this._self, this._then);

  final _TaskCreateRequestDto _self;
  final $Res Function(_TaskCreateRequestDto) _then;

  /// Create a copy of TaskCreateRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $Res call({
    Object? title = null,
    Object? description = null,
    Object? priority = null,
    Object? dueDate = freezed,
    Object? projectId = freezed,
    Object? assigneeId = freezed,
  }) {
    return _then(_TaskCreateRequestDto(
      title: null == title
          ? _self.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      description: null == description
          ? _self.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
      priority: null == priority
          ? _self.priority
          : priority // ignore: cast_nullable_to_non_nullable
              as Priority,
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
