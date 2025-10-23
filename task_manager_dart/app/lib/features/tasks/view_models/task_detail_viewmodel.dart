import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';
import 'package:task_manager_shared/models.dart';

import '../data/repositories/task_repository.dart';
import '../../../utils/command.dart';
import '../../../utils/result.dart';

class TaskDetailState {
  const TaskDetailState({this.task, this.error});
  final TaskDto? task;
  final String? error;

  TaskDetailState copyWith({TaskDto? task, String? error}) =>
      TaskDetailState(task: task ?? this.task, error: error);
}

class TaskDetailViewModel extends ChangeNotifier {
  TaskDetailViewModel({required TaskRepository repository}) : _repository = repository {
    load = Command1<void, String>(_load);
    changeStatus = Command1<void, (String id, TaskStatus status)>(_changeStatus);
    assign = Command1<void, (String id, String assigneeId)>(_assign);
    delete = Command1<void, String>(_delete);
  }

  final TaskRepository _repository;
  final _log = Logger('TaskDetailViewModel');

  var state = const TaskDetailState();

  late Command1<void, String> load;
  late Command1<void, (String, TaskStatus)> changeStatus;
  late Command1<void, (String, String)> assign;
  late Command1<void, String> delete;

  Future<Result<void>> _load(String id) async {
    try {
      final res = await _repository.getTask(id);
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
      _log.severe('Load task failed', e, st);
      state = state.copyWith(error: e.toString());
      notifyListeners();
      return Error<void>(e, st);
    }
  }

  Future<Result<void>> _changeStatus((String, TaskStatus) payload) async {
    final (id, status) = payload;
    try {
      final res = await _repository.changeTaskStatus(id, status);
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
      _log.severe('Change task status failed', e, st);
      return Error<void>(e, st);
    }
  }

  Future<Result<void>> _assign((String, String) payload) async {
    final (id, assignee) = payload;
    try {
      final res = await _repository.assignTask(id, assignee);
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
      _log.severe('Assign task failed', e, st);
      return Error<void>(e, st);
    }
  }

  Future<Result<void>> _delete(String id) async {
    try {
      final res = await _repository.deleteTask(id);
      return res is Ok<void> ? Ok<void>(null) : Error<void>(Exception((res as Error).error.toString()));
    } catch (e, st) {
      _log.severe('Delete task failed', e, st);
      return Error<void>(e, st);
    }
  }
}


