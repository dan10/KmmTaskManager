// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'task_status_change_request_dto.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$TaskStatusChangeRequestDto {

 TaskStatus get status;
/// Create a copy of TaskStatusChangeRequestDto
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$TaskStatusChangeRequestDtoCopyWith<TaskStatusChangeRequestDto> get copyWith => _$TaskStatusChangeRequestDtoCopyWithImpl<TaskStatusChangeRequestDto>(this as TaskStatusChangeRequestDto, _$identity);

  /// Serializes this TaskStatusChangeRequestDto to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is TaskStatusChangeRequestDto&&(identical(other.status, status) || other.status == status));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,status);

@override
String toString() {
  return 'TaskStatusChangeRequestDto(status: $status)';
}


}

/// @nodoc
abstract mixin class $TaskStatusChangeRequestDtoCopyWith<$Res>  {
  factory $TaskStatusChangeRequestDtoCopyWith(TaskStatusChangeRequestDto value, $Res Function(TaskStatusChangeRequestDto) _then) = _$TaskStatusChangeRequestDtoCopyWithImpl;
@useResult
$Res call({
 TaskStatus status
});




}
/// @nodoc
class _$TaskStatusChangeRequestDtoCopyWithImpl<$Res>
    implements $TaskStatusChangeRequestDtoCopyWith<$Res> {
  _$TaskStatusChangeRequestDtoCopyWithImpl(this._self, this._then);

  final TaskStatusChangeRequestDto _self;
  final $Res Function(TaskStatusChangeRequestDto) _then;

/// Create a copy of TaskStatusChangeRequestDto
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? status = null,}) {
  return _then(_self.copyWith(
status: null == status ? _self.status : status // ignore: cast_nullable_to_non_nullable
as TaskStatus,
  ));
}

}


/// Adds pattern-matching-related methods to [TaskStatusChangeRequestDto].
extension TaskStatusChangeRequestDtoPatterns on TaskStatusChangeRequestDto {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _TaskStatusChangeRequestDto value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _TaskStatusChangeRequestDto() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _TaskStatusChangeRequestDto value)  $default,){
final _that = this;
switch (_that) {
case _TaskStatusChangeRequestDto():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _TaskStatusChangeRequestDto value)?  $default,){
final _that = this;
switch (_that) {
case _TaskStatusChangeRequestDto() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( TaskStatus status)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _TaskStatusChangeRequestDto() when $default != null:
return $default(_that.status);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( TaskStatus status)  $default,) {final _that = this;
switch (_that) {
case _TaskStatusChangeRequestDto():
return $default(_that.status);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( TaskStatus status)?  $default,) {final _that = this;
switch (_that) {
case _TaskStatusChangeRequestDto() when $default != null:
return $default(_that.status);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _TaskStatusChangeRequestDto implements TaskStatusChangeRequestDto {
  const _TaskStatusChangeRequestDto({required this.status});
  factory _TaskStatusChangeRequestDto.fromJson(Map<String, dynamic> json) => _$TaskStatusChangeRequestDtoFromJson(json);

@override final  TaskStatus status;

/// Create a copy of TaskStatusChangeRequestDto
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$TaskStatusChangeRequestDtoCopyWith<_TaskStatusChangeRequestDto> get copyWith => __$TaskStatusChangeRequestDtoCopyWithImpl<_TaskStatusChangeRequestDto>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$TaskStatusChangeRequestDtoToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _TaskStatusChangeRequestDto&&(identical(other.status, status) || other.status == status));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,status);

@override
String toString() {
  return 'TaskStatusChangeRequestDto(status: $status)';
}


}

/// @nodoc
abstract mixin class _$TaskStatusChangeRequestDtoCopyWith<$Res> implements $TaskStatusChangeRequestDtoCopyWith<$Res> {
  factory _$TaskStatusChangeRequestDtoCopyWith(_TaskStatusChangeRequestDto value, $Res Function(_TaskStatusChangeRequestDto) _then) = __$TaskStatusChangeRequestDtoCopyWithImpl;
@override @useResult
$Res call({
 TaskStatus status
});




}
/// @nodoc
class __$TaskStatusChangeRequestDtoCopyWithImpl<$Res>
    implements _$TaskStatusChangeRequestDtoCopyWith<$Res> {
  __$TaskStatusChangeRequestDtoCopyWithImpl(this._self, this._then);

  final _TaskStatusChangeRequestDto _self;
  final $Res Function(_TaskStatusChangeRequestDto) _then;

/// Create a copy of TaskStatusChangeRequestDto
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? status = null,}) {
  return _then(_TaskStatusChangeRequestDto(
status: null == status ? _self.status : status // ignore: cast_nullable_to_non_nullable
as TaskStatus,
  ));
}


}

// dart format on
