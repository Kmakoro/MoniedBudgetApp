# 💰 MoniedBudgetApp

A comprehensive, feature-rich Android application designed for personal finance management. Track your expenses, set ambitious savings goals, manage monthly budgets, and earn achievement badges as you master your finances. 

Built with a focus on **core Android development principles**, this project demonstrates high-level proficiency in UI design, data persistence, and interactive user experiences.

## 📱 Key Features

### 💎 Gamification & Achievements
- **40+ Unlockable Badges**: From "First Step" to "Wealth Builder", the app rewards consistent tracking and smart saving.
- **Milestone Tracking**: Earn rewards for logging expenses, reaching savings goals, and staying under budget.
- **Dynamic Feedback**: Real-time notifications (toasts) when achievements are unlocked.

### 📈 Advanced Analytics
- **Spending Insights**: Interactive bar charts (powered by **MPAndroidChart**) visualizing spending across categories.
- **Performance Cards**: Quick summaries of your monthly budget status (On Track, Near Limit, or Over Budget).
- **Category Summary**: Deep dive into where your money goes with selectable date ranges.

### 🎯 Goal Setting
- **Savings Goals**: Create dedicated targets for emergencies, holidays, or major purchases. Track progress with visual indicators.
- **Monthly Budgeting**: Set minimum and maximum spending targets for each month to maintain financial discipline.

### 📸 Expense Management
- **Photo Evidence**: Capture receipt photos using the system camera or upload from the gallery.
- **Detailed Entries**: Log amounts, categories, descriptions, and precise time ranges.
- **Search & Filter**: View history by date range or specific categories.

---

## 🛠️ Technical Implementation

### Data Persistence Architecture
This project uniquely implements **Manual SQLite** (using `SQLiteOpenHelper`) alongside modern **Room Persistence Library**. This dual approach demonstrates:
- **Raw SQL Proficiency**: Complex join queries and cursor management for detailed reporting.
- **Background Operations**: Database tasks handled via Coroutines and Handlers to ensure smooth UI performance.
- **Schema Management**: Versioned database upgrades and multi-table relationships.

### UI/UX Design
- **Material Design 3**: Modern, clean interface with standard components.
- **ViewBinding & DataBinding**: Efficient UI interaction and data-to-view mapping.
- **Dynamic Navigation**: Bottom navigation for quick access to core features.

---

## 📂 Project Structure

```text
app/src/main/java/com/monied/budgetapp/
├── adapters/          # RecyclerView adapters (Expenses, Savings, Categories, Badges)
├── data/              # Persistence Layer
│   ├── database/      # Room DB configuration & Type Converters
│   ├── dao/           # Data Access Objects for Room
│   ├── model/         # Database Entities (Expense, Category, BudgetGoal, etc.)
│   └── DatabaseHelper.kt # Core Manual SQLite implementation & Business Logic
├── dialog/            # Custom UI Dialogs (Date Pickers, Badge Dialogs)
├── models/            # Domain and UI-specific data models
├── ui/                # UI Components
│   ├── auth/          # Login, Registration, and Session management
│   ├── fragments/     # Main feature fragments (Dashboard, History, Analytics)
│   └── main/          # Core Activities (AddExpense, SavingsGoal, SpendingInsights)
├── utils/             # Utility classes and GamificationManager
└── MoniedApplication.kt # Application class & Global dependency initialization
```

---

## 📥 Installation

### Prerequisites
- Android Studio (Iguana 2023.2.1 or newer)
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)

### Setup Steps
1. **Clone the repository**:
   ```bash
   git clone https://github.com/Kmakoro/MoniedBudgetApp.git
   ```
2. **Open in Android Studio**: Wait for Gradle to finish sync and indexing.
3. **Run**: Select an emulator or connected device and press `Shift + F10`.

---

## 🧪 Testing & Demo
- **Default Credentials**: 
  - Username: `cyril`
  - Password: `Password@123`
- **Sample Data**: The app initializes with default categories (Groceries, Transport, etc.) to get you started immediately.

## ▶️ Video Demo
[![Monied App Demo](https://img.shields.io/badge/YouTube-Watch%20Demo-red?style=for-the-badge&logo=youtube)](https://youtu.be/N9Pcvy3KOyg?si=YerJHw-VAHihpZtq)
