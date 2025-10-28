import 'package:flutter/material.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/ui/components/shimmer.dart';
import 'task_item_swipeable.dart';
import 'task_list_indicators.dart';

/// Shared paginated task list component
/// Used in both TaskListScreen and ProjectDetailScreen
class PaginatedTaskList extends StatelessWidget {
  final PagingController<int, TaskDto> pagingController;
  final void Function(String taskId) onTaskTap;
  final void Function(String taskId, TaskStatus status)? onTaskStatusChanged;
  final void Function(String taskId)? onTaskDelete;
  final String? errorMessage;
  final Future<void> Function() onRefresh;
  final Widget? emptyStateWidget;
  final EdgeInsetsGeometry? padding;
  final bool showProjectName;

  const PaginatedTaskList({
    super.key,
    required this.pagingController,
    required this.onTaskTap,
    required this.onRefresh,
    this.onTaskStatusChanged,
    this.onTaskDelete,
    this.errorMessage,
    this.emptyStateWidget,
    this.padding,
    this.showProjectName = true,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final shimmerGradientToUse = isDark ? shimmerGradientDark : shimmerGradient;

    return Shimmer(
      linearGradient: shimmerGradientToUse,
      child: RefreshIndicator(
        onRefresh: onRefresh,
        child: PagingListener<int, TaskDto>(
          controller: pagingController,
          builder: (context, pagingState, fetchNextPage) => 
            PagedListView<int, TaskDto>(
              state: pagingState,
              fetchNextPage: fetchNextPage,
              padding: padding ?? const EdgeInsets.symmetric(horizontal: 16),
              builderDelegate: PagedChildBuilderDelegate<TaskDto>(
                itemBuilder: (context, task, index) {
                  // Capture callbacks in local variables for null-safety
                  final statusChanged = onTaskStatusChanged;
                  final deleteCallback = onTaskDelete;
                  
                  return TaskItemSwipeable(
                    task: task,
                    onTap: () => onTaskTap(task.id),
                    onStatusChanged: statusChanged != null
                        ? (status) => statusChanged(task.id, status)
                        : (_) {},
                    onDelete: deleteCallback != null
                        ? () => deleteCallback(task.id)
                        : () {},
                    showProjectName: showProjectName,
                  );
                },
                firstPageErrorIndicatorBuilder: (context) => 
                  FirstPageErrorIndicator(
                    errorMessage: errorMessage,
                    onRetry: () => pagingController.refresh(),
                  ),
                newPageErrorIndicatorBuilder: (context) => 
                  NewPageErrorIndicator(
                    onRetry: () => pagingController.refresh(),
                  ),
                firstPageProgressIndicatorBuilder: (context) => 
                  const FirstPageProgressIndicator(),
                newPageProgressIndicatorBuilder: (context) => 
                  const NewPageProgressIndicator(),
                noItemsFoundIndicatorBuilder: (context) => 
                  emptyStateWidget ?? const NoItemsFoundIndicator(),
                noMoreItemsIndicatorBuilder: (context) => 
                  const NoMoreItemsIndicator(),
              ),
            ),
        ),
      ),
    );
  }
}

