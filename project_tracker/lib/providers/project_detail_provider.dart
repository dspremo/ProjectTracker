import 'package:flutter/material.dart';
import '../models/models.dart';
import '../services/database_service.dart';

class ProjectDetailProvider with ChangeNotifier {
  final DatabaseService _db = DatabaseService();
  
  String? _currentProjectId;
  List<TimeEntry> _timeEntries = [];
  List<Expense> _expenses = [];
  List<Income> _incomes = [];
  double _totalHours = 0;
  double _totalExpenses = 0;
  double _totalIncome = 0;
  bool _isLoading = false;

  String? get currentProjectId => _currentProjectId;
  List<TimeEntry> get timeEntries => _timeEntries;
  List<Expense> get expenses => _expenses;
  List<Income> get incomes => _incomes;
  double get totalHours => _totalHours;
  double get totalExpenses => _totalExpenses;
  double get totalIncome => _totalIncome;
  bool get isLoading => _isLoading;

  double get profit => _totalIncome - _totalExpenses;

  Future<void> loadProjectDetails(String projectId) async {
    if (_currentProjectId == projectId && !_isLoading) {
      return;
    }

    _currentProjectId = projectId;
    _isLoading = true;
    notifyListeners();

    try {
      final futures = await Future.wait([
        _db.getTimeEntries(projectId),
        _db.getExpenses(projectId),
        _db.getIncomes(projectId),
        _db.getTotalHoursForProject(projectId),
        _db.getTotalExpensesForProject(projectId),
        _db.getTotalIncomeForProject(projectId),
      ]);

      _timeEntries = futures[0] as List<TimeEntry>;
      _expenses = futures[1] as List<Expense>;
      _incomes = futures[2] as List<Income>;
      _totalHours = futures[3] as double;
      _totalExpenses = futures[4] as double;
      _totalIncome = futures[5] as double;
    } catch (e) {
      debugPrint('Error loading project details: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> refresh() async {
    if (_currentProjectId != null) {
      _isLoading = true;
      notifyListeners();
      await loadProjectDetails(_currentProjectId!);
    }
  }

  // Time Entry operations
  Future<void> addTimeEntry(TimeEntry entry) async {
    await _db.insertTimeEntry(entry);
    _timeEntries.insert(0, entry);
    _totalHours += entry.hours;
    notifyListeners();
  }

  Future<void> updateTimeEntry(TimeEntry entry) async {
    final oldEntry = _timeEntries.firstWhere((e) => e.id == entry.id);
    await _db.updateTimeEntry(entry);
    final index = _timeEntries.indexWhere((e) => e.id == entry.id);
    if (index != -1) {
      _totalHours = _totalHours - oldEntry.hours + entry.hours;
      _timeEntries[index] = entry;
      notifyListeners();
    }
  }

  Future<void> deleteTimeEntry(String id) async {
    final entry = _timeEntries.firstWhere((e) => e.id == id);
    await _db.deleteTimeEntry(id);
    _timeEntries.removeWhere((e) => e.id == id);
    _totalHours -= entry.hours;
    notifyListeners();
  }

  // Expense operations
  Future<void> addExpense(Expense expense) async {
    await _db.insertExpense(expense);
    _expenses.insert(0, expense);
    _totalExpenses += expense.amount;
    notifyListeners();
  }

  Future<void> updateExpense(Expense expense) async {
    final oldExpense = _expenses.firstWhere((e) => e.id == expense.id);
    await _db.updateExpense(expense);
    final index = _expenses.indexWhere((e) => e.id == expense.id);
    if (index != -1) {
      _totalExpenses = _totalExpenses - oldExpense.amount + expense.amount;
      _expenses[index] = expense;
      notifyListeners();
    }
  }

  Future<void> deleteExpense(String id) async {
    final expense = _expenses.firstWhere((e) => e.id == id);
    await _db.deleteExpense(id);
    _expenses.removeWhere((e) => e.id == id);
    _totalExpenses -= expense.amount;
    notifyListeners();
  }

  // Income operations
  Future<void> addIncome(Income income) async {
    await _db.insertIncome(income);
    _incomes.insert(0, income);
    _totalIncome += income.amount;
    notifyListeners();
  }

  Future<void> updateIncome(Income income) async {
    final oldIncome = _incomes.firstWhere((e) => e.id == income.id);
    await _db.updateIncome(income);
    final index = _incomes.indexWhere((e) => e.id == income.id);
    if (index != -1) {
      _totalIncome = _totalIncome - oldIncome.amount + income.amount;
      _incomes[index] = income;
      notifyListeners();
    }
  }

  Future<void> deleteIncome(String id) async {
    final income = _incomes.firstWhere((e) => e.id == id);
    await _db.deleteIncome(id);
    _incomes.removeWhere((e) => e.id == id);
    _totalIncome -= income.amount;
    notifyListeners();
  }

  void clear() {
    _currentProjectId = null;
    _timeEntries = [];
    _expenses = [];
    _incomes = [];
    _totalHours = 0;
    _totalExpenses = 0;
    _totalIncome = 0;
    notifyListeners();
  }
}
