import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';

import '../../viewmodels/auth_viewmodel.dart';
import '../../viewmodels/project_viewmodel.dart';
import '../../viewmodels/task_viewmodel.dart';

class HomeView extends StatefulWidget {
  const HomeView({super.key});

  @override
  State<HomeView> createState() => _HomeViewState();
}

class _HomeViewState extends State<HomeView> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadData();
    });
  }

  void _loadData() {
    final projectViewModel = Provider.of<ProjectViewModel>(context, listen: false);
    final taskViewModel = Provider.of<TaskViewModel>(context, listen: false);
    
    projectViewModel.loadProjects();
    taskViewModel.loadTasks();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: () async {
            _loadData();
          },
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Welcome Section
                Consumer<AuthViewModel>(
                  builder: (context, authViewModel, child) {
                    return Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Row(
                          children: [
                            const Icon(Icons.waving_hand, size: 32, color: Colors.orange),
                            const SizedBox(width: 16),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    'Welcome back, ${authViewModel.user?.name ?? 'User'}!',
                                    style: Theme.of(context).textTheme.titleLarge,
                                  ),
                                  const SizedBox(height: 4),
                                  Text(
                                    'Ready to manage your tasks?',
                                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                      color: Colors.grey[600],
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 24),

                // Quick Stats
                Consumer<TaskViewModel>(
                  builder: (context, taskViewModel, child) {
                    if (taskViewModel.state == TaskViewState.loaded) {
                      return Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Quick Overview',
                            style: Theme.of(context).textTheme.titleLarge,
                          ),
                          const SizedBox(height: 16),
                          Row(
                            children: [
                              Expanded(
                                child: _buildStatCard(
                                  icon: Icons.task_alt,
                                  title: 'Total Tasks',
                                  value: taskViewModel.totalTasks.toString(),
                                  color: Colors.blue,
                                ),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: _buildStatCard(
                                  icon: Icons.check_circle,
                                  title: 'Completed',
                                  value: taskViewModel.completedTasksCount.toString(),
                                  color: Colors.green,
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 12),
                          Row(
                            children: [
                              Expanded(
                                child: _buildStatCard(
                                  icon: Icons.pending_actions,
                                  title: 'Pending',
                                  value: taskViewModel.pendingTasksCount.toString(),
                                  color: Colors.orange,
                                ),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: _buildStatCard(
                                  icon: Icons.warning,
                                  title: 'Overdue',
                                  value: taskViewModel.overdueTasks.length.toString(),
                                  color: Colors.red,
                                ),
                              ),
                            ],
                          ),
                        ],
                      );
                    }
                    return const SizedBox.shrink();
                  },
                ),
                const SizedBox(height: 24),

                // Quick Actions
                Text(
                  'Quick Actions',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 16),
                Row(
                  children: [
                    Expanded(
                      child: _buildActionCard(
                        icon: Icons.folder,
                        title: 'Projects',
                        subtitle: 'Manage your projects',
                        onTap: () => context.go('/projects'),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: _buildActionCard(
                        icon: Icons.assignment,
                        title: 'All Tasks',
                        subtitle: 'View all tasks',
                        onTap: () => context.go('/tasks'),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 24),

                // Recent Projects
                Consumer<ProjectViewModel>(
                  builder: (context, projectViewModel, child) {
                    if (projectViewModel.isLoading) {
                      return const Center(child: CircularProgressIndicator());
                    }

                    if (projectViewModel.state == ProjectViewState.error) {
                      return Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            children: [
                              const Icon(Icons.error_outline, size: 48, color: Colors.red),
                              const SizedBox(height: 8),
                              Text(projectViewModel.errorMessage ?? 'Error loading projects'),
                              const SizedBox(height: 8),
                              ElevatedButton(
                                onPressed: () => projectViewModel.loadProjects(),
                                child: const Text('Retry'),
                              ),
                            ],
                          ),
                        ),
                      );
                    }

                    if (projectViewModel.hasProjects) {
                      final recentProjects = projectViewModel.projects.take(3).toList();
                      return Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text(
                                'Recent Projects',
                                style: Theme.of(context).textTheme.titleLarge,
                              ),
                              TextButton(
                                onPressed: () => context.go('/projects'),
                                child: const Text('View All'),
                              ),
                            ],
                          ),
                          const SizedBox(height: 16),
                          ...recentProjects.map((project) => Card(
                            margin: const EdgeInsets.only(bottom: 8),
                            child: ListTile(
                              leading: CircleAvatar(
                                backgroundColor: Theme.of(context).primaryColor,
                                child: Text(
                                  project.name.substring(0, 1).toUpperCase(),
                                  style: const TextStyle(color: Colors.white),
                                ),
                              ),
                              title: Text(project.name),
                              subtitle: Text(project.description ?? 'No description'),
                              trailing: Text(
                                '${project.taskCount} tasks',
                                style: Theme.of(context).textTheme.bodySmall,
                              ),
                              onTap: () => context.go('/projects/${project.id}'),
                            ),
                          )),
                        ],
                      );
                    }

                    return Card(
                      child: Padding(
                        padding: const EdgeInsets.all(32),
                        child: Column(
                          children: [
                            const Icon(Icons.folder_open, size: 64, color: Colors.grey),
                            const SizedBox(height: 16),
                            Text(
                              'No Projects Yet',
                              style: Theme.of(context).textTheme.titleMedium,
                            ),
                            const SizedBox(height: 8),
                            const Text('Create your first project to get started'),
                            const SizedBox(height: 16),
                            ElevatedButton(
                              onPressed: () => context.go('/projects'),
                              child: const Text('Create Project'),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildStatCard({
    required IconData icon,
    required String title,
    required String value,
    required Color color,
  }) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Icon(icon, size: 32, color: color),
            const SizedBox(height: 8),
            Text(
              value,
              style: const TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              title,
              style: const TextStyle(fontSize: 12),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildActionCard({
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return Card(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: [
              Icon(icon, size: 32, color: Theme.of(context).primaryColor),
              const SizedBox(height: 8),
              Text(
                title,
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 4),
              Text(
                subtitle,
                style: const TextStyle(fontSize: 12),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
} 