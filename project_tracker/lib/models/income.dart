import 'package:uuid/uuid.dart';

class Income {
  final String id;
  final String projectId;
  final double amount;
  final String? description;
  final DateTime date;
  final DateTime createdAt;

  Income({
    String? id,
    required this.projectId,
    required this.amount,
    this.description,
    required this.date,
    DateTime? createdAt,
  })  : id = id ?? const Uuid().v4(),
        createdAt = createdAt ?? DateTime.now();

  Income copyWith({
    double? amount,
    String? description,
    DateTime? date,
  }) {
    return Income(
      id: id,
      projectId: projectId,
      amount: amount ?? this.amount,
      description: description ?? this.description,
      date: date ?? this.date,
      createdAt: createdAt,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'projectId': projectId,
      'amount': amount,
      'description': description,
      'date': date.millisecondsSinceEpoch,
      'createdAt': createdAt.millisecondsSinceEpoch,
    };
  }

  factory Income.fromMap(Map<String, dynamic> map) {
    return Income(
      id: map['id'],
      projectId: map['projectId'],
      amount: map['amount'].toDouble(),
      description: map['description'],
      date: DateTime.fromMillisecondsSinceEpoch(map['date']),
      createdAt: DateTime.fromMillisecondsSinceEpoch(map['createdAt']),
    );
  }
}
