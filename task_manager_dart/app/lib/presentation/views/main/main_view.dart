import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

import '../tasks/task_list_view.dart';
import '../projects/project_list_view.dart';
import '../profile/profile_view.dart';

class MainView extends StatefulWidget {
  final int? initialTab;

  const MainView({
    super.key,
    this.initialTab,
  });

  @override
  State<MainView> createState() => _MainViewState();
}

class _MainViewState extends State<MainView> {
  late int _currentIndex;

  final List<Widget> _pages = [
    const TaskListView(),
    const ProjectListView(),
    const ProfileView(),
  ];

  @override
  void initState() {
    super.initState();
    _currentIndex = widget.initialTab ?? 0;
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      body: IndexedStack(
        index: _currentIndex,
        children: _pages,
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (index) {
          setState(() {
            _currentIndex = index;
          });
        },
        type: BottomNavigationBarType.fixed,
        selectedItemColor: Theme
            .of(context)
            .primaryColor,
        unselectedItemColor: Theme
            .of(context)
            .colorScheme
            .onSurface
            .withValues(alpha: 0.6),
        items: [
          BottomNavigationBarItem(
            icon: const Icon(Icons.task_alt),
            label: l10n.navTasks,
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.folder),
            label: l10n.navProjects,
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.person),
            label: l10n.navProfile,
          ),
        ],
      ),
    );
  }
} 