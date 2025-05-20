import 'package:postgres/postgres.dart';
import 'package:shared/src/models/task.dart' as shared;

class TaskRepository {
  final PostgreSQLConnection _db;

  TaskRepository(this._db);

  Future<List<shared.Task>> findAllByUserId(String userId) async {
    final result = await _db.query(
      'SELECT * FROM tasks WHERE creator_id = @userId OR assignee_id = @userId',
      substitutionValues: {'userId': userId},
    );
    return result.map(_mapTaskFromRow).toList();
  }

  Future<shared.Task?> findById(String id) async {
    final result = await _db.query(
      'SELECT * FROM tasks WHERE id = @id',
      substitutionValues: {'id': id},
    );

    if (result.isEmpty) return null;
    return _mapTaskFromRow(result.first);
  }

  Future<shared.Task> create(shared.Task task) async {
    final result = await _db.query(
      '''
      INSERT INTO tasks (id, title, description, status, priority, project_id, assignee_id, creator_id, due_date)
      VALUES (@id, @title, @description, @status, @priority, @projectId, @assigneeId, @creatorId, @dueDate)
      RETURNING *
      ''',
      substitutionValues: {
        'id': task.id,
        'title': task.title,
        'description': task.description,
        'status': task.status.toString(),
        'priority': task.priority.toString(),
        'projectId': task.projectId,
        'assigneeId': task.assigneeId,
        'creatorId': task.creatorId,
        'dueDate': task.dueDate?.toIso8601String(),
      },
    );

    return _mapTaskFromRow(result.first);
  }

  Future<shared.Task> update(shared.Task task) async {
    await _db.execute(
      '''
      UPDATE tasks
      SET title = @title,
          description = @description,
          status = @status,
          priority = @priority,
          project_id = @projectId,
          assignee_id = @assigneeId,
          due_date = @dueDate
      WHERE id = @id
      ''',
      substitutionValues: {
        'id': task.id,
        'title': task.title,
        'description': task.description,
        'status': task.status.toString(),
        'priority': task.priority.toString(),
        'projectId': task.projectId,
        'assigneeId': task.assigneeId,
        'dueDate': task.dueDate?.toIso8601String(),
      },
    );

    final updatedTask = await findById(task.id);
    if (updatedTask == null) {
      throw Exception('Task not found after update');
    }
    return updatedTask;
  }

  Future<void> delete(String id) async {
    await _db.execute(
      'DELETE FROM tasks WHERE id = @id',
      substitutionValues: {'id': id},
    );
  }

  shared.Task _mapTaskFromRow(List<dynamic> row) {
    final dueDateValue = row[8];
    final dueDate = dueDateValue is DateTime ? dueDateValue : null;

    return shared.Task(
      id: row[0] as String,
      title: row[1] as String,
      description: (row[2] as String?) ?? '',
      status: shared.TaskStatus.values.firstWhere(
        (e) => e.toString() == row[3] as String,
      ),
      priority: shared.Priority.values.firstWhere(
        (e) => e.toString() == row[4] as String,
      ),
      projectId: row[5] as String?,
      assigneeId: row[6] as String?,
      creatorId: row[7] as String,
      dueDate: dueDate,
    );
  }
}
