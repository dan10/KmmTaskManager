import 'package:postgres/postgres.dart';
import 'package:shared/src/models/project.dart' as shared;

class ProjectRepository {
  final PostgreSQLConnection _db;

  ProjectRepository(this._db);

  Future<shared.Project> createProject(shared.Project project) async {
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

    final createdProject = _mapProjectFromRow(result.first);
    final members = await _getProjectMembers(project.id);
    return createdProject.copyWith(memberIds: members);
  }

  Future<shared.Project?> findProjectById(String id) async {
    final result = await _db.query(
      'SELECT * FROM projects WHERE id = @id',
      substitutionValues: {'id': id},
    );

    if (result.isEmpty) return null;

    final project = _mapProjectFromRow(result.first);
    final members = await _getProjectMembers(id);
    return project.copyWith(memberIds: members);
  }

  Future<List<shared.Project>> findProjectsByCreatorId(String creatorId) async {
    final result = await _db.query(
      'SELECT * FROM projects WHERE creator_id = @creatorId',
      substitutionValues: {'creatorId': creatorId},
    );

    final projects = result.map(_mapProjectFromRow).toList();
    for (var i = 0; i < projects.length; i++) {
      final members = await _getProjectMembers(projects[i].id);
      projects[i] = projects[i].copyWith(memberIds: members);
    }
    return projects;
  }

  Future<List<shared.Project>> findProjectsByMemberId(String memberId) async {
    final result = await _db.query(
      '''
      SELECT p.* FROM projects p
      JOIN project_members pm ON p.id = pm.project_id
      WHERE pm.user_id = @memberId
      ''',
      substitutionValues: {'memberId': memberId},
    );

    final projects = result.map(_mapProjectFromRow).toList();
    for (var i = 0; i < projects.length; i++) {
      final members = await _getProjectMembers(projects[i].id);
      projects[i] = projects[i].copyWith(memberIds: members);
    }
    return projects;
  }

  Future<void> updateProject(shared.Project project) async {
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
  }

  Future<void> deleteProject(String id) async {
    await _db.execute(
      'DELETE FROM project_members WHERE project_id = @id',
      substitutionValues: {'id': id},
    );
    await _db.execute(
      'DELETE FROM projects WHERE id = @id',
      substitutionValues: {'id': id},
    );
  }

  Future<void> addMember(String projectId, String userId) async {
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
  }

  Future<void> removeMember(String projectId, String userId) async {
    await _db.execute(
      '''
      DELETE FROM project_members
      WHERE project_id = @projectId AND user_id = @userId
      ''',
      substitutionValues: {
        'projectId': projectId,
        'userId': userId,
      },
    );
  }

  Future<List<String>> _getProjectMembers(String projectId) async {
    final result = await _db.query(
      'SELECT user_id FROM project_members WHERE project_id = @projectId',
      substitutionValues: {'projectId': projectId},
    );
    return result.map((row) => row[0] as String).toList();
  }

  shared.Project _mapProjectFromRow(List<dynamic> row) {
    return shared.Project(
      id: row[0] as String,
      name: row[1] as String,
      description: row[2] as String,
      creatorId: row[3] as String,
      memberIds: [], // Will be populated separately
    );
  }
}
