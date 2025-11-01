import 'package:uuid/uuid.dart';

/// UUID utility class for generating UUIDs.
/// 
/// Uses UUIDv7 which is time-ordered, providing better database indexing performance
/// compared to UUIDv4 (random).
/// 
/// Benefits of UUIDv7:
/// - Time-ordered: Sequential nature improves B-tree index performance
/// - Sortable: Can be sorted by creation time
/// - Unique: Still maintains UUID uniqueness guarantees
/// - Database-friendly: Reduces index fragmentation
class UuidUtils {
  static const _uuid = Uuid();

  /// Generates a UUIDv7 (time-ordered UUID).
  /// 
  /// UUIDv7 embeds a timestamp in the first 48 bits, making it naturally
  /// sortable and providing better database performance than random UUIDs.
  static String generate() {
    return _uuid.v7();
  }

  /// Validates if a string is a valid UUID.
  static bool isValid(String uuid) {
    return Uuid.isValidUUID(fromString: uuid);
  }
}

