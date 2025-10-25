import 'package:postgres/postgres.dart';
import 'package:task_manager_shared/models.dart' as shared_models;
import '../exceptions/custom_exceptions.dart'; // Import new exceptions

class TaskRepository {
  final Connection _db;

  TaskRepository(this._db);

  // Helper to check if a task exists
  Future<bool> _taskExists(String taskId) async {
    final result = await _db.execute(
      Sql.named('SELECT 1 FROM tasks WHERE id = @taskId LIMIT 1'),
      parameters: {'taskId': taskId},
    );
    return result.isNotEmpty;
  }

  // Helper to check if a user exists
  Future<bool> _userExists(String userId) async {
    final result = await _db.execute(
      Sql.named('SELECT 1 FROM users WHERE id = @userId LIMIT 1'),
      parameters: {'userId': userId},
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
    final parameters = <String, dynamic>{
      'limit': size,
      'offset': page * size,
    };

    if (assigneeId != null) {
      conditions.add('t.assignee_id = @assigneeId');
      parameters['assigneeId'] = assigneeId;
    }
    if (creatorId != null) {
      conditions.add('t.creator_id = @creatorId');
      parameters['creatorId'] = creatorId;
    }
    if (projectId != null) {
      conditions.add('t.project_id = @projectId');
      parameters['projectId'] = projectId;
    }
    if (query != null && query.isNotEmpty) {
      conditions
          .add('(t.title ILIKE @searchQuery OR t.description ILIKE @searchQuery)');
      parameters['searchQuery'] = '%$query%';
    }

    // Join with projects table to get project name
    var sql = '''
      SELECT t.*, p.name as project_name 
      FROM tasks t 
      LEFT JOIN projects p ON t.project_id = p.id
    ''';
    if (conditions.isNotEmpty) {
      sql += ' WHERE ${conditions.join(' AND ')}';
    }
    sql +=
        ' ORDER BY t.due_date ASC NULLS LAST, t.title ASC LIMIT @limit OFFSET @offset';

    final result = await _db.execute(Sql.named(sql), parameters: parameters);
    return result.map((row) => _mapTaskFromRow(row.toColumnMap())).toList();
  }

  Future<shared_models.TaskDto?> findById(String id) async {
    final result = await _db.execute(
      Sql.named('''
        SELECT t.*, p.name as project_name 
        FROM tasks t 
        LEFT JOIN projects p ON t.project_id = p.id
        WHERE t.id = @id
      '''),
      parameters: {'id': id},
    );

    if (result.isEmpty) return null;
    return _mapTaskFromRow(result.first.toColumnMap());
  }

  Future<shared_models.TaskDto> create(shared_models.TaskDto task) async {
    final result = await _db.execute(
      Sql.named('''
      INSERT INTO tasks (id, title, description, status, priority, project_id, assignee_id, creator_id, due_date)
      VALUES (@id, @title, @description, @status, @priority, @projectId, @assigneeId, @creatorId, @dueDate)
      RETURNING *
      '''),
      parameters: {
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

    return _mapTaskFromRow(result.first.toColumnMap());
  }

  Future<shared_models.TaskDto> update(shared_models.TaskDto task) async {
    await _db.execute(
      Sql.named('''
      UPDATE tasks
      SET title = @title,
          description = @description,
          status = @status,
          priority = @priority,
          project_id = @projectId,
          assignee_id = @assigneeId,
          due_date = @dueDate,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = @id
      '''),
      parameters: {
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
      Sql.named('DELETE FROM tasks WHERE id = @id'),
      parameters: {'id': id},
    );
    if (result.affectedRows == 0) {
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
      Sql.named('UPDATE tasks SET assignee_id = @assigneeId, updated_at = CURRENT_TIMESTAMP WHERE id = @taskId'),
      parameters: {'assigneeId': assigneeId, 'taskId': taskId},
    );

    if (result.affectedRows > 0) {
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
      Sql.named('UPDATE tasks SET status = @status, updated_at = CURRENT_TIMESTAMP WHERE id = @taskId'),
      parameters: {'status': _mapTaskStatusToString(newStatus), 'taskId': taskId},
    );

    if (result.affectedRows > 0) {
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

  shared_models.TaskDto _mapTaskFromRow(Map<String, dynamic> row) {
    final dueDateValue = row['due_date'];
    final dueDate = dueDateValue is DateTime ? dueDateValue : null;

    shared_models.TaskStatus status;
    try {
      final statusString = row['status'] as String;
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
      print("Error mapping status: ${row['status']}, using default TaskStatus.todo");
      status = shared_models.TaskStatus.todo; // Default on error
    }

    shared_models.Priority priority;
    try {
      final priorityString = row['priority'] as String;
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
      print("Error mapping priority: ${row['priority']}, using default Priority.low");
      priority = shared_models.Priority.low; // Default on error
    }

    return shared_models.TaskDto(
      id: row['id'] as String,
      title: row['title'] as String,
      description: (row['description'] as String?) ?? '',
      status: status,
      priority: priority,
      projectId: row['project_id'] as String?,
      projectName: row['project_name'] as String?,
      assigneeId: row['assignee_id'] as String?,
      creatorId: row['creator_id'] as String,
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

  Future<shared_models.TaskProgress> getTaskProgress(String userId) async {
    // Count total tasks assigned to the user
    final totalResult = await _db.execute(
      Sql.named('SELECT COUNT(*) as total FROM tasks WHERE assignee_id = @userId'),
      parameters: {'userId': userId},
    );
    final totalTasks = totalResult.first['total'] as int? ?? 0;

    // Count completed tasks (status = DONE)
    final completedResult = await _db.execute(
      Sql.named(
        'SELECT COUNT(*) as completed FROM tasks WHERE assignee_id = @userId AND status = @status'
      ),
      parameters: {
        'userId': userId,
        'status': 'DONE',
      },
    );
    final completedTasks = completedResult.first['completed'] as int? ?? 0;

    return shared_models.TaskProgress(
      totalTasks: totalTasks,
      completedTasks: completedTasks,
    );
  }
}
