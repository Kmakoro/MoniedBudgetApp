package com.monied.budgetapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.monied.budgetapp.models.Category
import java.text.SimpleDateFormat
import java.util.*

import com.monied.budgetapp.models.Expense

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "BudgetApp.db"
        private const val DATABASE_VERSION = 3

        const val TABLE_CATEGORIES = "Categories"
        const val COL_CATEGORY_ID = "id"
        const val COL_CATEGORY_NAME = "name"

        const val TABLE_EXPENSES = "Expenses"
        const val COL_EXPENSE_ID = "id"
        const val COL_AMOUNT = "amount"
        const val COL_DESCRIPTION = "description"
        const val COL_DATE = "date"
        const val COL_START_TIME = "startTime"
        const val COL_END_TIME = "endTime"
        const val COL_PHOTO_URI = "photoUri"
        const val COL_CATEGORY_REF = "categoryId"

        const val TABLE_INCOME = "Income"
        const val COL_INCOME_ID = "id"
        const val COL_INCOME_AMOUNT = "amount"
        const val COL_INCOME_DESCRIPTION = "description"
        const val COL_INCOME_DATE = "date"
        const val COL_INCOME_CATEGORY_REF = "categoryId"

        const val TABLE_SAVINGS_GOALS = "SavingsGoals"
        const val COL_GOAL_ID = "id"
        const val COL_GOAL_NAME = "name"
        const val COL_TARGET_AMOUNT = "targetAmount"
        const val COL_CURRENT_AMOUNT = "currentAmount"
        const val COL_TARGET_DATE = "targetDate"

        const val TABLE_USERS = "Users"
        const val COL_USER_ID = "id"
        const val COL_USERNAME = "username"
        const val COL_PASSWORD = "password"
        const val COL_FULL_NAME = "fullName"
        const val COL_EMAIL = "email"
        const val COL_PHONE = "phone"
        const val COL_CREATED_AT = "createdAt"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_CATEGORIES ($COL_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_CATEGORY_NAME TEXT UNIQUE)")
        db.execSQL("CREATE TABLE $TABLE_EXPENSES ($COL_EXPENSE_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_AMOUNT REAL, $COL_DESCRIPTION TEXT, $COL_DATE TEXT, $COL_START_TIME TEXT, $COL_END_TIME TEXT, $COL_PHOTO_URI TEXT, $COL_CATEGORY_REF INTEGER, FOREIGN KEY($COL_CATEGORY_REF) REFERENCES $TABLE_CATEGORIES($COL_CATEGORY_ID))")
        db.execSQL("CREATE TABLE $TABLE_INCOME ($COL_INCOME_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_INCOME_AMOUNT REAL, $COL_INCOME_DESCRIPTION TEXT, $COL_INCOME_DATE TEXT, $COL_INCOME_CATEGORY_REF INTEGER, FOREIGN KEY($COL_INCOME_CATEGORY_REF) REFERENCES $TABLE_CATEGORIES($COL_CATEGORY_ID))")
        db.execSQL("CREATE TABLE $TABLE_SAVINGS_GOALS ($COL_GOAL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_GOAL_NAME TEXT, $COL_TARGET_AMOUNT REAL, $COL_CURRENT_AMOUNT REAL, $COL_TARGET_DATE TEXT)")
        db.execSQL("CREATE TABLE $TABLE_USERS ($COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_USERNAME TEXT UNIQUE, $COL_PASSWORD TEXT, $COL_FULL_NAME TEXT, $COL_EMAIL TEXT, $COL_PHONE TEXT, $COL_CREATED_AT TEXT)")

        db.execSQL("INSERT INTO $TABLE_CATEGORIES ($COL_CATEGORY_NAME) VALUES ('Groceries')")
        db.execSQL("INSERT INTO $TABLE_CATEGORIES ($COL_CATEGORY_NAME) VALUES ('Salary')")
        db.execSQL("INSERT INTO $TABLE_CATEGORIES ($COL_CATEGORY_NAME) VALUES ('Transport')")
        db.execSQL("INSERT INTO $TABLE_CATEGORIES ($COL_CATEGORY_NAME) VALUES ('Entertainment')")
        db.execSQL("INSERT INTO $TABLE_CATEGORIES ($COL_CATEGORY_NAME) VALUES ('Utilities')")

        db.execSQL("INSERT INTO $TABLE_USERS ($COL_USERNAME, $COL_PASSWORD, $COL_FULL_NAME, $COL_EMAIL, $COL_PHONE, $COL_CREATED_AT) VALUES ('cyril', 'password123', 'Cyril Ramaphosa', 'cyril@monied.app', '+27 82 123 4567', datetime('now'))")



    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INCOME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SAVINGS_GOALS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun addCategory(categoryName: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply { put(COL_CATEGORY_NAME, categoryName) }
        return db.insert(TABLE_CATEGORIES, null, values)
    }

    fun getAllCategories(): List<Category> {
        val list = mutableListOf<Category>()
        val db = readableDatabase


        val query = """
            SELECT c.$COL_CATEGORY_ID, 
                   c.$COL_CATEGORY_NAME, 
                   COUNT(e.$COL_EXPENSE_ID) as expense_count,
                   COALESCE(SUM(e.$COL_AMOUNT), 0.0) as total_spent
            FROM $TABLE_CATEGORIES c
            LEFT JOIN $TABLE_EXPENSES e ON c.$COL_CATEGORY_ID = e.$COL_CATEGORY_REF
            GROUP BY c.$COL_CATEGORY_ID, c.$COL_CATEGORY_NAME
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CATEGORY_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY_NAME))
                val count = cursor.getInt(cursor.getColumnIndexOrThrow("expense_count"))
                val totalSpent = cursor.getDouble(cursor.getColumnIndexOrThrow("total_spent"))

                list.add(Category(id, name, count, totalSpent))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
    fun updateCategory(categoryId: Int, newName: String): Boolean {
        val db = this.writableDatabase
        val values = android.content.ContentValues().apply {
            put(COL_CATEGORY_NAME, newName)
        }

        // Update the row where the ID matches
        val rowsAffected = db.update(TABLE_CATEGORIES, values, "$COL_CATEGORY_ID = ?", arrayOf(categoryId.toString()))
        return rowsAffected > 0
    }

    fun deleteCategory(categoryId: Int): Boolean {
        val db = this.writableDatabase
        // Delete the row where the ID matches
        val rowsDeleted = db.delete(TABLE_CATEGORIES, "$COL_CATEGORY_ID = ?", arrayOf(categoryId.toString()))
        return rowsDeleted > 0
    }

    fun addExpense(amount: Double, description: String, date: String, startTime: String, endTime: String, categoryId: Int, photoUri: String? = null): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_AMOUNT, amount)
            put(COL_DESCRIPTION, description)
            put(COL_DATE, date)
            put(COL_START_TIME, startTime)
            put(COL_END_TIME, endTime)
            put(COL_CATEGORY_REF, categoryId)
            put(COL_PHOTO_URI, photoUri)
        }
        return db.insert(TABLE_EXPENSES, null, values)
    }

    fun registerUser(username: String, password: String, fullName: String, email: String, phone: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USERNAME, username.trim())
            put(COL_PASSWORD, password.trim())
            put(COL_FULL_NAME, fullName.trim())
            put(COL_EMAIL, email.trim())
            put(COL_PHONE, phone.trim())
            put(COL_CREATED_AT, SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun checkUserCredentials(username: String, password: String): Boolean {
        val db = readableDatabase
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE trim($COL_USERNAME) = ? AND $COL_PASSWORD = ?",
            arrayOf(trimmedUsername, trimmedPassword)
        )
        val valid = cursor.count > 0
        cursor.close()
        return valid
    }

    fun getUser(username: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE trim($COL_USERNAME) = ?", arrayOf(username.trim()))
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
    fun getExpensesByDateRange(startDate: String, endDate: String): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = readableDatabase
        val query = """
        SELECT e.*, c.${DatabaseHelper.COL_CATEGORY_NAME} as category_name 
        FROM ${DatabaseHelper.TABLE_EXPENSES} e
        LEFT JOIN ${DatabaseHelper.TABLE_CATEGORIES} c ON e.${DatabaseHelper.COL_CATEGORY_REF} = c.${DatabaseHelper.COL_CATEGORY_ID}
        WHERE e.${DatabaseHelper.COL_DATE} BETWEEN ? AND ?
        ORDER BY e.${DatabaseHelper.COL_DATE} DESC, e.${DatabaseHelper.COL_START_TIME} DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))

        if (cursor.moveToFirst()) {
            do {
                expenses.add(
                    Expense(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_ID)),
                        amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AMOUNT)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION)),
                        date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE)),
                        startTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_START_TIME)),
                        endTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_END_TIME)),
                        categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_REF)),
                        photoUri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHOTO_URI)),
                        categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return expenses
    }

    fun getExpensesByCategory(categoryId: Int): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = readableDatabase

        // We select the expenses where the categoryRef matches our clicked ID
        val query = """
            SELECT e.*, c.${COL_CATEGORY_NAME} as category_name 
            FROM $TABLE_EXPENSES e
            LEFT JOIN $TABLE_CATEGORIES c ON e.$COL_CATEGORY_REF = c.$COL_CATEGORY_ID
            WHERE e.$COL_CATEGORY_REF = ?
            ORDER BY e.$COL_DATE DESC, e.$COL_START_TIME DESC
        """.trimIndent()

        // Pass the categoryId as the search parameter
        val cursor = db.rawQuery(query, arrayOf(categoryId.toString()))

        if (cursor.moveToFirst()) {
            do {
                expenses.add(
                    Expense(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EXPENSE_ID)),
                        amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                        date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                        startTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_START_TIME)),
                        endTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_END_TIME)),
                        categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CATEGORY_REF)),
                        photoUri = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHOTO_URI)),
                        categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return expenses
    }
    /*fun getExpensesByDateRange(startDate: String, endDate: String): List<ExpenseWithCategory> {
        val list = mutableListOf<ExpenseWithCategory>()
        val db = readableDatabase
        val query = """
            SELECT e.*, c.$COL_CATEGORY_NAME as categoryName
            FROM $TABLE_EXPENSES e
            JOIN $TABLE_CATEGORIES c ON e.$COL_CATEGORY_REF = c.$COL_CATEGORY_ID
            WHERE date(e.$COL_DATE) BETWEEN date(?) AND date(?)
            ORDER BY e.$COL_DATE DESC, e.$COL_START_TIME DESC
        """
        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))
        if (cursor.moveToFirst()) {
            do {
                list.add(ExpenseWithCategory(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EXPENSE_ID)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                    startTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_START_TIME)),
                    endTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_END_TIME)),
                    photoUri = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHOTO_URI)),
                    categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CATEGORY_REF)),
                    categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }*/

    fun getCategorySpendingForDateRange(startDate: String, endDate: String): List<CategorySpending> {
        val list = mutableListOf<CategorySpending>()
        val db = readableDatabase
        val query = """
            SELECT c.$COL_CATEGORY_NAME, SUM(e.$COL_AMOUNT) as total
            FROM $TABLE_EXPENSES e
            JOIN $TABLE_CATEGORIES c ON e.$COL_CATEGORY_REF = c.$COL_CATEGORY_ID
            WHERE date(e.$COL_DATE) BETWEEN date(?) AND date(?)
            GROUP BY c.$COL_CATEGORY_NAME
            ORDER BY total DESC
        """
        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))
        if (cursor.moveToFirst()) {
            do {
                list.add(CategorySpending(cursor.getString(0), cursor.getDouble(1)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getWeeklyBreakdown(startDate: String, endDate: String): List<WeeklySpending> {
        val list = mutableListOf<WeeklySpending>()
        val db = readableDatabase
        val query = """
            SELECT strftime('%W', date($COL_DATE)) as weekNum, MIN(date($COL_DATE)) as weekStart, SUM($COL_AMOUNT) as total
            FROM $TABLE_EXPENSES
            WHERE date($COL_DATE) BETWEEN date(?) AND date(?)
            GROUP BY weekNum
            ORDER BY weekNum
        """
        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))
        if (cursor.moveToFirst()) {
            do {
                list.add(WeeklySpending("Week ${cursor.getInt(0)}", cursor.getString(1), cursor.getDouble(2)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getUserStats(username: String): UserStats {
        val db = readableDatabase
        var expenseCount = 0
        val expCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_EXPENSES", null)
        if (expCursor.moveToFirst()) expenseCount = expCursor.getInt(0)
        expCursor.close()

        var categoryCount = 0
        val catCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CATEGORIES", null)
        if (catCursor.moveToFirst()) categoryCount = catCursor.getInt(0)
        catCursor.close()

        var daysActive = 0
        val userCursor = db.rawQuery("SELECT $COL_CREATED_AT FROM $TABLE_USERS WHERE trim($COL_USERNAME) = ?", arrayOf(username.trim()))
        if (userCursor.moveToFirst()) {
            val created = userCursor.getString(0)
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val createdDate = fmt.parse(created)
            val now = Date()
            val diff = now.time - createdDate!!.time
            daysActive = (diff / (24 * 60 * 60 * 1000)).toInt()
        }
        userCursor.close()
        return UserStats(expenseCount, categoryCount, daysActive)
    }

    fun getSavingsGoals(): List<SavingsGoal> {
        val list = mutableListOf<SavingsGoal>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SAVINGS_GOALS", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(SavingsGoal(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GOAL_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_NAME)),
                    targetAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TARGET_AMOUNT)),
                    currentAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_CURRENT_AMOUNT)),
                    targetDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_TARGET_DATE))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
    // Overloaded version with current amount parameter
    fun addSavingsGoal(name: String, targetAmount: Double, currentAmount: Double, targetDate: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_GOAL_NAME, name)
            put(COL_TARGET_AMOUNT, targetAmount)
            put(COL_CURRENT_AMOUNT, currentAmount)
            put(COL_TARGET_DATE, targetDate)
        }
        db.insert(TABLE_SAVINGS_GOALS, null, values)
        db.close()
    }

    // Function to update current savings amount for an existing goal
    fun updateSavingsGoalAmount(goalId: Int, newCurrentAmount: Double) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CURRENT_AMOUNT, newCurrentAmount)
        }
        db.update(TABLE_SAVINGS_GOALS, values, "$COL_GOAL_ID = ?", arrayOf(goalId.toString()))
        db.close()
    }

    // Function to add money to an existing savings goal
    fun addToSavingsGoal(goalId: Int, amountToAdd: Double) {
        val db = writableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_CURRENT_AMOUNT FROM $TABLE_SAVINGS_GOALS WHERE $COL_GOAL_ID = ?",
            arrayOf(goalId.toString())
        )

        var currentAmount = 0.0
        if (cursor.moveToFirst()) {
            currentAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_CURRENT_AMOUNT))
        }
        cursor.close()

        val newAmount = currentAmount + amountToAdd
        val values = ContentValues().apply {
            put(COL_CURRENT_AMOUNT, newAmount)
        }
        db.update(TABLE_SAVINGS_GOALS, values, "$COL_GOAL_ID = ?", arrayOf(goalId.toString()))
        db.close()
    }
    fun updateSavingsGoal(id: Int, name: String, targetAmount: Double, currentAmount: Double, targetDate: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_GOAL_NAME, name)
            put(COL_TARGET_AMOUNT, targetAmount)
            put(COL_CURRENT_AMOUNT, currentAmount)
            put(COL_TARGET_DATE, targetDate)
        }
        db.update(TABLE_SAVINGS_GOALS, values, "$COL_GOAL_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteSavingsGoal(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_SAVINGS_GOALS, "$COL_GOAL_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    // Add these methods to your existing DatabaseHelper class



    fun getAllExpenses(): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = readableDatabase
        val query = """
        SELECT e.*, c.${DatabaseHelper.COL_CATEGORY_NAME} as category_name 
        FROM ${DatabaseHelper.TABLE_EXPENSES} e
        LEFT JOIN ${DatabaseHelper.TABLE_CATEGORIES} c ON e.${DatabaseHelper.COL_CATEGORY_REF} = c.${DatabaseHelper.COL_CATEGORY_ID}
        ORDER BY e.${DatabaseHelper.COL_DATE} DESC, e.${DatabaseHelper.COL_START_TIME} DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                expenses.add(
                    Expense(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_ID)),
                        amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AMOUNT)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION)),
                        date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE)),
                        startTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_START_TIME)),
                        endTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_END_TIME)),
                        categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_REF)),
                        photoUri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHOTO_URI)),
                        categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return expenses
    }

    fun deleteExpense(expenseId: Int) {
        val db = writableDatabase
        db.delete(DatabaseHelper.TABLE_EXPENSES, "${DatabaseHelper.COL_EXPENSE_ID} = ?", arrayOf(expenseId.toString()))
        db.close()
    }

    fun getExpenseById(expenseId: Int): Expense? {
        val db = readableDatabase
        val query = """
        SELECT e.*, c.${DatabaseHelper.COL_CATEGORY_NAME} as category_name 
        FROM ${DatabaseHelper.TABLE_EXPENSES} e
        LEFT JOIN ${DatabaseHelper.TABLE_CATEGORIES} c ON e.${DatabaseHelper.COL_CATEGORY_REF} = c.${DatabaseHelper.COL_CATEGORY_ID}
        WHERE e.${DatabaseHelper.COL_EXPENSE_ID} = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(expenseId.toString()))
        var expense: Expense? = null

        if (cursor.moveToFirst()) {
            expense = Expense(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_ID)),
                amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AMOUNT)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION)),
                date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE)),
                startTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_START_TIME)),
                endTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_END_TIME)),
                categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_REF)),
                photoUri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHOTO_URI)),
                categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))
            )
        }
        cursor.close()
        db.close()
        return expense
    }

    fun getExpenseSummaryByDateRange(startDate: String, endDate: String): ExpenseSummary {
        val db = readableDatabase
        val query = """
        SELECT 
            COUNT(*) as total_count,
            SUM(${DatabaseHelper.COL_AMOUNT}) as total_amount,
            AVG(${DatabaseHelper.COL_AMOUNT}) as avg_amount,
            MIN(${DatabaseHelper.COL_AMOUNT}) as min_amount,
            MAX(${DatabaseHelper.COL_AMOUNT}) as max_amount
        FROM ${DatabaseHelper.TABLE_EXPENSES}
        WHERE ${DatabaseHelper.COL_DATE} BETWEEN ? AND ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))
        var summary = ExpenseSummary(0, 0.0, 0.0, 0.0, 0.0)

        if (cursor.moveToFirst()) {
            summary = ExpenseSummary(
                totalCount = cursor.getInt(0),
                totalAmount = cursor.getDouble(1),
                averageAmount = cursor.getDouble(2),
                minAmount = cursor.getDouble(3),
                maxAmount = cursor.getDouble(4)
            )
        }
        cursor.close()
        db.close()
        return summary
    }
}



data class ExpenseSummary(
    val totalCount: Int,
    val totalAmount: Double,
    val averageAmount: Double,
    val minAmount: Double,
    val maxAmount: Double
)

data class User(
    val id: Int,
    val username: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val createdAt: String
)

data class ExpenseWithCategory(
    val id: Int,
    val amount: Double,
    val description: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val photoUri: String?,
    val categoryId: Int,
    val categoryName: String
)

data class CategorySpending(
    val categoryName: String,
    val total: Double
)

data class WeeklySpending(
    val weekLabel: String,
    val weekStart: String,
    val total: Double
)

data class UserStats(
    val expenseCount: Int,
    val categoryCount: Int,
    val daysActive: Int
)

data class Category(
    val id: Int,
    val name: String,
    val expenseCount: Int = 0,
    val totalSpent: Double = 0.0
)

data class Expense(
    val id: Int,
    val amount: Double,
    val description: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val categoryId: Int,
    val photoUri: String? = null,
    val categoryName: String = ""
)