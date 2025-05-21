import 'package:postgres/postgres.dart';
import 'package:shared/models.dart' as shared_models;
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

  Future<List<shared_models.Task>> getTasks({
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
      conditions.add('(title ILIKE @searchQuery OR description ILIKE @searchQuery)');
      substitutionValues['searchQuery'] = '%$query%';
    }

    var sql = 'SELECT * FROM tasks';
    if (conditions.isNotEmpty) {
      sql += ' WHERE ${conditions.join(' AND ')}';
    }
    sql += ' ORDER BY due_date ASC NULLS LAST, title ASC LIMIT @limit OFFSET @offset';

    final result = await _db.query(sql, substitutionValues: substitutionValues);
    return result.map(_mapTaskFromRow).toList();
  }

  Future<shared_models.Task?> findById(String id) async {
    final result = await _db.query(
      'SELECT * FROM tasks WHERE id = @id',
      substitutionValues: {'id': id},
    );

    if (result.isEmpty) return null;
    return _mapTaskFromRow(result.first);
  }

  Future<shared_models.Task> create(shared_models.Task task) async {
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
        'status': task.status.name, // Use .name for enums from freezed
        'priority': task.priority.name, // Use .name for enums from freezed
        'projectId': task.projectId,
        'assigneeId': task.assigneeId,
        'creatorId': task.creatorId,
        'dueDate': task.dueDate?.toIso8601String(),
      },
    );

    return _mapTaskFromRow(result.first);
  }

  Future<shared_models.Task> update(shared_models.Task task) async {
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
        'status': task.status.name, // Use .name for enums
        'priority': task.priority.name, // Use .name for enums
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

  Future<shared_models.Task> assignTask(String taskId, String assigneeId) async {
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
      if (updatedTask == null) { // Should ideally not happen if update succeeded
          throw TaskNotFoundException(id: taskId);
      }
      return updatedTask;
    }
    // This path should ideally not be reached if _taskExists passed and DB is consistent.
    // Throwing to indicate update failed unexpectedly.
    throw Exception('Failed to assign task $taskId, update operation affected 0 rows.');
  }

  Future<shared_models.Task> changeTaskStatus(String taskId, shared_models.TaskStatus newStatus) async {
    if (!await _taskExists(taskId)) {
      throw TaskNotFoundException(id: taskId);
    }

    final result = await _db.execute(
      'UPDATE tasks SET status = @status WHERE id = @taskId',
      substitutionValues: {'status': newStatus.name, 'taskId': taskId},
    );

    if (result > 0) {
      final updatedTask = await findById(taskId);
       if (updatedTask == null) { // Should ideally not happen
          throw TaskNotFoundException(id: taskId);
      }
      return updatedTask;
    }
    // This path should ideally not be reached.
    throw Exception('Failed to change task status for $taskId, update operation affected 0 rows.');
  }

  shared_models.Task _mapTaskFromRow(List<dynamic> row) {
    final dueDateValue = row[8];
    final dueDate = dueDateValue is DateTime ? dueDateValue : null;

    // Assumes TaskStatus and Priority enums in shared_models have @JsonValue or similar for string mapping
    // If they use .name for serialization (as per freezed defaults with json_serializable)
    // then this mapping needs to match that.
    // The shared/task.dart uses @JsonKey(name: 'TODO') etc.
    // So, `TaskStatus.values.byName(row[3] as String)` might work if the stored value matches the enum key.
    // Or, if stored value is 'TODO', 'IN_PROGRESS', etc.
    // The current `e.toString() == row[3]` is fragile if enum.toString() changes.
    // Using .name is typically more robust if the DB stores the enum key string.
    // Given the shared model uses @JsonKey, it implies the string values in DB are 'TODO', 'IN_PROGRESS', etc.
    // So, a more robust mapping would be:
    // shared_models.TaskStatus.values.firstWhere((e) => e.toJson() == row[3] as String)
    // Or if .name is directly 'TODO', 'IN_PROGRESS'
    // shared_models.TaskStatus.values.byName( (row[3] as String).toLowerCase().replaceAll('_',' ') ) - this gets complex.
    // Let's stick to current mapping assuming DB stores exactly what e.toString() produces or what fromJson expects.
    // The shared Task.fromJson uses _$TaskStatusEnumMap.entries.firstWhere((e) => e.value == json).key;
    // And TaskStatus.g.dart uses _$TaskStatusEnumMap = { TaskStatus.todo: 'TODO', ... }
    // So, DB should store 'TODO', 'IN_PROGRESS', 'DONE'.

    shared_models.TaskStatus status;
    try {
       status = shared_models.TaskStatus.values.firstWhere(
        (e) => shared_models.TaskStatusEnumMap[e] == row[3] as String,
      );
    } catch (e) {
      print("Error mapping status: ${row[3]}, using default TaskStatus.todo");
      status = shared_models.TaskStatus.todo; // Default on error
    }

    shared_models.Priority priority;
    try {
      priority = shared_models.Priority.values.firstWhere(
        (e) => shared_models.PriorityEnumMap[e] == row[4] as String,
      );
    } catch (e) {
      print("Error mapping priority: ${row[4]}, using default Priority.low");
      priority = shared_models.Priority.low; // Default on error
    }


    return shared_models.Task(
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
}
