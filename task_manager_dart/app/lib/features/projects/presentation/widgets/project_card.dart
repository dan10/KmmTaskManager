import 'package:flutter/material.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/theme/theme.dart';

/// Project card widget that displays project information
/// Matches the design from the Kotlin version
class ProjectCard extends StatelessWidget {
  final Project project;
  final VoidCallback onTap;

  const ProjectCard({
    super.key,
    required this.project,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final progressPercentage = project.total > 0
        ? ((project.completed / project.total) * 100).toInt()
        : 0;

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Builder(
                builder: (context) {
                  final ext = context.extColors;
                  return Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Project name
                      Text(
                        project.name,
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                              fontWeight: FontWeight.bold,
                              color: ext.textPrimary,
                            ),
                      ),
                      
                      // Project description (if available)
                      if (project.description != null && project.description!.isNotEmpty) ...[
                        const SizedBox(height: 4),
                        Text(
                          project.description!,
                          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                color: ext.textSecondary,
                              ),
                        ),
                      ],
                      
                      const SizedBox(height: 12),
                      
                      // Progress text and percentage
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            '${project.completed} of ${project.total} tasks',
                            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                  color: ext.textSecondary,
                                ),
                          ),
                          Text(
                            '$progressPercentage%',
                            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                  fontWeight: FontWeight.w600,
                                  color: ext.textPrimary,
                                ),
                          ),
                        ],
                      ),
                      
                      const SizedBox(height: 8),
                      
                      // Progress bar
                      ClipRRect(
                        borderRadius: BorderRadius.circular(4),
                        child: LinearProgressIndicator(
                          value: project.total > 0 ? project.completed / project.total : 0,
                          minHeight: 8,
                          backgroundColor: ext.trackNeutral,
                          valueColor: AlwaysStoppedAnimation<Color>(
                            Theme.of(context).colorScheme.primary,
                          ),
                        ),
                      ),
                    ],
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}

