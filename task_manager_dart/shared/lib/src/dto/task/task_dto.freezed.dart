// dart format width=80
// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'task_dto.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$TaskDto {
  String get id;
  String get title;
  String get description;
  TaskStatus get status;
  Priority get priority;
  DateTime? get dueDate;
  String? get projectId;
  String? get assigneeId;
  String get creatorId;
  DateTime? get createdAt;
  DateTime? get updatedAt; // Optional user information for API responses
  UserPublicResponseDto? get assignee;
  UserPublicResponseDto? get creator;

  /// Create a copy of TaskDto
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  $TaskDtoCopyWith<TaskDto> get copyWith =>
      _$TaskDtoCopyWithImpl<TaskDto>(this as TaskDto, _$identity);

  /// Serializes this TaskDto to a JSON map.
  Map<String, dynamic> toJson();

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is TaskDto &&
            (identical(other.id, id) || other.id == id) &&
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
                other.assigneeId == assigneeId) &&
            (identical(other.creatorId, creatorId) ||
                other.creatorId == creatorId) &&
            (identical(other.createdAt, createdAt) ||
                other.createdAt == createdAt) &&
            (identical(other.updatedAt, updatedAt) ||
                other.updatedAt == updatedAt) &&
            (identical(other.assignee, assignee) ||
                other.assignee == assignee) &&
            (identical(other.creator, creator) || other.creator == creator));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      title,
      description,
      status,
      priority,
      dueDate,
      projectId,
      assigneeId,
      creatorId,
      createdAt,
      updatedAt,
      assignee,
      creator);

  @override
  String toString() {
    return 'TaskDto(id: $id, title: $title, description: $description, status: $status, priority: $priority, dueDate: $dueDate, projectId: $projectId, assigneeId: $assigneeId, creatorId: $creatorId, createdAt: $createdAt, updatedAt: $updatedAt, assignee: $assignee, creator: $creator)';
  }
}

/// @nodoc
abstract mixin class $TaskDtoCopyWith<$Res> {
  factory $TaskDtoCopyWith(TaskDto value, $Res Function(TaskDto) _then) =
      _$TaskDtoCopyWithImpl;
  @useResult
  $Res call(
      {String id,
      String title,
      String description,
      TaskStatus status,
      Priority priority,
      DateTime? dueDate,
      String? projectId,
      String? assigneeId,
      String creatorId,
      DateTime? createdAt,
      DateTime? updatedAt,
      UserPublicResponseDto? assignee,
      UserPublicResponseDto? creator});

  $UserPublicResponseDtoCopyWith<$Res>? get assignee;
  $UserPublicResponseDtoCopyWith<$Res>? get creator;
}

/// @nodoc
class _$TaskDtoCopyWithImpl<$Res> implements $TaskDtoCopyWith<$Res> {
  _$TaskDtoCopyWithImpl(this._self, this._then);

  final TaskDto _self;
  final $Res Function(TaskDto) _then;

  /// Create a copy of TaskDto
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? title = null,
    Object? description = null,
    Object? status = null,
    Object? priority = null,
    Object? dueDate = freezed,
    Object? projectId = freezed,
    Object? assigneeId = freezed,
    Object? creatorId = null,
    Object? createdAt = freezed,
    Object? updatedAt = freezed,
    Object? assignee = freezed,
    Object? creator = freezed,
  }) {
    return _then(_self.copyWith(
      id: null == id
          ? _self.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _self.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      description: null == description
          ? _self.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _self.status
          : status // ignore: cast_nullable_to_non_nullable
              as TaskStatus,
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
      creatorId: null == creatorId
          ? _self.creatorId
          : creatorId // ignore: cast_nullable_to_non_nullable
              as String,
      createdAt: freezed == createdAt
          ? _self.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      updatedAt: freezed == updatedAt
          ? _self.updatedAt
          : updatedAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      assignee: freezed == assignee
          ? _self.assignee
          : assignee // ignore: cast_nullable_to_non_nullable
              as UserPublicResponseDto?,
      creator: freezed == creator
          ? _self.creator
          : creator // ignore: cast_nullable_to_non_nullable
              as UserPublicResponseDto?,
    ));
  }

  /// Create a copy of TaskDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $UserPublicResponseDtoCopyWith<$Res>? get assignee {
    if (_self.assignee == null) {
      return null;
    }

    return $UserPublicResponseDtoCopyWith<$Res>(_self.assignee!, (value) {
      return _then(_self.copyWith(assignee: value));
    });
  }

  /// Create a copy of TaskDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $UserPublicResponseDtoCopyWith<$Res>? get creator {
    if (_self.creator == null) {
      return null;
    }

    return $UserPublicResponseDtoCopyWith<$Res>(_self.creator!, (value) {
      return _then(_self.copyWith(creator: value));
    });
  }
}

/// @nodoc
@JsonSerializable()
class _TaskDto implements TaskDto {
  const _TaskDto(
      {required this.id,
      required this.title,
      required this.description,
      required this.status,
      required this.priority,
      this.dueDate,
      this.projectId,
      this.assigneeId,
      required this.creatorId,
      this.createdAt,
      this.updatedAt,
      this.assignee,
      this.creator});
  factory _TaskDto.fromJson(Map<String, dynamic> json) =>
      _$TaskDtoFromJson(json);

  @override
  final String id;
  @override
  final String title;
  @override
  final String description;
  @override
  final TaskStatus status;
  @override
  final Priority priority;
  @override
  final DateTime? dueDate;
  @override
  final String? projectId;
  @override
  final String? assigneeId;
  @override
  final String creatorId;
  @override
  final DateTime? createdAt;
  @override
  final DateTime? updatedAt;
// Optional user information for API responses
  @override
  final UserPublicResponseDto? assignee;
  @override
  final UserPublicResponseDto? creator;

  /// Create a copy of TaskDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  _$TaskDtoCopyWith<_TaskDto> get copyWith =>
      __$TaskDtoCopyWithImpl<_TaskDto>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$TaskDtoToJson(
      this,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _TaskDto &&
            (identical(other.id, id) || other.id == id) &&
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
                other.assigneeId == assigneeId) &&
            (identical(other.creatorId, creatorId) ||
                other.creatorId == creatorId) &&
            (identical(other.createdAt, createdAt) ||
                other.createdAt == createdAt) &&
            (identical(other.updatedAt, updatedAt) ||
                other.updatedAt == updatedAt) &&
            (identical(other.assignee, assignee) ||
                other.assignee == assignee) &&
            (identical(other.creator, creator) || other.creator == creator));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      title,
      description,
      status,
      priority,
      dueDate,
      projectId,
      assigneeId,
      creatorId,
      createdAt,
      updatedAt,
      assignee,
      creator);

  @override
  String toString() {
    return 'TaskDto(id: $id, title: $title, description: $description, status: $status, priority: $priority, dueDate: $dueDate, projectId: $projectId, assigneeId: $assigneeId, creatorId: $creatorId, createdAt: $createdAt, updatedAt: $updatedAt, assignee: $assignee, creator: $creator)';
  }
}

/// @nodoc
abstract mixin class _$TaskDtoCopyWith<$Res> implements $TaskDtoCopyWith<$Res> {
  factory _$TaskDtoCopyWith(_TaskDto value, $Res Function(_TaskDto) _then) =
      __$TaskDtoCopyWithImpl;
  @override
  @useResult
  $Res call(
      {String id,
      String title,
      String description,
      TaskStatus status,
      Priority priority,
      DateTime? dueDate,
      String? projectId,
      String? assigneeId,
      String creatorId,
      DateTime? createdAt,
      DateTime? updatedAt,
      UserPublicResponseDto? assignee,
      UserPublicResponseDto? creator});

  @override
  $UserPublicResponseDtoCopyWith<$Res>? get assignee;
  @override
  $UserPublicResponseDtoCopyWith<$Res>? get creator;
}

/// @nodoc
class __$TaskDtoCopyWithImpl<$Res> implements _$TaskDtoCopyWith<$Res> {
  __$TaskDtoCopyWithImpl(this._self, this._then);

  final _TaskDto _self;
  final $Res Function(_TaskDto) _then;

  /// Create a copy of TaskDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $Res call({
    Object? id = null,
    Object? title = null,
    Object? description = null,
    Object? status = null,
    Object? priority = null,
    Object? dueDate = freezed,
    Object? projectId = freezed,
    Object? assigneeId = freezed,
    Object? creatorId = null,
    Object? createdAt = freezed,
    Object? updatedAt = freezed,
    Object? assignee = freezed,
    Object? creator = freezed,
  }) {
    return _then(_TaskDto(
      id: null == id
          ? _self.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _self.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      description: null == description
          ? _self.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _self.status
          : status // ignore: cast_nullable_to_non_nullable
              as TaskStatus,
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
      creatorId: null == creatorId
          ? _self.creatorId
          : creatorId // ignore: cast_nullable_to_non_nullable
              as String,
      createdAt: freezed == createdAt
          ? _self.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      updatedAt: freezed == updatedAt
          ? _self.updatedAt
          : updatedAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      assignee: freezed == assignee
          ? _self.assignee
          : assignee // ignore: cast_nullable_to_non_nullable
              as UserPublicResponseDto?,
      creator: freezed == creator
          ? _self.creator
          : creator // ignore: cast_nullable_to_non_nullable
              as UserPublicResponseDto?,
    ));
  }

  /// Create a copy of TaskDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $UserPublicResponseDtoCopyWith<$Res>? get assignee {
    if (_self.assignee == null) {
      return null;
    }

    return $UserPublicResponseDtoCopyWith<$Res>(_self.assignee!, (value) {
      return _then(_self.copyWith(assignee: value));
    });
  }

  /// Create a copy of TaskDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $UserPublicResponseDtoCopyWith<$Res>? get creator {
    if (_self.creator == null) {
      return null;
    }

    return $UserPublicResponseDtoCopyWith<$Res>(_self.creator!, (value) {
      return _then(_self.copyWith(creator: value));
    });
  }
}

// dart format on
