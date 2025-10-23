import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../view_models/task_list_viewmodel.dart';

class TaskListScreen extends StatefulWidget {
  const TaskListScreen({super.key, this.projectId});

  final String? projectId;

  @override
  State<TaskListScreen> createState() => _TaskListScreenState();
}

class _TaskListScreenState extends State<TaskListScreen> {
  @override
  void initState() {
    super.initState();
    // Trigger initial load when embedded (e.g., in bottom bar)
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final vm = Provider.of<TaskListViewModel>(context, listen: false);
      if (vm.state.items.isEmpty && !vm.load.running && !vm.load.completed) {
        vm.load.execute();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final vm = Provider.of<TaskListViewModel>(context);

    return Column(
      children: [
        Expanded(
          child: ListView.builder(
            itemCount: vm.state.items.length,
            itemBuilder: (_, i) {
              final t = vm.state.items[i];
              return ListTile(
                title: Text(t.title),
                subtitle: Text(t.description),
                trailing: _StatusChip(status: t.status),
              );
            },
          ),
        ),
        if (vm.loadMore.running) const Padding(
          padding: EdgeInsets.all(16.0),
          child: CircularProgressIndicator(),
        ),
      ],
    );
  }
}

class _StatusChip extends StatelessWidget {
  const _StatusChip({required this.status});
  final TaskStatus status;
  @override
  Widget build(BuildContext context) {
    final color = switch (status) {
      TaskStatus.todo => Colors.grey,
      TaskStatus.inProgress => Colors.orange,
      TaskStatus.done => Colors.green,
    };
    return Chip(label: Text(status.name), backgroundColor: color.withValues(alpha: 0.2));
  }
}


