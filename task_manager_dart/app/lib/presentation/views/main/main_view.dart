import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../../features/tasks/pages/task_list_screen.dart';
import '../../../features/projects/pages/project_list_screen.dart';

class MainView extends StatefulWidget {
  const MainView({super.key, this.initialTab});
  final int? initialTab;

  @override
  State<MainView> createState() => _MainViewState();
}

class _MainViewState extends State<MainView> {
  late int _currentIndex;

  @override
  void initState() {
    super.initState();
    _currentIndex = widget.initialTab ?? 0;
  }

  void _onTap(int index) {
    setState(() => _currentIndex = index);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('TaskIt')),
      body: IndexedStack(
        index: _currentIndex,
        children: const [
          // Embed feature list screens so bottom bar remains visible
          _TasksTab(),
          _ProjectsTab(),
          _CalendarTab(),
        ],
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: _onTap,
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.checklist), label: 'Tasks'),
          BottomNavigationBarItem(icon: Icon(Icons.folder), label: 'Projects'),
          BottomNavigationBarItem(icon: Icon(Icons.calendar_today), label: 'Calendar'),
        ],
      ),
    );
  }
}

class _TasksTab extends StatelessWidget {
  const _TasksTab();
  @override
  Widget build(BuildContext context) => const TaskListScreen();
}

class _ProjectsTab extends StatelessWidget {
  const _ProjectsTab();
  @override
  Widget build(BuildContext context) => const ProjectListScreen();
}

class _CalendarTab extends StatelessWidget {
  const _CalendarTab();
  @override
  Widget build(BuildContext context) => const Center(child: Text('Calendar')); // placeholder
}