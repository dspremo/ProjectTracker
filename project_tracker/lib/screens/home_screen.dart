import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/models.dart';
import '../providers/providers.dart';
import '../services/database_service.dart';
import '../widgets/widgets.dart';
import 'project_detail_screen.dart';
import 'add_project_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final DatabaseService _db = DatabaseService();
  Map<String, Map<String, double>> _projectStats = {};

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _loadProjectStats();
  }

  Future<void> _loadProjectStats() async {
    final projects = context.read<ProjectProvider>().projects;
    final stats = <String, Map<String, double>>{};

    for (final project in projects) {
      final hours = await _db.getTotalHoursForProject(project.id);
      final expenses = await _db.getTotalExpensesForProject(project.id);
      final income = await _db.getTotalIncomeForProject(project.id);
      stats[project.id] = {
        'hours': hours,
        'expenses': expenses,
        'income': income,
      };
    }

    if (mounted) {
      setState(() {
        _projectStats = stats;
      });
    }
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: NestedScrollView(
          headerSliverBuilder: (context, innerBoxIsScrolled) => [
            SliverAppBar(
              floating: true,
              snap: true,
              title: const Text(
                'My Projects',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              bottom: TabBar(
                controller: _tabController,
                tabs: const [
                  Tab(text: 'Active'),
                  Tab(text: 'Completed'),
                ],
                labelStyle: const TextStyle(fontWeight: FontWeight.w600),
                indicatorSize: TabBarIndicatorSize.label,
              ),
            ),
          ],
          body: Consumer<ProjectProvider>(
            builder: (context, provider, _) {
              if (provider.isLoading) {
                return const Center(child: CircularProgressIndicator());
              }

              return TabBarView(
                controller: _tabController,
                children: [
                  _ProjectList(
                    projects: provider.activeProjects,
                    stats: _projectStats,
                    onRefresh: () async {
                      await provider.loadProjects();
                      await _loadProjectStats();
                    },
                    emptyIcon: Icons.work_outline,
                    emptyTitle: 'No Active Projects',
                    emptySubtitle: 'Tap + to create your first project',
                  ),
                  _ProjectList(
                    projects: provider.doneProjects,
                    stats: _projectStats,
                    onRefresh: () async {
                      await provider.loadProjects();
                      await _loadProjectStats();
                    },
                    emptyIcon: Icons.check_circle_outline,
                    emptyTitle: 'No Completed Projects',
                    emptySubtitle: 'Completed projects will appear here',
                  ),
                ],
              );
            },
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _navigateToAddProject(context),
        icon: const Icon(Icons.add),
        label: const Text('New Project'),
      ),
    );
  }

  void _navigateToAddProject(BuildContext context) {
    Navigator.push(
      context,
      FadePageRoute(page: const AddProjectScreen()),
    ).then((_) {
      _loadProjectStats();
    });
  }
}

class _ProjectList extends StatelessWidget {
  final List<Project> projects;
  final Map<String, Map<String, double>> stats;
  final Future<void> Function() onRefresh;
  final IconData emptyIcon;
  final String emptyTitle;
  final String emptySubtitle;

  const _ProjectList({
    required this.projects,
    required this.stats,
    required this.onRefresh,
    required this.emptyIcon,
    required this.emptyTitle,
    required this.emptySubtitle,
  });

  @override
  Widget build(BuildContext context) {
    if (projects.isEmpty) {
      return EmptyState(
        icon: emptyIcon,
        title: emptyTitle,
        subtitle: emptySubtitle,
      );
    }

    return RefreshIndicator(
      onRefresh: onRefresh,
      child: ListView.builder(
        padding: const EdgeInsets.only(top: 8, bottom: 100),
        itemCount: projects.length,
        itemBuilder: (context, index) {
          final project = projects[index];
          final projectStats = stats[project.id] ?? {
            'hours': 0.0,
            'expenses': 0.0,
            'income': 0.0,
          };

          return AnimatedListItem(
            index: index,
            child: ProjectCard(
              project: project,
              totalHours: projectStats['hours']!,
              totalExpenses: projectStats['expenses']!,
              totalIncome: projectStats['income']!,
              onTap: () => _navigateToProjectDetail(context, project),
              onLongPress: () => _showProjectOptions(context, project),
            ),
          );
        },
      ),
    );
  }

  void _navigateToProjectDetail(BuildContext context, Project project) {
    Navigator.push(
      context,
      FadePageRoute(page: ProjectDetailScreen(project: project)),
    ).then((_) => onRefresh());
  }

  void _showProjectOptions(BuildContext context, Project project) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 40,
                height: 4,
                margin: const EdgeInsets.only(bottom: 16),
                decoration: BoxDecoration(
                  color: Colors.grey.shade300,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              ListTile(
                leading: Icon(
                  project.status == ProjectStatus.active
                      ? Icons.check_circle
                      : Icons.play_circle,
                ),
                title: Text(
                  project.status == ProjectStatus.active
                      ? 'Mark as Done'
                      : 'Mark as Active',
                ),
                onTap: () {
                  context.read<ProjectProvider>().toggleProjectStatus(project);
                  Navigator.pop(context);
                  onRefresh();
                },
              ),
              ListTile(
                leading: const Icon(Icons.edit),
                title: const Text('Edit Project'),
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    FadePageRoute(
                      page: AddProjectScreen(project: project),
                    ),
                  ).then((_) => onRefresh());
                },
              ),
              ListTile(
                leading: const Icon(Icons.delete, color: Colors.red),
                title: const Text(
                  'Delete Project',
                  style: TextStyle(color: Colors.red),
                ),
                onTap: () {
                  Navigator.pop(context);
                  _confirmDelete(context, project);
                },
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _confirmDelete(BuildContext context, Project project) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Project'),
        content: Text(
          'Are you sure you want to delete "${project.name}"? This action cannot be undone.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              context.read<ProjectProvider>().deleteProject(project.id);
              Navigator.pop(context);
              onRefresh();
            },
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }
}
