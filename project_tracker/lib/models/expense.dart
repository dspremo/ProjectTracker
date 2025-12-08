import 'package:uuid/uuid.dart';

class Expense {
  final String id;
  final String projectId;
  final double amount;
  final String? description;
  final String? category;
  final DateTime date;
  final DateTime createdAt;

  Expense({
    String? id,
    required this.projectId,
    required this.amount,
    this.description,
    this.category,
    required this.date,
    DateTime? createdAt,
  })  : id = id ?? const Uuid().v4(),
        createdAt = createdAt ?? DateTime.now();

  Expense copyWith({
    double? amount,
    String? description,
    String? category,
    DateTime? date,
  }) {
    return Expense(
      id: id,
      projectId: projectId,
      amount: amount ?? this.amount,
      description: description ?? this.description,
      category: category ?? this.category,
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
      'category': category,
      'date': date.millisecondsSinceEpoch,
      'createdAt': createdAt.millisecondsSinceEpoch,
    };
  }

  factory Expense.fromMap(Map<String, dynamic> map) {
    return Expense(
      id: map['id'],
      projectId: map['projectId'],
      amount: map['amount'].toDouble(),
      description: map['description'],
      category: map['category'],
      date: DateTime.fromMillisecondsSinceEpoch(map['date']),
      createdAt: DateTime.fromMillisecondsSinceEpoch(map['createdAt']),
    );
  }
}
