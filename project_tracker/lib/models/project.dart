import 'package:uuid/uuid.dart';

enum ProjectStatus { active, done }

class Project {
  final String id;
  final String name;
  final double price;
  final DateTime startDate;
  final ProjectStatus status;
  final DateTime createdAt;
  final DateTime updatedAt;

  Project({
    String? id,
    required this.name,
    required this.price,
    required this.startDate,
    this.status = ProjectStatus.active,
    DateTime? createdAt,
    DateTime? updatedAt,
  })  : id = id ?? const Uuid().v4(),
        createdAt = createdAt ?? DateTime.now(),
        updatedAt = updatedAt ?? DateTime.now();

  Project copyWith({
    String? name,
    double? price,
    DateTime? startDate,
    ProjectStatus? status,
    DateTime? updatedAt,
  }) {
    return Project(
      id: id,
      name: name ?? this.name,
      price: price ?? this.price,
      startDate: startDate ?? this.startDate,
      status: status ?? this.status,
      createdAt: createdAt,
      updatedAt: updatedAt ?? DateTime.now(),
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
      'price': price,
      'startDate': startDate.millisecondsSinceEpoch,
      'status': status.index,
      'createdAt': createdAt.millisecondsSinceEpoch,
      'updatedAt': updatedAt.millisecondsSinceEpoch,
    };
  }

  factory Project.fromMap(Map<String, dynamic> map) {
    return Project(
      id: map['id'],
      name: map['name'],
      price: map['price'].toDouble(),
      startDate: DateTime.fromMillisecondsSinceEpoch(map['startDate']),
      status: ProjectStatus.values[map['status']],
      createdAt: DateTime.fromMillisecondsSinceEpoch(map['createdAt']),
      updatedAt: DateTime.fromMillisecondsSinceEpoch(map['updatedAt']),
    );
  }

  @override
  String toString() {
    return 'Project(id: $id, name: $name, price: $price, startDate: $startDate, status: $status)';
  }
}
