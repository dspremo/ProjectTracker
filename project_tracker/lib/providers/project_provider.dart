import 'package:flutter/material.dart';
import '../models/models.dart';
import '../services/database_service.dart';

class ProjectProvider with ChangeNotifier {
  final DatabaseService _db = DatabaseService();
  
  List<Project> _projects = [];
  bool _isLoading = false;
  String? _error;

  List<Project> get projects => _projects;
  List<Project> get activeProjects => 
      _projects.where((p) => p.status == ProjectStatus.active).toList();
  List<Project> get doneProjects => 
      _projects.where((p) => p.status == ProjectStatus.done).toList();
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> loadProjects() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _projects = await _db.getProjects();
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> addProject(Project project) async {
    try {
      await _db.insertProject(project);
      _projects.insert(0, project);
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }

  Future<void> updateProject(Project project) async {
    try {
      await _db.updateProject(project);
      final index = _projects.indexWhere((p) => p.id == project.id);
      if (index != -1) {
        _projects[index] = project;
        notifyListeners();
      }
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }

  Future<void> deleteProject(String id) async {
    try {
      await _db.deleteProject(id);
      _projects.removeWhere((p) => p.id == id);
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }

  Future<void> toggleProjectStatus(Project project) async {
    final newStatus = project.status == ProjectStatus.active 
        ? ProjectStatus.done 
        : ProjectStatus.active;
    final updated = project.copyWith(status: newStatus);
    await updateProject(updated);
  }

  Project? getProjectById(String id) {
    try {
      return _projects.firstWhere((p) => p.id == id);
    } catch (_) {
      return null;
    }
  }
}
