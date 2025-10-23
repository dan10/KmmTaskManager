import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../view_models/task_create_edit_viewmodel.dart';

class TaskCreateEditScreen extends StatefulWidget {
  const TaskCreateEditScreen({super.key, this.taskId, this.projectId});
  final String? taskId;
  final String? projectId;

  @override
  State<TaskCreateEditScreen> createState() => _TaskCreateEditScreenState();
}

class _TaskCreateEditScreenState extends State<TaskCreateEditScreen> {
  final _title = TextEditingController();
  final _description = TextEditingController();
  Priority _priority = Priority.medium;

  @override
  Widget build(BuildContext context) {
    final vm = Provider.of<TaskCreateEditViewModel>(context);
    return Scaffold(
      appBar: AppBar(title: Text(widget.taskId == null ? 'Create Task' : 'Edit Task')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(controller: _title, decoration: const InputDecoration(labelText: 'Title')),
            const SizedBox(height: 12),
            TextField(controller: _description, decoration: const InputDecoration(labelText: 'Description')),
            const SizedBox(height: 12),
            DropdownButton<Priority>(
              value: _priority,
              items: const [
                DropdownMenuItem(value: Priority.low, child: Text('Low')),
                DropdownMenuItem(value: Priority.medium, child: Text('Medium')),
                DropdownMenuItem(value: Priority.high, child: Text('High')),
              ],
              onChanged: (p) => setState(() => _priority = p ?? Priority.medium),
            ),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: () {
                if (widget.taskId == null) {
                  final req = TaskCreateRequestDto(
                    title: _title.text,
                    description: _description.text,
                    priority: _priority,
                    projectId: widget.projectId,
                  );
                  vm.create.execute(req);
                } else {
                  final req = TaskUpdateRequestDto(
                    title: _title.text.isEmpty ? null : _title.text,
                    description: _description.text.isEmpty ? null : _description.text,
                    priority: _priority,
                  );
                  vm.update.execute((widget.taskId!, req));
                }
              },
              child: Text(widget.taskId == null ? 'Create' : 'Update'),
            ),
          ],
        ),
      ),
    );
  }
}


