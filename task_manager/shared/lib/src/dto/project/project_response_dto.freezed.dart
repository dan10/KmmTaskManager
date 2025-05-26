// dart format width=80
// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'project_response_dto.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$ProjectResponseDto {
  String get id;
  String get name;
  String? get description;
  int get completed;
  int get inProgress;
  int get total;
  String? get creatorId;
  List<String> get memberIds;
  User? get creator;
  List<User> get members;
  DateTime? get createdAt;
  DateTime? get updatedAt;

  /// Create a copy of ProjectResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  $ProjectResponseDtoCopyWith<ProjectResponseDto> get copyWith =>
      _$ProjectResponseDtoCopyWithImpl<ProjectResponseDto>(
          this as ProjectResponseDto, _$identity);

  /// Serializes this ProjectResponseDto to a JSON map.
  Map<String, dynamic> toJson();

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is ProjectResponseDto &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.description, description) ||
                other.description == description) &&
            (identical(other.completed, completed) ||
                other.completed == completed) &&
            (identical(other.inProgress, inProgress) ||
                other.inProgress == inProgress) &&
            (identical(other.total, total) || other.total == total) &&
            (identical(other.creatorId, creatorId) ||
                other.creatorId == creatorId) &&
            const DeepCollectionEquality().equals(other.memberIds, memberIds) &&
            (identical(other.creator, creator) || other.creator == creator) &&
            const DeepCollectionEquality().equals(other.members, members) &&
            (identical(other.createdAt, createdAt) ||
                other.createdAt == createdAt) &&
            (identical(other.updatedAt, updatedAt) ||
                other.updatedAt == updatedAt));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      name,
      description,
      completed,
      inProgress,
      total,
      creatorId,
      const DeepCollectionEquality().hash(memberIds),
      creator,
      const DeepCollectionEquality().hash(members),
      createdAt,
      updatedAt);

  @override
  String toString() {
    return 'ProjectResponseDto(id: $id, name: $name, description: $description, completed: $completed, inProgress: $inProgress, total: $total, creatorId: $creatorId, memberIds: $memberIds, creator: $creator, members: $members, createdAt: $createdAt, updatedAt: $updatedAt)';
  }
}

/// @nodoc
abstract mixin class $ProjectResponseDtoCopyWith<$Res> {
  factory $ProjectResponseDtoCopyWith(
          ProjectResponseDto value, $Res Function(ProjectResponseDto) _then) =
      _$ProjectResponseDtoCopyWithImpl;
  @useResult
  $Res call(
      {String id,
      String name,
      String? description,
      int completed,
      int inProgress,
      int total,
      String? creatorId,
      List<String> memberIds,
      User? creator,
      List<User> members,
      DateTime? createdAt,
      DateTime? updatedAt});

  $UserCopyWith<$Res>? get creator;
}

/// @nodoc
class _$ProjectResponseDtoCopyWithImpl<$Res>
    implements $ProjectResponseDtoCopyWith<$Res> {
  _$ProjectResponseDtoCopyWithImpl(this._self, this._then);

  final ProjectResponseDto _self;
  final $Res Function(ProjectResponseDto) _then;

  /// Create a copy of ProjectResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? name = null,
    Object? description = freezed,
    Object? completed = null,
    Object? inProgress = null,
    Object? total = null,
    Object? creatorId = freezed,
    Object? memberIds = null,
    Object? creator = freezed,
    Object? members = null,
    Object? createdAt = freezed,
    Object? updatedAt = freezed,
  }) {
    return _then(_self.copyWith(
      id: null == id
          ? _self.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _self.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      description: freezed == description
          ? _self.description
          : description // ignore: cast_nullable_to_non_nullable
              as String?,
      completed: null == completed
          ? _self.completed
          : completed // ignore: cast_nullable_to_non_nullable
              as int,
      inProgress: null == inProgress
          ? _self.inProgress
          : inProgress // ignore: cast_nullable_to_non_nullable
              as int,
      total: null == total
          ? _self.total
          : total // ignore: cast_nullable_to_non_nullable
              as int,
      creatorId: freezed == creatorId
          ? _self.creatorId
          : creatorId // ignore: cast_nullable_to_non_nullable
              as String?,
      memberIds: null == memberIds
          ? _self.memberIds
          : memberIds // ignore: cast_nullable_to_non_nullable
              as List<String>,
      creator: freezed == creator
          ? _self.creator
          : creator // ignore: cast_nullable_to_non_nullable
              as User?,
      members: null == members
          ? _self.members
          : members // ignore: cast_nullable_to_non_nullable
              as List<User>,
      createdAt: freezed == createdAt
          ? _self.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      updatedAt: freezed == updatedAt
          ? _self.updatedAt
          : updatedAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ));
  }

  /// Create a copy of ProjectResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $UserCopyWith<$Res>? get creator {
    if (_self.creator == null) {
      return null;
    }

    return $UserCopyWith<$Res>(_self.creator!, (value) {
      return _then(_self.copyWith(creator: value));
    });
  }
}

/// @nodoc
@JsonSerializable()
class _ProjectResponseDto implements ProjectResponseDto {
  const _ProjectResponseDto(
      {required this.id,
      required this.name,
      this.description,
      this.completed = 0,
      this.inProgress = 0,
      this.total = 0,
      this.creatorId,
      final List<String> memberIds = const [],
      this.creator,
      final List<User> members = const [],
      this.createdAt,
      this.updatedAt})
      : _memberIds = memberIds,
        _members = members;
  factory _ProjectResponseDto.fromJson(Map<String, dynamic> json) =>
      _$ProjectResponseDtoFromJson(json);

  @override
  final String id;
  @override
  final String name;
  @override
  final String? description;
  @override
  @JsonKey()
  final int completed;
  @override
  @JsonKey()
  final int inProgress;
  @override
  @JsonKey()
  final int total;
  @override
  final String? creatorId;
  final List<String> _memberIds;
  @override
  @JsonKey()
  List<String> get memberIds {
    if (_memberIds is EqualUnmodifiableListView) return _memberIds;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_memberIds);
  }

  @override
  final User? creator;
  final List<User> _members;
  @override
  @JsonKey()
  List<User> get members {
    if (_members is EqualUnmodifiableListView) return _members;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_members);
  }

  @override
  final DateTime? createdAt;
  @override
  final DateTime? updatedAt;

  /// Create a copy of ProjectResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  @pragma('vm:prefer-inline')
  _$ProjectResponseDtoCopyWith<_ProjectResponseDto> get copyWith =>
      __$ProjectResponseDtoCopyWithImpl<_ProjectResponseDto>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$ProjectResponseDtoToJson(
      this,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _ProjectResponseDto &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.description, description) ||
                other.description == description) &&
            (identical(other.completed, completed) ||
                other.completed == completed) &&
            (identical(other.inProgress, inProgress) ||
                other.inProgress == inProgress) &&
            (identical(other.total, total) || other.total == total) &&
            (identical(other.creatorId, creatorId) ||
                other.creatorId == creatorId) &&
            const DeepCollectionEquality()
                .equals(other._memberIds, _memberIds) &&
            (identical(other.creator, creator) || other.creator == creator) &&
            const DeepCollectionEquality().equals(other._members, _members) &&
            (identical(other.createdAt, createdAt) ||
                other.createdAt == createdAt) &&
            (identical(other.updatedAt, updatedAt) ||
                other.updatedAt == updatedAt));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      name,
      description,
      completed,
      inProgress,
      total,
      creatorId,
      const DeepCollectionEquality().hash(_memberIds),
      creator,
      const DeepCollectionEquality().hash(_members),
      createdAt,
      updatedAt);

  @override
  String toString() {
    return 'ProjectResponseDto(id: $id, name: $name, description: $description, completed: $completed, inProgress: $inProgress, total: $total, creatorId: $creatorId, memberIds: $memberIds, creator: $creator, members: $members, createdAt: $createdAt, updatedAt: $updatedAt)';
  }
}

/// @nodoc
abstract mixin class _$ProjectResponseDtoCopyWith<$Res>
    implements $ProjectResponseDtoCopyWith<$Res> {
  factory _$ProjectResponseDtoCopyWith(
          _ProjectResponseDto value, $Res Function(_ProjectResponseDto) _then) =
      __$ProjectResponseDtoCopyWithImpl;
  @override
  @useResult
  $Res call(
      {String id,
      String name,
      String? description,
      int completed,
      int inProgress,
      int total,
      String? creatorId,
      List<String> memberIds,
      User? creator,
      List<User> members,
      DateTime? createdAt,
      DateTime? updatedAt});

  @override
  $UserCopyWith<$Res>? get creator;
}

/// @nodoc
class __$ProjectResponseDtoCopyWithImpl<$Res>
    implements _$ProjectResponseDtoCopyWith<$Res> {
  __$ProjectResponseDtoCopyWithImpl(this._self, this._then);

  final _ProjectResponseDto _self;
  final $Res Function(_ProjectResponseDto) _then;

  /// Create a copy of ProjectResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $Res call({
    Object? id = null,
    Object? name = null,
    Object? description = freezed,
    Object? completed = null,
    Object? inProgress = null,
    Object? total = null,
    Object? creatorId = freezed,
    Object? memberIds = null,
    Object? creator = freezed,
    Object? members = null,
    Object? createdAt = freezed,
    Object? updatedAt = freezed,
  }) {
    return _then(_ProjectResponseDto(
      id: null == id
          ? _self.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _self.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      description: freezed == description
          ? _self.description
          : description // ignore: cast_nullable_to_non_nullable
              as String?,
      completed: null == completed
          ? _self.completed
          : completed // ignore: cast_nullable_to_non_nullable
              as int,
      inProgress: null == inProgress
          ? _self.inProgress
          : inProgress // ignore: cast_nullable_to_non_nullable
              as int,
      total: null == total
          ? _self.total
          : total // ignore: cast_nullable_to_non_nullable
              as int,
      creatorId: freezed == creatorId
          ? _self.creatorId
          : creatorId // ignore: cast_nullable_to_non_nullable
              as String?,
      memberIds: null == memberIds
          ? _self._memberIds
          : memberIds // ignore: cast_nullable_to_non_nullable
              as List<String>,
      creator: freezed == creator
          ? _self.creator
          : creator // ignore: cast_nullable_to_non_nullable
              as User?,
      members: null == members
          ? _self._members
          : members // ignore: cast_nullable_to_non_nullable
              as List<User>,
      createdAt: freezed == createdAt
          ? _self.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      updatedAt: freezed == updatedAt
          ? _self.updatedAt
          : updatedAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ));
  }

  /// Create a copy of ProjectResponseDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $UserCopyWith<$Res>? get creator {
    if (_self.creator == null) {
      return null;
    }

    return $UserCopyWith<$Res>(_self.creator!, (value) {
      return _then(_self.copyWith(creator: value));
    });
  }
}

// dart format on
