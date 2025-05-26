import 'package:postgres/postgres.dart';
import 'package:task_manager_shared/models.dart' as shared_models;
import '../exceptions/custom_exceptions.dart'; // Import new exceptions

class TaskRepository {
  final PostgreSQLConnection _db;

  TaskRepository(this._db);

  // Helper to check if a task exists
  Future<bool> _taskExists(String taskId) async {
    final result = await _db.query(
      'SELECT 1 FROM tasks WHERE id = @taskId LIMIT 1',
      substitutionValues: {'taskId': taskId},
    );
    return result.isNotEmpty;
  }

  // Helper to check if a user exists
  Future<bool> _userExists(String userId) async {
    final result = await _db.query(
      'SELECT 1 FROM users WHERE id = @userId LIMIT 1',
      substitutionValues: {'userId': userId},
    );
    return result.isNotEmpty;
  }

  Future<List<shared_models.TaskDto>> getTasks({
    String? assigneeId,
    String? creatorId,
    String? projectId,
    String? query,
    int page = 0,
    int size = 10,
  }) async {
    final conditions = <String>[];
    final substitutionValues = <String, dynamic>{
      'limit': size,
      'offset': page * size,
    };

    if (assigneeId != null) {
      conditions.add('assignee_id = @assigneeId');
      substitutionValues['assigneeId'] = assigneeId;
    }
    if (creatorId != null) {
      conditions.add('creator_id = @creatorId');
      substitutionValues['creatorId'] = creatorId;
    }
    if (projectId != null) {
      conditions.add('project_id = @projectId');
      substitutionValues['projectId'] = projectId;
    }
    if (query != null && query.isNotEmpty) {
      conditions
          .add('(title ILIKE @searchQuery OR description ILIKE @searchQuery)');
      substitutionValues['searchQuery'] = '%$query%';
    }

    var sql = 'SELECT * FROM tasks';
    if (conditions.isNotEmpty) {
      sql += ' WHERE ${conditions.join(' AND ')}';
    }
    sql +=
        ' ORDER BY due_date ASC NULLS LAST, title ASC LIMIT @limit OFFSET @offset';

    final result = await _db.query(sql, substitutionValues: substitutionValues);
    return result.map(_mapTaskFromRow).toList();
  }

  Future<shared_models.TaskDto?> findById(String id) async {
    final result = await _db.query(
      'SELECT * FROM tasks WHERE id = @id',
      substitutionValues: {'id': id},
    );

    if (result.isEmpty) return null;
    return _mapTaskFromRow(result.first);
  }

  Future<shared_models.TaskDto> create(shared_models.TaskDto task) async {
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
        'status': _mapTaskStatusToString(task.status),
        'priority': _mapPriorityToString(task.priority),
        'projectId': task.projectId,
        'assigneeId': task.assigneeId,
        'creatorId': task.creatorId,
        'dueDate': task.dueDate?.toIso8601String(),
      },
    );

    return _mapTaskFromRow(result.first);
  }

  Future<shared_models.TaskDto> update(shared_models.TaskDto task) async {
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
        'status': _mapTaskStatusToString(task.status),
        'priority': _mapPriorityToString(task.priority),
        'projectId': task.projectId,
        'assigneeId': task.assigneeId,
        'dueDate': task.dueDate?.toIso8601String(),
      },
    );

    final updatedTask = await findById(task.id);
    if (updatedTask == null) {
      throw TaskNotFoundException(id: task.id); // Use new exception
    }
    return updatedTask;
  }

  Future<void> delete(String id) async {
    final result = await _db.execute(
      'DELETE FROM tasks WHERE id = @id',
      substitutionValues: {'id': id},
    );
    if (result == 0) {
      throw TaskNotFoundException(id: id); // Use new exception
    }
  }

  Future<shared_models.TaskDto> assignTask(
      String taskId, String assigneeId) async {
    if (!await _taskExists(taskId)) {
      throw TaskNotFoundException(id: taskId);
    }
    if (!await _userExists(assigneeId)) {
      throw UserNotFoundException(id: assigneeId);
    }

    final result = await _db.execute(
      'UPDATE tasks SET assignee_id = @assigneeId WHERE id = @taskId',
      substitutionValues: {'assigneeId': assigneeId, 'taskId': taskId},
    );

    if (result > 0) {
      final updatedTask = await findById(taskId);
      if (updatedTask == null) {
        // Should ideally not happen if update succeeded
        throw TaskNotFoundException(id: taskId);
      }
      return updatedTask;
    }
    // This path should ideally not be reached if _taskExists passed and DB is consistent.
    // Throwing to indicate update failed unexpectedly.
    throw Exception(
        'Failed to assign task $taskId, update operation affected 0 rows.');
  }

  Future<shared_models.TaskDto> changeTaskStatus(
      String taskId, shared_models.TaskStatus newStatus) async {
    if (!await _taskExists(taskId)) {
      throw TaskNotFoundException(id: taskId);
    }

    final result = await _db.execute(
      'UPDATE tasks SET status = @status WHERE id = @taskId',
      substitutionValues: {'status': _mapTaskStatusToString(newStatus), 'taskId': taskId},
    );

    if (result > 0) {
      final updatedTask = await findById(taskId);
      if (updatedTask == null) {
        // Should ideally not happen
        throw TaskNotFoundException(id: taskId);
      }
      return updatedTask;
    }
    // This path should ideally not be reached.
    throw Exception(
        'Failed to change task status for $taskId, update operation affected 0 rows.');
  }

  shared_models.TaskDto _mapTaskFromRow(List<dynamic> row) {
    final dueDateValue = row[8];
    final dueDate = dueDateValue is DateTime ? dueDateValue : null;

    shared_models.TaskStatus status;
    try {
      final statusString = row[3] as String;
      switch (statusString) {
        case 'TODO':
          status = shared_models.TaskStatus.todo;
          break;
        case 'IN_PROGRESS':
          status = shared_models.TaskStatus.inProgress;
          break;
        case 'DONE':
          status = shared_models.TaskStatus.done;
          break;
        default:
          print("Error mapping status: $statusString, using default TaskStatus.todo");
          status = shared_models.TaskStatus.todo;
      }
    } catch (e) {
      print("Error mapping status: ${row[3]}, using default TaskStatus.todo");
      status = shared_models.TaskStatus.todo; // Default on error
    }

    shared_models.Priority priority;
    try {
      final priorityString = row[4] as String;
      switch (priorityString) {
        case 'LOW':
          priority = shared_models.Priority.low;
          break;
        case 'MEDIUM':
          priority = shared_models.Priority.medium;
          break;
        case 'HIGH':
          priority = shared_models.Priority.high;
          break;
        default:
          print("Error mapping priority: $priorityString, using default Priority.low");
          priority = shared_models.Priority.low;
      }
    } catch (e) {
      print("Error mapping priority: ${row[4]}, using default Priority.low");
      priority = shared_models.Priority.low; // Default on error
    }

    return shared_models.TaskDto(
      id: row[0] as String,
      title: row[1] as String,
      description: (row[2] as String?) ?? '',
      status: status,
      priority: priority,
      projectId: row[5] as String?,
      assigneeId: row[6] as String?,
      creatorId: row[7] as String,
      dueDate: dueDate,
    );
  }

  String _mapTaskStatusToString(shared_models.TaskStatus status) {
    switch (status) {
      case shared_models.TaskStatus.todo:
        return 'TODO';
      case shared_models.TaskStatus.inProgress:
        return 'IN_PROGRESS';
      case shared_models.TaskStatus.done:
        return 'DONE';
    }
  }

  String _mapPriorityToString(shared_models.Priority priority) {
    switch (priority) {
      case shared_models.Priority.low:
        return 'LOW';
      case shared_models.Priority.medium:
        return 'MEDIUM';
      case shared_models.Priority.high:
        return 'HIGH';
    }
  }
}
