import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../../core/l10n/app_l10n.dart';
import '../../data/repositories/project_repository.dart';
import '../../view_models/project_create_edit_viewmodel.dart';

/// Bottom sheet for creating a new project
/// 
/// Matches the TaskCreateBottomSheet behavior:
/// - Draggable sheet that opens to 60% height
/// - Form fields for name and description
/// - Cancel and Create buttons
/// - Dismisses and optionally triggers a refresh callback
class ProjectCreateBottomSheet extends StatefulWidget {
  const ProjectCreateBottomSheet({
    super.key,
    required this.onDismiss,
  });

  /// Called when the sheet is dismissed
  /// [shouldRefresh] is true if a project was created and the list should refresh
  final void Function(bool shouldRefresh) onDismiss;

  @override
  State<ProjectCreateBottomSheet> createState() => _ProjectCreateBottomSheetState();
}

class _ProjectCreateBottomSheetState extends State<ProjectCreateBottomSheet> {
  late ProjectCreateEditViewModel _viewModel;
  final _nameController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  bool _isValid = false;

  @override
  void initState() {
    super.initState();
    
    // Get repository and create ViewModel
    final repository = context.read<ProjectRepository>();
    _viewModel = ProjectCreateEditViewModel(repository: repository);
    
    // Add listener for command completion
    _viewModel.create.addListener(_onCreateResult);
    
    // Sync controllers with validation
    _nameController.addListener(_validateForm);
    _descriptionController.addListener(_validateForm);
  }

  @override
  void dispose() {
    _viewModel.create.removeListener(_onCreateResult);
    _nameController.dispose();
    _descriptionController.dispose();
    _viewModel.dispose();
    super.dispose();
  }

  void _validateForm() {
    setState(() {
      _isValid = _nameController.text.trim().isNotEmpty;
    });
  }

  void _onCreateResult() {
    if (_viewModel.create.completed) {
      // Success - dismiss and refresh
      if (mounted) {
        final l10n = AppLocalizations.of(context)!;
        Navigator.of(context).pop();
        widget.onDismiss(true);
        
        // Show success message
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(l10n.projectCreatedSuccess),
            backgroundColor: Colors.green,
          ),
        );
      }
    } else if (_viewModel.create.error) {
      // Error - show snackbar
      if (mounted) {
        final l10n = AppLocalizations.of(context)!;
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(l10n.projectCreatedError),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  void _handleCreate() {
    if (_formKey.currentState?.validate() ?? false) {
      final name = _nameController.text.trim();
      final description = _descriptionController.text.trim();
      
      _viewModel.create.execute((
        name,
        description.isEmpty ? null : description,
      ));
    }
  }

  void _handleCancel() {
    Navigator.of(context).pop();
    widget.onDismiss(false);
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);
    
    return DraggableScrollableSheet(
      initialChildSize: 0.6,
      minChildSize: 0.4,
      maxChildSize: 0.9,
      expand: false,
      builder: (context, scrollController) {
        return Container(
          decoration: BoxDecoration(
            color: theme.scaffoldBackgroundColor,
            borderRadius: const BorderRadius.vertical(
              top: Radius.circular(20),
            ),
          ),
          child: Column(
            children: [
              // Drag handle
              Container(
                margin: const EdgeInsets.symmetric(vertical: 12),
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: theme.dividerColor,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              
              // Header
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      l10n.projectCreateTitle,
                      style: theme.textTheme.headlineSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.close),
                      onPressed: _handleCancel,
                    ),
                  ],
                ),
              ),
              
              const Divider(height: 1),
              
              // Form content
              Expanded(
                child: SingleChildScrollView(
                  controller: scrollController,
                  padding: const EdgeInsets.all(20),
                  child: Form(
                    key: _formKey,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        // Project name field
                        TextFormField(
                          controller: _nameController,
                          decoration: InputDecoration(
                            labelText: l10n.projectNameLabel,
                            hintText: l10n.projectNameHint,
                            border: const OutlineInputBorder(),
                            prefixIcon: const Icon(Icons.folder),
                          ),
                          validator: (value) {
                            if (value == null || value.trim().isEmpty) {
                              return l10n.projectNameRequired;
                            }
                            return null;
                          },
                          textCapitalization: TextCapitalization.words,
                          autofocus: true,
                        ),
                        
                        const SizedBox(height: 16),
                        
                        // Description field
                        TextFormField(
                          controller: _descriptionController,
                          decoration: InputDecoration(
                            labelText: l10n.projectDescriptionLabel,
                            hintText: l10n.projectDescriptionHint,
                            border: const OutlineInputBorder(),
                            prefixIcon: const Icon(Icons.description),
                          ),
                          maxLines: 3,
                          textCapitalization: TextCapitalization.sentences,
                        ),
                      ],
                    ),
                  ),
                ),
              ),
              
              // Action buttons
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: theme.scaffoldBackgroundColor,
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.05),
                      blurRadius: 10,
                      offset: const Offset(0, -5),
                    ),
                  ],
                ),
                child: Row(
                  children: [
                    // Cancel button
                    Expanded(
                      child: OutlinedButton(
                        onPressed: _viewModel.create.running ? null : _handleCancel,
                        style: OutlinedButton.styleFrom(
                          padding: const EdgeInsets.symmetric(vertical: 16),
                        ),
                        child: Text(l10n.commonCancel),
                      ),
                    ),
                    
                    const SizedBox(width: 12),
                    
                    // Create button
                    Expanded(
                      flex: 2,
                      child: ElevatedButton(
                        onPressed: (_isValid && !_viewModel.create.running)
                            ? _handleCreate
                            : null,
                        style: ElevatedButton.styleFrom(
                          padding: const EdgeInsets.symmetric(vertical: 16),
                          backgroundColor: theme.colorScheme.primary,
                          foregroundColor: theme.colorScheme.onPrimary,
                        ),
                        child: _viewModel.create.running
                            ? SizedBox(
                                height: 20,
                                width: 20,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                  valueColor: AlwaysStoppedAnimation<Color>(
                                    theme.colorScheme.onPrimary,
                                  ),
                                ),
                              )
                            : Text(l10n.projectCreateTitle),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

/// Helper function to show the project create bottom sheet
void showProjectCreateBottomSheet({
  required BuildContext context,
  required void Function(bool shouldRefresh) onDismiss,
}) {
  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.transparent,
    builder: (context) => ProjectCreateBottomSheet(
      onDismiss: onDismiss,
    ),
  );
}

