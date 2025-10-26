import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/routing/app_router.dart';
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

  @override
  void initState() {
    super.initState();
    _viewModel = context.read<TasksViewModel>();
    
    // Add listeners for commands
    _viewModel.updateTaskStatus.addListener(_onUpdateStatusResult);
    _viewModel.deleteTask.addListener(_onDeleteResult);
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
      _showSnackBar('Task deleted successfully');
    }

    if (_viewModel.deleteTask.error) {
      _viewModel.deleteTask.clearResult();
      _showSnackBar('Failed to delete task', isError: true);
    }
  }

  void _onUpdateStatusResult() {
    if (_viewModel.updateTaskStatus.completed) {
      _viewModel.updateTaskStatus.clearResult();
      _showSnackBar('Task updated successfully');
    }

    if (_viewModel.updateTaskStatus.error) {
      _viewModel.updateTaskStatus.clearResult();
      _showSnackBar('Failed to update task', isError: true);
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
                    CustomScrollView(
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
                            itemBuilder: (context, task, index) => TaskItemSwipeable(
                              task: task,
                              onTap: () => _navigateToTaskDetails(context, task.id),
                              onStatusChanged: (status) {
                                _viewModel.updateTaskStatus.execute((task.id, status));
                              },
                              onDelete: () {
                                _viewModel.deleteTask.execute(task.id);
                              },
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
        ],
      ),
      floatingActionButton: _buildFAB(),
    );
  }

  PreferredSizeWidget _buildAppBar(TasksViewModel viewModel) {
    return TaskItTopAppBar(
      title: 'Tasks',
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
        placeholder: 'Search tasks...',
        enableClearOnClose: true,
      ),
    );
  }

  Widget _buildFAB() {
    return FloatingActionButton(
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
