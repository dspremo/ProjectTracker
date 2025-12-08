import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../providers/providers.dart';
import '../theme/app_theme.dart';
import '../widgets/widgets.dart';

class StatisticsScreen extends StatefulWidget {
  const StatisticsScreen({super.key});

  @override
  State<StatisticsScreen> createState() => _StatisticsScreenState();
}

class _StatisticsScreenState extends State<StatisticsScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<StatisticsProvider>().loadStatistics();
    });
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
                'Statistics',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              bottom: TabBar(
                controller: _tabController,
                tabs: const [
                  Tab(text: 'Overview'),
                  Tab(text: 'By Month'),
                ],
                labelStyle: const TextStyle(fontWeight: FontWeight.w600),
                indicatorSize: TabBarIndicatorSize.label,
              ),
            ),
          ],
          body: Consumer<StatisticsProvider>(
            builder: (context, provider, _) {
              if (provider.isLoading) {
                return const Center(child: CircularProgressIndicator());
              }

              return TabBarView(
                controller: _tabController,
                children: [
                  _OverviewTab(provider: provider),
                  _MonthlyTab(provider: provider),
                ],
              );
            },
          ),
        ),
      ),
    );
  }
}

class _OverviewTab extends StatelessWidget {
  final StatisticsProvider provider;

  const _OverviewTab({required this.provider});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    if (provider.projectStats.isEmpty) {
      return const EmptyState(
        icon: Icons.analytics_outlined,
        title: 'No Data Yet',
        subtitle: 'Create projects and add entries to see statistics',
      );
    }

    return RefreshIndicator(
      onRefresh: () => provider.loadStatistics(),
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Summary Cards
          Row(
            children: [
              Expanded(
                child: StatCard(
                  title: 'Total Income',
                  value: '\$${provider.totalIncome.toStringAsFixed(0)}',
                  icon: Icons.arrow_downward,
                  color: AppTheme.successColor,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: StatCard(
                  title: 'Total Expenses',
                  value: '\$${provider.totalExpenses.toStringAsFixed(0)}',
                  icon: Icons.arrow_upward,
                  color: AppTheme.errorColor,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: StatCard(
                  title: 'Total Profit',
                  value: '\$${provider.totalProfit.toStringAsFixed(0)}',
                  icon: Icons.trending_up,
                  color: provider.totalProfit >= 0
                      ? AppTheme.primaryColor
                      : AppTheme.errorColor,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: StatCard(
                  title: 'Total Hours',
                  value: '${provider.totalHours.toStringAsFixed(1)}h',
                  icon: Icons.access_time,
                  color: AppTheme.accentColor,
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),

          // Profit by Project Chart
          Text(
            'Profit by Project',
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          SizedBox(
            height: 250,
            child: _ProfitBarChart(stats: provider.projectStats),
          ),
          const SizedBox(height: 24),

          // Hours by Project Chart
          Text(
            'Hours by Project',
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          SizedBox(
            height: 250,
            child: _HoursPieChart(stats: provider.projectStats),
          ),
          const SizedBox(height: 24),

          // Project List
          Text(
            'Project Details',
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          ...provider.projectStats.map((stats) => _ProjectStatsCard(stats: stats)),
          const SizedBox(height: 80),
        ],
      ),
    );
  }
}

class _MonthlyTab extends StatelessWidget {
  final StatisticsProvider provider;

  const _MonthlyTab({required this.provider});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    if (provider.monthlyData.isEmpty) {
      return const EmptyState(
        icon: Icons.calendar_month,
        title: 'No Monthly Data',
        subtitle: 'Add entries to see monthly statistics',
      );
    }

    return RefreshIndicator(
      onRefresh: () => provider.loadStatistics(),
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Monthly Profit Chart
          Text(
            'Monthly Profit',
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          SizedBox(
            height: 300,
            child: _MonthlyProfitChart(data: provider.monthlyData),
          ),
          const SizedBox(height: 24),

          // Monthly Hours Chart
          Text(
            'Monthly Hours',
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          SizedBox(
            height: 250,
            child: _MonthlyHoursChart(data: provider.monthlyData),
          ),
          const SizedBox(height: 24),

          // Monthly Breakdown
          Text(
            'Monthly Breakdown',
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          ...provider.monthlyData.reversed.map((data) => _MonthlyDataCard(data: data)),
          const SizedBox(height: 80),
        ],
      ),
    );
  }
}

class _ProfitBarChart extends StatelessWidget {
  final List<ProjectStats> stats;

  const _ProfitBarChart({required this.stats});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final displayStats = stats.take(6).toList();

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: BarChart(
          BarChartData(
            alignment: BarChartAlignment.spaceAround,
            maxY: displayStats.isEmpty
                ? 100
                : displayStats.map((s) => s.profit.abs()).reduce((a, b) => a > b ? a : b) * 1.2,
            barTouchData: BarTouchData(
              enabled: true,
              touchTooltipData: BarTouchTooltipData(
                getTooltipColor: (_) => theme.colorScheme.surface,
                getTooltipItem: (group, groupIndex, rod, rodIndex) {
                  final stat = displayStats[group.x.toInt()];
                  return BarTooltipItem(
                    '${stat.project.name}\n\$${stat.profit.toStringAsFixed(0)}',
                    TextStyle(
                      color: theme.colorScheme.onSurface,
                      fontWeight: FontWeight.w600,
                    ),
                  );
                },
              ),
            ),
            titlesData: FlTitlesData(
              show: true,
              bottomTitles: AxisTitles(
                sideTitles: SideTitles(
                  showTitles: true,
                  getTitlesWidget: (value, meta) {
                    if (value.toInt() >= displayStats.length) return const SizedBox();
                    final name = displayStats[value.toInt()].project.name;
                    return Padding(
                      padding: const EdgeInsets.only(top: 8),
                      child: Text(
                        name.length > 8 ? '${name.substring(0, 8)}...' : name,
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
                        ),
                      ),
                    );
                  },
                  reservedSize: 40,
                ),
              ),
              leftTitles: AxisTitles(
                sideTitles: SideTitles(
                  showTitles: true,
                  reservedSize: 50,
                  getTitlesWidget: (value, meta) {
                    return Text(
                      '\$${value.toInt()}',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
                      ),
                    );
                  },
                ),
              ),
              topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
              rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
            ),
            borderData: FlBorderData(show: false),
            gridData: FlGridData(
              show: true,
              drawVerticalLine: false,
              horizontalInterval: displayStats.isEmpty
                  ? 20
                  : displayStats.map((s) => s.profit.abs()).reduce((a, b) => a > b ? a : b) / 4,
              getDrawingHorizontalLine: (value) {
                return FlLine(
                  color: theme.colorScheme.onSurface.withValues(alpha: 0.1),
                  strokeWidth: 1,
                );
              },
            ),
            barGroups: displayStats.asMap().entries.map((entry) {
              final stat = entry.value;
              return BarChartGroupData(
                x: entry.key,
                barRods: [
                  BarChartRodData(
                    toY: stat.profit.abs(),
                    color: stat.profit >= 0 ? AppTheme.successColor : AppTheme.errorColor,
                    width: 20,
                    borderRadius: const BorderRadius.vertical(top: Radius.circular(6)),
                  ),
                ],
              );
            }).toList(),
          ),
        ),
      ),
    );
  }
}

class _HoursPieChart extends StatelessWidget {
  final List<ProjectStats> stats;

  const _HoursPieChart({required this.stats});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final displayStats = stats.where((s) => s.totalHours > 0).take(6).toList();
    final totalHours = displayStats.fold(0.0, (sum, s) => sum + s.totalHours);

    final colors = [
      AppTheme.primaryColor,
      AppTheme.secondaryColor,
      AppTheme.accentColor,
      AppTheme.successColor,
      AppTheme.warningColor,
      const Color(0xFFEC4899),
    ];

    if (displayStats.isEmpty) {
      return Card(
        child: Center(
          child: Padding(
            padding: const EdgeInsets.all(32),
            child: Text(
              'No hours logged yet',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
              ),
            ),
          ),
        ),
      );
    }

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Expanded(
              child: PieChart(
                PieChartData(
                  sectionsSpace: 2,
                  centerSpaceRadius: 40,
                  sections: displayStats.asMap().entries.map((entry) {
                    final stat = entry.value;
                    final percentage = (stat.totalHours / totalHours * 100);
                    return PieChartSectionData(
                      color: colors[entry.key % colors.length],
                      value: stat.totalHours,
                      title: '${percentage.toStringAsFixed(0)}%',
                      radius: 50,
                      titleStyle: const TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                      ),
                    );
                  }).toList(),
                ),
              ),
            ),
            const SizedBox(width: 16),
            Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: displayStats.asMap().entries.map((entry) {
                final stat = entry.value;
                return Padding(
                  padding: const EdgeInsets.symmetric(vertical: 4),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Container(
                        width: 12,
                        height: 12,
                        decoration: BoxDecoration(
                          color: colors[entry.key % colors.length],
                          shape: BoxShape.circle,
                        ),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        stat.project.name.length > 12
                            ? '${stat.project.name.substring(0, 12)}...'
                            : stat.project.name,
                        style: theme.textTheme.bodySmall,
                      ),
                    ],
                  ),
                );
              }).toList(),
            ),
          ],
        ),
      ),
    );
  }
}

class _MonthlyProfitChart extends StatelessWidget {
  final List<MonthlyData> data;

  const _MonthlyProfitChart({required this.data});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final dateFormat = DateFormat('MMM');

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: LineChart(
          LineChartData(
            gridData: FlGridData(
              show: true,
              drawVerticalLine: false,
              getDrawingHorizontalLine: (value) {
                return FlLine(
                  color: theme.colorScheme.onSurface.withValues(alpha: 0.1),
                  strokeWidth: 1,
                );
              },
            ),
            titlesData: FlTitlesData(
              show: true,
              bottomTitles: AxisTitles(
                sideTitles: SideTitles(
                  showTitles: true,
                  reservedSize: 30,
                  interval: 1,
                  getTitlesWidget: (value, meta) {
                    if (value.toInt() >= data.length || value.toInt() < 0) {
                      return const SizedBox();
                    }
                    return Padding(
                      padding: const EdgeInsets.only(top: 8),
                      child: Text(
                        dateFormat.format(data[value.toInt()].month),
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
                        ),
                      ),
                    );
                  },
                ),
              ),
              leftTitles: AxisTitles(
                sideTitles: SideTitles(
                  showTitles: true,
                  reservedSize: 50,
                  getTitlesWidget: (value, meta) {
                    return Text(
                      '\$${value.toInt()}',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
                      ),
                    );
                  },
                ),
              ),
              topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
              rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
            ),
            borderData: FlBorderData(show: false),
            lineBarsData: [
              // Income line
              LineChartBarData(
                spots: data.asMap().entries.map((entry) {
                  return FlSpot(entry.key.toDouble(), entry.value.income);
                }).toList(),
                isCurved: true,
                color: AppTheme.successColor,
                barWidth: 3,
                isStrokeCapRound: true,
                dotData: const FlDotData(show: false),
                belowBarData: BarAreaData(
                  show: true,
                  color: AppTheme.successColor.withValues(alpha: 0.1),
                ),
              ),
              // Expenses line
              LineChartBarData(
                spots: data.asMap().entries.map((entry) {
                  return FlSpot(entry.key.toDouble(), entry.value.expenses);
                }).toList(),
                isCurved: true,
                color: AppTheme.errorColor,
                barWidth: 3,
                isStrokeCapRound: true,
                dotData: const FlDotData(show: false),
                belowBarData: BarAreaData(
                  show: true,
                  color: AppTheme.errorColor.withValues(alpha: 0.1),
                ),
              ),
            ],
            lineTouchData: LineTouchData(
              enabled: true,
              touchTooltipData: LineTouchTooltipData(
                getTooltipColor: (_) => theme.colorScheme.surface,
                getTooltipItems: (touchedSpots) {
                  return touchedSpots.map((spot) {
                    final isIncome = spot.barIndex == 0;
                    return LineTooltipItem(
                      '${isIncome ? "Income" : "Expenses"}: \$${spot.y.toStringAsFixed(0)}',
                      TextStyle(
                        color: isIncome ? AppTheme.successColor : AppTheme.errorColor,
                        fontWeight: FontWeight.w600,
                      ),
                    );
                  }).toList();
                },
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _MonthlyHoursChart extends StatelessWidget {
  final List<MonthlyData> data;

  const _MonthlyHoursChart({required this.data});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final dateFormat = DateFormat('MMM');

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: BarChart(
          BarChartData(
            alignment: BarChartAlignment.spaceAround,
            maxY: data.isEmpty
                ? 100
                : data.map((d) => d.hours).reduce((a, b) => a > b ? a : b) * 1.2,
            barTouchData: BarTouchData(
              enabled: true,
              touchTooltipData: BarTouchTooltipData(
                getTooltipColor: (_) => theme.colorScheme.surface,
                getTooltipItem: (group, groupIndex, rod, rodIndex) {
                  final monthData = data[group.x.toInt()];
                  return BarTooltipItem(
                    '${dateFormat.format(monthData.month)}\n${monthData.hours.toStringAsFixed(1)}h',
                    TextStyle(
                      color: theme.colorScheme.onSurface,
                      fontWeight: FontWeight.w600,
                    ),
                  );
                },
              ),
            ),
            titlesData: FlTitlesData(
              show: true,
              bottomTitles: AxisTitles(
                sideTitles: SideTitles(
                  showTitles: true,
                  getTitlesWidget: (value, meta) {
                    if (value.toInt() >= data.length) return const SizedBox();
                    return Padding(
                      padding: const EdgeInsets.only(top: 8),
                      child: Text(
                        dateFormat.format(data[value.toInt()].month),
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
                        ),
                      ),
                    );
                  },
                  reservedSize: 30,
                ),
              ),
              leftTitles: AxisTitles(
                sideTitles: SideTitles(
                  showTitles: true,
                  reservedSize: 40,
                  getTitlesWidget: (value, meta) {
                    return Text(
                      '${value.toInt()}h',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
                      ),
                    );
                  },
                ),
              ),
              topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
              rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
            ),
            borderData: FlBorderData(show: false),
            gridData: FlGridData(
              show: true,
              drawVerticalLine: false,
              getDrawingHorizontalLine: (value) {
                return FlLine(
                  color: theme.colorScheme.onSurface.withValues(alpha: 0.1),
                  strokeWidth: 1,
                );
              },
            ),
            barGroups: data.asMap().entries.map((entry) {
              return BarChartGroupData(
                x: entry.key,
                barRods: [
                  BarChartRodData(
                    toY: entry.value.hours,
                    color: AppTheme.accentColor,
                    width: 16,
                    borderRadius: const BorderRadius.vertical(top: Radius.circular(6)),
                  ),
                ],
              );
            }).toList(),
          ),
        ),
      ),
    );
  }
}

class _ProjectStatsCard extends StatelessWidget {
  final ProjectStats stats;

  const _ProjectStatsCard({required this.stats});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    stats.project.name,
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: stats.profit >= 0
                        ? AppTheme.successColor.withValues(alpha: 0.15)
                        : AppTheme.errorColor.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    '\$${stats.profit.toStringAsFixed(0)}',
                    style: TextStyle(
                      color: stats.profit >= 0
                          ? AppTheme.successColor
                          : AppTheme.errorColor,
                      fontWeight: FontWeight.w600,
                      fontSize: 12,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                _StatChip(
                  icon: Icons.access_time,
                  label: '${stats.totalHours.toStringAsFixed(1)}h',
                  color: AppTheme.accentColor,
                ),
                const SizedBox(width: 8),
                _StatChip(
                  icon: Icons.arrow_downward,
                  label: '\$${stats.totalIncome.toStringAsFixed(0)}',
                  color: AppTheme.successColor,
                ),
                const SizedBox(width: 8),
                _StatChip(
                  icon: Icons.arrow_upward,
                  label: '\$${stats.totalExpenses.toStringAsFixed(0)}',
                  color: AppTheme.errorColor,
                ),
              ],
            ),
            if (stats.totalHours > 0) ...[
              const SizedBox(height: 8),
              Text(
                'Rate: \$${(stats.profit / stats.totalHours).toStringAsFixed(2)}/hour',
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _StatChip extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;

  const _StatChip({
    required this.icon,
    required this.label,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: color),
          const SizedBox(width: 4),
          Text(
            label,
            style: TextStyle(
              color: color,
              fontSize: 12,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }
}

class _MonthlyDataCard extends StatelessWidget {
  final MonthlyData data;

  const _MonthlyDataCard({required this.data});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final dateFormat = DateFormat('MMMM yyyy');

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Text(
                  dateFormat.format(data.month),
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const Spacer(),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: data.profit >= 0
                        ? AppTheme.successColor.withValues(alpha: 0.15)
                        : AppTheme.errorColor.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    '\$${data.profit.toStringAsFixed(0)}',
                    style: TextStyle(
                      color: data.profit >= 0
                          ? AppTheme.successColor
                          : AppTheme.errorColor,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                _MonthlyStatItem(
                  icon: Icons.arrow_downward,
                  label: 'Income',
                  value: '\$${data.income.toStringAsFixed(0)}',
                  color: AppTheme.successColor,
                ),
                _MonthlyStatItem(
                  icon: Icons.arrow_upward,
                  label: 'Expenses',
                  value: '\$${data.expenses.toStringAsFixed(0)}',
                  color: AppTheme.errorColor,
                ),
                _MonthlyStatItem(
                  icon: Icons.access_time,
                  label: 'Hours',
                  value: '${data.hours.toStringAsFixed(1)}h',
                  color: AppTheme.accentColor,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _MonthlyStatItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  final Color color;

  const _MonthlyStatItem({
    required this.icon,
    required this.label,
    required this.value,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Column(
      children: [
        Icon(icon, color: color, size: 20),
        const SizedBox(height: 4),
        Text(
          value,
          style: theme.textTheme.titleSmall?.copyWith(
            fontWeight: FontWeight.bold,
          ),
        ),
        Text(
          label,
          style: theme.textTheme.bodySmall?.copyWith(
            color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
          ),
        ),
      ],
    );
  }
}
