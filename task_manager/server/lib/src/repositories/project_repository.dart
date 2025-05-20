import '../data/database.dart';
import 'package:shared/src/models/project.dart' as shared;

abstract class ProjectRepository {
  Future<shared.Project> create(shared.Project project);
  Future<shared.Project?> findById(String id);
  Future<List<shared.Project>> findAll();
  Future<shared.Project> update(shared.Project project);
  Future<void> delete(String id);
}

class ProjectRepositoryImpl implements ProjectRepository {
  final Database _db;

  ProjectRepositoryImpl(this._db);

  Future<shared.Project> create(shared.Project project) async {
    await _db.execute(
      '''
      INSERT INTO projects (id, name, description, creator_id)
      VALUES (@id, @name, @description, @creatorId)
      ''',
      parameters: {
        'id': project.id,
        'name': project.name,
        'description': project.description,
        'creatorId': project.creatorId,
      },
    );

    // Add creator as a member
    await _db.execute(
      '''
      INSERT INTO project_members (project_id, user_id)
      VALUES (@projectId, @userId)
      ''',
      parameters: {
        'projectId': project.id,
        'userId': project.creatorId,
      },
    );

    // Add other members
    for (final memberId in project.memberIds) {
      if (memberId != project.creatorId) {
        await _db.execute(
          '''
          INSERT INTO project_members (project_id, user_id)
          VALUES (@projectId, @userId)
          ''',
          parameters: {
            'projectId': project.id,
            'userId': memberId,
          },
        );
      }
    }

    return project;
  }

  Future<shared.Project?> findById(String projectId) async {
    final results = await _db.query(
      'SELECT * FROM projects WHERE id = @projectId',
      parameters: {'projectId': projectId},
    );

    if (results.isEmpty) {
      return null;
    }

    final row = results.first;
    final memberResults = await _db.query(
      'SELECT user_id FROM project_members WHERE project_id = @projectId',
      parameters: {'projectId': projectId},
    );

    final memberIds =
        memberResults.map((row) => row['user_id'] as String).toList();

    return shared.Project(
      id: row['id'] as String,
      name: row['name'] as String,
      description: row['description'] as String,
      creatorId: row['creator_id'] as String,
      memberIds: memberIds,
    );
  }

  Future<List<shared.Project>> findByUserId(String userId) async {
    final results = await _db.query(
      '''
      SELECT p.* FROM projects p
      JOIN project_members pm ON p.id = pm.project_id
      WHERE pm.user_id = @userId
      ''',
      parameters: {'userId': userId},
    );

    final projects = <shared.Project>[];
    for (final row in results) {
      final memberResults = await _db.query(
        'SELECT user_id FROM project_members WHERE project_id = @projectId',
        parameters: {'projectId': row['id']},
      );

      final memberIds =
          memberResults.map((row) => row['user_id'] as String).toList();

      projects.add(shared.Project(
        id: row['id'] as String,
        name: row['name'] as String,
        description: row['description'] as String,
        creatorId: row['creator_id'] as String,
        memberIds: memberIds,
      ));
    }

    return projects;
  }

  Future<shared.Project> update(shared.Project project) async {
    await _db.execute(
      '''
      UPDATE projects
      SET
        name = @name,
        description = @description
      WHERE id = @id
      ''',
      parameters: {
        'id': project.id,
        'name': project.name,
        'description': project.description,
      },
    );

    // Update members
    await _db.execute(
      'DELETE FROM project_members WHERE project_id = @projectId',
      parameters: {'projectId': project.id},
    );

    for (final memberId in project.memberIds) {
      await _db.execute(
        '''
        INSERT INTO project_members (project_id, user_id)
        VALUES (@projectId, @userId)
        ''',
        parameters: {
          'projectId': project.id,
          'userId': memberId,
        },
      );
    }

    return project;
  }

  Future<void> delete(String projectId) async {
    await _db.execute(
      'DELETE FROM project_members WHERE project_id = @projectId',
      parameters: {'projectId': projectId},
    );

    await _db.execute(
      'DELETE FROM projects WHERE id = @projectId',
      parameters: {'projectId': projectId},
    );
  }

  Future<List<shared.Project>> findAll() async {
    final results = await _db.query('SELECT * FROM projects');
    final projects = <shared.Project>[];
    for (final row in results) {
      final memberResults = await _db.query(
        'SELECT user_id FROM project_members WHERE project_id = @projectId',
        parameters: {'projectId': row['id']},
      );
      final memberIds =
          memberResults.map((row) => row['user_id'] as String).toList();
      projects.add(shared.Project(
        id: row['id'] as String,
        name: row['name'] as String,
        description: row['description'] as String,
        creatorId: row['creator_id'] as String,
        memberIds: memberIds,
      ));
    }
    return projects;
  }
}
