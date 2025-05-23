import 'package:postgres/postgres.dart';
import 'package:shared/models.dart' as shared_models;
import '../exceptions/custom_exceptions.dart'; // Import new exceptions

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
}

class ProjectRepositoryImpl implements ProjectRepository {
  final PostgreSQLConnection _db;

  ProjectRepositoryImpl(this._db);

  Future<List<shared_models.Project>> getProjects({
    String? creatorId,
    String? query,
    int page = 0,
    int size = 10,
  }) async {
    var sql = StringBuffer('''
      SELECT DISTINCT p.id, p.name, p.description, p.creator_id FROM projects p
      LEFT JOIN project_members pm ON p.id = pm.project_id
    ''');

    final substitutionValues = <String, dynamic>{
      'limit': size,
      'offset': page * size,
    };

    var whereClause = <String>[];

    // Only filter by creator if creatorId is provided
    if (creatorId != null && creatorId.isNotEmpty) {
      whereClause.add('(p.creator_id = @creatorId OR pm.user_id = @creatorId)');
      substitutionValues['creatorId'] = creatorId;
    }

    if (query != null && query.isNotEmpty) {
      whereClause.add('(p.name ILIKE @searchQuery OR p.description ILIKE @searchQuery)');
      substitutionValues['searchQuery'] = '%$query%';
    }

    if (whereClause.isNotEmpty) {
      sql.write(' WHERE ${whereClause.join(' AND ')}');
    }

    sql.write(' ORDER BY p.name LIMIT @limit OFFSET @offset');

    final result = await _db.query(
      sql.toString(),
      substitutionValues: substitutionValues,
    );

    // Note: _mapProjectFromRow expects specific column order if using indexed access.
    // The query explicitly lists columns now, so mapping by name is safer if order changes.
    // For now, _mapProjectFromRow uses indexed access.
    // id (0), name (1), description (2), creator_id (3)
    final projects = result.map((row) {
      return shared_models.Project(
        id: row[0] as String,
        name: row[1] as String,
        description: row[2] as String,
        creatorId: row[3] as String,
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
    final substitutionValues = <String, dynamic>{
      'limit': size,
      'offset': page * size,
    };

    if (query != null && query.isNotEmpty) {
      sql.write(
          ' WHERE (p.name ILIKE @searchQuery OR p.description ILIKE @searchQuery)');
      substitutionValues['searchQuery'] = '%$query%';
    }

    sql.write(' ORDER BY p.name LIMIT @limit OFFSET @offset');

    final result = await _db.query(
      sql.toString(),
      substitutionValues: substitutionValues,
    );

    final projects = result.map((row) {
      return shared_models.Project(
        id: row[0] as String,
        name: row[1] as String,
        description: row[2] as String,
        creatorId: row[3] as String,
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
    final result = await _db.query(
      'SELECT 1 FROM projects WHERE id = @projectId LIMIT 1',
      substitutionValues: {'projectId': projectId},
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

  // Helper to check if a user is already a member of a project
  Future<bool> _isProjectMember(String projectId, String userId) async {
    final result = await _db.query(
      'SELECT 1 FROM project_members WHERE project_id = @projectId AND user_id = @userId LIMIT 1',
      substitutionValues: {'projectId': projectId, 'userId': userId},
    );
    return result.isNotEmpty;
  }

  Future<shared_models.Project?> findById(String id) async {
    final result = await _db.query(
      'SELECT id, name, description, creator_id FROM projects WHERE id = @id', // Explicit columns
      substitutionValues: {'id': id},
    );

    if (result.isEmpty) return null;

    // Use the same mapping logic as in getAllProjects for consistency
    final projectData = result.first;
    final project = shared_models.Project(
      id: projectData[0] as String,
      name: projectData[1] as String,
      description: projectData[2] as String,
      creatorId: projectData[3] as String,
      memberIds: [], // Placeholder
    );
    final members = await _getProjectMembers(id);
    return project.copyWith(memberIds: members);
  }

  Future<shared_models.Project> create(shared_models.Project project) async {
    final result = await _db.query(
      '''
      INSERT INTO projects (id, name, description, creator_id)
      VALUES (@id, @name, @description, @creatorId)
      RETURNING *
      ''',
      substitutionValues: {
        'id': project.id,
        'name': project.name,
        'description': project.description,
        'creatorId': project.creatorId,
      },
    );

    // Add project members
    for (final memberId in project.memberIds) {
      await _db.execute(
        '''
        INSERT INTO project_members (project_id, user_id)
        VALUES (@projectId, @userId)
        ''',
        substitutionValues: {
          'projectId': project.id,
          'userId': memberId,
        },
      );
    }

    // Use the same mapping logic as in getAllProjects for consistency
    final createdRow = result.first;
    final createdProject = shared_models.Project(
      id: createdRow[0] as String,
      name: createdRow[1] as String,
      description: createdRow[2] as String,
      creatorId: createdRow[3] as String,
      memberIds: [], // Placeholder, will be filled by project.memberIds used in loop
    );
    final members =
        await _getProjectMembers(project.id); // These are the ones just added
    return createdProject.copyWith(memberIds: members);
  }

  Future<shared_models.Project> update(shared_models.Project project) async {
    await _db.execute(
      '''
      UPDATE projects
      SET name = @name,
          description = @description
      WHERE id = @id
      ''',
      substitutionValues: {
        'id': project.id,
        'name': project.name,
        'description': project.description,
      },
    );

    // Update project members
    await _db.execute(
      'DELETE FROM project_members WHERE project_id = @projectId',
      substitutionValues: {'projectId': project.id},
    );

    for (final memberId in project.memberIds) {
      await _db.execute(
        '''
        INSERT INTO project_members (project_id, user_id)
        VALUES (@projectId, @userId)
        ''',
        substitutionValues: {
          'projectId': project.id,
          'userId': memberId,
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
      'DELETE FROM project_members WHERE project_id = @id',
      substitutionValues: {'id': id},
    );
    await _db.execute(
      'DELETE FROM projects WHERE id = @id',
      substitutionValues: {'id': id},
    );
    return true;
  }

  Future<Map<String, String>> assignUserToProject(
      String projectId, String userId) async {
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

    await _db.execute(
      '''
      INSERT INTO project_members (project_id, user_id) 
      VALUES (@projectId, @userId)
      ''',
      substitutionValues: {
        'projectId': projectId,
        'userId': userId,
      },
    );
    return {'projectId': projectId, 'userId': userId};
  }

  Future<bool> removeUserFromProject(String projectId, String userId) async {
    // Optionally, check if project and user exist if strictness is required,
    // but DELETE won't fail if they don't, it just won't affect rows.
    // The main concern is if the assignment existed.
    final result = await _db.execute(
      '''
      DELETE FROM project_members
      WHERE project_id = @projectId AND user_id = @userId
      ''',
      substitutionValues: {
        'projectId': projectId,
        'userId': userId,
      },
    );
    return result > 0; // Returns true if a row was deleted
  }

  Future<List<String>> _getProjectMembers(String projectId) async {
    final result = await _db.query(
      'SELECT user_id FROM project_members WHERE project_id = @projectId',
      substitutionValues: {'projectId': projectId},
    );
    return result.map((row) => row[0] as String).toList();
  }

  // _mapProjectFromRow is no longer needed as mapping is done inline or specifically.

  Future<List<shared_models.User>> getUsersByProject(String projectId) async {
    if (!await _projectExists(projectId)) {
      throw ProjectNotFoundException(id: projectId);
    }

    final result = await _db.query(
      '''
      SELECT u.id, u.email, u.display_name, u.google_id, u.created_at FROM users u 
      JOIN project_members pm ON u.id = pm.user_id 
      WHERE pm.project_id = @projectId
      ''',
      substitutionValues: {'projectId': projectId},
    );

    return result.map((row) {
      return shared_models.User(
        id: row[0] as String,
        email: row[1] as String,
        displayName: row[2] as String,
        googleId: row[3] as String?,
        createdAt: row[4] as String,
      );
    }).toList();
  }

  Future<List<shared_models.Project>> getProjectsByUser(String userId) async {
    if (!await _userExists(userId)) {
      throw UserNotFoundException(id: userId);
    }

    final result = await _db.query(
      '''
      SELECT DISTINCT p.id, p.name, p.description, p.creator_id
      FROM projects p
      LEFT JOIN project_members pm ON p.id = pm.project_id
      WHERE p.creator_id = @userId OR pm.user_id = @userId
      ORDER BY p.name
      ''',
      substitutionValues: {'userId': userId},
    );

    final projects = result.map((row) {
      return shared_models.Project(
        id: row[0] as String,
        name: row[1] as String,
        description: row[2] as String,
        creatorId: row[3] as String,
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

    final result = await _db.query(
      '''
      SELECT DISTINCT p.id, p.name, p.description, p.creator_id
      FROM projects p
      LEFT JOIN project_members pm ON p.id = pm.project_id
      WHERE p.creator_id = @userId OR pm.user_id = @userId
      ORDER BY p.name
      ''',
      substitutionValues: {'userId': userId},
    );

    final projects = result.map((row) {
      return shared_models.Project(
        id: row[0] as String,
        name: row[1] as String,
        description: row[2] as String,
        creatorId: row[3] as String,
        memberIds: [], // Will be populated separately
      );
    }).toList();

    for (var i = 0; i < projects.length; i++) {
      final members = await _getProjectMembers(projects[i].id);
      projects[i] = projects[i].copyWith(memberIds: members);
    }
    return projects;
  }
}
