import 'package:flutter/material.dart';

/// Search state for TaskIt AppBar
class TaskItSearchState {
  final String query;
  final ValueChanged<String> onQueryChange;
  final bool isActive;
  final ValueChanged<bool> onActiveChange;
  final String placeholder;
  final bool enableClearOnClose;

  const TaskItSearchState({
    required this.query,
    required this.onQueryChange,
    required this.isActive,
    required this.onActiveChange,
    required this.placeholder,
    this.enableClearOnClose = true,
  });
}

/// TaskIt TopAppBar - Reusable app bar with optional search below
/// Similar to KMM's PrincipalTaskItTopAppBar
/// Returns a Column with AppBar + AnimatedSearch field
class TaskItTopAppBar extends StatefulWidget implements PreferredSizeWidget {
  final String title;
  final bool showNavigationIcon;
  final VoidCallback? onNavigateBack;
  final TaskItSearchState? searchState;
  final List<Widget> actions;

  const TaskItTopAppBar({
    super.key,
    required this.title,
    this.showNavigationIcon = false,
    this.onNavigateBack,
    this.searchState,
    this.actions = const [],
  });

  @override
  State<TaskItTopAppBar> createState() => _TaskItTopAppBarState();

  @override
  Size get preferredSize {
    // Return base height - the Column will grow dynamically
    // but this tells Scaffold the minimum space needed
    return const Size.fromHeight(kToolbarHeight);
  }
}

class _TaskItTopAppBarState extends State<TaskItTopAppBar> {
  final FocusNode _focusNode = FocusNode();
  final TextEditingController _controller = TextEditingController();

  @override
  void initState() {
    super.initState();
    if (widget.searchState != null) {
      _controller.text = widget.searchState!.query;
    }
  }

  @override
  void didUpdateWidget(TaskItTopAppBar oldWidget) {
    super.didUpdateWidget(oldWidget);
    
    // Update controller if query changed externally
    if (widget.searchState != null && 
        widget.searchState!.query != _controller.text) {
      _controller.text = widget.searchState!.query;
    }

    // Request/clear focus based on search state
    if (widget.searchState != null) {
      if (widget.searchState!.isActive && !oldWidget.searchState!.isActive) {
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted) _focusNode.requestFocus();
        });
      } else if (!widget.searchState!.isActive && oldWidget.searchState!.isActive) {
        _focusNode.unfocus();
      }
    }
  }

  @override
  void dispose() {
    _focusNode.dispose();
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final searchState = widget.searchState;
    final isSearchActive = searchState?.isActive ?? false;

    // Use a flexible container that doesn't constrain height
    return Material(
      color: theme.colorScheme.surface,
      elevation: 0,
      child: SafeArea(
        bottom: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            SizedBox(
              height: kToolbarHeight,
              child: AppBar(
                backgroundColor: Colors.transparent,
                foregroundColor: theme.colorScheme.onSurface,
                elevation: 0,
                leading: widget.showNavigationIcon && widget.onNavigateBack != null
                    ? IconButton(
                        icon: const Icon(Icons.arrow_back),
                        onPressed: widget.onNavigateBack,
                      )
                    : null,
                title: Text(
                  widget.title,
                  style: theme.textTheme.titleLarge?.copyWith(
                    color: theme.colorScheme.onSurface,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                actions: [
                  ...widget.actions,
                  if (searchState != null)
                    IconButton(
                      icon: const Icon(Icons.search),
                      onPressed: () {
                        searchState.onActiveChange(!isSearchActive);
                      },
                    ),
                ],
              ),
            ),
            // Animated search field below the AppBar
            if (searchState != null)
              AnimatedSize(
                duration: const Duration(milliseconds: 300),
                curve: Curves.easeInOut,
                child: isSearchActive
                    ? Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 16,
                          vertical: 8,
                        ),
                        child: TextField(
                          controller: _controller,
                          focusNode: _focusNode,
                          decoration: InputDecoration(
                            hintText: searchState.placeholder,
                            border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(8),
                              borderSide: BorderSide(
                                color: theme.colorScheme.outline,
                              ),
                            ),
                            enabledBorder: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(8),
                              borderSide: BorderSide(
                                color: theme.colorScheme.outline.withOpacity(0.5),
                              ),
                            ),
                            focusedBorder: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(8),
                              borderSide: BorderSide(
                                color: theme.colorScheme.primary,
                                width: 2,
                              ),
                            ),
                            contentPadding: const EdgeInsets.symmetric(
                              horizontal: 16,
                              vertical: 12,
                            ),
                            prefixIcon: Icon(
                              Icons.search,
                              color: theme.colorScheme.onSurfaceVariant,
                            ),
                            suffixIcon: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                if (searchState.query.isNotEmpty)
                                  IconButton(
                                    icon: const Icon(Icons.clear),
                                    onPressed: () {
                                      _controller.clear();
                                      searchState.onQueryChange('');
                                    },
                                  ),
                                IconButton(
                                  icon: const Icon(Icons.close),
                                  onPressed: () {
                                    if (searchState.enableClearOnClose) {
                                      _controller.clear();
                                      searchState.onQueryChange('');
                                    }
                                    searchState.onActiveChange(false);
                                  },
                                ),
                              ],
                            ),
                          ),
                          style: theme.textTheme.bodyLarge,
                          onChanged: searchState.onQueryChange,
                          textInputAction: TextInputAction.search,
                          onSubmitted: (_) => _focusNode.unfocus(),
                        ),
                      )
                    : const SizedBox.shrink(),
              ),
          ],
        ),
      ),
    );
  }
}

