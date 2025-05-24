import 'package:flutter/foundation.dart';
import '../../domain/entities/project.dart';

enum ProjectState {
  initial,
  loading,
  loaded,
  error,
}

class ProjectProvider extends ChangeNotifier {
  ProjectState _state = ProjectState.initial;
  List<Project> _projects = [];
  String? _errorMessage;

  ProjectState get state => _state;
  List<Project> get projects => List.unmodifiable(_projects);
  String? get errorMessage => _errorMessage;

  Future<void> loadProjects() async {
    _setState(ProjectState.loading);
    
    try {
      // TODO: Implement actual API call with repository
      await Future.delayed(const Duration(seconds: 1)); // Simulate API call
      
      _projects = [
        Project(
          id: '1',
          name: 'Sample Project',
          description: 'This is a sample project',
          ownerId: '1',
          createdAt: DateTime.now().subtract(const Duration(days: 7)),
          taskCount: 5,
        ),
        Project(
          id: '2',
          name: 'Another Project',
          description: 'Another sample project',
          ownerId: '1',
          createdAt: DateTime.now().subtract(const Duration(days: 3)),
          taskCount: 2,
        ),
      ];
      
      _setState(ProjectState.loaded);
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> createProject(String name, String? description) async {
    try {
      // TODO: Implement actual API call with repository
      await Future.delayed(const Duration(milliseconds: 500));
      
      final newProject = Project(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        name: name,
        description: description,
        ownerId: '1', // TODO: Get from auth provider
        createdAt: DateTime.now(),
      );
      
      _projects.add(newProject);
      notifyListeners();
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> updateProject(String id, String name, String? description) async {
    try {
      // TODO: Implement actual API call with repository
      await Future.delayed(const Duration(milliseconds: 500));
      
      final index = _projects.indexWhere((project) => project.id == id);
      if (index != -1) {
        _projects[index] = _projects[index].copyWith(
          name: name,
          description: description,
          updatedAt: DateTime.now(),
        );
        notifyListeners();
      }
    } catch (e) {
      _setError(e.toString());
    }
  }

  Future<void> deleteProject(String id) async {
    try {
      // TODO: Implement actual API call with repository
      await Future.delayed(const Duration(milliseconds: 500));
      
      _projects.removeWhere((project) => project.id == id);
      notifyListeners();
    } catch (e) {
      _setError(e.toString());
    }
  }

  Project? getProject(String id) {
    try {
      return _projects.firstWhere((project) => project.id == id);
    } catch (e) {
      return null;
    }
  }

  void _setState(ProjectState newState) {
    _state = newState;
    _errorMessage = null;
    notifyListeners();
  }

  void _setError(String error) {
    _state = ProjectState.error;
    _errorMessage = error;
    notifyListeners();
  }

  void clearError() {
    if (_state == ProjectState.error) {
      _setState(_projects.isNotEmpty ? ProjectState.loaded : ProjectState.initial);
    }
  }
} 