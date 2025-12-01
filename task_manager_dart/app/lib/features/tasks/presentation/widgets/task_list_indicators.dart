import 'package:flutter/material.dart';
import 'package:task_manager_shared/models.dart';

import 'task_item_widget.dart';
import 'task_placeholders.dart';
import '../../../../core/ui/components/shimmer.dart';

/// Task item builder - no longer handles progress summary
class TaskListItemBuilder extends StatelessWidget {
  final TaskDto task;
  final VoidCallback onTap;
  final Function(TaskStatus) onStatusChanged;
  final VoidCallback onDelete;

  const TaskListItemBuilder({
    super.key,
    required this.task,
    required this.onTap,
    required this.onStatusChanged,
    required this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: TaskItemWidget(
        task: task,
        onTap: onTap,
        onStatusChanged: onStatusChanged,
        onDelete: onDelete,
      ),
    );
  }
}

/// First page error indicator
class FirstPageErrorIndicator extends StatelessWidget {
  final String? errorMessage;
  final VoidCallback onRetry;

  const FirstPageErrorIndicator({
    super.key,
    this.errorMessage,
    required this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.error_outline, size: 64, color: Colors.red),
          const SizedBox(height: 16),
          Text(
            errorMessage ?? 'Failed to load tasks',
            style: theme.textTheme.bodyLarge,
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: onRetry,
            child: const Text('Retry'),
          ),
        ],
      ),
    );
  }
}

/// New page error indicator
class NewPageErrorIndicator extends StatelessWidget {
  final VoidCallback onRetry;

  const NewPageErrorIndicator({
    super.key,
    required this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    return Padding(
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
              onPressed: onRetry,
              child: const Text('Retry'),
            ),
          ],
        ),
      ),
    );
  }
}

/// First page progress indicator with shimmer effect
class FirstPageProgressIndicator extends StatelessWidget {
  const FirstPageProgressIndicator({super.key});

  @override
  Widget build(BuildContext context) {
    return Column(
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
    );
  }
}

/// New page progress indicator
class NewPageProgressIndicator extends StatelessWidget {
  const NewPageProgressIndicator({super.key});

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.all(16),
      child: Center(
        child: CircularProgressIndicator(),
      ),
    );
  }
}

/// No items found indicator - simplified without progress
class NoItemsFoundIndicator extends StatelessWidget {
  const NoItemsFoundIndicator({super.key});

  @override
  Widget build(BuildContext context) {
    return const Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.inbox_outlined, size: 80, color: Colors.grey),
          SizedBox(height: 16),
          Text(
            'No tasks found',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.w500,
              color: Colors.grey,
            ),
          ),
          SizedBox(height: 8),
          Text(
            'Create a task to get started',
            style: TextStyle(color: Colors.grey),
          ),
        ],
      ),
    );
  }
}

/// No more items indicator
class NoMoreItemsIndicator extends StatelessWidget {
  const NoMoreItemsIndicator({super.key});

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.all(16),
      child: Center(
        child: Text(
          'No more tasks',
          style: TextStyle(color: Colors.grey),
        ),
      ),
    );
  }
}

