class PaginatedResponse<T> {
  final List<T> items;
  final int total;
  final int page;
  final int size;
  final int totalPages;

  const PaginatedResponse({
    required this.items,
    required this.total,
    required this.page,
    required this.size,
    required this.totalPages,
  });

  factory PaginatedResponse.fromJson(
    Map<String, dynamic> json,
    T Function(Object?) fromJsonT,
  ) {
    return PaginatedResponse<T>(
      items: (json['items'] as List<dynamic>)
          .map((item) => fromJsonT(item))
          .toList(),
      total: json['total'] as int,
      page: json['page'] as int,
      size: json['size'] as int,
      totalPages: json['totalPages'] as int,
    );
  }

  Map<String, dynamic> toJson(Object? Function(T) toJsonT) {
    return {
      'items': items.map((item) => toJsonT(item)).toList(),
      'total': total,
      'page': page,
      'size': size,
      'totalPages': totalPages,
    };
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is PaginatedResponse<T> &&
        other.items == items &&
        other.total == total &&
        other.page == page &&
        other.size == size &&
        other.totalPages == totalPages;
  }

  @override
  int get hashCode {
    return items.hashCode ^
        total.hashCode ^
        page.hashCode ^
        size.hashCode ^
        totalPages.hashCode;
  }

  @override
  String toString() {
    return 'PaginatedResponse<$T>(items: $items, total: $total, page: $page, size: $size, totalPages: $totalPages)';
  }
}
