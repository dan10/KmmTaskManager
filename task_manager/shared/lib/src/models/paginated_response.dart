import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:json_annotation/json_annotation.dart';

part 'paginated_response.freezed.dart';
part 'paginated_response.g.dart';

@freezed
abstract class PaginatedResponse<T> with _$PaginatedResponse {
  const factory PaginatedResponse({
    required List<T> items,
    required int total,
    required int page,
    required int size,
    required int totalPages,
  }) = _PaginatedResponse;

  factory PaginatedResponse.fromJson(
    Map<String, dynamic> json,
    T Function(Object?) fromJsonT,
  ) =>
      _$PaginatedResponseFromJson(json, fromJsonT);

  @override
  Map<String, dynamic> toJson(
    Object? Function(T) toJsonT,
  );
}
