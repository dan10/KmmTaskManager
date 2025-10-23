import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';
import 'package:task_manager_shared/models.dart';

import '../data/repositories/task_repository.dart';
import '../../../utils/command.dart';
import '../../../utils/result.dart';

class TaskCreateEditState {
  const TaskCreateEditState({this.task, this.error});
  final TaskDto? task;
  final String? error;

  TaskCreateEditState copyWith({TaskDto? task, String? error}) =>
      TaskCreateEditState(task: task ?? this.task, error: error);
}

class TaskCreateEditViewModel extends ChangeNotifier {
  TaskCreateEditViewModel({required TaskRepository repository}) : _repository = repository {
    create = Command1<void, TaskCreateRequestDto>(_create);
    update = Command1<void, (String id, TaskUpdateRequestDto req)>(_update);
  }

  final TaskRepository _repository;
  final _log = Logger('TaskCreateEditViewModel');

  var state = const TaskCreateEditState();

  late Command1<void, TaskCreateRequestDto> create;
  late Command1<void, (String, TaskUpdateRequestDto)> update;

  Future<Result<void>> _create(TaskCreateRequestDto req) async {
    try {
      final res = await _repository.createTask(req);
      if (res is Ok<TaskDto>) {
        state = state.copyWith(task: res.value, error: null);
        notifyListeners();
        return Ok<void>(null);
      } else {
        final err = (res as Error).error.toString();
        state = state.copyWith(error: err);
        notifyListeners();
        return Error<void>(Exception(err));
      }
    } catch (e, st) {
      _log.severe('Create task failed', e, st);
      state = state.copyWith(error: e.toString());
      notifyListeners();
      return Error<void>(e, st);
    }
  }

  Future<Result<void>> _update((String, TaskUpdateRequestDto) payload) async {
    final (id, req) = payload;
    try {
      final res = await _repository.updateTask(id, req);
      if (res is Ok<TaskDto>) {
        state = state.copyWith(task: res.value, error: null);
        notifyListeners();
        return Ok<void>(null);
      } else {
        final err = (res as Error).error.toString();
        state = state.copyWith(error: err);
        notifyListeners();
        return Error<void>(Exception(err));
      }
    } catch (e, st) {
      _log.severe('Update task failed', e, st);
      state = state.copyWith(error: e.toString());
      notifyListeners();
      return Error<void>(e, st);
    }
  }
}


