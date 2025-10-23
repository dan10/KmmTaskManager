import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';
import 'package:task_manager_shared/models.dart';

import '../data/repositories/project_repository.dart';
import '../../../utils/command.dart';
import '../../../utils/result.dart';

class ProjectListState {
  const ProjectListState({
    this.items = const <Project>[],
    this.page = 0,
    this.size = 10,
    this.total = 0,
    this.totalPages = 0,
    this.query = '',
    this.error,
  });

  final List<Project> items;
  final int page;
  final int size;
  final int total;
  final int totalPages;
  final String query;
  final String? error;

  ProjectListState copyWith({
    List<Project>? items,
    int? page,
    int? size,
    int? total,
    int? totalPages,
    String? query,
    String? error,
  }) {
    return ProjectListState(
      items: items ?? this.items,
      page: page ?? this.page,
      size: size ?? this.size,
      total: total ?? this.total,
      totalPages: totalPages ?? this.totalPages,
      query: query ?? this.query,
      error: error,
    );
  }
}

class ProjectListViewModel extends ChangeNotifier {
  ProjectListViewModel({required ProjectRepository repository})
      : _repository = repository {
    load = Command0<void>(_load);
    loadMore = Command0<void>(_loadMore);
  }

  final _log = Logger('ProjectListViewModel');
  final ProjectRepository _repository;

  var state = const ProjectListState();

  late Command0<void> load;
  late Command0<void> loadMore;

  Future<Result<void>> _load() async {
    try {
      final res = await _repository.getProjects(page: 0, size: state.size, query: state.query);
      if (res is Ok<PaginatedResponse<Project>>) {
        final p = res.value;
        state = state.copyWith(
          items: p.items,
          page: 1,
          total: p.total,
          totalPages: p.totalPages,
          error: null,
        );
        notifyListeners();
        return Ok<void>(null);
      } else {
        final err = (res as Error).error.toString();
        state = state.copyWith(error: err);
        notifyListeners();
        return Error<void>(Exception(err));
      }
    } catch (e, st) {
      _log.severe('Load projects failed', e, st);
      state = state.copyWith(error: e.toString());
      notifyListeners();
      return Error<void>(e, st);
    }
  }

  Future<Result<void>> _loadMore() async {
    if (state.page >= state.totalPages) {
      return Ok<void>(null);
    }
    try {
      final res = await _repository.getProjects(page: state.page, size: state.size, query: state.query);
      if (res is Ok<PaginatedResponse<Project>>) {
        final p = res.value;
        state = state.copyWith(
          items: [...state.items, ...p.items],
          page: state.page + 1,
          total: p.total,
          totalPages: p.totalPages,
          error: null,
        );
        notifyListeners();
        return Ok<void>(null);
      } else {
        final err = (res as Error).error.toString();
        state = state.copyWith(error: err);
        notifyListeners();
        return Error<void>(Exception(err));
      }
    } catch (e, st) {
      _log.severe('Load more projects failed', e, st);
      state = state.copyWith(error: e.toString());
      notifyListeners();
      return Error<void>(e, st);
    }
  }
}


