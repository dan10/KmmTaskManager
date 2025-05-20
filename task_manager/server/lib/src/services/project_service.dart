import 'package:shared/src/models/project.dart' as shared;
import '../repositories/project_repository.dart';

abstract class ProjectService {
  Future<shared.Project> createProject(shared.Project project);
  Future<shared.Project?> getProjectById(String id);
  Future<List<shared.Project>> getAllProjects();
  Future<shared.Project> updateProject(shared.Project project);
  Future<void> deleteProject(String id);
}

class ProjectServiceImpl implements ProjectService {
  final ProjectRepository _repository;

  ProjectServiceImpl(this._repository);

  @override
  Future<shared.Project> createProject(shared.Project project) async {
    return await _repository.create(project);
  }

  @override
  Future<shared.Project?> getProjectById(String id) async {
    return await _repository.findById(id);
  }

  @override
  Future<List<shared.Project>> getAllProjects() async {
    return await _repository.findAll();
  }

  @override
  Future<shared.Project> updateProject(shared.Project project) async {
    return await _repository.update(project);
  }

  @override
  Future<void> deleteProject(String id) async {
    await _repository.delete(id);
  }
}
