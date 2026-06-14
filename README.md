# 💰 Monied - Personal Finance & Budgeting App

Monied is a comprehensive, feature-rich Android application designed to empower users to take control of their personal finances. It combines powerful tracking tools with a unique gamification engine to make financial discipline engaging and rewarding.

Built with a focus on **modern Android development principles**, Monied demonstrates proficiency in complex data management, interactive UI/UX design, and performance optimization.

---

## 📱 Core Features

### 🏆 Advanced Gamification Engine
Monied turns financial management into a journey with an extensive achievement system:
- **40+ Unlockable Badges**: Categorized by behavior and milestones.
  - **Milestone Badges**: "First Step", "Active Contributor", "Centurion" (based on entry counts).
  - **Savings Badges**: "Bronze/Silver/Gold Saver" and "Savings Pro" as you grow your nest egg.
  - **Behavioral Badges**: "Early Bird" (morning logs), "Night Owl" (late-night tracking), and "Weekend Warrior".
  - **Discipline Badges**: "Streak Starter", "Consistency King", and "Financial Discipline" for regular usage.
  - **Speciality Badges**: "Penny Pincher" (small savings), "Smart Spender" (detailed logging), and "Frugal February".
- **Real-time Notifications**: Instant feedback via custom toasts and alerts when achievements are unlocked.
- **Badge Showcase**: A dedicated gallery to view earned rewards and progress.

### 📊 Precision Analytics & Reporting
Visualize your financial health with interactive data tools:
- **Dynamic Charts**: Powered by **MPAndroidChart**, providing high-level spending overviews.
- **Weekly Breakdowns**: Track spending trends week-over-week to identify habits.
- **Category Deep-Dives**: Analyze spending distribution across categories like Groceries, Transport, and Entertainment.
- **Custom Date Ranges**: Filter history and analytics to specific periods for targeted reviews.

### 🛡️ Smart Budgeting & Alerts
Stay on track with proactive financial monitoring:
- **Flexible Budgeting**: Set monthly minimum and maximum targets.
- **Intelligent Alerts**: 
  - **Warning System**: Receive alerts when reaching 80% of your monthly limit.
  - **Over-limit Alerts**: Instant notification when a budget is exceeded.
- **Budget Status Indicators**: Visual cues (On Track, Near Limit, Over Budget) across the dashboard.

### 🎯 Goal Tracking
- **Savings Targets**: Create goals for emergencies, vacations, or major purchases.
- **Visual Progress**: Real-time progress bars showing how close you are to your targets.
- **Goal Crusher Rewards**: Earn special recognition when you successfully reach a milestone.

### 📸 Rich Expense Management
- **Photo Evidence**: Attach receipt photos using the device camera or gallery integration (powered by **Glide**).
- **Time-Range Tracking**: Log not just the date, but precise time ranges for activities.
- **Detailed Categorization**: Pre-loaded categories with the ability to add and manage custom ones.
- **Advanced History**: Searchable and filterable list of all financial transactions.

---

## 🛠️ Technical Architecture

### Hybrid Data Persistence
Monied employs a sophisticated dual-layered storage strategy to demonstrate versatility:
- **Manual SQLite (SQLiteOpenHelper)**: Used for the core business logic, complex reporting queries, and the gamification engine. Features raw SQL proficiency with complex JOINs and aggregations.
- **Room Persistence Library**: Leveraged for structured data entities and modern reactive data patterns.
- **DataStore**: Used for lightweight preference management and session persistence.

### Modern Android Tech Stack
- **UI Framework**: Material Design 3 (M3) components for a modern, accessible interface.
- **Binding**: Extensive use of **ViewBinding** and **DataBinding** to reduce boilerplate and ensure type-safety.
- **Concurrency**: **Kotlin Coroutines** for smooth, non-blocking database operations.
- **Image Loading**: **Glide** for optimized bitmap handling and receipt photo caching.
- **Navigation**: **Jetpack Navigation Component** with Bottom Navigation for seamless flow.
- **Background Tasks**: **WorkManager** for scheduled tasks (where applicable).

---

## 📂 Project Structure

```text
app/src/main/java/com/monied/budgetapp/
├── adapters/          # RecyclerView adapters for Expenses, Savings, and Badges
├── data/              # Persistence Layer
│   ├── database/      # Room DB configuration & Type Converters
│   ├── dao/           # Data Access Objects (Room)
│   ├── model/         # Database Entities (Expense, Category, BudgetGoal)
│   └── DatabaseHelper.kt # Core SQLite Logic, Reporting, & Gamification Engine
├── dialog/            # Custom BottomSheet and Dialog fragments
├── models/            # Domain-specific data models
├── ui/                # UI Layer
│   ├── auth/          # Login & Registration flows
│   ├── fragments/     # Feature Fragments (Dashboard, History, Analytics, Profile)
│   └── main/          # Core Activities (AddExpense, SavingsGoal, Insights)
├── utils/             # Helper classes for dates, formatting, and UI
└── MoniedApplication.kt # Global context and dependency initialization
```

---

## 📥 Getting Started

### Prerequisites
- **Android Studio**: Iguana (2023.2.1) or newer recommended.
- **SDK**: Minimum API 24, Target API 34.

### Setup
1. Clone the repository: `git clone https://github.com/Kmakoro/MoniedBudgetApp.git`
2. Open the project in Android Studio.
3. Sync Gradle and build the project.
4. Run on an emulator or physical device.

### Demo Credentials
- **Username**: `cyril`
- **Password**: `Password@123`

---

## ▶️ Video Demo
[![Monied App Demo](https://img.shields.io/badge/YouTube-Watch%20Demo-red?style=for-the-badge&logo=youtube)](https://youtu.be/N9Pcvy3KOyg?si=YerJHw-VAHihpZtq)
