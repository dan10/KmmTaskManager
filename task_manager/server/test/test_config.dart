import 'package:test/test.dart';
import '../lib/src/data/test_database.dart';

Future<void> setupTestDatabase() async {
  final db = TestDatabase();
  await db.connect();
  await db.clearTables();
  await db.disconnect();
}

void main() {
  setUpAll(() async {
    await setupTestDatabase();
  });
}
