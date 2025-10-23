import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../view_models/project_list_viewmodel.dart';

class ProjectListScreen extends StatelessWidget {
  const ProjectListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final vm = Provider.of<ProjectListViewModel>(context);

    return Scaffold(
      appBar: AppBar(title: const Text('Projects')),
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
      floatingActionButton: FloatingActionButton(
        onPressed: () => vm.loadMore.execute(),
        child: const Icon(Icons.more_horiz),
      ),
    );
  }
}


