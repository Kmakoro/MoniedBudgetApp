
# 💰 MoniedBudgetApp

A comprehensive Android application for tracking personal expenses, managing budgets by category, and monitoring spending goals. Built with **SQLite** (no RoomDB) to demonstrate core Android development concepts including layouts, event handling, intents, and data persistence.

## 📱 Features

### Core Functionality
- ✅ **User Authentication** - Login system with username/password
- ✅ **Category Management** - Create and manage expense categories
- ✅ **Expense Tracking** - Add expenses with date, time range, description, category, and optional photos
- ✅ **Monthly Goals** - Set minimum and maximum spending targets
- ✅ **Periodic Reports** - View expenses within user-selectable date ranges
- ✅ **Category Summary** - See total spending per category for any period
- ✅ **Photo Evidence** - Capture and attach photos to expense entries
- ✅ **Data Persistence** - All data saved locally using SQLite database

### Technical Highlights
- 📐 **Multiple Layouts** - ConstraintLayout, LinearLayout, RelativeLayout
- 📝 **EditText & NumberFormat** - Input validation and currency formatting
- 🎚️ **SeekBar** - Interactive goal setting interface
- 🎯 **Event Handling** - Click listeners, text watchers, seek bar listeners
- 🔄 **Intents** - Explicit (activity navigation) & Implicit (camera, image viewing)
- 🗄️ **SQLite Database** - Complete CRUD operations without RoomDB abstraction

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| **Language** | Java / Kotlin (choose one) |
| **Database** | SQLite (SQLiteOpenHelper) |
| **UI** | XML layouts + Material Design |
| **Image Handling** | Camera Intent + Internal Storage |
| **Date/Time** | DatePickerDialog, TimePickerDialog |
| **Concurrency** | AsyncTask / ExecutorService |
| **Session Management** | SharedPreferences |

## 📥 Installation

### Prerequisites
- Android Studio (Latest stable version)
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/Kmakoro/MoniedBudgetApp.git
   cd MoniedBudgetApp
