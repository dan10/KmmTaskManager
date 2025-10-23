import 'package:flutter/material.dart';
import 'package:task_manager_app/l10n/app_localizations.dart';

import '../../../features/tasks/presentation/views/tasks_screen.dart';
import '../../../features/calendar/presentation/views/calendar_screen.dart';
import '../projects/project_list_view.dart';

/// Main view with bottom navigation matching KMM's 3-tab layout
/// Tabs: Tasks, Calendar, Projects (matching KMM)
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
    const TasksScreen(),
    const CalendarScreen(),
    const ProjectListView(),
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
            icon: const Icon(Icons.check_circle),
            label: l10n.navTasks,
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.calendar_month),
            label: l10n.navCalendar,
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.folder),
            label: l10n.navProjects,
          ),
        ],
      ),
    );
  }
} 