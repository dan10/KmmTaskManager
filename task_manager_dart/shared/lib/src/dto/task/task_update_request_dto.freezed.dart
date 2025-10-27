// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
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

 String? get title; String? get description; TaskStatus? get status; Priority? get priority; DateTime? get dueDate; String? get assigneeId;
/// Create a copy of TaskUpdateRequestDto
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$TaskUpdateRequestDtoCopyWith<TaskUpdateRequestDto> get copyWith => _$TaskUpdateRequestDtoCopyWithImpl<TaskUpdateRequestDto>(this as TaskUpdateRequestDto, _$identity);

  /// Serializes this TaskUpdateRequestDto to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is TaskUpdateRequestDto&&(identical(other.title, title) || other.title == title)&&(identical(other.description, description) || other.description == description)&&(identical(other.status, status) || other.status == status)&&(identical(other.priority, priority) || other.priority == priority)&&(identical(other.dueDate, dueDate) || other.dueDate == dueDate)&&(identical(other.assigneeId, assigneeId) || other.assigneeId == assigneeId));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,title,description,status,priority,dueDate,assigneeId);

@override
String toString() {
  return 'TaskUpdateRequestDto(title: $title, description: $description, status: $status, priority: $priority, dueDate: $dueDate, assigneeId: $assigneeId)';
}


}

/// @nodoc
abstract mixin class $TaskUpdateRequestDtoCopyWith<$Res>  {
  factory $TaskUpdateRequestDtoCopyWith(TaskUpdateRequestDto value, $Res Function(TaskUpdateRequestDto) _then) = _$TaskUpdateRequestDtoCopyWithImpl;
@useResult
$Res call({
 String? title, String? description, TaskStatus? status, Priority? priority, DateTime? dueDate, String? assigneeId
});




}
/// @nodoc
class _$TaskUpdateRequestDtoCopyWithImpl<$Res>
    implements $TaskUpdateRequestDtoCopyWith<$Res> {
  _$TaskUpdateRequestDtoCopyWithImpl(this._self, this._then);

  final TaskUpdateRequestDto _self;
  final $Res Function(TaskUpdateRequestDto) _then;

/// Create a copy of TaskUpdateRequestDto
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? title = freezed,Object? description = freezed,Object? status = freezed,Object? priority = freezed,Object? dueDate = freezed,Object? assigneeId = freezed,}) {
  return _then(_self.copyWith(
title: freezed == title ? _self.title : title // ignore: cast_nullable_to_non_nullable
as String?,description: freezed == description ? _self.description : description // ignore: cast_nullable_to_non_nullable
as String?,status: freezed == status ? _self.status : status // ignore: cast_nullable_to_non_nullable
as TaskStatus?,priority: freezed == priority ? _self.priority : priority // ignore: cast_nullable_to_non_nullable
as Priority?,dueDate: freezed == dueDate ? _self.dueDate : dueDate // ignore: cast_nullable_to_non_nullable
as DateTime?,assigneeId: freezed == assigneeId ? _self.assigneeId : assigneeId // ignore: cast_nullable_to_non_nullable
as String?,
  ));
}

}


/// Adds pattern-matching-related methods to [TaskUpdateRequestDto].
extension TaskUpdateRequestDtoPatterns on TaskUpdateRequestDto {
/// A variant of `map` that fallback to returning `orElse`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _TaskUpdateRequestDto value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _TaskUpdateRequestDto() when $default != null:
return $default(_that);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// Callbacks receives the raw object, upcasted.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case final Subclass2 value:
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _TaskUpdateRequestDto value)  $default,){
final _that = this;
switch (_that) {
case _TaskUpdateRequestDto():
return $default(_that);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `map` that fallback to returning `null`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _TaskUpdateRequestDto value)?  $default,){
final _that = this;
switch (_that) {
case _TaskUpdateRequestDto() when $default != null:
return $default(_that);case _:
  return null;

}
}
/// A variant of `when` that fallback to an `orElse` callback.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String? title,  String? description,  TaskStatus? status,  Priority? priority,  DateTime? dueDate,  String? assigneeId)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _TaskUpdateRequestDto() when $default != null:
return $default(_that.title,_that.description,_that.status,_that.priority,_that.dueDate,_that.assigneeId);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// As opposed to `map`, this offers destructuring.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case Subclass2(:final field2):
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String? title,  String? description,  TaskStatus? status,  Priority? priority,  DateTime? dueDate,  String? assigneeId)  $default,) {final _that = this;
switch (_that) {
case _TaskUpdateRequestDto():
return $default(_that.title,_that.description,_that.status,_that.priority,_that.dueDate,_that.assigneeId);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `when` that fallback to returning `null`
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String? title,  String? description,  TaskStatus? status,  Priority? priority,  DateTime? dueDate,  String? assigneeId)?  $default,) {final _that = this;
switch (_that) {
case _TaskUpdateRequestDto() when $default != null:
return $default(_that.title,_that.description,_that.status,_that.priority,_that.dueDate,_that.assigneeId);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _TaskUpdateRequestDto implements TaskUpdateRequestDto {
  const _TaskUpdateRequestDto({this.title, this.description, this.status, this.priority, this.dueDate, this.assigneeId});
  factory _TaskUpdateRequestDto.fromJson(Map<String, dynamic> json) => _$TaskUpdateRequestDtoFromJson(json);

@override final  String? title;
@override final  String? description;
@override final  TaskStatus? status;
@override final  Priority? priority;
@override final  DateTime? dueDate;
@override final  String? assigneeId;

/// Create a copy of TaskUpdateRequestDto
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$TaskUpdateRequestDtoCopyWith<_TaskUpdateRequestDto> get copyWith => __$TaskUpdateRequestDtoCopyWithImpl<_TaskUpdateRequestDto>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$TaskUpdateRequestDtoToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _TaskUpdateRequestDto&&(identical(other.title, title) || other.title == title)&&(identical(other.description, description) || other.description == description)&&(identical(other.status, status) || other.status == status)&&(identical(other.priority, priority) || other.priority == priority)&&(identical(other.dueDate, dueDate) || other.dueDate == dueDate)&&(identical(other.assigneeId, assigneeId) || other.assigneeId == assigneeId));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,title,description,status,priority,dueDate,assigneeId);

@override
String toString() {
  return 'TaskUpdateRequestDto(title: $title, description: $description, status: $status, priority: $priority, dueDate: $dueDate, assigneeId: $assigneeId)';
}


}

/// @nodoc
abstract mixin class _$TaskUpdateRequestDtoCopyWith<$Res> implements $TaskUpdateRequestDtoCopyWith<$Res> {
  factory _$TaskUpdateRequestDtoCopyWith(_TaskUpdateRequestDto value, $Res Function(_TaskUpdateRequestDto) _then) = __$TaskUpdateRequestDtoCopyWithImpl;
@override @useResult
$Res call({
 String? title, String? description, TaskStatus? status, Priority? priority, DateTime? dueDate, String? assigneeId
});




}
/// @nodoc
class __$TaskUpdateRequestDtoCopyWithImpl<$Res>
    implements _$TaskUpdateRequestDtoCopyWith<$Res> {
  __$TaskUpdateRequestDtoCopyWithImpl(this._self, this._then);

  final _TaskUpdateRequestDto _self;
  final $Res Function(_TaskUpdateRequestDto) _then;

/// Create a copy of TaskUpdateRequestDto
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? title = freezed,Object? description = freezed,Object? status = freezed,Object? priority = freezed,Object? dueDate = freezed,Object? assigneeId = freezed,}) {
  return _then(_TaskUpdateRequestDto(
title: freezed == title ? _self.title : title // ignore: cast_nullable_to_non_nullable
as String?,description: freezed == description ? _self.description : description // ignore: cast_nullable_to_non_nullable
as String?,status: freezed == status ? _self.status : status // ignore: cast_nullable_to_non_nullable
as TaskStatus?,priority: freezed == priority ? _self.priority : priority // ignore: cast_nullable_to_non_nullable
as Priority?,dueDate: freezed == dueDate ? _self.dueDate : dueDate // ignore: cast_nullable_to_non_nullable
as DateTime?,assigneeId: freezed == assigneeId ? _self.assigneeId : assigneeId // ignore: cast_nullable_to_non_nullable
as String?,
  ));
}


}

// dart format on
