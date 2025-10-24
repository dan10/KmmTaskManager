import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../view_models/project_list_viewmodel.dart';

class ProjectListScreen extends StatelessWidget {
  const ProjectListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final vm = Provider.of<ProjectListViewModel>(context);

    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: const [
            Icon(Icons.folder, size: 28),
            SizedBox(width: 12),
            Text('Projects'),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () {
              // TODO: Navigate to create project
            },
          ),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
              itemCount: vm.state.items.length,
              itemBuilder: (_, i) {
                final p = vm.state.items[i];
                return ListTile(
                  title: Text(p.name),
                  subtitle: Text(p.description ?? ''),
                );
              },
            ),
          ),
          if (vm.loadMore.running) const Padding(
            padding: EdgeInsets.all(16.0),
            child: CircularProgressIndicator(),
          ),
        ],
      ),
      floatingActionButton:  FloatingActionButton(
          heroTag: 'add_fab',
          onPressed: () {
            // TODO: Navigate to create project
          },
          child: const Icon(Icons.add),
      ),
    );
  }
}


