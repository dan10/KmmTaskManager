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
      appBar: AppBar(title: Text(t?.title ?? 'Task')),
      body: t == null
          ? const Center(child: CircularProgressIndicator())
          : Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(t.title, style: Theme.of(context).textTheme.headlineSmall),
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
    );
  }
}


