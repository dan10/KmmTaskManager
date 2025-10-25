import 'package:flutter/material.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../viewmodels/tasks_viewmodel.dart';
import '../widgets/empty_tasks_list.dart';
import '../widgets/progress_summary_item.dart';
import '../widgets/task_item_widget.dart';
import '../actions/tasks_action.dart';
import '../effects/tasks_effect.dart';

/// Task list screen with infinite scroll pagination
/// Matches KMM's TasksScreen
class TaskListScreen extends StatefulWidget {
  final String? projectId;

  const TaskListScreen({super.key, this.projectId});

  @override
  State<TaskListScreen> createState() => _TaskListScreenState();
}

typedef FetchNextPage = void Function();

class _TaskListScreenState extends State<TaskListScreen> {
  @override
  void initState() {
    super.initState();
    // Handle effects after first frame
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _handleEffects();
    });
  }

  void _handleEffects() {
    final viewModel = context.read<TasksViewModel>();

    // Listen to effects
    viewModel.addListener(() {
      if (!mounted) return;

      final effects = viewModel.effects;
      for (final effect in effects) {
        switch (effect) {
          case NavigateToTaskDetail():
            // TODO: Navigate to task detail
            break;
          case ShowCreateTaskBottomSheet():
            // TODO: Show create task bottom sheet
            break;
          case ShowSuccessSnackbar():
            _showSnackBar(effect.message, isError: false);
            break;
          case ShowErrorSnackbar():
            _showSnackBar(
              effect.message,
              isError: true,
              actionLabel: effect.actionLabel,
              onAction: effect.onAction,
            );
            break;
          case ShowConfirmationSnackbar():
            _showConfirmationDialog(
              effect.message,
              effect.actionLabel,
              effect.onAction,
            );
            break;
        }
      }
      viewModel.clearEffects();
    });
  }

  void _showSnackBar(
    String message, {
    bool isError = false,
    String? actionLabel,
    VoidCallback? onAction,
  }) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: isError ? Colors.red : Colors.green,
        action: actionLabel != null && onAction != null
            ? SnackBarAction(
                label: actionLabel,
                textColor: Colors.white,
                onPressed: onAction,
              )
            : null,
      ),
    );
  }

  void _showConfirmationDialog(
    String message,
    String actionLabel,
    VoidCallback onConfirm,
  ) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Confirm'),
        content: Text(message),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
              onConfirm();
            },
            child: Text(actionLabel),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<TasksViewModel>();
    final vmState = viewModel.state;
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Row(
          children: [
            Icon(Icons.checklist, size: 28),
            SizedBox(width: 12),
            Text('Tasks'),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: () {
              // TODO: Implement search
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          viewModel.handleAction(const RefreshTasks());
          // Wait a bit for the refresh to complete
          await Future.delayed(const Duration(milliseconds: 500));
        },
        child: PagingListener<int, TaskDto>(
          controller: viewModel.pagingController,
          builder: (context, pagingState, fetchNextPage) => PagedListView<int, TaskDto>.separated(
            state: pagingState,
            fetchNextPage: fetchNextPage,
            separatorBuilder: (context, index) => const SizedBox(height: 0),
            builderDelegate: PagedChildBuilderDelegate<TaskDto>(
            itemBuilder: (context, task, index) {
              // First item: show progress summary
              if (index == 0) {
                return Column(
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(16),
                      child: ProgressSummaryItem(
                        completedTasks: vmState.completedTasks,
                        totalTasks: vmState.totalTasks,
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                      child: TaskItemWidget(
                        task: task,
                        onTap: () {
                          viewModel.handleAction(OpenTaskDetails(task.id));
                        },
                      ),
                    ),
                  ],
                );
              }

              // Regular task items
              return Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: TaskItemWidget(
                  task: task,
                  onTap: () {
                    viewModel.handleAction(OpenTaskDetails(task.id));
                  },
                ),
              );
            },
            firstPageErrorIndicatorBuilder: (context) => Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.error_outline, size: 64, color: Colors.red),
                  const SizedBox(height: 16),
                  Text(
                    vmState.errorMessage ?? 'Failed to load tasks',
                    style: theme.textTheme.bodyLarge,
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () => viewModel.pagingController.refresh(),
                    child: const Text('Retry'),
                  ),
                ],
              ),
            ),
            newPageErrorIndicatorBuilder: (context) => Padding(
              padding: const EdgeInsets.all(16),
              child: Center(
                child: Column(
                  children: [
                    Text(
                      'Failed to load more tasks',
                      style: theme.textTheme.bodyMedium,
                    ),
                    const SizedBox(height: 8),
                    TextButton(
                      onPressed: () => viewModel.pagingController.refresh(),
                      child: const Text('Retry'),
                    ),
                  ],
                ),
              ),
            ),
            firstPageProgressIndicatorBuilder: (context) => const Center(
              child: CircularProgressIndicator(),
            ),
            newPageProgressIndicatorBuilder: (context) => const Padding(
              padding: EdgeInsets.all(16),
              child: Center(
                child: CircularProgressIndicator(),
              ),
            ),
            noItemsFoundIndicatorBuilder: (context) => Column(
              children: [
                Padding(
                  padding: const EdgeInsets.all(16),
                  child: ProgressSummaryItem(
                    completedTasks: vmState.completedTasks,
                    totalTasks: vmState.totalTasks,
                  ),
                ),
                const Expanded(
                  child: EmptyTasksList(),
                ),
              ],
            ),
            noMoreItemsIndicatorBuilder: (context) => const Padding(
              padding: EdgeInsets.all(16),
              child: Center(
                child: Text(
                  'No more tasks',
                  style: TextStyle(color: Colors.grey),
                ),
              ),
            ),
          ),
        ),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        heroTag: 'add_task_fab',
        onPressed: () {
          viewModel.handleAction(const OpenCreateTask());
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}

