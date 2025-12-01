// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'project_update_request_dto.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$ProjectUpdateRequestDto {

 String? get name; String? get description; List<String>? get memberIds;
/// Create a copy of ProjectUpdateRequestDto
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$ProjectUpdateRequestDtoCopyWith<ProjectUpdateRequestDto> get copyWith => _$ProjectUpdateRequestDtoCopyWithImpl<ProjectUpdateRequestDto>(this as ProjectUpdateRequestDto, _$identity);

  /// Serializes this ProjectUpdateRequestDto to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is ProjectUpdateRequestDto&&(identical(other.name, name) || other.name == name)&&(identical(other.description, description) || other.description == description)&&const DeepCollectionEquality().equals(other.memberIds, memberIds));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,name,description,const DeepCollectionEquality().hash(memberIds));

@override
String toString() {
  return 'ProjectUpdateRequestDto(name: $name, description: $description, memberIds: $memberIds)';
}


}

/// @nodoc
abstract mixin class $ProjectUpdateRequestDtoCopyWith<$Res>  {
  factory $ProjectUpdateRequestDtoCopyWith(ProjectUpdateRequestDto value, $Res Function(ProjectUpdateRequestDto) _then) = _$ProjectUpdateRequestDtoCopyWithImpl;
@useResult
$Res call({
 String? name, String? description, List<String>? memberIds
});




}
/// @nodoc
class _$ProjectUpdateRequestDtoCopyWithImpl<$Res>
    implements $ProjectUpdateRequestDtoCopyWith<$Res> {
  _$ProjectUpdateRequestDtoCopyWithImpl(this._self, this._then);

  final ProjectUpdateRequestDto _self;
  final $Res Function(ProjectUpdateRequestDto) _then;

/// Create a copy of ProjectUpdateRequestDto
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? name = freezed,Object? description = freezed,Object? memberIds = freezed,}) {
  return _then(_self.copyWith(
name: freezed == name ? _self.name : name // ignore: cast_nullable_to_non_nullable
as String?,description: freezed == description ? _self.description : description // ignore: cast_nullable_to_non_nullable
as String?,memberIds: freezed == memberIds ? _self.memberIds : memberIds // ignore: cast_nullable_to_non_nullable
as List<String>?,
  ));
}

}


/// Adds pattern-matching-related methods to [ProjectUpdateRequestDto].
extension ProjectUpdateRequestDtoPatterns on ProjectUpdateRequestDto {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _ProjectUpdateRequestDto value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _ProjectUpdateRequestDto() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _ProjectUpdateRequestDto value)  $default,){
final _that = this;
switch (_that) {
case _ProjectUpdateRequestDto():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _ProjectUpdateRequestDto value)?  $default,){
final _that = this;
switch (_that) {
case _ProjectUpdateRequestDto() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String? name,  String? description,  List<String>? memberIds)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _ProjectUpdateRequestDto() when $default != null:
return $default(_that.name,_that.description,_that.memberIds);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String? name,  String? description,  List<String>? memberIds)  $default,) {final _that = this;
switch (_that) {
case _ProjectUpdateRequestDto():
return $default(_that.name,_that.description,_that.memberIds);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String? name,  String? description,  List<String>? memberIds)?  $default,) {final _that = this;
switch (_that) {
case _ProjectUpdateRequestDto() when $default != null:
return $default(_that.name,_that.description,_that.memberIds);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _ProjectUpdateRequestDto implements ProjectUpdateRequestDto {
  const _ProjectUpdateRequestDto({this.name, this.description, final  List<String>? memberIds}): _memberIds = memberIds;
  factory _ProjectUpdateRequestDto.fromJson(Map<String, dynamic> json) => _$ProjectUpdateRequestDtoFromJson(json);

@override final  String? name;
@override final  String? description;
 final  List<String>? _memberIds;
@override List<String>? get memberIds {
  final value = _memberIds;
  if (value == null) return null;
  if (_memberIds is EqualUnmodifiableListView) return _memberIds;
  // ignore: implicit_dynamic_type
  return EqualUnmodifiableListView(value);
}


/// Create a copy of ProjectUpdateRequestDto
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$ProjectUpdateRequestDtoCopyWith<_ProjectUpdateRequestDto> get copyWith => __$ProjectUpdateRequestDtoCopyWithImpl<_ProjectUpdateRequestDto>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$ProjectUpdateRequestDtoToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _ProjectUpdateRequestDto&&(identical(other.name, name) || other.name == name)&&(identical(other.description, description) || other.description == description)&&const DeepCollectionEquality().equals(other._memberIds, _memberIds));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,name,description,const DeepCollectionEquality().hash(_memberIds));

@override
String toString() {
  return 'ProjectUpdateRequestDto(name: $name, description: $description, memberIds: $memberIds)';
}


}

/// @nodoc
abstract mixin class _$ProjectUpdateRequestDtoCopyWith<$Res> implements $ProjectUpdateRequestDtoCopyWith<$Res> {
  factory _$ProjectUpdateRequestDtoCopyWith(_ProjectUpdateRequestDto value, $Res Function(_ProjectUpdateRequestDto) _then) = __$ProjectUpdateRequestDtoCopyWithImpl;
@override @useResult
$Res call({
 String? name, String? description, List<String>? memberIds
});




}
/// @nodoc
class __$ProjectUpdateRequestDtoCopyWithImpl<$Res>
    implements _$ProjectUpdateRequestDtoCopyWith<$Res> {
  __$ProjectUpdateRequestDtoCopyWithImpl(this._self, this._then);

  final _ProjectUpdateRequestDto _self;
  final $Res Function(_ProjectUpdateRequestDto) _then;

/// Create a copy of ProjectUpdateRequestDto
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? name = freezed,Object? description = freezed,Object? memberIds = freezed,}) {
  return _then(_ProjectUpdateRequestDto(
name: freezed == name ? _self.name : name // ignore: cast_nullable_to_non_nullable
as String?,description: freezed == description ? _self.description : description // ignore: cast_nullable_to_non_nullable
as String?,memberIds: freezed == memberIds ? _self._memberIds : memberIds // ignore: cast_nullable_to_non_nullable
as List<String>?,
  ));
}


}

// dart format on
