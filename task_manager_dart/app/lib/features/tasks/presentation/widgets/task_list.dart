import 'package:flutter/material.dart';
import 'package:task_manager_app/l10n/app_localizations.dart';
import '../../../../core/ui/components/components.dart';
import '../state/tasks_state.dart';
import '../actions/tasks_action.dart';
import 'package:task_manager_shared/models.dart';

/// Task list widget matching KMM's task list layout
class TaskList extends StatefulWidget {
  final TasksState state;
  final Function(TasksAction) onAction;

  const TaskList({
    super.key,
    required this.state,
    required this.onAction,
  });

  @override
  State<TaskList> createState() => _TaskListState();
}

class _TaskListState extends State<TaskList> {
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollController.position.pixels >=
        _scrollController.position.maxScrollExtent * 0.9) {
      // Load more when scrolled to 90% of the list
      if (widget.state.hasMorePages && !widget.state.isLoading) {
        widget.onAction(const LoadMoreTasks());
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    if (widget.state.isLoading && widget.state.tasks.isEmpty) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }

    if (widget.state.tasks.isEmpty) {
      return TaskItEmptyState(
        icon: Icons.assignment_outlined,
        title: l10n.tasksEmptyTitle,
        subtitle: l10n.tasksEmptySubtitle,
        actionLabel: l10n.createTask,
        onAction: () => widget.onAction(const OpenCreateTask()),
        suggestions: const [
          '• Create tasks to organize your work',
          '• Set priorities to focus on what matters',
          '• Add due dates to stay on track',
        ],
      );
    }

    return ListView.builder(
      controller: _scrollController,
      padding: const EdgeInsets.all(16),
      itemCount: widget.state.tasks.length + 2, // +2 for header and loading indicator
      itemBuilder: (context, index) {
        // First item: Progress summary
        if (index == 0) {
          return Padding(
            padding: const EdgeInsets.only(bottom: 16),
            child: TaskItProgressSummary(
              completedTasks: widget.state.completedTasks,
              totalTasks: widget.state.totalTasks,
            ),
          );
        }

        // Last item: Loading indicator
        if (index == widget.state.tasks.length + 1) {
          if (widget.state.hasMorePages) {
            return const Padding(
              padding: EdgeInsets.all(16),
              child: Center(
                child: CircularProgressIndicator(),
              ),
            );
          }
          return const SizedBox.shrink();
        }

        // Regular task items
        final taskIndex = index - 1;
        final task = widget.state.tasks[taskIndex];

        return Padding(
          padding: const EdgeInsets.only(bottom: 8),
          child: TaskItTaskCard(
            task: task,
            onTap: () => widget.onAction(OpenTaskDetails(task.id)),
            onStatusChanged: (isDone) {
              final newStatus = isDone ? TaskStatus.done : TaskStatus.todo;
              widget.onAction(UpdateTaskStatus(task.id, newStatus));
            },
            onDelete: () => widget.onAction(ConfirmTaskDeletion(task.id)),
          ),
        );
      },
    );
  }
}

