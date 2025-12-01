import 'package:flutter/material.dart';

/// TaskIt primary top app bar with optional search toggle.
class PrincipalTaskItTopAppBar extends StatefulWidget
    implements PreferredSizeWidget {
  const PrincipalTaskItTopAppBar({
    super.key,
    required this.title,
    this.onSearch,
    this.actions,
  });

  /// Title displayed in the app bar.
  final String title;

  /// Optional callback executed when a search is submitted.
  final ValueChanged<String>? onSearch;

  /// Optional additional action buttons displayed beside the search icon.
  final List<Widget>? actions;

  @override
  State<PrincipalTaskItTopAppBar> createState() =>
      _PrincipalTaskItTopAppBarState();

  /// Maximum height when the search field is expanded.
  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight + 72);
}

class _PrincipalTaskItTopAppBarState extends State<PrincipalTaskItTopAppBar> {
  final TextEditingController _controller = TextEditingController();
  final FocusNode _focusNode = FocusNode();

  bool _showSearch = false;

  @override
  void initState() {
    super.initState();
    _controller.addListener(_onQueryChanged);
  }

  @override
  void dispose() {
    _controller.removeListener(_onQueryChanged);
    _controller.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  void _onQueryChanged() {
    // Trigger search on every text change
    widget.onSearch?.call(_controller.text.trim());
    setState(() {});
  }

  void _toggleSearch() {
    if (widget.onSearch == null) {
      return;
    }

    setState(() {
      _showSearch = !_showSearch;
    });

    if (_showSearch) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) {
          _focusNode.requestFocus();
        }
      });
    } else {
      // Clear search when closing
      _controller.clear();
      widget.onSearch?.call('');
      _focusNode.unfocus();
    }
  }

  void _clearAndFocus() {
    _controller.clear();
    widget.onSearch?.call('');
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        _focusNode.requestFocus();
      }
    });
  }

  void _submitSearch(String value) {
    // Search is already triggered on text change, just unfocus
    _focusNode.unfocus();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return AppBar(
      elevation: 0,
      backgroundColor: theme.colorScheme.surface,
      foregroundColor: theme.colorScheme.onSurface,
      titleSpacing: 0,
      title: Text(
        widget.title,
        style: theme.textTheme.titleLarge?.copyWith(
          color: theme.colorScheme.onSurface,
        ),
      ),
      actions: [
        if (widget.actions != null) ...widget.actions!,
        if (widget.onSearch != null)
          IconButton(
            icon: Icon(_showSearch ? Icons.close : Icons.search),
            onPressed: _toggleSearch,
            tooltip: _showSearch ? 'Close search' : 'Search',
          ),
      ],
      bottom: PreferredSize(
        preferredSize: Size.fromHeight(_showSearch ? 64 : 0),
        child: AnimatedSwitcher(
          duration: const Duration(milliseconds: 200),
          transitionBuilder: (child, animation) => SizeTransition(
            sizeFactor: animation,
            axisAlignment: -1,
            child: child,
          ),
          child: _showSearch
              ? Container(
                  key: const ValueKey('taskit-search-bar'),
                  color: theme.colorScheme.surface,
                  padding:
                      const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  child: TextField(
                    controller: _controller,
                    focusNode: _focusNode,
                    textInputAction: TextInputAction.search,
                    onSubmitted: _submitSearch,
                    decoration: InputDecoration(
                      hintText: 'Search tasks',
                      prefixIcon: const Icon(Icons.search),
                      suffixIcon: _controller.text.isNotEmpty
                          ? IconButton(
                              icon: const Icon(Icons.clear),
                              onPressed: _clearAndFocus,
                              tooltip: 'Clear search',
                            )
                          : null,
                      border: const OutlineInputBorder(
                        borderRadius: BorderRadius.all(Radius.circular(16)),
                      ),
                      filled: true,
                      fillColor: theme.colorScheme.surfaceVariant
                          .withOpacity(0.35),
                      contentPadding: const EdgeInsets.symmetric(
                        horizontal: 12,
                        vertical: 12,
                      ),
                    ),
                  ),
                )
              : const SizedBox.shrink(),
        ),
      ),
    );
  }
}

