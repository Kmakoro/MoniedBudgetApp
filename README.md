
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
   

# 🚀 Usage Guide
## 1. First Time Setup
Launch the app

Create an account (Sign Up)

Login with your credentials

## 2. Create Categories
Navigate to "Manage Categories"

Tap "Add Category"

Enter category name (e.g., "Food", "Transport", "Entertainment")

## 3. Add an Expense
Tap "Add Expense" on main screen

Fill in:

Date (select from calendar)

Start/End times

Description

Amount spent

Category (from dropdown)

Optional: Tap camera icon to attach photo

Tap "Save"

## 4. Set Monthly Goals
Go to "Monthly Goals"

Use SeekBars or enter values for:

Minimum spending goal

Maximum spending goal

Goals are saved per month

## 5. View Reports
Expense List: Select date range → View all expenses with photos

Category Summary: Select date range → View total per category


This project explicitly uses raw SQLite instead of RoomDB to demonstrate:

Manual SQLiteOpenHelper implementation

Raw SQL queries with rawQuery()

Cursor management and data mapping

Database operations on background threads

Photo Handling
Photos captured via Intent(MediaStore.ACTION_IMAGE_CAPTURE)

Stored in app-specific external storage directory

Path saved as string in database

Retrieved and displayed using BitmapFactory

## 🧪 Testing
Sample Test Data
Default User:

Username: cyril

Password: password123

Test Categories:

Food & Dining

Transportation

Shopping

Entertainment

Bills & Utilities

## 📋 Requirements Checklist
Apply layouts in an app

Use EditText, NumberFormat, and SeekBar

Apply event handling

Create an activity

Apply an intent in an application

Reading and writing to SQLite (not RoomDB)

User login authentication

Category creation

Expense entry with date, times, description, category

Optional photo attachment

Monthly min/max goals

View expenses by selectable period

Photo access from expense list

Category totals by selectable period

## 🐛 Known Issues & Limitations
Photos are stored locally; deleting app removes all images

No cloud backup or sync functionality

Password hashing not implemented (plain text for prototype)

No email/password recovery

Reports don't support CSV/PDF export

## 🔜 Future Enhancements
Password encryption (SHA-256 or bcrypt)

Export reports to CSV/PDF

Data visualization (pie charts, bar graphs)

Recurring expenses automation

Budget alerts and notifications

Dark mode support

Multi-currency support

Backup/Restore functionality

Fingerprint/Face ID login

## ▶️ YouTube
   ```bash
   https://youtu.be/N9Pcvy3KOyg?si=YerJHw-VAHihpZtq
