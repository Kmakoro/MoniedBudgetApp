package com.monied.budgetapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.monied.budgetapp.models.Category
import com.monied.budgetapp.models.Expense
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MoniedProV4.db"
        private const val DATABASE_VERSION = 4

        const val TABLE_USERS = "Users"
        const val COL_USER_ID = "id"
        const val COL_USERNAME = "username"
        const val COL_PASSWORD = "password"
        const val COL_FULL_NAME = "fullName"
        const val COL_EMAIL = "email"
        const val COL_PHONE = "phone"
        const val COL_CREATED_AT = "createdAt"

        const val TABLE_CATEGORIES = "Categories"
        const val COL_CATEGORY_ID = "id"
        const val COL_CATEGORY_NAME = "name"
        const val COL_CATEGORY_USER_ID = "userId"

        const val TABLE_EXPENSES = "Expenses"
        const val COL_EXPENSE_ID = "id"
        const val COL_AMOUNT = "amount"
        const val COL_DESCRIPTION = "description"
        const val COL_DATE = "date"
        const val COL_START_TIME = "startTime"
        const val COL_END_TIME = "endTime"
        const val COL_PHOTO_URI = "photoUri"
        const val COL_CATEGORY_REF = "categoryId"
        const val COL_EXPENSE_USER_ID = "userId"

        const val TABLE_SAVINGS_GOALS = "SavingsGoals"
        const val COL_GOAL_ID = "id"
        const val COL_GOAL_NAME = "name"
        const val COL_TARGET_AMOUNT = "targetAmount"
        const val COL_CURRENT_AMOUNT = "currentAmount"
        const val COL_TARGET_DATE = "targetDate"
        const val COL_SAVINGS_USER_ID = "userId"

        const val TABLE_BUDGET_GOALS = "BudgetGoals"
        const val COL_BUDGET_ID = "id"
        const val COL_BUDGET_MONTH = "month"
        const val COL_BUDGET_MIN = "minGoal"
        const val COL_BUDGET_MAX = "maxGoal"
        const val COL_BUDGET_USER_ID = "userId"

        const val TABLE_ALERTS = "BudgetAlerts"
        const val COL_ALERT_ID = "id"
        const val COL_ALERT_USER_ID = "userId"
        const val COL_ALERT_TITLE = "title"
        const val COL_ALERT_MESSAGE = "message"
        const val COL_ALERT_DATE = "date"

        const val TABLE_BADGES = "Badges"
        const val COL_BADGE_ID = "id"
        const val COL_BADGE_USER_ID = "userId"
        const val COL_BADGE_NAME = "badgeName"
        const val COL_BADGE_DATE = "dateAwarded"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_USERS ($COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_USERNAME TEXT UNIQUE, $COL_PASSWORD TEXT, $COL_FULL_NAME TEXT, $COL_EMAIL TEXT, $COL_PHONE TEXT, $COL_CREATED_AT TEXT)")
        db.execSQL("CREATE TABLE $TABLE_CATEGORIES ($COL_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_CATEGORY_NAME TEXT, $COL_CATEGORY_USER_ID INTEGER, FOREIGN KEY($COL_CATEGORY_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID))")
        db.execSQL("CREATE TABLE $TABLE_EXPENSES ($COL_EXPENSE_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_AMOUNT REAL, $COL_DESCRIPTION TEXT, $COL_DATE TEXT, $COL_START_TIME TEXT, $COL_END_TIME TEXT, $COL_PHOTO_URI TEXT, $COL_CATEGORY_REF INTEGER, $COL_EXPENSE_USER_ID INTEGER, FOREIGN KEY($COL_CATEGORY_REF) REFERENCES $TABLE_CATEGORIES($COL_CATEGORY_ID), FOREIGN KEY($COL_EXPENSE_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID))")
        db.execSQL("CREATE TABLE $TABLE_SAVINGS_GOALS ($COL_GOAL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_GOAL_NAME TEXT, $COL_TARGET_AMOUNT REAL, $COL_CURRENT_AMOUNT REAL, $COL_TARGET_DATE TEXT, $COL_SAVINGS_USER_ID INTEGER, FOREIGN KEY($COL_SAVINGS_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID))")
        db.execSQL("CREATE TABLE $TABLE_BUDGET_GOALS ($COL_BUDGET_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_BUDGET_MONTH TEXT, $COL_BUDGET_MIN REAL, $COL_BUDGET_MAX REAL, $COL_BUDGET_USER_ID INTEGER, FOREIGN KEY($COL_BUDGET_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID))")
        db.execSQL("CREATE TABLE $TABLE_ALERTS ($COL_ALERT_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_ALERT_USER_ID INTEGER, $COL_ALERT_TITLE TEXT, $COL_ALERT_MESSAGE TEXT, $COL_ALERT_DATE TEXT, FOREIGN KEY($COL_ALERT_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID))")
        db.execSQL("CREATE TABLE $TABLE_BADGES ($COL_BADGE_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_BADGE_USER_ID INTEGER, $COL_BADGE_NAME TEXT, $COL_BADGE_DATE TEXT, FOREIGN KEY($COL_BADGE_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID))")

        // Demo User
        val values = ContentValues().apply {
            put(COL_USERNAME, "cyril")
            put(COL_PASSWORD, "Password@123")
            put(COL_FULL_NAME, "Cyril Ramaphosa")
            put(COL_EMAIL, "cyril@monied.app")
            put(COL_PHONE, "+27 82 123 4567")
            put(COL_CREATED_AT, "2024-01-01")
        }
        val id = db.insert(TABLE_USERS, null, values)
        if (id != -1L) initializeUserCategories(db, id.toInt())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BADGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALERTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGET_GOALS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SAVINGS_GOALS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // --- User Methods ---

    fun registerUser(username: String, password: String, fullName: String, email: String, phone: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USERNAME, username.trim())
            put(COL_PASSWORD, password)
            put(COL_FULL_NAME, fullName)
            put(COL_EMAIL, email)
            put(COL_PHONE, phone)
            put(COL_CREATED_AT, SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
        }
        return try { db.insert(TABLE_USERS, null, values) } catch (e: Exception) { -1L }
    }

    fun checkUserCredentials(username: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COL_USERNAME = ? AND $COL_PASSWORD = ?", arrayOf(username.trim(), password))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUser(username: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COL_USERNAME = ?", arrayOf(username.trim()))
        if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
                fullName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)),
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
            )
            cursor.close()
            return user
        }
        cursor.close()
        return null
    }

    // --- Category Methods ---

    fun initializeUserCategories(userId: Int) { initializeUserCategories(writableDatabase, userId) }

    fun initializeUserCategories(db: SQLiteDatabase, userId: Int) {
        val categories = listOf("Groceries", "Entertainment", "Transport", "Utilities", "Health", "Personal")
        for (cat in categories) {
            val values = ContentValues().apply {
                put(COL_CATEGORY_NAME, cat)
                put(COL_CATEGORY_USER_ID, userId)
            }
            db.insert(TABLE_CATEGORIES, null, values)
        }
    }

    fun getAllCategories(userId: Int): List<Category> {
        val list = mutableListOf<Category>()
        val query = "SELECT c.*, (SELECT COUNT(*) FROM $TABLE_EXPENSES e WHERE e.$COL_CATEGORY_REF = c.$COL_CATEGORY_ID) as count, " +
                "(SELECT SUM($COL_AMOUNT) FROM $TABLE_EXPENSES e WHERE e.$COL_CATEGORY_REF = c.$COL_CATEGORY_ID) as total " +
                "FROM $TABLE_CATEGORIES c WHERE c.$COL_CATEGORY_USER_ID = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(Category(cursor.getInt(0), cursor.getString(1), cursor.getInt(3), cursor.getDouble(4)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addCategory(name: String, userId: Int): Long {
        val values = ContentValues().apply { put(COL_CATEGORY_NAME, name); put(COL_CATEGORY_USER_ID, userId) }
        val id = writableDatabase.insert(TABLE_CATEGORIES, null, values)
        if (id != -1L) checkAndAwardBadges(userId)
        return id
    }

    fun updateCategory(id: Int, name: String): Boolean {
        val values = ContentValues().apply { put(COL_CATEGORY_NAME, name) }
        return writableDatabase.update(TABLE_CATEGORIES, values, "$COL_CATEGORY_ID = ?", arrayOf(id.toString())) > 0
    }

    fun deleteCategory(id: Int): Boolean {
        return writableDatabase.delete(TABLE_CATEGORIES, "$COL_CATEGORY_ID = ?", arrayOf(id.toString())) > 0
    }

    // --- Expense Methods ---

    fun addExpense(amount: Double, description: String, date: String, startTime: String, endTime: String, categoryId: Int, userId: Int, photoUri: String? = null): Long {
        val values = ContentValues().apply {
            put(COL_AMOUNT, amount)
            put(COL_DESCRIPTION, description)
            put(COL_DATE, date)
            put(COL_START_TIME, startTime)
            put(COL_END_TIME, endTime)
            put(COL_CATEGORY_REF, categoryId)
            put(COL_EXPENSE_USER_ID, userId)
            put(COL_PHOTO_URI, photoUri)
        }
        val id = writableDatabase.insert(TABLE_EXPENSES, null, values)
        if (id != -1L) {
            checkAndAwardBadges(userId)
            checkBudgetAlerts(userId, amount)
        }
        return id
    }

    fun deleteExpense(expenseId: Int) {
        writableDatabase.delete(TABLE_EXPENSES, "$COL_EXPENSE_ID = ?", arrayOf(expenseId.toString()))
    }

    fun getExpensesByDateRange(startDate: String, endDate: String, userId: Int): List<Expense> {
        val list = mutableListOf<Expense>()
        var query = "SELECT e.*, c.$COL_CATEGORY_NAME FROM $TABLE_EXPENSES e JOIN $TABLE_CATEGORIES c ON e.$COL_CATEGORY_REF = c.$COL_CATEGORY_ID WHERE e.$COL_EXPENSE_USER_ID = ? "
        val params = mutableListOf<String>()
        params.add(userId.toString())

        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            query += "AND e.$COL_DATE BETWEEN ? AND ? "
            params.add(startDate)
            params.add(endDate)
        }
        query += "ORDER BY e.$COL_DATE DESC"

        val cursor = readableDatabase.rawQuery(query, params.toTypedArray())
        if (cursor.moveToFirst()) {
            do {
                list.add(Expense(cursor.getInt(0), cursor.getDouble(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(7), cursor.getString(6), cursor.getString(9)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getExpensesByCategory(categoryId: Int, userId: Int): List<Expense> {
        val list = mutableListOf<Expense>()
        val query = "SELECT e.*, c.$COL_CATEGORY_NAME FROM $TABLE_EXPENSES e JOIN $TABLE_CATEGORIES c ON e.$COL_CATEGORY_REF = c.$COL_CATEGORY_ID WHERE e.$COL_CATEGORY_REF = ? AND e.$COL_EXPENSE_USER_ID = ? ORDER BY e.$COL_DATE DESC"
        val cursor = readableDatabase.rawQuery(query, arrayOf(categoryId.toString(), userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(Expense(cursor.getInt(0), cursor.getDouble(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(7), cursor.getString(6), cursor.getString(9)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // --- Savings Methods ---

    fun getSavingsGoals(userId: Int): List<SavingsGoal> {
        val list = mutableListOf<SavingsGoal>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_SAVINGS_GOALS WHERE $COL_SAVINGS_USER_ID = ?", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(SavingsGoal(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addSavingsGoal(name: String, targetAmount: Double, currentAmount: Double, targetDate: String, userId: Int) {
        val values = ContentValues().apply { put(COL_GOAL_NAME, name); put(COL_TARGET_AMOUNT, targetAmount); put(COL_CURRENT_AMOUNT, currentAmount); put(COL_TARGET_DATE, targetDate); put(COL_SAVINGS_USER_ID, userId) }
        writableDatabase.insert(TABLE_SAVINGS_GOALS, null, values)
        checkAndAwardBadges(userId)
    }

    fun updateSavingsGoal(id: Int, name: String, targetAmount: Double, currentAmount: Double, targetDate: String) {
        val values = ContentValues().apply {
            put(COL_GOAL_NAME, name)
            put(COL_TARGET_AMOUNT, targetAmount)
            put(COL_CURRENT_AMOUNT, currentAmount)
            put(COL_TARGET_DATE, targetDate)
        }
        writableDatabase.update(TABLE_SAVINGS_GOALS, values, "$COL_GOAL_ID = ?", arrayOf(id.toString()))
        // Since savings goal amount changed, check badges
        val cursor = readableDatabase.rawQuery("SELECT $COL_SAVINGS_USER_ID FROM $TABLE_SAVINGS_GOALS WHERE $COL_GOAL_ID = ?", arrayOf(id.toString()))
        if (cursor.moveToFirst()) {
            checkAndAwardBadges(cursor.getInt(0))
        }
        cursor.close()
    }

    fun updateSavingsGoalAmount(id: Int, current: Double) {
        val values = ContentValues().apply { put(COL_CURRENT_AMOUNT, current) }
        writableDatabase.update(TABLE_SAVINGS_GOALS, values, "$COL_GOAL_ID = ?", arrayOf(id.toString()))
        // Since savings goal amount changed, check badges
        val cursor = readableDatabase.rawQuery("SELECT $COL_SAVINGS_USER_ID FROM $TABLE_SAVINGS_GOALS WHERE $COL_GOAL_ID = ?", arrayOf(id.toString()))
        if (cursor.moveToFirst()) {
            checkAndAwardBadges(cursor.getInt(0))
        }
        cursor.close()
    }

    fun deleteSavingsGoal(id: Int) {
        writableDatabase.delete(TABLE_SAVINGS_GOALS, "$COL_GOAL_ID = ?", arrayOf(id.toString()))
    }

    // --- Budget Methods ---

    fun getBudgetGoal(month: String, userId: Int): BudgetGoalData? {
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_BUDGET_GOALS WHERE $COL_BUDGET_MONTH = ? AND $COL_BUDGET_USER_ID = ?", arrayOf(month, userId.toString()))
        if (cursor.moveToFirst()) {
            val goal = BudgetGoalData(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_BUDGET_MIN)), cursor.getDouble(cursor.getColumnIndexOrThrow(COL_BUDGET_MAX)))
            cursor.close()
            return goal
        }
        cursor.close()
        return null
    }

    fun updateBudgetGoal(month: String, min: Double, max: Double, userId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply { put(COL_BUDGET_MONTH, month); put(COL_BUDGET_MIN, min); put(COL_BUDGET_MAX, max); put(COL_BUDGET_USER_ID, userId) }
        val affected = db.update(TABLE_BUDGET_GOALS, values, "$COL_BUDGET_MONTH = ? AND $COL_BUDGET_USER_ID = ?", arrayOf(month, userId.toString()))
        if (affected == 0) db.insert(TABLE_BUDGET_GOALS, null, values)
        checkAndAwardBadges(userId)
    }

    // --- Alerts & Badges ---

    fun getAlerts(userId: Int): List<BudgetAlertData> {
        val list = mutableListOf<BudgetAlertData>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_ALERTS WHERE $COL_ALERT_USER_ID = ? ORDER BY $COL_ALERT_DATE DESC", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(BudgetAlertData(cursor.getString(cursor.getColumnIndexOrThrow(COL_ALERT_TITLE)), cursor.getString(cursor.getColumnIndexOrThrow(COL_ALERT_MESSAGE)), cursor.getString(cursor.getColumnIndexOrThrow(COL_ALERT_DATE))))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    private fun checkBudgetAlerts(userId: Int, amount: Double) {
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        val budget = getBudgetGoal(currentMonth, userId) ?: return
        val totalSpent = getMonthlyTotalSpent(userId, currentMonth)

        if (totalSpent >= budget.maxGoal) {
            addAlert(userId, "Budget Exceeded", "You have exceeded your monthly budget of R${budget.maxGoal}")
        } else if (totalSpent >= budget.maxGoal * 0.8) {
            addAlert(userId, "Budget Warning", "You have reached 80% of your monthly budget.")
        }
    }

    private fun addAlert(userId: Int, title: String, message: String) {
        val values = ContentValues().apply {
            put(COL_ALERT_USER_ID, userId); put(COL_ALERT_TITLE, title); put(COL_ALERT_MESSAGE, message)
            put(COL_ALERT_DATE, SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date()))
        }
        writableDatabase.insert(TABLE_ALERTS, null, values)
    }

    fun checkAndAwardBadges(userId: Int) {
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        val totalSpent = getMonthlyTotalSpent(userId, currentMonth)
        val goals = getSavingsGoals(userId)
        val totalSaved = goals.sumOf { it.currentAmount }
        val expenseCount = getExpenseCount(userId)

        // Basic Badges
        if (expenseCount >= 1) awardBadge(userId, "First Step")
        if (totalSaved >= 500) awardBadge(userId, "Bronze Saver")
        if (totalSaved >= 2000) awardBadge(userId, "Silver Saver")
        if (totalSaved >= 5000) awardBadge(userId, "Gold Saver")
        if (totalSaved >= 10000) awardBadge(userId, "Savings Pro")

        if (expenseCount >= 15) awardBadge(userId, "Active Contributor")
        if (expenseCount >= 50) awardBadge(userId, "Half Century")
        if (expenseCount >= 100) awardBadge(userId, "Centurion")

        // Budget Badge
        val budgetGoal = getBudgetGoal(currentMonth, userId)
        if (budgetGoal != null && totalSpent > 0 && totalSpent <= budgetGoal.maxGoal) awardBadge(userId, "Budget Master")
        if (budgetGoal != null && totalSpent > 0 && totalSpent <= budgetGoal.maxGoal * 0.8) awardBadge(userId, "Budget Hero")

        // Goal Crusher
        if (goals.any { it.currentAmount >= it.targetAmount && it.targetAmount > 0 }) awardBadge(userId, "Goal Crusher")

        // Time & Specific Expense Badges
        checkTimeAndAmountBadges(userId)

        // Category Badges
        val categoriesUsed = getCategoriesUsedCount(userId)
        if (categoriesUsed >= 5) awardBadge(userId, "Categorizer")
        if (categoriesUsed >= 10) awardBadge(userId, "Diversified")

        if (getCategoryExpenseCount(userId, "Entertainment") >= 5) awardBadge(userId, "Entertainment Enthusiast")
        if (getCategoryExpenseCount(userId, "Transport") >= 10) awardBadge(userId, "Transport Titan")
        if (getCategoryExpenseCount(userId, "Groceries") >= 20) awardBadge(userId, "Groceries Guru")

        // Consistency Badge (5 different days)
        val activeDays = getActiveDaysCount(userId)
        if (activeDays >= 5) awardBadge(userId, "Financial Discipline")

        // Photo Badge
        val photoCount = getPhotoExpenseCount(userId)
        if (photoCount >= 5) awardBadge(userId, "Photo Enthusiast")
        if (photoCount >= 20) awardBadge(userId, "Photo Master")

        // Weekend Badge
        if (hasWeekendActivity(userId)) awardBadge(userId, "Weekend Warrior")

        // Streaks
        if (hasConsecutiveDays(userId, 3)) awardBadge(userId, "Streak Starter")
        if (hasConsecutiveDays(userId, 7)) awardBadge(userId, "Consistency King")

        // Master Planner
        if (getBudgetGoalCount(userId) >= 3) awardBadge(userId, "Master Planner")

        // Penny Pincher
        if (getSmallExpenseCount(userId, 50.0) >= 5) awardBadge(userId, "Penny Pincher")

        // Smart Spender
        if (getDetailedExpenseCount(userId) >= 10) awardBadge(userId, "Smart Spender")

        // Yearly Tracker
        if (getMonthsCount(userId) >= 12) awardBadge(userId, "Yearly Tracker")

        // Frugal February
        if (currentMonth.endsWith("-02")) {
            val budgetFeb = getBudgetGoal(currentMonth, userId)
            if (budgetFeb != null && totalSpent > 0 && totalSpent <= budgetFeb.maxGoal) {
                awardBadge(userId, "Frugal February")
            }
        }
    }

    private fun getCategoryExpenseCount(userId: Int, catName: String): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_EXPENSES e JOIN $TABLE_CATEGORIES c ON e.$COL_CATEGORY_REF = c.$COL_CATEGORY_ID WHERE e.$COL_EXPENSE_USER_ID = ? AND c.$COL_CATEGORY_NAME = ?", arrayOf(userId.toString(), catName))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    private fun getDetailedExpenseCount(userId: Int): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ? AND length($COL_DESCRIPTION) > 10", arrayOf(userId.toString()))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    private fun getMonthsCount(userId: Int): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(DISTINCT substr($COL_DATE, 1, 7)) FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ?", arrayOf(userId.toString()))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    private fun getActiveDaysCount(userId: Int): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(DISTINCT $COL_DATE) FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ?", arrayOf(userId.toString()))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    private fun getPhotoExpenseCount(userId: Int): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ? AND $COL_PHOTO_URI IS NOT NULL AND $COL_PHOTO_URI != ''", arrayOf(userId.toString()))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    private fun hasWeekendActivity(userId: Int): Boolean {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(DISTINCT strftime('%w', $COL_DATE)) FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ? AND strftime('%w', $COL_DATE) IN ('0', '6')", arrayOf(userId.toString()))
        var weekendDays = 0
        if (cursor.moveToFirst()) weekendDays = cursor.getInt(0)
        cursor.close()
        return weekendDays >= 2
    }

    private fun checkTimeAndAmountBadges(userId: Int) {
        val db = readableDatabase
        val earlyBirdCursor = db.rawQuery("SELECT * FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ? AND $COL_START_TIME != '' AND $COL_START_TIME < '07:00'", arrayOf(userId.toString()))
        if (earlyBirdCursor.count > 0) awardBadge(userId, "Early Bird")
        earlyBirdCursor.close()

        val middayCursor = db.rawQuery("SELECT * FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ? AND $COL_START_TIME BETWEEN '12:00' AND '14:00'", arrayOf(userId.toString()))
        if (middayCursor.count > 0) awardBadge(userId, "Midday Shopper")
        middayCursor.close()

        val nightOwlCursor = db.rawQuery("SELECT * FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ? AND $COL_START_TIME != '' AND $COL_START_TIME > '21:00'", arrayOf(userId.toString()))
        if (nightOwlCursor.count > 0) awardBadge(userId, "Night Owl")
        nightOwlCursor.close()

        val bigSpenderCursor = db.rawQuery("SELECT * FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ? AND $COL_AMOUNT > 1000", arrayOf(userId.toString()))
        if (bigSpenderCursor.count > 0) awardBadge(userId, "Big Spender")
        bigSpenderCursor.close()

        val highFlyerCursor = db.rawQuery("SELECT * FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ? AND $COL_AMOUNT > 5000", arrayOf(userId.toString()))
        if (highFlyerCursor.count > 0) awardBadge(userId, "High Flyer")
        highFlyerCursor.close()

        val luxuryCursor = db.rawQuery("SELECT * FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ? AND $COL_AMOUNT > 10000", arrayOf(userId.toString()))
        if (luxuryCursor.count > 0) awardBadge(userId, "Luxury Living")
        luxuryCursor.close()
    }

    private fun getSmallExpenseCount(userId: Int, limit: Double): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ? AND $COL_AMOUNT < ?", arrayOf(userId.toString(), limit.toString()))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    private fun getBudgetGoalCount(userId: Int): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_BUDGET_GOALS WHERE $COL_BUDGET_USER_ID = ?", arrayOf(userId.toString()))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    private fun hasConsecutiveDays(userId: Int, required: Int): Boolean {
        val cursor = readableDatabase.rawQuery("SELECT DISTINCT $COL_DATE FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ? ORDER BY $COL_DATE ASC", arrayOf(userId.toString()))
        if (cursor.count < required) { cursor.close(); return false }

        var streak = 1
        var lastDate: Date? = null
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        if (cursor.moveToFirst()) {
            do {
                val dateStr = cursor.getString(0)
                if (dateStr.isEmpty()) continue
                val currentDate = try { sdf.parse(dateStr) } catch(e: Exception) { null } ?: continue
                if (lastDate != null) {
                    val diff = (currentDate.time - lastDate.time) / (1000 * 60 * 60 * 24)
                    if (diff == 1L) {
                        streak++
                        if (streak >= required) { cursor.close(); return true }
                    } else {
                        streak = 1
                    }
                }
                lastDate = currentDate
            } while (cursor.moveToNext())
        }
        cursor.close()
        return false
    }

    private fun getCategoriesUsedCount(userId: Int): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(DISTINCT $COL_CATEGORY_REF) FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ?", arrayOf(userId.toString()))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    private fun getExpenseCount(userId: Int): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ?", arrayOf(userId.toString()))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    fun awardBadge(userId: Int, badgeName: String) {
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_BADGES WHERE $COL_BADGE_USER_ID = ? AND $COL_BADGE_NAME = ?", arrayOf(userId.toString(), badgeName))
        if (cursor.count == 0) {
            val values = ContentValues().apply {
                put(COL_BADGE_NAME, badgeName)
                put(COL_BADGE_DATE, SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
                put(COL_BADGE_USER_ID, userId)
            }
            writableDatabase.insert(TABLE_BADGES, null, values)

            // Live Notification (Tick)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "🏆 Achievement Unlocked: $badgeName!", Toast.LENGTH_LONG).show()
            }
        }
        cursor.close()
    }

    fun getAwardedBadges(userId: Int): List<String> {
        val list = mutableListOf<String>()
        val cursor = readableDatabase.rawQuery("SELECT $COL_BADGE_NAME FROM $TABLE_BADGES WHERE $COL_BADGE_USER_ID = ?", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) { do { list.add(cursor.getString(0)) } while (cursor.moveToNext()) }
        cursor.close()
        return list
    }

    // --- Statistics ---

    fun getMonthlyTotalSpent(userId: Int, month: String): Double {
        val pattern = "$month%"
        val cursor = readableDatabase.rawQuery("SELECT SUM($COL_AMOUNT) FROM $TABLE_EXPENSES WHERE $COL_EXPENSE_USER_ID = ? AND $COL_DATE LIKE ?", arrayOf(userId.toString(), pattern))
        var total = 0.0
        if (cursor.moveToFirst()) total = cursor.getDouble(0)
        cursor.close()
        return total
    }

    fun getCategorySpendingForDateRange(startDate: String, endDate: String, userId: Int): List<CategorySpending> {
        val list = mutableListOf<CategorySpending>()
        val query = "SELECT c.$COL_CATEGORY_NAME, SUM(e.$COL_AMOUNT) FROM $TABLE_EXPENSES e JOIN $TABLE_CATEGORIES c ON e.$COL_CATEGORY_REF = c.$COL_CATEGORY_ID WHERE e.$COL_DATE BETWEEN ? AND ? AND e.$COL_EXPENSE_USER_ID = ? GROUP BY c.$COL_CATEGORY_NAME ORDER BY SUM(e.$COL_AMOUNT) DESC"
        val cursor = readableDatabase.rawQuery(query, arrayOf(startDate, endDate, userId.toString()))
        if (cursor.moveToFirst()) { do { list.add(CategorySpending(cursor.getString(0), cursor.getDouble(1))) } while (cursor.moveToNext()) }
        cursor.close()
        return list
    }

    fun getWeeklyBreakdown(startDate: String, endDate: String, userId: Int): List<WeeklySpending> {
        val list = mutableListOf<WeeklySpending>()
        val query = "SELECT strftime('%W', $COL_DATE) as weekNum, MIN($COL_DATE) as weekStart, SUM($COL_AMOUNT) as total FROM $TABLE_EXPENSES WHERE $COL_DATE BETWEEN ? AND ? AND $COL_EXPENSE_USER_ID = ? GROUP BY weekNum ORDER BY weekNum"
        val cursor = readableDatabase.rawQuery(query, arrayOf(startDate, endDate, userId.toString()))
        if (cursor.moveToFirst()) { do { list.add(WeeklySpending("Week " + cursor.getString(0), cursor.getString(1), cursor.getDouble(2))) } while (cursor.moveToNext()) }
        cursor.close()
        return list
    }

    fun getUserStats(username: String): UserStats {
        val user = getUser(username) ?: return UserStats(0, 0, 1)
        val userId = user.id
        val expenseCount = getExpenseCount(userId)
        val catCursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_CATEGORIES WHERE $COL_CATEGORY_USER_ID = ?", arrayOf(userId.toString()))
        var categoryCount = 0
        if (catCursor.moveToFirst()) categoryCount = catCursor.getInt(0)
        catCursor.close()

        val createdDate = try { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(user.createdAt) } catch (e: Exception) { null }
        val daysActive = if (createdDate != null) {
            ((Date().time - createdDate.time) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
        } else 1
        return UserStats(expenseCount, categoryCount, daysActive)
    }
}

data class BudgetGoalData(val minGoal: Double, val maxGoal: Double)
data class User(val id: Int, val username: String, val fullName: String, val email: String, val phone: String, val createdAt: String)
data class CategorySpending(val categoryName: String, val total: Double)
data class WeeklySpending(val weekLabel: String, val weekStart: String, val total: Double)
data class UserStats(val expenseCount: Int, val categoryCount: Int, val daysActive: Int)
data class BudgetAlertData(val title: String, val message: String, val date: String)
