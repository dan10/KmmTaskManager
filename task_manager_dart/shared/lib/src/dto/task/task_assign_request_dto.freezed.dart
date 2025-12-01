// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
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
$TaskAssignRequestDtoCopyWith<TaskAssignRequestDto> get copyWith => _$TaskAssignRequestDtoCopyWithImpl<TaskAssignRequestDto>(this as TaskAssignRequestDto, _$identity);

  /// Serializes this TaskAssignRequestDto to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is TaskAssignRequestDto&&(identical(other.assigneeId, assigneeId) || other.assigneeId == assigneeId));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,assigneeId);

@override
String toString() {
  return 'TaskAssignRequestDto(assigneeId: $assigneeId)';
}


}

/// @nodoc
abstract mixin class $TaskAssignRequestDtoCopyWith<$Res>  {
  factory $TaskAssignRequestDtoCopyWith(TaskAssignRequestDto value, $Res Function(TaskAssignRequestDto) _then) = _$TaskAssignRequestDtoCopyWithImpl;
@useResult
$Res call({
 String assigneeId
});




}
/// @nodoc
class _$TaskAssignRequestDtoCopyWithImpl<$Res>
    implements $TaskAssignRequestDtoCopyWith<$Res> {
  _$TaskAssignRequestDtoCopyWithImpl(this._self, this._then);

  final TaskAssignRequestDto _self;
  final $Res Function(TaskAssignRequestDto) _then;

/// Create a copy of TaskAssignRequestDto
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? assigneeId = null,}) {
  return _then(_self.copyWith(
assigneeId: null == assigneeId ? _self.assigneeId : assigneeId // ignore: cast_nullable_to_non_nullable
as String,
  ));
}

}


/// Adds pattern-matching-related methods to [TaskAssignRequestDto].
extension TaskAssignRequestDtoPatterns on TaskAssignRequestDto {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _TaskAssignRequestDto value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _TaskAssignRequestDto() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _TaskAssignRequestDto value)  $default,){
final _that = this;
switch (_that) {
case _TaskAssignRequestDto():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _TaskAssignRequestDto value)?  $default,){
final _that = this;
switch (_that) {
case _TaskAssignRequestDto() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String assigneeId)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _TaskAssignRequestDto() when $default != null:
return $default(_that.assigneeId);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String assigneeId)  $default,) {final _that = this;
switch (_that) {
case _TaskAssignRequestDto():
return $default(_that.assigneeId);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String assigneeId)?  $default,) {final _that = this;
switch (_that) {
case _TaskAssignRequestDto() when $default != null:
return $default(_that.assigneeId);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _TaskAssignRequestDto implements TaskAssignRequestDto {
  const _TaskAssignRequestDto({required this.assigneeId});
  factory _TaskAssignRequestDto.fromJson(Map<String, dynamic> json) => _$TaskAssignRequestDtoFromJson(json);

@override final  String assigneeId;

/// Create a copy of TaskAssignRequestDto
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$TaskAssignRequestDtoCopyWith<_TaskAssignRequestDto> get copyWith => __$TaskAssignRequestDtoCopyWithImpl<_TaskAssignRequestDto>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$TaskAssignRequestDtoToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _TaskAssignRequestDto&&(identical(other.assigneeId, assigneeId) || other.assigneeId == assigneeId));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,assigneeId);

@override
String toString() {
  return 'TaskAssignRequestDto(assigneeId: $assigneeId)';
}


}

/// @nodoc
abstract mixin class _$TaskAssignRequestDtoCopyWith<$Res> implements $TaskAssignRequestDtoCopyWith<$Res> {
  factory _$TaskAssignRequestDtoCopyWith(_TaskAssignRequestDto value, $Res Function(_TaskAssignRequestDto) _then) = __$TaskAssignRequestDtoCopyWithImpl;
@override @useResult
$Res call({
 String assigneeId
});




}
/// @nodoc
class __$TaskAssignRequestDtoCopyWithImpl<$Res>
    implements _$TaskAssignRequestDtoCopyWith<$Res> {
  __$TaskAssignRequestDtoCopyWithImpl(this._self, this._then);

  final _TaskAssignRequestDto _self;
  final $Res Function(_TaskAssignRequestDto) _then;

/// Create a copy of TaskAssignRequestDto
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? assigneeId = null,}) {
  return _then(_TaskAssignRequestDto(
assigneeId: null == assigneeId ? _self.assigneeId : assigneeId // ignore: cast_nullable_to_non_nullable
as String,
  ));
}


}

// dart format on
