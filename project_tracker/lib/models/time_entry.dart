import 'package:uuid/uuid.dart';

class TimeEntry {
  final String id;
  final String projectId;
  final double hours;
  final String? description;
  final DateTime date;
  final DateTime createdAt;

  TimeEntry({
    String? id,
    required this.projectId,
    required this.hours,
    this.description,
    required this.date,
    DateTime? createdAt,
  })  : id = id ?? const Uuid().v4(),
        createdAt = createdAt ?? DateTime.now();

  TimeEntry copyWith({
    double? hours,
    String? description,
    DateTime? date,
  }) {
    return TimeEntry(
      id: id,
      projectId: projectId,
      hours: hours ?? this.hours,
      description: description ?? this.description,
      date: date ?? this.date,
      createdAt: createdAt,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'projectId': projectId,
      'hours': hours,
      'description': description,
      'date': date.millisecondsSinceEpoch,
      'createdAt': createdAt.millisecondsSinceEpoch,
    };
  }

  factory TimeEntry.fromMap(Map<String, dynamic> map) {
    return TimeEntry(
      id: map['id'],
      projectId: map['projectId'],
      hours: map['hours'].toDouble(),
      description: map['description'],
      date: DateTime.fromMillisecondsSinceEpoch(map['date']),
      createdAt: DateTime.fromMillisecondsSinceEpoch(map['createdAt']),
    );
  }
}
