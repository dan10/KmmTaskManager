import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../view_models/task_detail_viewmodel.dart';

class TaskDetailScreen extends StatefulWidget {
  const TaskDetailScreen({super.key, required this.taskId});
  final String taskId;

  @override
  State<TaskDetailScreen> createState() => _TaskDetailScreenState();
}

class _TaskDetailScreenState extends State<TaskDetailScreen> {
  @override
  void initState() {
    super.initState();
    Provider.of<TaskDetailViewModel>(context, listen: false).load.execute(widget.taskId);
  }

  @override
  Widget build(BuildContext context) {
    final vm = Provider.of<TaskDetailViewModel>(context);
    final t = vm.state.task;
    return Scaffold(
      appBar: AppBar(
        title: Hero(
          tag: 'task_title_${t?.id ?? widget.taskId}',
          placeholderBuilder: (context, size, child) => Material(
            type: MaterialType.transparency,
            child: Text(
              t?.title ?? 'Task',
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ),
          child: Material(
            type: MaterialType.transparency,
            child: Text(
              t?.title ?? 'Task',
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ),
        ),
      ),
      body: t == null
          ? Stack(
              children: [
                Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: IntrinsicHeight(
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Hero(
                          tag: 'task_indicator_${widget.taskId}',
                          child: Container(
                            width: 4,
                            decoration: const BoxDecoration(
                              gradient: LinearGradient(
                                colors: [Color(0xFF9CA3AF), Color(0xFF9CA3AF)],
                                begin: Alignment.topCenter,
                                end: Alignment.bottomCenter,
                              ),
                              borderRadius: BorderRadius.only(
                                topLeft: Radius.circular(8),
                                bottomLeft: Radius.circular(8),
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Row(
                            children: [
                              Expanded(
                                child: Text(
                                  'Task',
                                  style: Theme.of(context).textTheme.headlineSmall,
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                              const SizedBox(width: 8),
                              Hero(
                                tag: 'task_status_${widget.taskId}',
                                child: Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFF9CA3AF).withOpacity(0.12),
                                    borderRadius: BorderRadius.circular(4),
                                  ),
                                  child: Text(
                                    '...',
                                    style: Theme.of(context).textTheme.labelSmall?.copyWith(
                                      color: const Color(0xFF9CA3AF),
                                    ),
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const Center(child: CircularProgressIndicator()),
              ],
            )
          : Padding(
              padding: const EdgeInsets.all(16.0),
              child: IntrinsicHeight(
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Hero(
                      tag: 'task_indicator_${t.id}',
                      child: Container(
                        width: 4,
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            colors: [
                              ((t.dueDate != null && t.status != TaskStatus.done && t.dueDate!.isBefore(DateTime.now())) ? const Color(0xFFEF4444) : (t.priority == Priority.high ? const Color(0xFFEF4444) : t.priority == Priority.medium ? const Color(0xFFF59E0B) : const Color(0xFF10B981))).withOpacity(0.95),
                              ((t.dueDate != null && t.status != TaskStatus.done && t.dueDate!.isBefore(DateTime.now())) ? const Color(0xFFEF4444) : (t.priority == Priority.high ? const Color(0xFFEF4444) : t.priority == Priority.medium ? const Color(0xFFF59E0B) : const Color(0xFF10B981))).withOpacity(0.65),
                            ],
                            begin: Alignment.topCenter,
                            end: Alignment.bottomCenter,
                          ),
                          borderRadius: const BorderRadius.only(
                            topLeft: Radius.circular(8),
                            bottomLeft: Radius.circular(8),
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Expanded(
                                child: Text(
                                  t.title,
                                  style: Theme.of(context).textTheme.headlineSmall,
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                              const SizedBox(width: 8),
                              Hero(
                                tag: 'task_status_${t.id}',
                                child: Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                                  decoration: BoxDecoration(
                                    color: (t.status == TaskStatus.todo ? const Color(0xFF6B7280) : t.status == TaskStatus.inProgress ? const Color(0xFF3B82F6) : const Color(0xFF10B981)).withOpacity(0.12),
                                    borderRadius: BorderRadius.circular(4),
                                  ),
                                  child: Text(
                                    t.status == TaskStatus.todo ? 'To Do' : t.status == TaskStatus.inProgress ? 'In Progress' : 'Done',
                                    style: Theme.of(context).textTheme.labelSmall?.copyWith(
                                      color: (t.status == TaskStatus.todo ? const Color(0xFF6B7280) : t.status == TaskStatus.inProgress ? const Color(0xFF3B82F6) : const Color(0xFF10B981)),
                                    ),
                                  ),
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 8),
                          Text(t.description),
                          const SizedBox(height: 16),
                          Wrap(spacing: 8, children: [
                            ElevatedButton(
                              onPressed: () => vm.changeStatus.execute((t.id, TaskStatus.todo)),
                              child: const Text('To Do'),
                            ),
                            ElevatedButton(
                              onPressed: () => vm.changeStatus.execute((t.id, TaskStatus.inProgress)),
                              child: const Text('In Progress'),
                            ),
                            ElevatedButton(
                              onPressed: () => vm.changeStatus.execute((t.id, TaskStatus.done)),
                              child: const Text('Done'),
                            ),
                          ]),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
    );
  }
}


