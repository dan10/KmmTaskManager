import 'package:flutter/material.dart';
import 'package:task_manager_app/l10n/app_localizations.dart';
import 'package:provider/provider.dart';
import '../viewmodels/tasks_viewmodel.dart';
import '../actions/tasks_action.dart';
import 'tasks_effect_handler.dart';
import '../widgets/task_list.dart';
import '../../../../core/ui/components/components.dart';

/// Tasks screen matching KMM's TasksScreen
class TasksScreen extends StatefulWidget {
  const TasksScreen({super.key});

  @override
  State<TasksScreen> createState() => _TasksScreenState();
}

class _TasksScreenState extends State<TasksScreen> with AutomaticKeepAliveClientMixin {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();

  @override
  bool get wantKeepAlive => true;

  @override
  Widget build(BuildContext context) {
    super.build(context);
    final l10n = AppLocalizations.of(context)!;
    final viewModel = Provider.of<TasksViewModel>(context);
    
    return TasksEffectHandler(
      viewModel: viewModel,
      child: Scaffold(
        key: _scaffoldKey,
        appBar: PrincipalTaskItTopAppBar(
          title: l10n.tasksTitle,
          onSearch: (query) {
            viewModel.handleAction(SearchTasks(query));
          },
        ),
        body: RefreshIndicator(
          onRefresh: () async {
            viewModel.handleAction(const RefreshTasks());
            // Wait a bit for the refresh to complete
            await Future.delayed(const Duration(milliseconds: 500));
          },
          child: TaskList(
            state: viewModel.state,
            onAction: viewModel.handleAction,
          ),
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: () {
            viewModel.handleAction(const OpenCreateTask());
          },
          child: const Icon(Icons.add),
        ),
      ),
    );
  }
}

