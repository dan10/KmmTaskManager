import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';

import '../data/repositories/project_repository.dart';
import '../../../core/utils/command.dart';
import '../../../core/utils/result.dart';

class ProjectCreateEditState {
  const ProjectCreateEditState({this.id, this.name = '', this.description = '', this.error});
  final String? id;
  final String name;
  final String description;
  final String? error;

  ProjectCreateEditState copyWith({String? id, String? name, String? description, String? error}) =>
      ProjectCreateEditState(
        id: id ?? this.id,
        name: name ?? this.name,
        description: description ?? this.description,
        error: error,
      );
}

class ProjectCreateEditViewModel extends ChangeNotifier {
  ProjectCreateEditViewModel({required ProjectRepository repository}) : _repository = repository {
    create = Command1<void, (String name, String? description)>(_create);
    update = Command1<void, (String id, String? name, String? description)>(_update);
  }

  final ProjectRepository _repository;
  final _log = Logger('ProjectCreateEditViewModel');

  var state = const ProjectCreateEditState();

  late Command1<void, (String, String?)> create;
  late Command1<void, (String, String?, String?)> update;

  Future<Result<void>> _create((String, String?) payload) async {
    final (name, description) = payload;
    try {
      final res = await _repository.createProject(name: name, description: description);
      if (res is Ok) {
        return Result.ok(null);
      } else {
        final err = (res as Error).error.toString();
        state = state.copyWith(error: err);
        notifyListeners();
        return Result.error(Exception(err));
      }
    } catch (e) {
      _log.severe('Create project failed', e);
      state = state.copyWith(error: e.toString());
      notifyListeners();
      return Result.error(e is Exception ? e : Exception(e.toString()));
    }
  }

  Future<Result<void>> _update((String, String?, String?) payload) async {
    final (id, name, description) = payload;
    try {
      final res = await _repository.updateProject(id, name: name, description: description);
      if (res is Ok) {
        return Result.ok(null);
      } else {
        final err = (res as Error).error.toString();
        state = state.copyWith(error: err);
        notifyListeners();
        return Result.error(Exception(err));
      }
    } catch (e) {
      _log.severe('Update project failed', e);
      state = state.copyWith(error: e.toString());
      notifyListeners();
      return Result.error(e is Exception ? e : Exception(e.toString()));
    }
  }
}


