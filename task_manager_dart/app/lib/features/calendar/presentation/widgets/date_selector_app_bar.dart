import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

/// Date selector app bar that integrates with TaskItTopAppBar
/// Shows a week strip below the app bar title
class DateSelectorAppBar extends StatelessWidget implements PreferredSizeWidget {
  final DateTime selectedDate;
  final ValueChanged<DateTime> onDateSelected;

  const DateSelectorAppBar({
    super.key,
    required this.selectedDate,
    required this.onDateSelected,
  });

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight + 80);

  @override
  Widget build(BuildContext context) {
    return AppBar(
      title: const Text('Calendar'),
      bottom: PreferredSize(
        preferredSize: const Size.fromHeight(80),
        child: _WeekDayStrip(
          selectedDate: selectedDate,
          onDateSelected: onDateSelected,
        ),
      ),
    );
  }
}

class _WeekDayStrip extends StatelessWidget {
  final DateTime selectedDate;
  final ValueChanged<DateTime> onDateSelected;

  const _WeekDayStrip({
    required this.selectedDate,
    required this.onDateSelected,
  });

  @override
  Widget build(BuildContext context) {
    final startDate = selectedDate.subtract(const Duration(days: 3));
    final dates = List.generate(7, (i) => startDate.add(Duration(days: i)));
    final today = DateTime.now();

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Row(
        children: dates.map((date) {
          final isSelected = _isSameDay(date, selectedDate);
          final isToday = _isSameDay(date, today);

          return Expanded(
            child: _DayItem(
              date: date,
              isSelected: isSelected,
              isToday: isToday,
              onTap: () => onDateSelected(date),
            ),
          );
        }).toList(),
      ),
    );
  }

  bool _isSameDay(DateTime a, DateTime b) {
    return a.year == b.year && a.month == b.month && a.day == b.day;
  }
}

class _DayItem extends StatelessWidget {
  final DateTime date;
  final bool isSelected;
  final bool isToday;
  final VoidCallback onTap;

  const _DayItem({
    required this.date,
    required this.isSelected,
    required this.isToday,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final dayName = DateFormat('E').format(date);

    final backgroundColor = isSelected
        ? theme.colorScheme.primary
        : theme.colorScheme.surfaceContainerHighest;

    final contentColor = isSelected
        ? theme.colorScheme.onPrimary
        : theme.colorScheme.onSurfaceVariant;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 4),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 8),
          decoration: BoxDecoration(
            color: backgroundColor,
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                dayName,
                style: theme.textTheme.labelSmall?.copyWith(
                  color: contentColor,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                '${date.day}',
                style: theme.textTheme.titleMedium?.copyWith(
                  color: contentColor,
                  fontWeight: isToday ? FontWeight.bold : FontWeight.normal,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

