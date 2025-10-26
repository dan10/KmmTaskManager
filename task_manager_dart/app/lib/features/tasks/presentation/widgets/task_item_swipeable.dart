import 'package:flutter/material.dart';
import 'package:task_manager_shared/models.dart';

import 'task_item_widget.dart';

/// Swipeable wrapper for TaskItemWidget
/// 
/// Matches KMM's TaskItemWithSwipe behavior:
/// - Swipe left to right: Mark as done/undone (green background)
/// - Swipe right to left: Delete task (red background)
class TaskItemSwipeable extends StatefulWidget {
  const TaskItemSwipeable({
    super.key,
    required this.task,
    required this.onTap,
    required this.onStatusChanged,
    required this.onDelete,
  });

  final TaskDto task;
  final VoidCallback onTap;
  final void Function(TaskStatus status) onStatusChanged;
  final VoidCallback onDelete;

  @override
  State<TaskItemSwipeable> createState() => _TaskItemSwipeableState();
}

class _TaskItemSwipeableState extends State<TaskItemSwipeable> {
  // Reset key to force rebuild and reset swipe state when task changes
  Key _itemKey = UniqueKey();

  @override
  void didUpdateWidget(TaskItemSwipeable oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Reset swipe state when task ID or status changes
    if (oldWidget.task.id != widget.task.id || 
        oldWidget.task.status != widget.task.status) {
      setState(() {
        _itemKey = UniqueKey();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dismissible(
      key: _itemKey,
      confirmDismiss: (direction) async {
        if (direction == DismissDirection.startToEnd) {
          // Swipe left to right - Toggle completion status
          _handleStatusToggle();
          return false; // Don't actually dismiss
        } else if (direction == DismissDirection.endToStart) {
          // Swipe right to left - Delete task
          return await _showDeleteConfirmation(context);
        }
        return false;
      },
      background: _buildSwipeBackground(isLeftToRight: true),
      secondaryBackground: _buildSwipeBackground(isLeftToRight: false),
      child: TaskItemWidget(
        task: widget.task,
        onTap: widget.onTap,
        onStatusChanged: widget.onStatusChanged,
        onDelete: widget.onDelete,
      ),
    );
  }

  void _handleStatusToggle() {
    final newStatus = widget.task.status == TaskStatus.done 
        ? TaskStatus.todo 
        : TaskStatus.done;
    widget.onStatusChanged(newStatus);
  }

  Future<bool> _showDeleteConfirmation(BuildContext context) async {
    final result = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Task'),
        content: Text('Are you sure you want to delete "${widget.task.title}"?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(true),
            style: FilledButton.styleFrom(
              backgroundColor: Colors.red,
            ),
            child: const Text('Delete'),
          ),
        ],
      ),
    );
    
    if (result == true) {
      widget.onDelete();
    }
    
    return false; // Don't actually dismiss the item
  }

  Widget _buildSwipeBackground({required bool isLeftToRight}) {
    return Container(
      color: isLeftToRight ? const Color(0xFF2E7D32) : const Color(0xFFFFEBEE),
      alignment: isLeftToRight ? Alignment.centerLeft : Alignment.centerRight,
      padding: const EdgeInsets.symmetric(horizontal: 20),
      child: Icon(
        isLeftToRight ? Icons.done : Icons.delete,
        color: isLeftToRight ? Colors.white : const Color(0xFFD32F2F),
        size: 32,
      ),
    );
  }
}

