import 'package:flutter/material.dart';
import '../../../../core/l10n/app_l10n.dart';
import 'package:go_router/go_router.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/routing/app_router.dart';
import '../../../../core/theme/theme.dart';
import '../../../../core/ui/components/taskit_top_app_bar.dart';
import '../state/project_detail_state.dart';
import '../viewmodels/project_tasks_viewmodel.dart';
import '../../../tasks/presentation/widgets/task_create_bottom_sheet.dart';
import '../../../tasks/presentation/widgets/paginated_task_list.dart';

/// Project detail screen showing project info and its tasks
class ProjectDetailScreen extends StatefulWidget {
  final String projectId;

  const ProjectDetailScreen({
    super.key,
    required this.projectId,
  });

  @override
  State<ProjectDetailScreen> createState() => _ProjectDetailScreenState();
}

class _ProjectDetailScreenState extends State<ProjectDetailScreen> {
  late ProjectTasksViewModel _viewModel;

  @override
  void initState() {
    super.initState();
    _viewModel = context.read<ProjectTasksViewModel>();
    
    // Add listeners for commands
    _viewModel.updateTaskStatus.addListener(_onUpdateStatusResult);
  }

  @override
  void dispose() {
    _viewModel.updateTaskStatus.removeListener(_onUpdateStatusResult);
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
    final viewModel = context.watch<ProjectTasksViewModel>();
    final vmState = viewModel.state;

    return Builder(
      builder: (context) => Scaffold(
        backgroundColor: Theme.of(context).colorScheme.surface,
        appBar: _buildAppBar(vmState),
        body: vmState.isLoading
          ? const Center(child: CircularProgressIndicator())
          : vmState.errorMessage != null
              ? _buildError(vmState.errorMessage!)
              : vmState.project == null
                  ? _buildEmpty()
                  : Column(
                      children: [
                        // Project header with progress
                        _ProjectHeader(project: vmState.project!),
                        
                        
                        
                        // Task list - using shared component
                        Expanded(
                          child: PaginatedTaskList(
                            pagingController: viewModel.pagingController,
                            onTaskTap: (taskId) => _navigateToTaskDetails(context, taskId),
                            onTaskStatusChanged: (taskId, status) {
                              _viewModel.updateTaskStatus.execute((taskId, status));
                            },
                            onRefresh: () => _handleRefresh(viewModel),
                            emptyStateWidget: _buildEmptyTasks(),
                            showProjectName: false, // Hide project name since we're in project detail
                          ),
                        ),
                      ],
                    ),
        floatingActionButton: _buildFAB(),
      ),
    );
  }

  PreferredSizeWidget _buildAppBar(ProjectDetailState state) {
    final l10n = AppLocalizations.of(context)!;
    return TaskItTopAppBar(
      title: state.project?.name ?? l10n.projectDetailsTitle,
      showNavigationIcon: true,
      onNavigateBack: () => context.pop(),
    );
  }

  Widget _buildError(String message) {
    final l10n = AppLocalizations.of(context)!;
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.error_outline, size: 64, color: Colors.red),
          const SizedBox(height: 16),
          Text(message, textAlign: TextAlign.center),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: () => _viewModel.loadProject.execute(),
            child: Text(l10n.commonRetry),
          ),
        ],
      ),
    );
  }

  Widget _buildEmpty() {
    return const Center(
      child: Text('Project not found'),
    );
  }

  Widget _buildEmptyTasks() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.task_outlined,
              size: 80,
              color: Theme.of(context).colorScheme.primary.withValues(alpha: 0.3),
            ),
            const SizedBox(height: 16),
            Text(
              'No tasks yet',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 8),
            Text(
              'Create your first task for this project',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: Colors.grey,
                  ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFAB() {
    return FloatingActionButton(
      heroTag: 'add_task_fab',
      onPressed: () => showTaskCreateBottomSheet(
        context: context,
        projectId: widget.projectId,
        onDismiss: (shouldRefresh) {
          if (shouldRefresh) {
            _viewModel.refresh();
          }
        },
      ),
      child: const Icon(Icons.add),
    );
  }

  Future<void> _handleRefresh(ProjectTasksViewModel viewModel) async {
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
      extra: task, // Pass task for Hero animation
    ).then((_) {
      // Refresh list when returning from details
      _viewModel.refresh();
    });
  }
}

/// Project header widget showing progress
/// Matches the design from the screenshot - compact layout with just statistics and progress bar
class _ProjectHeader extends StatelessWidget {
  final Project project;

  const _ProjectHeader({required this.project});

  @override
  Widget build(BuildContext context) {
    return Builder(
      builder: (context) => Container(
        width: double.infinity,
        color: context.extColors.surfaceCard,
        padding: const EdgeInsets.fromLTRB(24, 20, 24, 24),
      child: Column(
        children: [
          // Statistics row
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              // Completed
              Expanded(
                child: _StatisticItem(
                  label: 'Completed',
                  value: '${project.completed}',
                  color: Colors.green,
                ),
              ),
              
              // In Progress
              Expanded(
                child: _StatisticItem(
                  label: 'In Progress',
                  value: '${project.inProgress}',
                  color: Colors.orange,
                ),
              ),
              
              // Total
              Expanded(
                child: _StatisticItem(
                  label: 'Total',
                  value: '${project.total}',
                  color: context.extColors.textPrimary,
                ),
              ),
            ],
          ),
          
          const SizedBox(height: 16),
          
          // Progress bar
          ClipRRect(
            borderRadius: BorderRadius.circular(4),
            child: LinearProgressIndicator(
              value: project.total > 0 ? project.completed / project.total : 0,
              minHeight: 6,
              backgroundColor: context.extColors.trackNeutral,
              valueColor: AlwaysStoppedAnimation<Color>(
                Theme.of(context).colorScheme.primary,
              ),
            ),
          ),
        ],
        ),
      ),
    );
  }
}

/// Individual statistic item widget
class _StatisticItem extends StatelessWidget {
  final String label;
  final String value;
  final Color color;

  const _StatisticItem({
    required this.label,
    required this.value,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(
          label,
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: context.extColors.textSecondary,
              ),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 8),
        Text(
          value,
          style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                fontWeight: FontWeight.bold,
                color: color,
              ),
          textAlign: TextAlign.center,
        ),
      ],
    );
  }
}
