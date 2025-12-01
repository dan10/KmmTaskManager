// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'task_progress_dto.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$TaskProgress {

 int get totalTasks; int get completedTasks;
/// Create a copy of TaskProgress
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$TaskProgressCopyWith<TaskProgress> get copyWith => _$TaskProgressCopyWithImpl<TaskProgress>(this as TaskProgress, _$identity);

  /// Serializes this TaskProgress to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is TaskProgress&&(identical(other.totalTasks, totalTasks) || other.totalTasks == totalTasks)&&(identical(other.completedTasks, completedTasks) || other.completedTasks == completedTasks));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,totalTasks,completedTasks);

@override
String toString() {
  return 'TaskProgress(totalTasks: $totalTasks, completedTasks: $completedTasks)';
}


}

/// @nodoc
abstract mixin class $TaskProgressCopyWith<$Res>  {
  factory $TaskProgressCopyWith(TaskProgress value, $Res Function(TaskProgress) _then) = _$TaskProgressCopyWithImpl;
@useResult
$Res call({
 int totalTasks, int completedTasks
});




}
/// @nodoc
class _$TaskProgressCopyWithImpl<$Res>
    implements $TaskProgressCopyWith<$Res> {
  _$TaskProgressCopyWithImpl(this._self, this._then);

  final TaskProgress _self;
  final $Res Function(TaskProgress) _then;

/// Create a copy of TaskProgress
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? totalTasks = null,Object? completedTasks = null,}) {
  return _then(_self.copyWith(
totalTasks: null == totalTasks ? _self.totalTasks : totalTasks // ignore: cast_nullable_to_non_nullable
as int,completedTasks: null == completedTasks ? _self.completedTasks : completedTasks // ignore: cast_nullable_to_non_nullable
as int,
  ));
}

}


/// Adds pattern-matching-related methods to [TaskProgress].
extension TaskProgressPatterns on TaskProgress {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _TaskProgress value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _TaskProgress() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _TaskProgress value)  $default,){
final _that = this;
switch (_that) {
case _TaskProgress():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _TaskProgress value)?  $default,){
final _that = this;
switch (_that) {
case _TaskProgress() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( int totalTasks,  int completedTasks)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _TaskProgress() when $default != null:
return $default(_that.totalTasks,_that.completedTasks);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( int totalTasks,  int completedTasks)  $default,) {final _that = this;
switch (_that) {
case _TaskProgress():
return $default(_that.totalTasks,_that.completedTasks);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( int totalTasks,  int completedTasks)?  $default,) {final _that = this;
switch (_that) {
case _TaskProgress() when $default != null:
return $default(_that.totalTasks,_that.completedTasks);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _TaskProgress implements TaskProgress {
  const _TaskProgress({required this.totalTasks, required this.completedTasks});
  factory _TaskProgress.fromJson(Map<String, dynamic> json) => _$TaskProgressFromJson(json);

@override final  int totalTasks;
@override final  int completedTasks;

/// Create a copy of TaskProgress
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$TaskProgressCopyWith<_TaskProgress> get copyWith => __$TaskProgressCopyWithImpl<_TaskProgress>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$TaskProgressToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _TaskProgress&&(identical(other.totalTasks, totalTasks) || other.totalTasks == totalTasks)&&(identical(other.completedTasks, completedTasks) || other.completedTasks == completedTasks));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,totalTasks,completedTasks);

@override
String toString() {
  return 'TaskProgress(totalTasks: $totalTasks, completedTasks: $completedTasks)';
}


}

/// @nodoc
abstract mixin class _$TaskProgressCopyWith<$Res> implements $TaskProgressCopyWith<$Res> {
  factory _$TaskProgressCopyWith(_TaskProgress value, $Res Function(_TaskProgress) _then) = __$TaskProgressCopyWithImpl;
@override @useResult
$Res call({
 int totalTasks, int completedTasks
});




}
/// @nodoc
class __$TaskProgressCopyWithImpl<$Res>
    implements _$TaskProgressCopyWith<$Res> {
  __$TaskProgressCopyWithImpl(this._self, this._then);

  final _TaskProgress _self;
  final $Res Function(_TaskProgress) _then;

/// Create a copy of TaskProgress
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? totalTasks = null,Object? completedTasks = null,}) {
  return _then(_TaskProgress(
totalTasks: null == totalTasks ? _self.totalTasks : totalTasks // ignore: cast_nullable_to_non_nullable
as int,completedTasks: null == completedTasks ? _self.completedTasks : completedTasks // ignore: cast_nullable_to_non_nullable
as int,
  ));
}


}

// dart format on
