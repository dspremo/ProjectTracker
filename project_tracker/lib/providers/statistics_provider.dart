import 'package:flutter/material.dart';
import '../models/models.dart';
import '../services/database_service.dart';

class MonthlyData {
  final DateTime month;
  final double income;
  final double expenses;
  final double hours;

  MonthlyData({
    required this.month,
    required this.income,
    required this.expenses,
    required this.hours,
  });

  double get profit => income - expenses;
}

class ProjectStats {
  final Project project;
  final double totalHours;
  final double totalExpenses;
  final double totalIncome;

  ProjectStats({
    required this.project,
    required this.totalHours,
    required this.totalExpenses,
    required this.totalIncome,
  });

  double get profit => totalIncome - totalExpenses;
  double get profitMargin => totalIncome > 0 ? (profit / totalIncome) * 100 : 0;
}

class StatisticsProvider with ChangeNotifier {
  final DatabaseService _db = DatabaseService();

  List<ProjectStats> _projectStats = [];
  List<MonthlyData> _monthlyData = [];
  double _totalIncome = 0;
  double _totalExpenses = 0;
  double _totalHours = 0;
  bool _isLoading = false;

  List<ProjectStats> get projectStats => _projectStats;
  List<MonthlyData> get monthlyData => _monthlyData;
  double get totalIncome => _totalIncome;
  double get totalExpenses => _totalExpenses;
  double get totalHours => _totalHours;
  double get totalProfit => _totalIncome - _totalExpenses;
  bool get isLoading => _isLoading;

  Future<void> loadStatistics() async {
    _isLoading = true;
    notifyListeners();

    try {
      final projects = await _db.getProjects();
      final allTimeEntries = await _db.getAllTimeEntries();
      final allExpenses = await _db.getAllExpenses();
      final allIncomes = await _db.getAllIncomes();

      // Calculate project stats
      _projectStats = [];
      _totalIncome = 0;
      _totalExpenses = 0;
      _totalHours = 0;

      for (final project in projects) {
        final hours = allTimeEntries
            .where((e) => e.projectId == project.id)
            .fold(0.0, (sum, e) => sum + e.hours);
        final expenses = allExpenses
            .where((e) => e.projectId == project.id)
            .fold(0.0, (sum, e) => sum + e.amount);
        final income = allIncomes
            .where((e) => e.projectId == project.id)
            .fold(0.0, (sum, e) => sum + e.amount);

        _projectStats.add(ProjectStats(
          project: project,
          totalHours: hours,
          totalExpenses: expenses,
          totalIncome: income,
        ));

        _totalHours += hours;
        _totalExpenses += expenses;
        _totalIncome += income;
      }

      // Sort by profit descending
      _projectStats.sort((a, b) => b.profit.compareTo(a.profit));

      // Calculate monthly data
      _monthlyData = _calculateMonthlyData(allTimeEntries, allExpenses, allIncomes);
    } catch (e) {
      debugPrint('Error loading statistics: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  List<MonthlyData> _calculateMonthlyData(
    List<TimeEntry> timeEntries,
    List<Expense> expenses,
    List<Income> incomes,
  ) {
    final Map<String, MonthlyData> monthMap = {};

    // Process time entries
    for (final entry in timeEntries) {
      final key = '${entry.date.year}-${entry.date.month.toString().padLeft(2, '0')}';
      final existing = monthMap[key];
      if (existing != null) {
        monthMap[key] = MonthlyData(
          month: DateTime(entry.date.year, entry.date.month),
          income: existing.income,
          expenses: existing.expenses,
          hours: existing.hours + entry.hours,
        );
      } else {
        monthMap[key] = MonthlyData(
          month: DateTime(entry.date.year, entry.date.month),
          income: 0,
          expenses: 0,
          hours: entry.hours,
        );
      }
    }

    // Process expenses
    for (final expense in expenses) {
      final key = '${expense.date.year}-${expense.date.month.toString().padLeft(2, '0')}';
      final existing = monthMap[key];
      if (existing != null) {
        monthMap[key] = MonthlyData(
          month: DateTime(expense.date.year, expense.date.month),
          income: existing.income,
          expenses: existing.expenses + expense.amount,
          hours: existing.hours,
        );
      } else {
        monthMap[key] = MonthlyData(
          month: DateTime(expense.date.year, expense.date.month),
          income: 0,
          expenses: expense.amount,
          hours: 0,
        );
      }
    }

    // Process incomes
    for (final income in incomes) {
      final key = '${income.date.year}-${income.date.month.toString().padLeft(2, '0')}';
      final existing = monthMap[key];
      if (existing != null) {
        monthMap[key] = MonthlyData(
          month: DateTime(income.date.year, income.date.month),
          income: existing.income + income.amount,
          expenses: existing.expenses,
          hours: existing.hours,
        );
      } else {
        monthMap[key] = MonthlyData(
          month: DateTime(income.date.year, income.date.month),
          income: income.amount,
          expenses: 0,
          hours: 0,
        );
      }
    }

    // Sort by month
    final sortedData = monthMap.values.toList()
      ..sort((a, b) => a.month.compareTo(b.month));

    // Return last 12 months
    if (sortedData.length > 12) {
      return sortedData.sublist(sortedData.length - 12);
    }
    return sortedData;
  }
}
