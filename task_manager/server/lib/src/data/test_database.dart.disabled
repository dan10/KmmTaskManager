import 'in_memory_database.dart';

class TestDatabase extends InMemoryDatabase {
  @override
  Future<void> connect() async {
    await super.connect();
    await _createTestTables();
  }

  Future<void> _createTestTables() async {
    // No-op since tables are created in the constructor
  }
}
