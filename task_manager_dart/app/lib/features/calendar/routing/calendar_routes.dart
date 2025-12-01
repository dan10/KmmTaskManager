import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../presentation/views/calendar_screen.dart';

/// Calendar feature routing configuration
class CalendarRoutes {
  CalendarRoutes._();

  static const String calendar = '/calendar';

  static List<RouteBase> routes = [
    GoRoute(
      path: calendar,
      name: 'calendar',
      pageBuilder: (context, state) => const MaterialPage(
        child: CalendarScreen(),
      ),
    ),
  ];
}
