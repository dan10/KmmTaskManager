import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';
import 'package:task_manager_shared/models.dart';

import '../data/repositories/task_repository.dart';
import '../../../core/utils/command.dart';
import '../../../core/utils/result.dart';

class TaskListState {
  const TaskListState({
    this.items = const <TaskDto>[],
    this.page = 0,
    this.size = 20,
    this.total = 0,
    this.totalPages = 0,
    this.query = '',
    this.projectId,
    this.error,
  });

  final List<TaskDto> items;
  final int page;
  final int size;
  final int total;
  final int totalPages;
  final String query;
  final String? projectId;
  final String? error;

  TaskListState copyWith({
    List<TaskDto>? items,
    int? page,
    int? size,
    int? total,
    int? totalPages,
    String? query,
    String? projectId,
    String? error,
  }) {
    return TaskListState(
      items: items ?? this.items,
      page: page ?? this.page,
      size: size ?? this.size,
      total: total ?? this.total,
      totalPages: totalPages ?? this.totalPages,
      query: query ?? this.query,
      projectId: projectId ?? this.projectId,
      error: error,
    );
  }
}

class TaskListViewModel extends ChangeNotifier {
  TaskListViewModel({required TaskRepository repository}) : _repository = repository {
    load = Command0<void>(_load);
    loadMore = Command0<void>(_loadMore);
  }

  final TaskRepository _repository;
  final _log = Logger('TaskListViewModel');

  var state = const TaskListState();

  late Command0<void> load;
  late Command0<void> loadMore;

  Future<Result<void>> _load() async {
    try {
      final res = await _repository.getTasks(page: 0, size: state.size, query: state.query, projectId: state.projectId);
      if (res is Ok<PaginatedResponse<TaskDto>>) {
        final p = res.value;
        state = state.copyWith(
          items: p.items,
          page: 1,
          total: p.total,
          totalPages: p.totalPages,
          error: null,
        );
        notifyListeners();
        return Result.ok(null);
      } else {
        final err = (res as Error).error.toString();
        state = state.copyWith(error: err);
        notifyListeners();
        return Result.error(Exception(err));
      }
    } catch (e) {
      _log.severe('Load tasks failed', e);
      state = state.copyWith(error: e.toString());
      notifyListeners();
      return Result.error(e is Exception ? e : Exception(e.toString()));
    }
  }

  Future<Result<void>> _loadMore() async {
    if (state.page >= state.totalPages) {
      return Result.ok(null);
    }
    try {
      final res = await _repository.getTasks(page: state.page, size: state.size, query: state.query, projectId: state.projectId);
      if (res is Ok<PaginatedResponse<TaskDto>>) {
        final p = res.value;
        state = state.copyWith(
          items: [...state.items, ...p.items],
          page: state.page + 1,
          total: p.total,
          totalPages: p.totalPages,
          error: null,
        );
        notifyListeners();
        return Result.ok(null);
      } else {
        final err = (res as Error).error.toString();
        state = state.copyWith(error: err);
        notifyListeners();
        return Result.error(Exception(err));
      }
    } catch (e) {
      _log.severe('Load more tasks failed', e);
      state = state.copyWith(error: e.toString());
      notifyListeners();
      return Result.error(e is Exception ? e : Exception(e.toString()));
    }
  }
}


