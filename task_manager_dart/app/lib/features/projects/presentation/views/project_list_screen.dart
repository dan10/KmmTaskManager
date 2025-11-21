import 'package:flutter/material.dart';
import '../../../../core/l10n/app_l10n.dart';
import 'package:go_router/go_router.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';
import 'package:provider/provider.dart';
import 'package:task_manager_shared/models.dart';

import '../../../../core/data/local/secure_storage.dart';
import '../../../../core/routing/app_router.dart';
import '../../../../core/testing/test_ids.dart';
import '../../../../core/ui/components/shimmer.dart';
import '../../../../core/ui/components/taskit_top_app_bar.dart';
import '../viewmodels/projects_viewmodel.dart';
import '../widgets/project_card.dart';
import '../widgets/project_list_indicators.dart';
import '../widgets/project_create_bottom_sheet.dart';

/// Project list screen with infinite scroll pagination
class ProjectListScreen extends StatefulWidget {
  const ProjectListScreen({super.key});

  @override
  State<ProjectListScreen> createState() => _ProjectListScreenState();
}

class _ProjectListScreenState extends State<ProjectListScreen> {
  late ProjectsViewModel _viewModel;
  bool _isSearchActive = false;
  String? _userInitials;

  @override
  void initState() {
    super.initState();
    _viewModel = context.read<ProjectsViewModel>();
    
    // Add listeners for commands
    _viewModel.deleteProject.addListener(_onDeleteResult);
    
    // Check and refresh if needed
    _viewModel.checkAndRefresh();
    
    // Load user initials
    _loadUserInitials();
  }
  
  Future<void> _loadUserInitials() async {
    final secureStorage = context.read<SecureStorage>();
    final user = await secureStorage.getUser();
    
    if (user != null && mounted) {
      setState(() {
        _userInitials = _computeInitials(user.displayName);
      });
    }
  }
  
  String _computeInitials(String displayName) {
    if (displayName.isEmpty) return '';
    final parts = displayName.trim().split(' ');
    if (parts.length >= 2) {
      return '${parts[0][0]}${parts[1][0]}'.toUpperCase();
    }
    return displayName[0].toUpperCase();
  }

  @override
  void dispose() {
    _viewModel.deleteProject.removeListener(_onDeleteResult);
    super.dispose();
  }

  void _showSnackBar(String message, {bool isError = false}) {
    if (!mounted) return;
    
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: isError ? Colors.red : Colors.green,
      ),
    );
  }

  void _onDeleteResult() {
    if (_viewModel.deleteProject.completed) {
      _viewModel.deleteProject.clearResult();
      final l10n = AppLocalizations.of(context)!;
      _showSnackBar(l10n.projectDeletedSuccess);
    }

    if (_viewModel.deleteProject.error) {
      _viewModel.deleteProject.clearResult();
      final l10n = AppLocalizations.of(context)!;
      _showSnackBar(l10n.projectDeletedError, isError: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<ProjectsViewModel>();
    final vmState = viewModel.state;
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final shimmerGradientToUse = isDark ? shimmerGradientDark : shimmerGradient;

    return Scaffold(
      resizeToAvoidBottomInset: false,
      body: Column(
        children: [
          // AppBar with search
          _buildAppBar(viewModel),
          // Body content - must be wrapped in Expanded
          Expanded(
            child: Shimmer(
              linearGradient: shimmerGradientToUse,
              child: RefreshIndicator(
                onRefresh: () => _handleRefresh(viewModel),
                child: PagingListener<int, Project>(
                  controller: viewModel.pagingController,
                  builder: (context, pagingState, fetchNextPage) {
                    return Semantics(
                      identifier: TestIds.listProjects,
                      child: PagedListView<int, Project>(
                        state: pagingState,
                        fetchNextPage: fetchNextPage,
                        padding: const EdgeInsets.all(16),
                        builderDelegate: PagedChildBuilderDelegate<Project>(
                          itemBuilder: (context, project, index) => Semantics(
                            identifier: TestIds.projectCard(project.id),
                            child: ProjectCard(
                              key: Key(TestIds.projectCard(project.id)),
                              project: project,
                              onTap: () => _navigateToProjectDetail(context, project.id),
                            ),
                          ),
                          firstPageErrorIndicatorBuilder: (context) =>
                              FirstPageErrorIndicator(
                                errorMessage: vmState.errorMessage,
                                onRetry: () => viewModel.pagingController.refresh(),
                              ),
                          newPageErrorIndicatorBuilder: (context) =>
                              NewPageErrorIndicator(
                                onRetry: () => viewModel.pagingController.refresh(),
                              ),
                          firstPageProgressIndicatorBuilder: (context) =>
                              const FirstPageProgressIndicator(),
                          newPageProgressIndicatorBuilder: (context) =>
                              const NewPageProgressIndicator(),
                          noItemsFoundIndicatorBuilder: (context) =>
                              const NoItemsFoundIndicator(),
                          noMoreItemsIndicatorBuilder: (context) =>
                              const NoMoreItemsIndicator(),
                        ),
                      ),
                    );
                  },
                ),
              ),
            ),
          ),
        ],
      ),
      floatingActionButton: _buildFAB(),
    );
  }

  PreferredSizeWidget _buildAppBar(ProjectsViewModel viewModel) {
    final l10n = AppLocalizations.of(context)!;
    return TaskItTopAppBar(
      title: l10n.projectsTitle,
      searchState: TaskItSearchState(
        query: viewModel.state.searchQuery,
        onQueryChange: (query) {
          viewModel.setSearchQuery(query);
        },
        isActive: _isSearchActive,
        onActiveChange: (active) {
          setState(() {
            _isSearchActive = active;
            if (!active) {
              viewModel.clearSearch();
            }
          });
        },
        placeholder: l10n.projectsSearchPlaceholder,
        enableClearOnClose: true,
      ),
      userInitials: _userInitials,
      onProfileClick: () => context.push(AppRoutes.profile),
    );
  }

  Widget _buildFAB() {
    return Semantics(
      identifier: TestIds.btnAddProject,
      button: true,
      child: FloatingActionButton(
        key: const Key(TestIds.btnAddProject),
        heroTag: 'add_project_fab',
        onPressed: () => _showCreateProjectBottomSheet(context),
        child: const Icon(Icons.add),
      ),
    );
  }

  Future<void> _handleRefresh(ProjectsViewModel viewModel) async {
    viewModel.refresh();
    await Future.delayed(const Duration(milliseconds: 500));
  }

  void _navigateToProjectDetail(BuildContext context, String projectId) {
    context.push(
      AppRoutes.projectDetail.replaceFirst(':projectId', projectId),
    ).then((_) {
      // Refresh list when returning from details
      _viewModel.refresh();
    });
  }

  void _showCreateProjectBottomSheet(BuildContext context) {
    showProjectCreateBottomSheet(
      context: context,
      onDismiss: (shouldRefresh) {
        if (shouldRefresh) {
          _viewModel.refresh();
        }
      },
    );
  }
}

