import 'package:flutter/material.dart';
import '../../../../core/l10n/app_l10n.dart';
import 'package:go_router/go_router.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/data/local/secure_storage.dart';
import '../../../../core/routing/app_router.dart';
import '../../../../core/testing/test_ids.dart';
import '../../../../core/ui/components/shimmer.dart';
import '../../../../core/ui/components/taskit_top_app_bar.dart';
import '../viewmodels/tasks_viewmodel.dart';
import '../widgets/progress_summary_sliver.dart';
import '../widgets/task_create_bottom_sheet.dart';
import '../widgets/task_item_swipeable.dart';
import '../widgets/task_list_indicators.dart';

/// Task list screen with infinite scroll pagination
/// Uses CustomScrollView with slivers for better separation
class TaskListScreen extends StatefulWidget {
  final String? projectId;

  const TaskListScreen({super.key, this.projectId});

  @override
  State<TaskListScreen> createState() => _TaskListScreenState();
}

class _TaskListScreenState extends State<TaskListScreen> {
  late TasksViewModel _viewModel;
  bool _isSearchActive = false;
  String? _userInitials;

  @override
  void initState() {
    super.initState();
    _viewModel = context.read<TasksViewModel>();
    
    // Add listeners for commands
    _viewModel.updateTaskStatus.addListener(_onUpdateStatusResult);
    _viewModel.deleteTask.addListener(_onDeleteResult);
    
    // Load user initials
    _loadUserInitials();
  }
  
  Future<void> _loadUserInitials() async {
    final secureStorage = context.read<SecureStorage>();
    final user = await secureStorage.getUser();
    
    if (user != null && mounted) {
      setState(() {
        _userInitials = _computeInitials(user.displayName);
      });
    }
  }
  
  String _computeInitials(String displayName) {
    if (displayName.isEmpty) return '';
    final parts = displayName.trim().split(' ');
    if (parts.length >= 2) {
      return '${parts[0][0]}${parts[1][0]}'.toUpperCase();
    }
    return displayName[0].toUpperCase();
  }

  @override
  void dispose() {
    _viewModel.updateTaskStatus.removeListener(_onUpdateStatusResult);
    _viewModel.deleteTask.removeListener(_onDeleteResult);
    super.dispose();
  }

  void _showSnackBar(String message, {bool isError = false}) {
    if (!mounted) return;
    
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: isError ? Colors.red : Colors.green,
      ),
    );
  }

  void _onDeleteResult() {
    if (_viewModel.deleteTask.completed) {
      _viewModel.deleteTask.clearResult();
      // final l10n = AppLocalizations.of(context)!;
      // _showSnackBar(l10n.taskDeletedSuccess);
    }

    if (_viewModel.deleteTask.error) {
      _viewModel.deleteTask.clearResult();
      final l10n = AppLocalizations.of(context)!;
      _showSnackBar(l10n.taskDeletedError, isError: true);
    }
  }

  void _onUpdateStatusResult() {
    if (_viewModel.updateTaskStatus.completed) {
      _viewModel.updateTaskStatus.clearResult();
      // final l10n = AppLocalizations.of(context)!;
      // _showSnackBar(l10n.taskUpdatedSuccess);
    }

    if (_viewModel.updateTaskStatus.error) {
      _viewModel.updateTaskStatus.clearResult();
      final l10n = AppLocalizations.of(context)!;
      _showSnackBar(l10n.taskUpdatedError, isError: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<TasksViewModel>();
    final vmState = viewModel.state;
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final shimmerGradientToUse = isDark ? shimmerGradientDark : shimmerGradient;

    return Scaffold(
      resizeToAvoidBottomInset: false, // Prevent shifting when keyboard appears
      body: Column(
        children: [
          // TaskIt AppBar with search
          _buildAppBar(viewModel),
          // Body content - must be wrapped in Expanded
          Expanded(
            child: Shimmer(
              linearGradient: shimmerGradientToUse,
              child: RefreshIndicator(
                onRefresh: () => _handleRefresh(viewModel),
                child: PagingListener<int, TaskDto>(
                  controller: viewModel.pagingController,
                  builder: (context, pagingState, fetchNextPage) => 
                    Semantics(
                      identifier: TestIds.listTasks,
                      container: true,
                      explicitChildNodes: true,
                      child: CustomScrollView(
                        slivers: [
                          // Progress summary loaded independently
                          ProgressSummarySliver(
                            completedTasks: vmState.completedTasks,
                          totalTasks: vmState.totalTasks,
                          isLoading: vmState.isLoading,
                        ),
                        
                        // Paginated task list with swipeable items
                        PagedSliverList<int, TaskDto>(
                          state: pagingState,
                          fetchNextPage: fetchNextPage,
                          builderDelegate: PagedChildBuilderDelegate<TaskDto>(
                            itemBuilder: (context, task, index) => Semantics(
                              identifier: TestIds.taskItem(task.id),
                              child: TaskItemSwipeable(
                                key: Key(TestIds.taskItem(task.id)),
                                task: task,
                                onTap: () => _navigateToTaskDetails(context, task.id),
                                onStatusChanged: (status) {
                                  _viewModel.updateTaskStatus.execute((task.id, status));
                                },
                                onDelete: () {
                                  _viewModel.deleteTask.execute(task.id);
                                },
                              ),
                            ),
                            firstPageErrorIndicatorBuilder: (context) => 
                              FirstPageErrorIndicator(
                                errorMessage: vmState.errorMessage,
                                onRetry: () => viewModel.pagingController.refresh(),
                              ),
                            newPageErrorIndicatorBuilder: (context) => 
                              NewPageErrorIndicator(
                                onRetry: () => viewModel.pagingController.refresh(),
                              ),
                            firstPageProgressIndicatorBuilder: (context) => 
                              const FirstPageProgressIndicator(),
                            newPageProgressIndicatorBuilder: (context) => 
                              const NewPageProgressIndicator(),
                            noItemsFoundIndicatorBuilder: (context) => 
                              const NoItemsFoundIndicator(),
                            noMoreItemsIndicatorBuilder: (context) => 
                              const NoMoreItemsIndicator(),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
      floatingActionButton: _buildFAB(),
    );
  }

  PreferredSizeWidget _buildAppBar(TasksViewModel viewModel) {
    final l10n = AppLocalizations.of(context)!;
    return TaskItTopAppBar(
      title: l10n.taskListTitle,
      searchState: TaskItSearchState(
        query: viewModel.state.searchQuery,
        onQueryChange: (query) {
          viewModel.setSearchQuery(query);
        },
        isActive: _isSearchActive,
        onActiveChange: (active) {
          setState(() {
            _isSearchActive = active;
            if (!active) {
              viewModel.clearSearch();
            }
          });
        },
        placeholder: l10n.taskSearchPlaceholder,
        enableClearOnClose: true,
      ),
      userInitials: _userInitials,
      onProfileClick: () => context.push(AppRoutes.profile),
    );
  }

  Widget _buildFAB() {
    return Semantics(
      identifier: TestIds.btnAddTask,
      button: true,
      child: FloatingActionButton(
        key: const Key(TestIds.btnAddTask),
        heroTag: 'add_task_fab',
        onPressed: () => showTaskCreateBottomSheet(
          context: context,
          onDismiss: (shouldRefresh) {
            if (shouldRefresh) {
              _viewModel.refresh();
            }
          },
        ),
        child: const Icon(Icons.add),
      ),
    );
  }

  Future<void> _handleRefresh(TasksViewModel viewModel) async {
    viewModel.refresh();
    await Future.delayed(const Duration(milliseconds: 500));
  }

  void _navigateToTaskDetails(BuildContext context, String taskId) {
    // Find the task from current list to pass for Hero animation
    TaskDto? task;
    try {
      final items = _viewModel.pagingController.value.items;
      if (items != null && items.isNotEmpty) {
        task = items.firstWhere(
          (t) => t.id == taskId,
          orElse: () => items.first,
        );
      }
    } catch (e) {
      // Task not found, will load from API
      task = null;
    }

    context.push(
      AppRoutes.taskDetail.replaceFirst(':taskId', taskId),
      extra: task, // Pass task for instant display
    ).then((_) {
      // Refresh list when returning from details
      _viewModel.refresh();
    });
  }
}
