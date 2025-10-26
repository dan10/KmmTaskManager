import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/ui/components/taskit_top_app_bar.dart';
import '../viewmodels/task_details_viewmodel.dart';

/// Task details screen with Hero animations
/// Follows Flutter's best practices and matches KMM's TaskDetailsScreen
class TaskDetailsScreen extends StatefulWidget {
  final String taskId;

  const TaskDetailsScreen({
    super.key,
    required this.taskId,
  });

  @override
  State<TaskDetailsScreen> createState() => _TaskDetailsScreenState();
}

class _TaskDetailsScreenState extends State<TaskDetailsScreen> {
  late TaskDetailsViewModel _viewModel;

  @override
  void initState() {
    super.initState();
    _viewModel = context.read<TaskDetailsViewModel>();
    
    // Add listener for delete command
    _viewModel.deleteTask.addListener(_onDeleteResult);
  }

  @override
  void dispose() {
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

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<TaskDetailsViewModel>();
    final state = viewModel.state;

    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      appBar: _buildAppBar(viewModel, state),
      body: _buildBody(state),
    );
  }

  PreferredSizeWidget _buildAppBar(TaskDetailsViewModel viewModel, state) {
    return TaskItTopAppBar(
      title: 'Task Details',
      showNavigationIcon: true,
      onNavigateBack: () => viewModel.handleNavigateBack(),
      actions: [
        // Edit button
        IconButton(
          icon: const Icon(Icons.edit),
          onPressed: () => viewModel.handleEditTask(),
        ),
        // Delete button
        IconButton(
          icon: state.isDeleting
              ? const SizedBox(
                  width: 24,
                  height: 24,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                  ),
                )
              : const Icon(Icons.delete),
          onPressed: state.isDeleting
              ? null
              : () => viewModel.deleteTask.execute(),
        ),
      ],
    );
  }

  Widget _buildBody(state) {
    if (state.isLoading) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }

    if (state.errorMessage != null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(
              Icons.error_outline,
              size: 64,
              color: Colors.red,
            ),
            const SizedBox(height: 16),
            Text(
              state.errorMessage!,
              style: const TextStyle(fontSize: 16),
            ),
          ],
        ),
      );
    }

    if (state.task == null) {
      return const Center(
        child: Text('Task not found'),
      );
    }

    return RefreshIndicator(
      onRefresh: () => _viewModel.refresh(),
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildTaskHeaderCard(state.task!),
            const SizedBox(height: 16),
            if (state.task!.description.isNotEmpty) ...[
              _buildDescriptionCard(state.task!),
              const SizedBox(height: 16),
            ],
            _buildTaskInformationCard(state.task!),
            const SizedBox(height: 16),
            _buildDatesCard(state.task!),
          ],
        ),
      ),
    );
  }

  Widget _buildTaskHeaderCard(TaskDto task) {
    return Card(
      color: Colors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      elevation: 1,
      child: IntrinsicHeight(
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Yellow left border with Hero animation
            Hero(
              tag: 'task_indicator_${task.id}',
              child: Material(
                color: Colors.transparent,
                child: Container(
                  width: 4,
                  decoration: const BoxDecoration(
                    color: Color(0xFFFDB022),
                    borderRadius: BorderRadius.only(
                      topLeft: Radius.circular(12),
                      bottomLeft: Radius.circular(12),
                    ),
                  ),
                ),
              ),
            ),
            // Content
            Expanded(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Title with Hero animation
                    Hero(
                      tag: 'task_title_${task.id}',
                      child: Material(
                        color: Colors.transparent,
                        child: Text(
                          task.title,
                          style: const TextStyle(
                            fontSize: 22,
                            fontWeight: FontWeight.bold,
                            color: Color(0xFF1A1A1A),
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(height: 8),
                    // Status and Priority
                    Row(
                      children: [
                        Hero(
                          tag: 'task_status_${task.id}',
                          child: _buildStatusBadge(task.status),
                        ),
                        const SizedBox(width: 8),
                        const Icon(
                          Icons.edit,
                          size: 16,
                          color: Color(0xFF6B7280),
                        ),
                        const SizedBox(width: 4),
                        Text(
                          '${_formatPriority(task.priority)} Priority',
                          style: const TextStyle(
                            fontSize: 14,
                            color: Color(0xFF6B7280),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatusBadge(TaskStatus status) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: const Color(0xFFE8F5E9),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        _formatStatus(status),
        style: const TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.w500,
          color: Color(0xFF2E7D32),
        ),
      ),
    );
  }

  Widget _buildDescriptionCard(TaskDto task) {
    return Card(
      color: Colors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      elevation: 1,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Description',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Color(0xFF1A1A1A),
              ),
            ),
            const SizedBox(height: 8),
            Hero(
              tag: 'task_description_${task.id}',
              child: Material(
                color: Colors.transparent,
                child: Text(
                  task.description,
                  style: const TextStyle(
                    fontSize: 14,
                    color: Color(0xFF6B7280),
                    height: 1.5,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTaskInformationCard(TaskDto task) {
    return Card(
      color: Colors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      elevation: 1,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Task Information',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Color(0xFF1A1A1A),
              ),
            ),
            const SizedBox(height: 16),
            _buildInfoRow(
              icon: Icons.calendar_today_outlined,
              label: 'Due Date',
              value: task.dueDate != null
                  ? DateFormat('MMM d, y').format(task.dueDate!)
                  : 'No due date',
            ),
            const SizedBox(height: 12),
            _buildInfoRow(
              icon: Icons.edit_outlined,
              label: 'Status',
              value: _formatStatus(task.status),
            ),
            const SizedBox(height: 12),
            _buildInfoRow(
              icon: Icons.flag_outlined,
              label: 'Priority',
              value: _formatPriority(task.priority),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDatesCard(TaskDto task) {
    return Card(
      color: Colors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      elevation: 1,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Dates',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Color(0xFF1A1A1A),
              ),
            ),
            const SizedBox(height: 16),
            _buildDateRow(
              icon: Icons.add,
              label: 'Created',
              value: task.createdAt != null
                  ? DateFormat('MMM d, y').format(task.createdAt!)
                  : 'N/A',
            ),
            const SizedBox(height: 12),
            _buildDateRow(
              icon: Icons.date_range,
              label: 'Last Updated',
              value: task.updatedAt != null
                  ? DateFormat('MMM d, y').format(task.updatedAt!)
                  : task.createdAt != null
                      ? DateFormat('MMM d, y').format(task.createdAt!)
                      : 'N/A',
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow({
    required IconData icon,
    required String label,
    required String value,
  }) {
    return Row(
      children: [
        Icon(
          icon,
          size: 20,
          color: const Color(0xFF9CA3AF),
        ),
        const SizedBox(width: 12),
        SizedBox(
          width: 80,
          child: Text(
            label,
            style: const TextStyle(
              fontSize: 14,
              color: Color(0xFF6B7280),
            ),
          ),
        ),
        Text(
          value,
          style: const TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w500,
            color: Color(0xFF1A1A1A),
          ),
        ),
      ],
    );
  }

  Widget _buildDateRow({
    required IconData icon,
    required String label,
    required String value,
  }) {
    return Row(
      children: [
        Icon(
          icon,
          size: 20,
          color: const Color(0xFF7C3AED),
        ),
        const SizedBox(width: 12),
        SizedBox(
          width: 100,
          child: Text(
            label,
            style: const TextStyle(
              fontSize: 14,
              color: Color(0xFF6B7280),
            ),
          ),
        ),
        Text(
          value,
          style: const TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w500,
            color: Color(0xFF1A1A1A),
          ),
        ),
      ],
    );
  }

  String _formatStatus(TaskStatus status) {
    return switch (status) {
      TaskStatus.todo => 'To Do',
      TaskStatus.inProgress => 'In Progress',
      TaskStatus.done => 'Done',
    };
  }

  String _formatPriority(Priority priority) {
    return switch (priority) {
      Priority.high => 'High',
      Priority.medium => 'Medium',
      Priority.low => 'Low',
    };
  }
}

