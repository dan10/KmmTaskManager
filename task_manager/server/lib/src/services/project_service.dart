import 'package:shared/models.dart' as shared_models;
import '../repositories/project_repository.dart';

import '../exceptions/custom_exceptions.dart'; // Import new exceptions

abstract class ProjectService {
  Future<List<shared_models.Project>> getProjects({
    String? creatorId,
    String? query,
    int? page,
    int? size,
  });

  Future<shared_models.Project?> getProjectById(String id, String userId);

  Future<shared_models.Project> createProject(shared_models.Project project);

  Future<shared_models.Project> updateProject(
    String id,
    String userId,
    shared_models.Project project,
  );

  Future<bool> deleteProject(String id, String userId);

  Future<List<shared_models.Project>> getProjectsByMember(String userId);
}

// Old custom exceptions removed from here. They are now in custom_exceptions.dart

class ProjectServiceImpl implements ProjectService {
  final ProjectRepository _repository;

  ProjectServiceImpl(this._repository);

  @override
  Future<List<shared_models.Project>> getProjects({
    String? creatorId,
    String? query,
    int? page,
    int? size,
  }) async {
    return _repository.getProjects(
      creatorId: creatorId,
      query: query,
      page: page ?? 0,
      size: size ?? 10,
    );
  }

  @override
  Future<shared_models.Project?> getProjectById(
      String id, String userId) async {
    final project = await _repository.findById(id);
    if (project == null) {
      return null;
    }
    // Check if user is creator or member
    if (project.creatorId != userId && !project.memberIds.contains(userId)) {
      return null;
    }
    return project;
  }

  @override
  Future<shared_models.Project> createProject(
      shared_models.Project project) async {
    return _repository.create(project);
  }

  @override
  Future<shared_models.Project> updateProject(
      String id, String userId, shared_models.Project project) async {
    final existingProject = await _repository.findById(id);
    if (existingProject == null) {
      throw ProjectNotFoundException(id: id);
    }
    // Only creator can update project
    if (existingProject.creatorId != userId) {
      throw ForbiddenException(
          message: 'User not authorized to update project $id.');
    }
    return _repository.update(project);
  }

  @override
  Future<bool> deleteProject(String id, String userId) async {
    final project = await _repository.findById(id);
    if (project == null) {
      throw ProjectNotFoundException(id: id);
    }
    // Only creator can delete project
    if (project.creatorId != userId) {
      throw ForbiddenException(
          message: 'User not authorized to delete project $id.');
    }
    return _repository.delete(id);
  }

  @override
  Future<List<shared_models.Project>> getProjectsByMember(String userId) async {
    return _repository.findByMemberId(userId);
  }
}
