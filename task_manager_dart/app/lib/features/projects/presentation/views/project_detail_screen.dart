import 'package:flutter/material.dart';
import '../../../../core/l10n/app_l10n.dart';
import 'package:go_router/go_router.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/routing/app_router.dart';
import '../../../../core/ui/components/shimmer.dart';
import '../../../../core/ui/components/taskit_top_app_bar.dart';
import '../state/project_detail_state.dart';
import '../viewmodels/project_tasks_viewmodel.dart';
import '../../../tasks/presentation/widgets/task_create_bottom_sheet.dart';
import '../../../tasks/presentation/widgets/task_item_swipeable.dart';
import '../../../tasks/presentation/widgets/task_list_indicators.dart';

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
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final shimmerGradientToUse = isDark ? shimmerGradientDark : shimmerGradient;

    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
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
                        
                        // Tasks section title
                        Padding(
                          padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
                          child: Row(
                            children: [
                              Text(
                                'Tasks',
                                style: theme.textTheme.titleLarge?.copyWith(
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ],
                          ),
                        ),
                        
                        // Task list
                        Expanded(
                          child: Shimmer(
                            linearGradient: shimmerGradientToUse,
                            child: RefreshIndicator(
                              onRefresh: () => _handleRefresh(viewModel),
                              child: PagingListener<int, TaskDto>(
                                controller: viewModel.pagingController,
                                builder: (context, pagingState, fetchNextPage) => 
                                  PagedListView<int, TaskDto>(
                                    state: pagingState,
                                    fetchNextPage: fetchNextPage,
                                    padding: const EdgeInsets.symmetric(horizontal: 16),
                                    builderDelegate: PagedChildBuilderDelegate<TaskDto>(
                                      itemBuilder: (context, task, index) => TaskItemSwipeable(
                                        task: task,
                                        onTap: () => _navigateToTaskDetails(context, task.id),
                                        onStatusChanged: (status) {
                                          _viewModel.updateTaskStatus.execute((task.id, status));
                                        },
                                        onDelete: () {
                                          // Delete functionality can be added here
                                        },
                                      ),
                                      firstPageErrorIndicatorBuilder: (context) => 
                                        FirstPageErrorIndicator(
                                          errorMessage: null,
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
                                        _buildEmptyTasks(),
                                      noMoreItemsIndicatorBuilder: (context) => 
                                        const NoMoreItemsIndicator(),
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
              color: Theme.of(context).colorScheme.primary.withOpacity(0.3),
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
    context.push(
      AppRoutes.taskDetail.replaceFirst(':taskId', taskId),
    ).then((_) {
      // Refresh list when returning from details
      _viewModel.refresh();
    });
  }
}

/// Project header widget showing progress
class _ProjectHeader extends StatelessWidget {
  final Project project;

  const _ProjectHeader({required this.project});

  @override
  Widget build(BuildContext context) {
    final progressPercentage = project.total > 0
        ? ((project.completed / project.total) * 100).toInt()
        : 0;

    return Container(
      width: double.infinity,
      color: Colors.white,
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Project name
          Text(
            project.name,
            style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: const Color(0xFF1A1A1A),
                ),
          ),
          
          // Description
          if (project.description != null && project.description!.isNotEmpty) ...[
            const SizedBox(height: 8),
            Text(
              project.description!,
              style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                    color: const Color(0xFF6B7280),
                  ),
            ),
          ],
          
          const SizedBox(height: 20),
          
          // Progress section
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              // Completed tasks
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Completed',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: const Color(0xFF6B7280),
                        ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${project.completed}',
                    style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                          fontWeight: FontWeight.bold,
                          color: Colors.green,
                        ),
                  ),
                ],
              ),
              
              // In Progress tasks
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'In Progress',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: const Color(0xFF6B7280),
                        ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${project.inProgress}',
                    style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                          fontWeight: FontWeight.bold,
                          color: Colors.orange,
                        ),
                  ),
                ],
              ),
              
              // Total tasks
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Total',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: const Color(0xFF6B7280),
                        ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${project.total}',
                    style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                          fontWeight: FontWeight.bold,
                          color: const Color(0xFF1A1A1A),
                        ),
                  ),
                ],
              ),
              
              // Progress percentage
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    'Progress',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: const Color(0xFF6B7280),
                        ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '$progressPercentage%',
                    style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                          fontWeight: FontWeight.bold,
                          color: Theme.of(context).colorScheme.primary,
                        ),
                  ),
                ],
              ),
            ],
          ),
          
          const SizedBox(height: 16),
          
          // Progress bar
          ClipRRect(
            borderRadius: BorderRadius.circular(8),
            child: LinearProgressIndicator(
              value: project.total > 0 ? project.completed / project.total : 0,
              minHeight: 12,
              backgroundColor: const Color(0xFFE5E7EB),
              valueColor: AlwaysStoppedAnimation<Color>(
                Theme.of(context).colorScheme.primary,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
