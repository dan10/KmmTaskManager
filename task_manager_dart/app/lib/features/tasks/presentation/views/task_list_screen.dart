import 'package:flutter/material.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../viewmodels/tasks_viewmodel.dart';
import '../widgets/task_list_indicators.dart';
import '../widgets/progress_summary_sliver.dart';
import '../actions/tasks_action.dart';
import '../../../../core/utils/result.dart';
import '../../../../core/ui/components/shimmer.dart';

/// Task list screen with infinite scroll pagination
/// Uses CustomScrollView with slivers for better separation
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
      appBar: _buildAppBar(),
      body: Shimmer(
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
                  
                  // Paginated task list
                  PagedSliverList<int, TaskDto>(
                    state: pagingState,
                    fetchNextPage: fetchNextPage,
                    builderDelegate: PagedChildBuilderDelegate<TaskDto>(
                      itemBuilder: (context, task, index) => TaskListItemBuilder(
                        task: task,
                        onTap: () {
                          // TODO: Navigate to task details
                        },
                        onStatusChanged: (status) => _handleUpdateStatus(task.id, status),
                        onDelete: () => _handleDeleteTask(task.id),
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
      floatingActionButton: _buildFAB(),
    );
  }

  AppBar _buildAppBar() {
    return AppBar(
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
    );
  }

  Widget _buildFAB() {
    return FloatingActionButton(
      heroTag: 'add_task_fab',
      onPressed: () {
        // TODO: Navigate to create task
      },
      child: const Icon(Icons.add),
    );
  }

  Future<void> _handleRefresh(TasksViewModel viewModel) async {
    viewModel.handleAction(const RefreshTasks());
    await Future.delayed(const Duration(milliseconds: 500));
  }
}
