import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

import '../../viewmodels/project_viewmodel.dart';
import '../../../domain/entities/project.dart';

class ProjectCreateEditView extends StatefulWidget {
  final String? projectId;

  const ProjectCreateEditView({
    super.key,
    this.projectId,
  });

  @override
  State<ProjectCreateEditView> createState() => _ProjectCreateEditViewState();
}

class _ProjectCreateEditViewState extends State<ProjectCreateEditView> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _descriptionController = TextEditingController();

  bool _isLoading = false;
  String? _errorMessage;
  Project? _currentProject;

  bool get _isCreating => widget.projectId == null;

  bool get _isButtonEnabled => _nameController.text.isNotEmpty && !_isLoading;

  @override
  void initState() {
    super.initState();

    if (!_isCreating) {
      _loadProject();
    }

    _nameController.addListener(_updateButtonState);
  }

  @override
  void dispose() {
    _nameController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  void _updateButtonState() {
    setState(() {});
  }

  void _loadProject() async {
    if (widget.projectId == null) return;

    final l10n = AppLocalizations.of(context)!;

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final projectViewModel = Provider.of<ProjectViewModel>(
          context, listen: false);
      final project = projectViewModel.getProject(widget.projectId!);

      if (project != null) {
        setState(() {
          _currentProject = project;
          _nameController.text = project.name;
          _descriptionController.text = project.description ?? '';
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = l10n.projectLoadError(e.toString());
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: _buildAppBar(),
      body: _isLoading && _isCreating
          ? const Center(child: CircularProgressIndicator())
          : _buildBody(),
    );
  }

  PreferredSizeWidget _buildAppBar() {
    final l10n = AppLocalizations.of(context)!;
    
    return AppBar(
      title: Text(_isCreating ? l10n.createProject : l10n.editProject),
      leading: IconButton(
        icon: const Icon(Icons.arrow_back),
        onPressed: () => context.go('/?tab=1'),
      ),
    );
  }

  Widget _buildBody() {
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            if (_errorMessage != null) _buildErrorMessage(),
            Expanded(
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _buildNameField(),
                    const SizedBox(height: 16),
                    _buildDescriptionField(),
                  ],
                ),
              ),
            ),
            _buildActionButtons(),
          ],
        ),
      ),
    );
  }

  Widget _buildErrorMessage() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(12),
      margin: const EdgeInsets.only(bottom: 16),
      decoration: BoxDecoration(
        color: Theme
            .of(context)
            .colorScheme
            .errorContainer,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        _errorMessage!,
        style: TextStyle(
          color: Theme
              .of(context)
              .colorScheme
              .onErrorContainer,
          fontSize: 14,
        ),
      ),
    );
  }

  Widget _buildNameField() {
    final l10n = AppLocalizations.of(context)!;
    
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          l10n.projectNameLabel,
          style: Theme
              .of(context)
              .textTheme
              .bodySmall
              ?.copyWith(
            color: Theme
                .of(context)
                .colorScheme
                .onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 4),
        TextFormField(
          controller: _nameController,
          enabled: !_isLoading,
          decoration: InputDecoration(
            hintText: l10n.projectNamePlaceholder,
            filled: true,
            fillColor: Theme
                .of(context)
                .colorScheme
                .surface,
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
              borderSide: BorderSide.none,
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
              borderSide: BorderSide(
                color: Theme
                    .of(context)
                    .colorScheme
                    .primary,
                width: 2,
              ),
            ),
            errorBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
              borderSide: BorderSide(
                color: Theme
                    .of(context)
                    .colorScheme
                    .error,
                width: 2,
              ),
            ),
          ),
          validator: (value) {
            if (value == null || value
                .trim()
                .isEmpty) {
              return l10n.projectNameError;
            }
            return null;
          },
          maxLines: 1,
        ),
      ],
    );
  }

  Widget _buildDescriptionField() {
    final l10n = AppLocalizations.of(context)!;
    
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          l10n.projectDescriptionLabel,
          style: Theme
              .of(context)
              .textTheme
              .bodySmall
              ?.copyWith(
            color: Theme
                .of(context)
                .colorScheme
                .onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 4),
        Container(
          height: 120,
          child: TextFormField(
            controller: _descriptionController,
            enabled: !_isLoading,
            decoration: InputDecoration(
              hintText: l10n.projectDescriptionPlaceholder,
              filled: true,
              fillColor: Theme
                  .of(context)
                  .colorScheme
                  .surface,
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
                borderSide: BorderSide.none,
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
                borderSide: BorderSide(
                  color: Theme
                      .of(context)
                      .colorScheme
                      .primary,
                  width: 2,
                ),
              ),
            ),
            maxLines: null,
            expands: true,
            textAlignVertical: TextAlignVertical.top,
          ),
        ),
      ],
    );
  }

  Widget _buildActionButtons() {
    final l10n = AppLocalizations.of(context)!;
    
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: Row(
        children: [
          Expanded(
            child: OutlinedButton(
              onPressed: _isLoading ? null : () => context.go('/?tab=1'),
              style: OutlinedButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
                side: BorderSide(
                  color: Theme
                      .of(context)
                      .colorScheme
                      .primary,
                ),
              ),
              child: Text(l10n.projectCancelButton),
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: ElevatedButton(
              onPressed: _isButtonEnabled ? _handleCreateOrUpdate : null,
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
                backgroundColor: Theme
                    .of(context)
                    .colorScheme
                    .primary,
              ),
              child: _isLoading
                  ? const SizedBox(
                height: 20,
                width: 20,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                ),
              )
                  : Text(
                _isCreating ? l10n.projectCreateButton : l10n
                    .projectUpdateButton,
                style: const TextStyle(color: Colors.white),
              ),
            ),
          ),
        ],
      ),
    );
  }

  void _handleCreateOrUpdate() async {
    final l10n = AppLocalizations.of(context)!;
    
    if (!_formKey.currentState!.validate()) {
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final projectViewModel = Provider.of<ProjectViewModel>(
          context, listen: false);

      if (_isCreating) {
        await projectViewModel.createProject(
          _nameController.text.trim(),
          _descriptionController.text
              .trim()
              .isEmpty ? null : _descriptionController.text.trim(),
        );
      } else {
        await projectViewModel.updateProject(
          widget.projectId!,
          _nameController.text.trim(),
          _descriptionController.text
              .trim()
              .isEmpty ? null : _descriptionController.text.trim(),
        );
      }

      if (mounted) {
        context.go('/?tab=1');
      }
    } catch (e) {
      setState(() {
        _errorMessage =
        _isCreating ? l10n.projectCreateError(e.toString()) : l10n
            .projectUpdateError(e.toString());
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }
} 