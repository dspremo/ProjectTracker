import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart' as path;
import '../models/models.dart';

class DatabaseService {
  static final DatabaseService _instance = DatabaseService._internal();
  factory DatabaseService() => _instance;
  DatabaseService._internal();

  static Database? _database;

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    String dbPath = path.join(await getDatabasesPath(), 'project_tracker.db');
    return await openDatabase(
      dbPath,
      version: 1,
      onCreate: _onCreate,
    );
  }

  Future<void> _onCreate(Database db, int version) async {
    await db.execute('''
      CREATE TABLE projects(
        id TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        price REAL NOT NULL,
        startDate INTEGER NOT NULL,
        status INTEGER NOT NULL,
        createdAt INTEGER NOT NULL,
        updatedAt INTEGER NOT NULL
      )
    ''');

    await db.execute('''
      CREATE TABLE time_entries(
        id TEXT PRIMARY KEY,
        projectId TEXT NOT NULL,
        hours REAL NOT NULL,
        description TEXT,
        date INTEGER NOT NULL,
        createdAt INTEGER NOT NULL,
        FOREIGN KEY (projectId) REFERENCES projects (id) ON DELETE CASCADE
      )
    ''');

    await db.execute('''
      CREATE TABLE expenses(
        id TEXT PRIMARY KEY,
        projectId TEXT NOT NULL,
        amount REAL NOT NULL,
        description TEXT,
        category TEXT,
        date INTEGER NOT NULL,
        createdAt INTEGER NOT NULL,
        FOREIGN KEY (projectId) REFERENCES projects (id) ON DELETE CASCADE
      )
    ''');

    await db.execute('''
      CREATE TABLE incomes(
        id TEXT PRIMARY KEY,
        projectId TEXT NOT NULL,
        amount REAL NOT NULL,
        description TEXT,
        date INTEGER NOT NULL,
        createdAt INTEGER NOT NULL,
        FOREIGN KEY (projectId) REFERENCES projects (id) ON DELETE CASCADE
      )
    ''');
  }

  // Project CRUD
  Future<int> insertProject(Project project) async {
    final db = await database;
    return await db.insert('projects', project.toMap());
  }

  Future<List<Project>> getProjects() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'projects',
      orderBy: 'createdAt DESC',
    );
    return List.generate(maps.length, (i) => Project.fromMap(maps[i]));
  }

  Future<Project?> getProject(String id) async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'projects',
      where: 'id = ?',
      whereArgs: [id],
    );
    if (maps.isEmpty) return null;
    return Project.fromMap(maps.first);
  }

  Future<int> updateProject(Project project) async {
    final db = await database;
    return await db.update(
      'projects',
      project.toMap(),
      where: 'id = ?',
      whereArgs: [project.id],
    );
  }

  Future<int> deleteProject(String id) async {
    final db = await database;
    // Delete related entries first
    await db.delete('time_entries', where: 'projectId = ?', whereArgs: [id]);
    await db.delete('expenses', where: 'projectId = ?', whereArgs: [id]);
    await db.delete('incomes', where: 'projectId = ?', whereArgs: [id]);
    return await db.delete('projects', where: 'id = ?', whereArgs: [id]);
  }

  // Time Entry CRUD
  Future<int> insertTimeEntry(TimeEntry entry) async {
    final db = await database;
    return await db.insert('time_entries', entry.toMap());
  }

  Future<List<TimeEntry>> getTimeEntries(String projectId) async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'time_entries',
      where: 'projectId = ?',
      whereArgs: [projectId],
      orderBy: 'date DESC',
    );
    return List.generate(maps.length, (i) => TimeEntry.fromMap(maps[i]));
  }

  Future<List<TimeEntry>> getAllTimeEntries() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'time_entries',
      orderBy: 'date DESC',
    );
    return List.generate(maps.length, (i) => TimeEntry.fromMap(maps[i]));
  }

  Future<int> updateTimeEntry(TimeEntry entry) async {
    final db = await database;
    return await db.update(
      'time_entries',
      entry.toMap(),
      where: 'id = ?',
      whereArgs: [entry.id],
    );
  }

  Future<int> deleteTimeEntry(String id) async {
    final db = await database;
    return await db.delete('time_entries', where: 'id = ?', whereArgs: [id]);
  }

  // Expense CRUD
  Future<int> insertExpense(Expense expense) async {
    final db = await database;
    return await db.insert('expenses', expense.toMap());
  }

  Future<List<Expense>> getExpenses(String projectId) async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'expenses',
      where: 'projectId = ?',
      whereArgs: [projectId],
      orderBy: 'date DESC',
    );
    return List.generate(maps.length, (i) => Expense.fromMap(maps[i]));
  }

  Future<List<Expense>> getAllExpenses() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'expenses',
      orderBy: 'date DESC',
    );
    return List.generate(maps.length, (i) => Expense.fromMap(maps[i]));
  }

  Future<int> updateExpense(Expense expense) async {
    final db = await database;
    return await db.update(
      'expenses',
      expense.toMap(),
      where: 'id = ?',
      whereArgs: [expense.id],
    );
  }

  Future<int> deleteExpense(String id) async {
    final db = await database;
    return await db.delete('expenses', where: 'id = ?', whereArgs: [id]);
  }

  // Income CRUD
  Future<int> insertIncome(Income income) async {
    final db = await database;
    return await db.insert('incomes', income.toMap());
  }

  Future<List<Income>> getIncomes(String projectId) async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'incomes',
      where: 'projectId = ?',
      whereArgs: [projectId],
      orderBy: 'date DESC',
    );
    return List.generate(maps.length, (i) => Income.fromMap(maps[i]));
  }

  Future<List<Income>> getAllIncomes() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'incomes',
      orderBy: 'date DESC',
    );
    return List.generate(maps.length, (i) => Income.fromMap(maps[i]));
  }

  Future<int> updateIncome(Income income) async {
    final db = await database;
    return await db.update(
      'incomes',
      income.toMap(),
      where: 'id = ?',
      whereArgs: [income.id],
    );
  }

  Future<int> deleteIncome(String id) async {
    final db = await database;
    return await db.delete('incomes', where: 'id = ?', whereArgs: [id]);
  }

  // Statistics queries
  Future<double> getTotalHoursForProject(String projectId) async {
    final db = await database;
    final result = await db.rawQuery(
      'SELECT SUM(hours) as total FROM time_entries WHERE projectId = ?',
      [projectId],
    );
    return (result.first['total'] as num?)?.toDouble() ?? 0.0;
  }

  Future<double> getTotalExpensesForProject(String projectId) async {
    final db = await database;
    final result = await db.rawQuery(
      'SELECT SUM(amount) as total FROM expenses WHERE projectId = ?',
      [projectId],
    );
    return (result.first['total'] as num?)?.toDouble() ?? 0.0;
  }

  Future<double> getTotalIncomeForProject(String projectId) async {
    final db = await database;
    final result = await db.rawQuery(
      'SELECT SUM(amount) as total FROM incomes WHERE projectId = ?',
      [projectId],
    );
    return (result.first['total'] as num?)?.toDouble() ?? 0.0;
  }
}
