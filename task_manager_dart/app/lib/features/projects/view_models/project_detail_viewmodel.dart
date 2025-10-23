import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';
import 'package:task_manager_shared/models.dart';

import '../data/repositories/project_repository.dart';
import '../../../utils/command.dart';
import '../../../utils/result.dart';

class ProjectDetailState {
  const ProjectDetailState({this.project, this.error});
  final Project? project;
  final String? error;

  ProjectDetailState copyWith({Project? project, String? error}) =>
      ProjectDetailState(project: project ?? this.project, error: error);
}

class ProjectDetailViewModel extends ChangeNotifier {
  ProjectDetailViewModel({required ProjectRepository repository}) : _repository = repository {
    load = Command1<void, String>(_load);
    delete = Command1<void, String>(_delete);
  }

  final ProjectRepository _repository;
  final _log = Logger('ProjectDetailViewModel');

  var state = const ProjectDetailState();

  late Command1<void, String> load;
  late Command1<void, String> delete;

  Future<Result<void>> _load(String id) async {
    try {
      final res = await _repository.getProject(id);
      if (res is Ok<Project>) {
        state = state.copyWith(project: res.value, error: null);
        notifyListeners();
        return Ok<void>(null);
      } else {
        final err = (res as Error).error.toString();
        state = state.copyWith(error: err);
        notifyListeners();
        return Error<void>(Exception(err));
      }
    } catch (e, st) {
      _log.severe('Load project failed', e, st);
      state = state.copyWith(error: e.toString());
      notifyListeners();
      return Error<void>(e, st);
    }
  }

  Future<Result<void>> _delete(String id) async {
    try {
      final res = await _repository.deleteProject(id);
      if (res is Ok<void>) {
        return Ok<void>(null);
      } else {
        final err = (res as Error).error.toString();
        return Error<void>(Exception(err));
      }
    } catch (e, st) {
      _log.severe('Delete project failed', e, st);
      return Error<void>(e, st);
    }
  }
}


