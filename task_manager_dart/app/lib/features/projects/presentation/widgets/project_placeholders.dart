import 'package:flutter/material.dart';
import '../../../../core/ui/components/shimmer.dart';

/// Skeleton loading placeholder for project items
class ProjectItemSkeleton extends StatelessWidget {
  const ProjectItemSkeleton({super.key});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Project name skeleton
            ShimmerLoading(
              isLoading: true,
              child: Container(
                height: 24,
                width: double.infinity,
                decoration: BoxDecoration(
                  color: Colors.black,
                  borderRadius: BorderRadius.circular(4),
                ),
              ),
            ),
            
            const SizedBox(height: 8),
            
            // Description skeleton
            ShimmerLoading(
              isLoading: true,
              child: Container(
                height: 16,
                width: double.infinity * 0.7,
                decoration: BoxDecoration(
                  color: Colors.black,
                  borderRadius: BorderRadius.circular(4),
                ),
              ),
            ),
            
            const SizedBox(height: 16),
            
            // Progress text skeleton
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                ShimmerLoading(
                  isLoading: true,
                  child: Container(
                    height: 14,
                    width: 100,
                    decoration: BoxDecoration(
                      color: Colors.black,
                      borderRadius: BorderRadius.circular(4),
                    ),
                  ),
                ),
                ShimmerLoading(
                  isLoading: true,
                  child: Container(
                    height: 14,
                    width: 40,
                    decoration: BoxDecoration(
                      color: Colors.black,
                      borderRadius: BorderRadius.circular(4),
                    ),
                  ),
                ),
              ],
            ),
            
            const SizedBox(height: 8),
            
            // Progress bar skeleton
            ShimmerLoading(
              isLoading: true,
              child: Container(
                height: 8,
                width: double.infinity,
                decoration: BoxDecoration(
                  color: Colors.black,
                  borderRadius: BorderRadius.circular(4),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

