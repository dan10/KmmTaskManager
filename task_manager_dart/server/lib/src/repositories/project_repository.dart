import 'package:postgres/postgres.dart';
import 'package:task_manager_shared/models.dart' as shared_models;
import '../exceptions/custom_exceptions.dart'; // Import new exceptions
import '../utils/uuid_utils.dart';

abstract class ProjectRepository {
  Future<List<shared_models.Project>> getProjects({
    String? creatorId,
    String? query,
    int page = 0,
    int size = 10,
  });

  Future<shared_models.Project?> findById(String id);

  Future<shared_models.Project> create(shared_models.Project project);

  Future<shared_models.Project> update(shared_models.Project project);

  Future<bool> delete(String id);

  Future<List<shared_models.Project>> findByMemberId(String userId);
  
  // Project member management methods
  Future<Map<String, String>> assignUserToProject(
      String projectId, String userId, String assignedBy);
  
  Future<bool> removeUserFromProject(String projectId, String userId);
  
  Future<List<shared_models.User>> getUsersByProject(String projectId);
  
  Future<bool> isUserAssignedToProject(String projectId, String userId);
}

class ProjectRepositoryImpl implements ProjectRepository {
  final Connection _db;

  ProjectRepositoryImpl(this._db);

  Future<List<shared_models.Project>> getProjects({
    String? creatorId,
    String? query,
    int page = 0,
    int size = 10,
  }) async {
    var sql = StringBuffer('''
      SELECT DISTINCT p.id, p.name, p.description, p.creator_id FROM projects p
      LEFT JOIN project_assignments pm ON p.id = pm.project_id
    ''');

    final parameters = <String, dynamic>{
      'limit': size,
      'offset': page * size,
    };

    var whereClause = <String>[];

    // Only filter by creator if creatorId is provided
    if (creatorId != null && creatorId.isNotEmpty) {
      whereClause.add('(p.creator_id = @creatorId OR pm.user_id = @creatorId)');
      parameters['creatorId'] = creatorId;
    }

    if (query != null && query.isNotEmpty) {
      whereClause.add('(p.name ILIKE @searchQuery OR p.description ILIKE @searchQuery)');
      parameters['searchQuery'] = '%$query%';
    }

    if (whereClause.isNotEmpty) {
      sql.write(' WHERE ${whereClause.join(' AND ')}');
    }

    sql.write(' ORDER BY p.name LIMIT @limit OFFSET @offset');

    final result = await _db.execute(
      Sql.named(sql.toString()),
      parameters: parameters,
    );

    final projects = result.map((row) {
      final columnMap = row.toColumnMap();
      return shared_models.Project(
        id: columnMap['id'] as String,
        name: columnMap['name'] as String,
        description: columnMap['description'] as String,
        creatorId: columnMap['creator_id'] as String,
        memberIds: [], // Will be populated separately
      );
    }).toList();

    for (var i = 0; i < projects.length; i++) {
      final members = await _getProjectMembers(projects[i].id);
      projects[i] = projects[i].copyWith(memberIds: members);
    }
    return projects;
  }

  Future<List<shared_models.Project>> getAllSystemProjects(
      int page, int size, String? query) async {
    var sql = StringBuffer(
        'SELECT DISTINCT p.id, p.name, p.description, p.creator_id FROM projects p');
    final parameters = <String, dynamic>{
      'limit': size,
      'offset': page * size,
    };

    if (query != null && query.isNotEmpty) {
      sql.write(
          ' WHERE (p.name ILIKE @searchQuery OR p.description ILIKE @searchQuery)');
      parameters['searchQuery'] = '%$query%';
    }

    sql.write(' ORDER BY p.name LIMIT @limit OFFSET @offset');

    final result = await _db.execute(
      Sql.named(sql.toString()),
      parameters: parameters,
    );

    final projects = result.map((row) {
      final columnMap = row.toColumnMap();
      return shared_models.Project(
        id: columnMap['id'] as String,
        name: columnMap['name'] as String,
        description: columnMap['description'] as String,
        creatorId: columnMap['creator_id'] as String,
        memberIds: [], // Will be populated separately
      );
    }).toList();

    for (var i = 0; i < projects.length; i++) {
      final members = await _getProjectMembers(projects[i].id);
      projects[i] = projects[i].copyWith(memberIds: members);
    }
    return projects;
  }

  // Helper to check if a project exists
  Future<bool> _projectExists(String projectId) async {
    final result = await _db.execute(
      Sql.named('SELECT 1 FROM projects WHERE id = @projectId LIMIT 1'),
      parameters: {'projectId': projectId},
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

  // Helper to check if a user is already a member of a project
  Future<bool> _isProjectMember(String projectId, String userId) async {
    final result = await _db.execute(
      Sql.named('SELECT 1 FROM project_assignments WHERE project_id = @projectId AND user_id = @userId LIMIT 1'),
      parameters: {'projectId': projectId, 'userId': userId},
    );
    return result.isNotEmpty;
  }

  Future<shared_models.Project?> findById(String id) async {
    final result = await _db.execute(
      Sql.named('SELECT id, name, description, creator_id FROM projects WHERE id = @id'),
      parameters: {'id': id},
    );

    if (result.isEmpty) return null;

    final columnMap = result.first.toColumnMap();
    final project = shared_models.Project(
      id: columnMap['id'] as String,
      name: columnMap['name'] as String,
      description: columnMap['description'] as String,
      creatorId: columnMap['creator_id'] as String,
      memberIds: [], // Placeholder
    );
    final members = await _getProjectMembers(id);
    return project.copyWith(memberIds: members);
  }

  Future<shared_models.Project> create(shared_models.Project project) async {
    final result = await _db.execute(
      Sql.named('''
      INSERT INTO projects (id, name, description, creator_id)
      VALUES (@id, @name, @description, @creatorId)
      RETURNING *
      '''),
      parameters: {
        'id': project.id,
        'name': project.name,
        'description': project.description,
        'creatorId': project.creatorId,
      },
    );

    // Add project members (creator assigns themselves)
    for (final memberId in project.memberIds) {
      await _db.execute(
        Sql.named('''
        INSERT INTO project_assignments (id, project_id, user_id, assigned_by)
        VALUES (@id, @projectId, @userId, @assignedBy)
        '''),
        parameters: {
          'id': UuidUtils.generate(), // Generate unique UUIDv7
          'projectId': project.id,
          'userId': memberId,
          'assignedBy': project.creatorId, // Creator assigns members
        },
      );
    }

    final columnMap = result.first.toColumnMap();
    final createdProject = shared_models.Project(
      id: columnMap['id'] as String,
      name: columnMap['name'] as String,
      description: columnMap['description'] as String,
      creatorId: columnMap['creator_id'] as String,
      memberIds: [], // Placeholder, will be filled by project.memberIds used in loop
    );
    final members =
        await _getProjectMembers(project.id); // These are the ones just added
    return createdProject.copyWith(memberIds: members);
  }

  Future<shared_models.Project> update(shared_models.Project project) async {
    await _db.execute(
      Sql.named('''
      UPDATE projects
      SET name = @name,
          description = @description,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = @id
      '''),
      parameters: {
        'id': project.id,
        'name': project.name,
        'description': project.description,
      },
    );

    // Update project members
    await _db.execute(
      Sql.named('DELETE FROM project_assignments WHERE project_id = @projectId'),
      parameters: {'projectId': project.id},
    );

    for (final memberId in project.memberIds) {
      await _db.execute(
        Sql.named('''
        INSERT INTO project_assignments (id, project_id, user_id, assigned_by)
        VALUES (@id, @projectId, @userId, @assignedBy)
        '''),
        parameters: {
          'id': UuidUtils.generate(), // Generate unique UUIDv7
          'projectId': project.id,
          'userId': memberId,
          'assignedBy': project.creatorId,
        },
      );
    }

    final updatedProject = await findById(project.id);
    if (updatedProject == null) {
      // This case should ideally not be reached if the project existed at the start of update.
      // However, if it does, it means the project was deleted mid-operation or ID changed.
      throw ProjectNotFoundException(id: project.id);
    }
    return updatedProject;
  }

  Future<bool> delete(String id) async {
    await _db.execute(
      Sql.named('DELETE FROM project_assignments WHERE project_id = @id'),
      parameters: {'id': id},
    );
    await _db.execute(
      Sql.named('DELETE FROM projects WHERE id = @id'),
      parameters: {'id': id},
    );
    return true;
  }

  Future<Map<String, String>> assignUserToProject(
      String projectId, String userId, String assignedBy) async {
    if (!await _projectExists(projectId)) {
      throw ProjectNotFoundException(id: projectId);
    }
    if (!await _userExists(userId)) {
      throw UserNotFoundException(id: userId);
    }
    if (await _isProjectMember(projectId, userId)) {
      throw AlreadyAssignedException(
          message: 'User $userId is already assigned to project $projectId.');
    }

    final assignmentId = UuidUtils.generate(); // Generate unique UUIDv7
    await _db.execute(
      Sql.named('''
      INSERT INTO project_assignments (id, project_id, user_id, assigned_by)
      VALUES (@id, @projectId, @userId, @assignedBy)
      '''),
      parameters: {
        'id': assignmentId,
        'projectId': projectId,
        'userId': userId,
        'assignedBy': assignedBy,
      },
    );
    return {'projectId': projectId, 'userId': userId};
  }

  Future<bool> removeUserFromProject(String projectId, String userId) async {
    // Optionally, check if project and user exist if strictness is required,
    // but DELETE won't fail if they don't, it just won't affect rows.
    // The main concern is if the assignment existed.
    final result = await _db.execute(
      Sql.named('''
      DELETE FROM project_assignments
      WHERE project_id = @projectId AND user_id = @userId
      '''),
      parameters: {
        'projectId': projectId,
        'userId': userId,
      },
    );
    return result.affectedRows > 0; // Returns true if a row was deleted
  }

  Future<List<String>> _getProjectMembers(String projectId) async {
    final result = await _db.execute(
      Sql.named('SELECT user_id FROM project_assignments WHERE project_id = @projectId'),
      parameters: {'projectId': projectId},
    );
    return result.map((row) => row.toColumnMap()['user_id'] as String).toList();
  }

  // _mapProjectFromRow is no longer needed as mapping is done inline or specifically.

  Future<List<shared_models.User>> getUsersByProject(String projectId) async {
    if (!await _projectExists(projectId)) {
      throw ProjectNotFoundException(id: projectId);
    }

    final result = await _db.execute(
      Sql.named('''
      SELECT u.id, u.email, u.display_name, u.google_id, u.created_at FROM users u
      JOIN project_assignments pm ON u.id = pm.user_id
      WHERE pm.project_id = @projectId
      '''),
      parameters: {'projectId': projectId},
    );

    return result.map((row) {
      final columnMap = row.toColumnMap();
      // Handle created_at which can be DateTime or String
      final createdAtValue = columnMap['created_at'];
      final createdAtString = createdAtValue is DateTime
          ? createdAtValue.toIso8601String()
          : createdAtValue as String;

      return shared_models.User(
        id: columnMap['id'] as String,
        email: columnMap['email'] as String,
        displayName: columnMap['display_name'] as String,
        googleId: columnMap['google_id'] as String?,
        createdAt: createdAtString,
      );
    }).toList();
  }

  Future<List<shared_models.Project>> getProjectsByUser(String userId) async {
    if (!await _userExists(userId)) {
      throw UserNotFoundException(id: userId);
    }

    final result = await _db.execute(
      Sql.named('''
      SELECT DISTINCT p.id, p.name, p.description, p.creator_id
      FROM projects p
      LEFT JOIN project_assignments pm ON p.id = pm.project_id
      WHERE p.creator_id = @userId OR pm.user_id = @userId
      ORDER BY p.name
      '''),
      parameters: {'userId': userId},
    );

    final projects = result.map((row) {
      final columnMap = row.toColumnMap();
      return shared_models.Project(
        id: columnMap['id'] as String,
        name: columnMap['name'] as String,
        description: columnMap['description'] as String,
        creatorId: columnMap['creator_id'] as String,
        memberIds: [], // Will be populated separately
      );
    }).toList();

    for (var i = 0; i < projects.length; i++) {
      final members = await _getProjectMembers(projects[i].id);
      projects[i] = projects[i].copyWith(memberIds: members);
    }
    return projects;
  }

  Future<List<shared_models.Project>> findByMemberId(String userId) async {
    if (!await _userExists(userId)) {
      throw UserNotFoundException(id: userId);
    }

    final result = await _db.execute(
      Sql.named('''
      SELECT DISTINCT p.id, p.name, p.description, p.creator_id
      FROM projects p
      LEFT JOIN project_assignments pm ON p.id = pm.project_id
      WHERE p.creator_id = @userId OR pm.user_id = @userId
      ORDER BY p.name
      '''),
      parameters: {'userId': userId},
    );

    final projects = result.map((row) {
      final columnMap = row.toColumnMap();
      return shared_models.Project(
        id: columnMap['id'] as String,
        name: columnMap['name'] as String,
        description: columnMap['description'] as String,
        creatorId: columnMap['creator_id'] as String,
        memberIds: [], // Will be populated separately
      );
    }).toList();

    for (var i = 0; i < projects.length; i++) {
      final members = await _getProjectMembers(projects[i].id);
      projects[i] = projects[i].copyWith(memberIds: members);
    }
    return projects;
  }

  Future<bool> isUserAssignedToProject(String projectId, String userId) async {
    return _isProjectMember(projectId, userId);
  }
}
