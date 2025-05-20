import 'package:postgres/postgres.dart';
import 'package:shared/src/models/task.dart';
import '../data/database.dart';

class TaskRepositoryImpl implements TaskRepository {
  final PostgreSQLConnection _db;

  TaskRepositoryImpl(this._db);

  @override
  Future<List<Task>> findAllByProjectId(
    String? projectId, {
    int page = 0,
    int size = 10,
  }) async {
    final query = '''
      SELECT * FROM tasks
      WHERE project_id = @projectId
      ORDER BY created_at DESC
      LIMIT @limit OFFSET @offset
    ''';

    final results = await _db.mappedResultsQuery(
      query,
      substitutionValues: {
        'projectId': projectId,
        'limit': size,
        'offset': page * size,
      },
    );

    return results.map((row) => _mapToTask(row['tasks']!)).toList();
  }

  @override
  Future<List<Task>> findAllByOwnerId(
    String ownerId, {
    int page = 0,
    int size = 10,
  }) async {
    final query = '''
      SELECT * FROM tasks
      WHERE creator_id = @ownerId
      ORDER BY created_at DESC
      LIMIT @limit OFFSET @offset
    ''';

    final results = await _db.mappedResultsQuery(
      query,
      substitutionValues: {
        'ownerId': ownerId,
        'limit': size,
        'offset': page * size,
      },
    );

    return results.map((row) => _mapToTask(row['tasks']!)).toList();
  }

  @override
  Future<List<Task>> findAllByAssigneeId(
    String assigneeId, {
    int page = 0,
    int size = 10,
    String? query,
  }) async {
    final sqlQuery = '''
      SELECT * FROM tasks
      WHERE assignee_id = @assigneeId
      ${query != null ? 'AND title ILIKE @searchQuery' : ''}
      ORDER BY created_at DESC
      LIMIT @limit OFFSET @offset
    ''';

    final results = await _db.mappedResultsQuery(
      sqlQuery,
      substitutionValues: {
        'assigneeId': assigneeId,
        'searchQuery': '%$query%',
        'limit': size,
        'offset': page * size,
      },
    );

    return results.map((row) => _mapToTask(row['tasks']!)).toList();
  }

  @override
  Future<List<Task>> findAllTasksForUser(String userId) async {
    final query = '''
      SELECT * FROM tasks
      WHERE creator_id = @userId OR assignee_id = @userId
      ORDER BY created_at DESC
    ''';

    final results = await _db.mappedResultsQuery(
      query,
      substitutionValues: {'userId': userId},
    );

    return results.map((row) => _mapToTask(row['tasks']!)).toList();
  }

  @override
  Future<Task?> findById(String id) async {
    final query = 'SELECT * FROM tasks WHERE id = @id';
    final results = await _db.mappedResultsQuery(
      query,
      substitutionValues: {'id': id},
    );

    if (results.isEmpty) return null;
    return _mapToTask(results.first['tasks']!);
  }

  @override
  Future<Task> create(Task task) async {
    final query = '''
      INSERT INTO tasks (
        title, description, status, priority, due_date,
        project_id, assignee_id, creator_id
      ) VALUES (
        @title, @description, @status, @priority, @dueDate,
        @projectId, @assigneeId, @creatorId
      ) RETURNING *
    ''';

    final results = await _db.mappedResultsQuery(
      query,
      substitutionValues: {
        'title': task.title,
        'description': task.description,
        'status': task.isCompleted ? 'DONE' : 'TODO',
        'priority': task.priority,
        'dueDate': task.dueDate,
        'projectId': task.projectId,
        'assigneeId': task.assigneeId,
        'creatorId': task.creatorId,
      },
    );

    return _mapToTask(results.first['tasks']!);
  }

  @override
  Future<Task?> update(String id, Task task) async {
    final query = '''
      UPDATE tasks
      SET title = @title,
          description = @description,
          status = @status,
          priority = @priority,
          due_date = @dueDate,
          project_id = @projectId,
          assignee_id = @assigneeId
      WHERE id = @id
      RETURNING *
    ''';

    final results = await _db.mappedResultsQuery(
      query,
      substitutionValues: {
        'id': id,
        'title': task.title,
        'description': task.description,
        'status': task.isCompleted ? 'DONE' : 'TODO',
        'priority': task.priority,
        'dueDate': task.dueDate,
        'projectId': task.projectId,
        'assigneeId': task.assigneeId,
        'creatorId': task.creatorId,
      },
    );

    if (results.isEmpty) return null;
    return _mapToTask(results.first['tasks']!);
  }

  @override
  Future<bool> delete(String id) async {
    final query = 'DELETE FROM tasks WHERE id = @id';
    final result = await _db.execute(query, substitutionValues: {'id': id});

    return result > 0;
  }

  Task _mapToTask(Map<String, dynamic> row) {
    return Task(
      id: row['id'].toString(),
      title: row['title'],
      description: row['description'] ?? '',
      isCompleted: row['status'] == 'DONE',
      projectId: row['project_id']?.toString(),
      assigneeId: row['assignee_id']?.toString(),
      creatorId: row['creator_id'].toString(),
      priority: row['priority'],
      dueDate: row['due_date'],
    );
  }
}
