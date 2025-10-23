import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../view_models/project_detail_viewmodel.dart';

class ProjectDetailScreen extends StatefulWidget {
  const ProjectDetailScreen({super.key, required this.projectId});
  final String projectId;

  @override
  State<ProjectDetailScreen> createState() => _ProjectDetailScreenState();
}

class _ProjectDetailScreenState extends State<ProjectDetailScreen> {
  @override
  void initState() {
    super.initState();
    final vm = Provider.of<ProjectDetailViewModel>(context, listen: false);
    vm.load.execute(widget.projectId);
  }

  @override
  Widget build(BuildContext context) {
    final vm = Provider.of<ProjectDetailViewModel>(context);
    final p = vm.state.project;
    return Scaffold(
      appBar: AppBar(title: Text(p?.name ?? 'Project')),
      body: p == null
          ? const Center(child: CircularProgressIndicator())
          : Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(p.name, style: Theme.of(context).textTheme.headlineSmall),
                  const SizedBox(height: 8),
                  Text(p.description ?? ''),
                  const Spacer(),
                  Row(
                    children: [
                      ElevatedButton(
                        onPressed: () => context.go('/project/${p.id}/edit'),
                        child: const Text('Edit'),
                      ),
                      const SizedBox(width: 12),
                      ElevatedButton(
                        onPressed: () => vm.delete.execute(p.id),
                        child: const Text('Delete'),
                      ),
                    ],
                  )
                ],
              ),
            ),
    );
  }
}


