import 'package:flutter/material.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../viewmodels/tasks_viewmodel.dart';
import '../widgets/empty_tasks_list.dart';
import '../widgets/progress_summary_item.dart';
import '../widgets/task_item_widget.dart';
import '../widgets/task_placeholders.dart';
import '../actions/tasks_action.dart';
import '../../../../core/utils/result.dart';
import '../../../../core/ui/components/shimmer.dart';

/// Task list screen with infinite scroll pagination
/// Follows Flutter's best practices for UI events
class TaskListScreen extends StatefulWidget {
  final String? projectId;

  const TaskListScreen({super.key, this.projectId});

  @override
  State<TaskListScreen> createState() => _TaskListScreenState();
}

class _TaskListScreenState extends State<TaskListScreen> {
  void _showSnackBar(String message, {bool isError = false}) {
    if (!mounted) return;
    
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: isError ? Colors.red : Colors.green,
      ),
    );
  }

  // Handle delete with direct callback - Flutter pattern
  Future<void> _handleDeleteTask(String taskId) async {
    final viewModel = context.read<TasksViewModel>();
    final result = await viewModel.deleteTask(taskId);
    
    if (!mounted) return;
    
    switch (result) {
      case Ok():
        _showSnackBar('Task deleted successfully');
      case Error(:final error):
        _showSnackBar('Failed to delete task: ${error.toString()}', isError: true);
    }
  }

  // Handle status update with direct callback - Flutter pattern
  Future<void> _handleUpdateStatus(String taskId, TaskStatus status) async {
    final viewModel = context.read<TasksViewModel>();
    final result = await viewModel.updateTaskStatus(taskId, status);
    
    if (!mounted) return;
    
    switch (result) {
      case Ok():
        _showSnackBar('Task status updated');
      case Error(:final error):
        _showSnackBar('Failed to update task: ${error.toString()}', isError: true);
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
      body: Shimmer(
        linearGradient: shimmerGradientToUse,
        child: RefreshIndicator(
          onRefresh: () async {
            viewModel.handleAction(const RefreshTasks());
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
                              // TODO: Navigate to task details
                            },
                            onStatusChanged: (status) => _handleUpdateStatus(task.id, status),
                            onDelete: () => _handleDeleteTask(task.id),
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
                        // TODO: Navigate to task details
                      },
                      onStatusChanged: (status) => _handleUpdateStatus(task.id, status),
                      onDelete: () => _handleDeleteTask(task.id),
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
                firstPageProgressIndicatorBuilder: (context) => Column(
                  children: [
                    ShimmerLoading(
                      isLoading: true,
                      child: const ProgressSummaryPlaceholder(),
                    ),
                    ShimmerLoading(
                      isLoading: true,
                      child: const TaskItemPlaceholder(),
                    ),
                    ShimmerLoading(
                      isLoading: true,
                      child: const TaskItemPlaceholder(),
                    ),
                    ShimmerLoading(
                      isLoading: true,
                      child: const TaskItemPlaceholder(),
                    ),
                    ShimmerLoading(
                      isLoading: true,
                      child: const TaskItemPlaceholder(),
                    ),
                  ],
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
      ),
      floatingActionButton: FloatingActionButton(
        heroTag: 'add_task_fab',
        onPressed: () {
          // TODO: Navigate to create task
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}
