import 'package:shared/src/models/user.dart';
import 'package:shared/src/models/task.dart' as shared;
import 'database.dart';

class InMemoryDatabase implements Database {
  final Map<String, User> _users = {};
  final Map<String, shared.Task> _tasks = {};
  final Map<String, Map<String, dynamic>> _projects = {};
  final Map<String, List<String>> _projectMembers = {};

  @override
  Future<void> clearTables() async {
    _users.clear();
    _tasks.clear();
    _projects.clear();
    _projectMembers.clear();
  }

  @override
  Future<User?> findUserById(String id) async {
    return _users[id];
  }

  @override
  Future<User?> findUserByEmail(String email) async {
    try {
      return _users.values.firstWhere((user) => user.email == email);
    } catch (_) {
      return null;
    }
  }

  @override
  Future<User> createUser(User user) async {
    _users[user.id] = user;
    return user;
  }

  @override
  Future<void> deleteUser(String id) async {
    _users.remove(id);
  }

  @override
  Future<void> connect() async {
    // No-op for in-memory database
  }

  @override
  Future<void> disconnect() async {
    // No-op for in-memory database
  }

  @override
  Future<void> execute(String query, {Map<String, dynamic>? parameters}) async {
    if (query.contains('INSERT INTO tasks')) {
      final task = shared.Task(
        id: parameters!['id'] as String,
        title: parameters['title'] as String,
        description: parameters['description'] as String,
        status: shared.TaskStatus.values.firstWhere(
          (e) => e.toString() == parameters['status'] as String,
        ),
        priority: shared.Priority.values.firstWhere(
          (e) => e.toString() == parameters['priority'] as String,
        ),
        dueDate: parameters['dueDate'] != null
            ? DateTime.parse(parameters['dueDate'] as String)
            : null,
        projectId: parameters['projectId'] as String?,
        assigneeId: parameters['assigneeId'] as String?,
        creatorId: parameters['creatorId'] as String,
      );
      _tasks[task.id] = task;
    } else if (query.contains('UPDATE tasks')) {
      final task = shared.Task(
        id: parameters!['id'] as String,
        title: parameters['title'] as String,
        description: parameters['description'] as String,
        status: shared.TaskStatus.values.firstWhere(
          (e) => e.toString() == parameters['status'] as String,
        ),
        priority: shared.Priority.values.firstWhere(
          (e) => e.toString() == parameters['priority'] as String,
        ),
        dueDate: parameters['dueDate'] != null
            ? DateTime.parse(parameters['dueDate'] as String)
            : null,
        projectId: parameters['projectId'] as String?,
        assigneeId: parameters['assigneeId'] as String?,
        creatorId: parameters['creatorId'] as String,
      );
      _tasks[task.id] = task;
    } else if (query.contains('DELETE FROM tasks')) {
      _tasks.remove(parameters!['id'] as String);
    } else if (query.contains('INSERT INTO projects')) {
      final id = parameters!['id'] as String;
      _projects[id] = {
        'id': id,
        'name': parameters['name'],
        'description': parameters['description'],
        'creator_id': parameters['creatorId'],
      };
    } else if (query.contains('INSERT INTO project_members')) {
      final projectId = parameters!['projectId'] as String;
      final userId = parameters['userId'] as String;
      _projectMembers.putIfAbsent(projectId, () => []).add(userId);
    } else if (query.contains('UPDATE projects')) {
      final id = parameters!['id'] as String;
      if (_projects.containsKey(id)) {
        _projects[id]!.update('name', (_) => parameters['name']);
        _projects[id]!.update('description', (_) => parameters['description']);
      }
    } else if (query.contains('DELETE FROM project_members')) {
      final projectId = parameters!['projectId'] as String;
      _projectMembers.remove(projectId);
    } else if (query.contains('DELETE FROM projects')) {
      final projectId = parameters!['projectId'] as String;
      _projects.remove(projectId);
      _projectMembers.remove(projectId);
    }
  }

  @override
  Future<List<Map<String, dynamic>>> query(String query,
      {Map<String, dynamic>? parameters}) async {
    if (query.contains('SELECT * FROM tasks WHERE id =')) {
      final task = _tasks[parameters!['taskId'] as String];
      if (task == null) return [];
      return [
        {
          'id': task.id,
          'title': task.title,
          'description': task.description,
          'status': task.status.toString(),
          'priority': task.priority.toString(),
          'due_date': task.dueDate?.toIso8601String(),
          'project_id': task.projectId,
          'assignee_id': task.assigneeId,
          'creator_id': task.creatorId,
        }
      ];
    } else if (query.contains('SELECT * FROM tasks')) {
      return _tasks.values.map((task) {
        return {
          'id': task.id,
          'title': task.title,
          'description': task.description,
          'status': task.status.toString(),
          'priority': task.priority.toString(),
          'due_date': task.dueDate?.toIso8601String(),
          'project_id': task.projectId,
          'assignee_id': task.assigneeId,
          'creator_id': task.creatorId,
        };
      }).toList();
    } else if (query.contains('SELECT * FROM projects')) {
      return _projects.values.toList();
    } else if (query.contains('SELECT * FROM projects WHERE id = @projectId')) {
      final projectId = parameters!['projectId'] as String;
      return _projects.containsKey(projectId) ? [_projects[projectId]!] : [];
    } else if (query.contains(
        'SELECT user_id FROM project_members WHERE project_id = @projectId')) {
      final projectId = parameters!['projectId'] as String;
      return _projectMembers[projectId]
              ?.map((userId) => {'user_id': userId})
              .toList() ??
          [];
    }
    return [];
  }
}
