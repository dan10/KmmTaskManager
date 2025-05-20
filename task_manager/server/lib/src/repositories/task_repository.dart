import '../data/database.dart';
import '../models/models.dart';
import 'package:shared/src/models/task.dart' as shared;

abstract class TaskRepository {
  Future<shared.Task> create(shared.Task task);
  Future<shared.Task?> findById(String id);
  Future<List<shared.Task>> findAll();
  Future<shared.Task> update(shared.Task task);
  Future<void> delete(String id);
}

class TaskRepositoryImpl implements TaskRepository {
  final Database _db;

  TaskRepositoryImpl(this._db);

  Future<List<shared.Task>> findByUserId(String userId) async {
    final results = await _db.query(
      '''
      SELECT * FROM tasks WHERE creator_id = @userId
      ''',
      parameters: {'userId': userId},
    );

    return results
        .map((row) => shared.Task(
              id: row['id'] as String,
              title: row['title'] as String,
              description: row['description'] as String,
              status: shared.TaskStatus.values.firstWhere(
                (e) => e.toString() == row['status'] as String,
              ),
              priority: shared.Priority.values.firstWhere(
                (e) => e.toString() == row['priority'] as String,
              ),
              dueDate: row['due_date'] != null
                  ? DateTime.parse(row['due_date'] as String)
                  : null,
              projectId: row['project_id'] as String?,
              assigneeId: row['assignee_id'] as String?,
              creatorId: row['creator_id'] as String,
            ))
        .toList();
  }

  @override
  Future<shared.Task?> findById(String taskId) async {
    final results = await _db.query(
      'SELECT * FROM tasks WHERE id = @taskId',
      parameters: {'taskId': taskId},
    );

    if (results.isEmpty) {
      return null;
    }

    final row = results.first;
    return shared.Task(
      id: row['id'] as String,
      title: row['title'] as String,
      description: row['description'] as String,
      status: shared.TaskStatus.values.firstWhere(
        (e) => e.toString() == row['status'] as String,
      ),
      priority: shared.Priority.values.firstWhere(
        (e) => e.toString() == row['priority'] as String,
      ),
      dueDate: row['due_date'] != null
          ? DateTime.parse(row['due_date'] as String)
          : null,
      projectId: row['project_id'] as String?,
      assigneeId: row['assignee_id'] as String?,
      creatorId: row['creator_id'] as String,
    );
  }

  @override
  Future<shared.Task> create(shared.Task task) async {
    await _db.execute(
      '''
      INSERT INTO tasks (id, title, description, status, priority, due_date, project_id, assignee_id, creator_id)
      VALUES (@id, @title, @description, @status, @priority, @dueDate, @projectId, @assigneeId, @creatorId)
      ''',
      parameters: {
        'id': task.id,
        'title': task.title,
        'description': task.description,
        'status': task.status.toString(),
        'priority': task.priority.toString(),
        'dueDate': task.dueDate?.toIso8601String(),
        'projectId': task.projectId,
        'assigneeId': task.assigneeId,
        'creatorId': task.creatorId,
      },
    );
    return task;
  }

  @override
  Future<List<shared.Task>> findAll() async {
    final results = await _db.query('SELECT * FROM tasks');
    return results
        .map((row) => shared.Task(
              id: row['id'] as String,
              title: row['title'] as String,
              description: row['description'] as String,
              status: shared.TaskStatus.values.firstWhere(
                (e) => e.toString() == row['status'] as String,
              ),
              priority: shared.Priority.values.firstWhere(
                (e) => e.toString() == row['priority'] as String,
              ),
              dueDate: row['due_date'] != null
                  ? DateTime.parse(row['due_date'] as String)
                  : null,
              projectId: row['project_id'] as String?,
              assigneeId: row['assignee_id'] as String?,
              creatorId: row['creator_id'] as String,
            ))
        .toList();
  }

  @override
  Future<shared.Task> update(shared.Task task) async {
    await _db.execute(
      '''
      UPDATE tasks
      SET title = @title, description = @description, status = @status, priority = @priority, due_date = @dueDate, project_id = @projectId, assignee_id = @assigneeId, creator_id = @creatorId
      WHERE id = @id
      ''',
      parameters: {
        'id': task.id,
        'title': task.title,
        'description': task.description,
        'status': task.status.toString(),
        'priority': task.priority.toString(),
        'dueDate': task.dueDate?.toIso8601String(),
        'projectId': task.projectId,
        'assigneeId': task.assigneeId,
        'creatorId': task.creatorId,
      },
    );
    return task;
  }

  @override
  Future<void> delete(String id) async {
    await _db.execute(
      'DELETE FROM tasks WHERE id = @id',
      parameters: {'id': id},
    );
  }
}
