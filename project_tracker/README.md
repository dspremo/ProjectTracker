# Project Tracker 📊

A modern Flutter application for freelancers and professionals to track ongoing projects, manage time, expenses, and income with beautiful statistics and charts.

![Flutter](https://img.shields.io/badge/Flutter-3.x-blue.svg)
![Dart](https://img.shields.io/badge/Dart-3.x-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

## ✨ Features

### 📁 Project Management
- Create and manage multiple projects
- Track project name, price, start date, and status (Active/Done)
- Edit or delete projects with swipe actions
- Toggle project status between Active and Done

### ⏱️ Time Tracking
- Log hours worked on each project
- Add descriptions to time entries
- View total hours per project
- Calculate hourly rate based on profit

### 💰 Financial Tracking
- **Expenses**: Record project expenses with categories
- **Income**: Track payments received (installments)
- **Profit Calculation**: Automatic profit calculation (Income - Expenses)
- **Payment Progress**: Visual progress bar showing received vs total price

### 📈 Statistics & Analytics
- **Overview Dashboard**:
  - Total income, expenses, profit, and hours
  - Profit by project bar chart
  - Hours distribution pie chart
  - Detailed project statistics with hourly rates
  
- **Monthly Analytics**:
  - Income vs expenses trend line chart
  - Monthly hours bar chart
  - Month-by-month breakdown

## 🎨 Design

- **Material 3** design language
- **Light & Dark** theme support
- Smooth animations and transitions
- Modern rounded cards and typography
- Interactive charts with touch tooltips

## 🏗️ Architecture

```
lib/
├── main.dart                 # App entry point
├── models/                   # Data models
│   ├── project.dart
│   ├── time_entry.dart
│   ├── expense.dart
│   └── income.dart
├── providers/                # State management (Provider)
│   ├── project_provider.dart
│   ├── project_detail_provider.dart
│   └── statistics_provider.dart
├── screens/                  # UI screens
│   ├── home_screen.dart
│   ├── project_detail_screen.dart
│   ├── add_project_screen.dart
│   └── statistics_screen.dart
├── services/                 # Business logic
│   └── database_service.dart
├── theme/                    # App theming
│   └── app_theme.dart
└── widgets/                  # Reusable components
    ├── project_card.dart
    ├── stat_card.dart
    ├── empty_state.dart
    └── animations.dart
```

## 📦 Dependencies

| Package | Purpose |
|---------|---------|
| `provider` | State management |
| `sqflite` | Local SQLite database |
| `path_provider` | File system paths |
| `fl_chart` | Beautiful charts |
| `intl` | Date formatting |
| `uuid` | Unique ID generation |
| `animations` | Smooth transitions |

## 🚀 Getting Started

### Prerequisites
- Flutter SDK 3.x or higher
- Dart SDK 3.x or higher

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/project_tracker.git
cd project_tracker
```

2. Install dependencies:
```bash
flutter pub get
```

3. Run the app:
```bash
flutter run
```

### Build for Production

```bash
# Android
flutter build apk --release

# iOS
flutter build ios --release

# Web
flutter build web --release
```

## 📱 Screenshots

| Home Screen | Project Details | Statistics |
|-------------|-----------------|------------|
| Projects list with status tabs | Time, expenses & income tracking | Charts and analytics |

## 🔧 Usage

### Creating a Project
1. Tap the **"+ New Project"** button on the home screen
2. Enter project name, price, and start date
3. Select status (Active or Done)
4. Tap **"Create Project"**

### Tracking Time & Money
1. Open a project from the list
2. Tap the **"+"** button
3. Choose to add:
   - **Time Entry**: Hours worked + description
   - **Expense**: Amount + description + category
   - **Income**: Payment received + description

### Viewing Statistics
1. Tap the **"Statistics"** tab in the bottom navigation
2. Browse **Overview** for project comparisons
3. Switch to **By Month** for temporal analysis

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Flutter](https://flutter.dev/) - UI framework
- [fl_chart](https://pub.dev/packages/fl_chart) - Charts library
- [Provider](https://pub.dev/packages/provider) - State management

---

Made with ❤️ using Flutter
