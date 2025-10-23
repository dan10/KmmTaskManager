import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../view_models/project_create_edit_viewmodel.dart';

class ProjectCreateEditScreen extends StatefulWidget {
  const ProjectCreateEditScreen({super.key, this.projectId});
  final String? projectId;

  @override
  State<ProjectCreateEditScreen> createState() => _ProjectCreateEditScreenState();
}

class _ProjectCreateEditScreenState extends State<ProjectCreateEditScreen> {
  final _name = TextEditingController();
  final _description = TextEditingController();

  @override
  Widget build(BuildContext context) {
    final vm = Provider.of<ProjectCreateEditViewModel>(context);
    return Scaffold(
      appBar: AppBar(title: Text(widget.projectId == null ? 'Create Project' : 'Edit Project')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(controller: _name, decoration: const InputDecoration(labelText: 'Name')),
            const SizedBox(height: 12),
            TextField(controller: _description, decoration: const InputDecoration(labelText: 'Description')),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: () {
                if (widget.projectId == null) {
                  vm.create.execute((_name.text, _description.text.isEmpty ? null : _description.text));
                } else {
                  vm.update.execute((widget.projectId!, _name.text.isEmpty ? null : _name.text, _description.text.isEmpty ? null : _description.text));
                }
              },
              child: Text(widget.projectId == null ? 'Create' : 'Update'),
            )
          ],
        ),
      ),
    );
  }
}


