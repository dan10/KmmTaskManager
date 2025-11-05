import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/l10n/app_l10n.dart';
import '../../../../core/routing/app_router.dart';
import '../../../../core/ui/components/shimmer.dart';
import '../../../tasks/presentation/widgets/task_item_swipeable.dart';
import '../../../tasks/presentation/widgets/task_list_indicators.dart';
import '../viewmodels/calendar_viewmodel.dart';
import '../widgets/calendar_header.dart';
import '../widgets/date_selector_app_bar.dart';

/// Calendar screen showing tasks due on selected date
/// Follows the same architecture as TaskListScreen
class CalendarScreen extends StatefulWidget {
  const CalendarScreen({super.key});

  @override
  State<CalendarScreen> createState() => _CalendarScreenState();
}

class _CalendarScreenState extends State<CalendarScreen> {
  late CalendarViewModel _viewModel;

  @override
  void initState() {
    super.initState();
    _viewModel = context.read<CalendarViewModel>();

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
      final l10n = AppLocalizations.of(context)!;
      _showSnackBar(l10n.taskDeletedSuccess);
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
      final l10n = AppLocalizations.of(context)!;
      _showSnackBar(l10n.taskUpdatedSuccess);
    }

    if (_viewModel.updateTaskStatus.error) {
      _viewModel.updateTaskStatus.clearResult();
      final l10n = AppLocalizations.of(context)!;
      _showSnackBar(l10n.taskUpdatedError, isError: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<CalendarViewModel>();
    final vmState = viewModel.state;
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final shimmerGradientToUse = isDark ? shimmerGradientDark : shimmerGradient;

    return Scaffold(
      appBar: DateSelectorAppBar(
        selectedDate: vmState.selectedDate,
        onDateSelected: (date) => viewModel.selectDate(date),
      ),
      body: Shimmer(
        linearGradient: shimmerGradientToUse,
        child: RefreshIndicator(
          onRefresh: () => _handleRefresh(viewModel),
          child: PagingListener<int, TaskDto>(
            controller: viewModel.pagingController,
            builder: (context, pagingState, fetchNextPage) => CustomScrollView(
              slivers: [
                // Calendar header with task count
                SliverToBoxAdapter(
                  child: CalendarHeader(
                    selectedDate: vmState.selectedDate,
                    taskCount: vmState.totalTasks,
                  ),
                ),

                // Paginated task list with swipeable items
                PagedSliverList<int, TaskDto>(
                  state: pagingState,
                  fetchNextPage: fetchNextPage,
                  builderDelegate: PagedChildBuilderDelegate<TaskDto>(
                    itemBuilder: (context, task, index) => Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                      child: TaskItemSwipeable(
                        task: task,
                        onTap: () => _navigateToTaskDetails(context, task),
                        onStatusChanged: (status) {
                          _viewModel.updateTaskStatus.execute((task.id, status));
                        },
                        onDelete: () {
                          _viewModel.deleteTask.execute(task.id);
                        },
                        showProjectName: true,
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
                        _buildEmptyState(context, vmState.selectedDate),
                    noMoreItemsIndicatorBuilder: (context) =>
                        const NoMoreItemsIndicator(),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildEmptyState(BuildContext context, DateTime selectedDate) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Card(
        color: theme.colorScheme.surfaceContainerHighest,
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                Icons.calendar_month,
                size: 80,
                color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.5),
              ),
              const SizedBox(height: 16),
              Text(
                'No tasks scheduled for this date',
                style: theme.textTheme.bodyLarge?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _handleRefresh(CalendarViewModel viewModel) async {
    viewModel.refresh();
    await Future.delayed(const Duration(milliseconds: 500));
  }

  void _navigateToTaskDetails(BuildContext context, TaskDto task) {
    context.push(
      AppRoutes.taskDetail.replaceFirst(':taskId', task.id),
      extra: task,
    ).then((_) {
      // Refresh list when returning from details
      _viewModel.refresh();
    });
  }
}
