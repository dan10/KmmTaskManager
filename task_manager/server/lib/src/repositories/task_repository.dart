import 'package:postgres/postgres.dart';
import 'package:shared/src/models/task.dart' as shared;
import 'package:shared/src/models/user.dart' as shared;

class TaskRepository {
  final PostgreSQLConnection _db;

  TaskRepository(this._db);

  Future<shared.Task> createTask(shared.Task task) async {
    final result = await _db.query(
      '''
      INSERT INTO tasks (id, title, description, status, priority, project_id, assignee_id, creator_id)
      VALUES (@id, @title, @description, @status, @priority, @projectId, @assigneeId, @creatorId)
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
      },
    );

    return _mapTaskFromRow(result.first);
  }

  Future<shared.Task?> findTaskById(String id) async {
    final result = await _db.query(
      'SELECT * FROM tasks WHERE id = @id',
      substitutionValues: {'id': id},
    );

    if (result.isEmpty) return null;
    return _mapTaskFromRow(result.first);
  }

  Future<List<shared.Task>> findAllTasks() async {
    final result = await _db.query('SELECT * FROM tasks');
    return result.map(_mapTaskFromRow).toList();
  }

  Future<List<shared.Task>> findTasksByProjectId(String projectId) async {
    final result = await _db.query(
      'SELECT * FROM tasks WHERE project_id = @projectId',
      substitutionValues: {'projectId': projectId},
    );

    return result.map(_mapTaskFromRow).toList();
  }

  Future<List<shared.Task>> findTasksByAssigneeId(String assigneeId) async {
    final result = await _db.query(
      'SELECT * FROM tasks WHERE assignee_id = @assigneeId',
      substitutionValues: {'assigneeId': assigneeId},
    );

    return result.map(_mapTaskFromRow).toList();
  }

  Future<List<shared.Task>> findTasksByCreatorId(String creatorId) async {
    final result = await _db.query(
      'SELECT * FROM tasks WHERE creator_id = @creatorId',
      substitutionValues: {'creatorId': creatorId},
    );

    return result.map(_mapTaskFromRow).toList();
  }

  Future<List<shared.Task>> findAllByUserId(String userId) async {
    final result = await _db.query(
      'SELECT * FROM tasks WHERE creator_id = @userId OR assignee_id = @userId',
      substitutionValues: {'userId': userId},
    );
    return result.map(_mapTaskFromRow).toList();
  }

  Future<void> updateTask(shared.Task task) async {
    await _db.execute(
      '''
      UPDATE tasks
      SET title = @title,
          description = @description,
          status = @status,
          priority = @priority,
          project_id = @projectId,
          assignee_id = @assigneeId
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
      },
    );
  }

  Future<void> deleteTask(String id) async {
    await _db.execute(
      'DELETE FROM tasks WHERE id = @id',
      substitutionValues: {'id': id},
    );
  }

  shared.Task _mapTaskFromRow(List<dynamic> row) {
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
    );
  }
}
