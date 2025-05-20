import 'package:shared/src/models/project.dart' as shared;
import '../repositories/project_repository.dart';

abstract class ProjectService {
  Future<List<shared.Project>> getAllProjects(String userId);
  Future<shared.Project?> getProjectById(String id, String userId);
  Future<shared.Project> createProject(shared.Project project);
  Future<shared.Project> updateProject(
      String id, shared.Project project, String userId);
  Future<void> deleteProject(String id, String userId);
}

class ProjectServiceImpl implements ProjectService {
  final ProjectRepository _repository;

  ProjectServiceImpl(this._repository);

  @override
  Future<List<shared.Project>> getAllProjects(String userId) async {
    return _repository.findAllByUserId(userId);
  }

  @override
  Future<shared.Project?> getProjectById(String id, String userId) async {
    final project = await _repository.findById(id);
    if (project == null || project.creatorId != userId) {
      return null;
    }
    return project;
  }

  @override
  Future<shared.Project> createProject(shared.Project project) async {
    return _repository.create(project);
  }

  @override
  Future<shared.Project> updateProject(
      String id, shared.Project project, String userId) async {
    final existingProject = await _repository.findById(id);
    if (existingProject == null || existingProject.creatorId != userId) {
      throw Exception('Project not found or unauthorized');
    }
    return _repository.update(project);
  }

  @override
  Future<void> deleteProject(String id, String userId) async {
    final project = await _repository.findById(id);
    if (project == null || project.creatorId != userId) {
      throw Exception('Project not found or unauthorized');
    }
    await _repository.delete(id);
  }
}
