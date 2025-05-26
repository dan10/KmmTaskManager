import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../../viewmodels/project_viewmodel.dart';
import '../../../domain/entities/project.dart';

class ProjectListView extends StatefulWidget {
  const ProjectListView({super.key});

  @override
  State<ProjectListView> createState() => _ProjectListViewState();
}

class _ProjectListViewState extends State<ProjectListView> {
  final _searchController = TextEditingController();

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    
    return Scaffold(
      backgroundColor: const Color(0xFFF1F5F9), // Light gray background to match Compose
      floatingActionButton: FloatingActionButton(
        onPressed: () => context.go('/project/create'),
        child: const Icon(Icons.add),
      ),
      body: SafeArea(
        child: Consumer<ProjectViewModel>(
          builder: (context, projectViewModel, child) {
            return Column(
              children: [
                // Header Section
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Title
                      Text(
                        l10n.projectsTitle,
                        style: Theme
                            .of(context)
                            .textTheme
                            .headlineMedium
                            ?.copyWith(
                          fontWeight: FontWeight.bold,
                          color: Theme
                              .of(context)
                              .colorScheme
                              .onSurface,
                        ),
                      ),
                      const SizedBox(height: 16),

                      // Search Field
                      TextField(
                        controller: _searchController,
                        decoration: InputDecoration(
                          labelText: l10n.projectsSearchPlaceholder,
                          prefixIcon: const Icon(Icons.search),
                          filled: true,
                          fillColor: Theme
                              .of(context)
                              .colorScheme
                              .surface,
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(16),
                            borderSide: BorderSide.none,
                          ),
                        ),
                        onChanged: (value) {
                          // TODO: Implement search functionality
                        },
                      ),
                      const SizedBox(height: 16),

                      // All Projects subtitle
                      Text(
                        l10n.projectsAll,
                        style: Theme
                            .of(context)
                            .textTheme
                            .titleMedium
                            ?.copyWith(
                          fontWeight: FontWeight.w500,
                          color: Theme
                              .of(context)
                              .colorScheme
                              .onSurface,
                        ),
                      ),
                    ],
                  ),
                ),

                // Projects List
                Expanded(
                  child: projectViewModel.projects.isEmpty
                      ? _buildEmptyState(l10n)
                      : RefreshIndicator(
                    onRefresh: () async {
                      // TODO: Implement refresh
                    },
                    child: ListView.builder(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      itemCount: projectViewModel.projects.length,
                      itemBuilder: (context, index) {
                        final project = projectViewModel.projects[index];
                        return _buildProjectCard(context, project, l10n);
                      },
                    ),
                  ),
                ),
              ],
            );
          },
        ),
      ),
    );
  }

  Widget _buildEmptyState(AppLocalizations l10n) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.folder_outlined,
            size: 64,
            color: Theme
                .of(context)
                .colorScheme
                .onSurface
                .withValues(alpha: 0.5),
          ),
          const SizedBox(height: 16),
          Text(
            l10n.projectsEmptyTitle,
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
              color: Theme
                  .of(context)
                  .colorScheme
                  .onSurface
                  .withValues(alpha: 0.7),
            ),
          ),
          const SizedBox(height: 8),
          Text(
            l10n.projectsEmptySubtitle,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: Theme
                  .of(context)
                  .colorScheme
                  .onSurface
                  .withValues(alpha: 0.5),
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 24),
          ElevatedButton.icon(
            onPressed: () => context.go('/project/create'),
            icon: const Icon(Icons.add),
            label: Text(l10n.projectsAdd),
          ),
        ],
      ),
    );
  }

  Widget _buildProjectCard(BuildContext context, Project project,
      AppLocalizations l10n) {
    // Generate a consistent random color based on project name
    final colorSeed = project.name.hashCode;
    final colors = [
      Colors.red,
      Colors.blue,
      Colors.green,
      Colors.orange,
      Colors.purple,
      Colors.teal,
      Colors.pink,
      Colors.indigo,
    ];
    final projectColor = colors[colorSeed.abs() % colors.length];

    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8),
      ),
      elevation: 4,
      child: InkWell(
        borderRadius: BorderRadius.circular(8),
        onTap: () => context.push('/projects/${project.id}'),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header with icon and title
              Row(
                children: [
                  // Project Icon with colored background
                  Container(
                    width: 36,
                    height: 36,
                    decoration: BoxDecoration(
                      color: projectColor.withOpacity(0.15),
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Icon(
                      Icons.folder,
                      color: projectColor,
                      size: 24,
                    ),
                  ),
                  const SizedBox(width: 8),

                  // Project Name
                  Expanded(
                    child: Text(
                      project.name,
                      style: Theme
                          .of(context)
                          .textTheme
                          .titleMedium
                          ?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              
              // Progress Bar
              LinearProgressIndicator(
                value: project.total > 0
                    ? project.completed / project.total
                    : 0.0,
                backgroundColor: Theme
                    .of(context)
                    .colorScheme
                    .surfaceVariant,
                valueColor: AlwaysStoppedAnimation<Color>(projectColor),
                borderRadius: BorderRadius.circular(4),
              ),
              const SizedBox(height: 8),

              // Stats Row
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    l10n.projectCompleted(project.completed),
                    style: Theme
                        .of(context)
                        .textTheme
                        .bodySmall,
                  ),
                  Text(
                    l10n.projectInProgress(project.inProgress),
                    style: Theme
                        .of(context)
                        .textTheme
                        .bodySmall,
                  ),
                  Text(
                    l10n.projectTotal(project.total),
                    style: Theme
                        .of(context)
                        .textTheme
                        .bodySmall,
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _showCreateProjectDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => _ProjectDialog(),
    );
  }

  void _showEditProjectDialog(BuildContext context, Project project) {
    showDialog(
      context: context,
      builder: (context) => _ProjectDialog(project: project),
    );
  }

  void _showDeleteConfirmation(BuildContext context, Project project) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Project'),
        content: Text('Are you sure you want to delete "${project.name}"?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            onPressed: () {
              Provider.of<ProjectViewModel>(context, listen: false)
                  .deleteProject(project.id);
              Navigator.of(context).pop();
            },
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }
}

class _ProjectDialog extends StatefulWidget {
  final Project? project;

  const _ProjectDialog({this.project});

  @override
  State<_ProjectDialog> createState() => _ProjectDialogState();
}

class _ProjectDialogState extends State<_ProjectDialog> {
  late final TextEditingController _nameController;
  late final TextEditingController _descriptionController;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.project?.name ?? '');
    _descriptionController = TextEditingController(text: widget.project?.description ?? '');
  }

  @override
  void dispose() {
    _nameController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.project != null;

    return AlertDialog(
      title: Text(isEditing ? 'Edit Project' : 'Create Project'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextField(
            controller: _nameController,
            decoration: const InputDecoration(
              labelText: 'Project Name',
              hintText: 'Enter project name',
            ),
          ),
          const SizedBox(height: 16),
          TextField(
            controller: _descriptionController,
            decoration: const InputDecoration(
              labelText: 'Description (Optional)',
              hintText: 'Enter project description',
            ),
            maxLines: 3,
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancel'),
        ),
        ElevatedButton(
          onPressed: () {
            if (_nameController.text.isNotEmpty) {
              final projectViewModel = Provider.of<ProjectViewModel>(context, listen: false);
              
              if (isEditing) {
                projectViewModel.updateProject(
                  widget.project!.id,
                  _nameController.text,
                  _descriptionController.text.isEmpty ? null : _descriptionController.text,
                );
              } else {
                projectViewModel.createProject(
                  _nameController.text,
                  _descriptionController.text.isEmpty ? null : _descriptionController.text,
                );
              }
              
              Navigator.of(context).pop();
            }
          },
          child: Text(isEditing ? 'Update' : 'Create'),
        ),
      ],
    );
  }
} 