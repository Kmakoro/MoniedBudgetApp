package com.monied.budgetapp.utils

import android.content.Context
import com.monied.budgetapp.R
import com.monied.budgetapp.data.DatabaseHelper

class GamificationManager(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)

    enum class Badge(val title: String, val description: String, val iconRes: Int) {
        FIRST_STEP("First Step", "Logged your first expense", R.drawable.ic_trophy),
        BRONZE_SAVER("Bronze Saver", "Saved your first R500", R.drawable.ic_trophy),
        SILVER_SAVER("Silver Saver", "Saved over R2000", R.drawable.ic_trophy),
        GOLD_SAVER("Gold Saver", "Saved over R5000", R.drawable.ic_trophy),
        SAVINGS_PRO("Savings Pro", "Saved over R10000", R.drawable.ic_trophy),
        WEALTH_BUILDER("Wealth Builder", "Saved over R50000", R.drawable.ic_trophy),
        BUDGET_MASTER("Budget Master", "Stayed within budget for the month", R.drawable.ic_trophy),
        BUDGET_HERO("Budget Hero", "Stayed 20% under your budget", R.drawable.ic_trophy),
        ACTIVE_CONTRIBUTOR("Active Contributor", "Logged 15 or more expenses", R.drawable.ic_trophy),
        HALF_CENTURY("Half Century", "Logged 50 or more expenses", R.drawable.ic_trophy),
        CENTURION("Centurion", "Logged 100 or more expenses", R.drawable.ic_trophy),
        GOAL_CRUSHER("Goal Crusher", "Successfully reached a savings goal", R.drawable.ic_trophy),
        SAVINGS_MASTER("Savings Master", "Successfully reached 3 savings goals", R.drawable.ic_trophy),
        EARLY_BIRD("Early Bird", "Logged an expense before 7 AM", R.drawable.ic_trophy),
        MIDDAY_SHOPPER("Midday Shopper", "Logged an expense between 12 PM and 2 PM", R.drawable.ic_trophy),
        NIGHT_OWL("Night Owl", "Logged an expense after 9 PM", R.drawable.ic_trophy),
        PENNY_PINCHER("Penny Pincher", "Logged 5 expenses under R50", R.drawable.ic_trophy),
        BIG_SPENDER("Big Spender", "Logged an expense over R1000", R.drawable.ic_trophy),
        HIGH_FLYER("High Flyer", "Logged an expense over R5000", R.drawable.ic_trophy),
        LUXURY_LIVING("Luxury Living", "Logged an expense over R10000", R.drawable.ic_trophy),
        CATEGORIZER("Categorizer", "Used 5 or more different categories", R.drawable.ic_trophy),
        DIVERSIFIED("Diversified", "Used 10 or more different categories", R.drawable.ic_trophy),
        GROCERIES_GURU("Groceries Guru", "Logged 20 grocery expenses", R.drawable.ic_trophy),
        TRANSPORT_TITAN("Transport Titan", "Logged 10 transport expenses", R.drawable.ic_trophy),
        ENTERTAINMENT_ENTHUSIAST("Entertainment Enthusiast", "Logged 10 entertainment expenses", R.drawable.ic_trophy),
        HEALTH_CONSCIOUS("Health Conscious", "Logged 5 health expenses", R.drawable.ic_trophy),
        FINANCIAL_DISCIPLINE("Financial Discipline", "Logged expenses on 5 different days", R.drawable.ic_trophy),
        CONSISTENCY_KING("Consistency King", "Logged expenses for 7 consecutive days", R.drawable.ic_trophy),
        STREAK_STARTER("Streak Starter", "Logged expenses for 3 consecutive days", R.drawable.ic_trophy),
        PHOTO_ENTHUSIAST("Photo Enthusiast", "Attached photos to 5 or more expenses", R.drawable.ic_trophy),
        PHOTO_MASTER("Photo Master", "Attached photos to 20 or more expenses", R.drawable.ic_trophy),
        WEEKEND_WARRIOR("Weekend Warrior", "Logged expenses on a Saturday and Sunday", R.drawable.ic_trophy),
        MASTER_PLANNER("Master Planner", "Set budget goals for 3 different months", R.drawable.ic_trophy),
        SMART_SPENDER("Smart Spender", "Detailed 10 expenses with long notes", R.drawable.ic_trophy),
        PLANNER_PRO("Planner Pro", "Set a savings goal with a target over R20000", R.drawable.ic_trophy),
        POWER_USER("Power User", "Logged expenses in 5 categories in one day", R.drawable.ic_trophy),
        YEARLY_TRACKER("Yearly Tracker", "Logged expenses in 12 different months", R.drawable.ic_trophy),
        DEBT_DESTROYER("Debt Destroyer", "Reached a goal named 'Debt' or 'Loan'", R.drawable.ic_trophy),
        EMERGENCY_HERO("Emergency Hero", "Reached an 'Emergency Fund' goal", R.drawable.ic_trophy),
        BARGAIN_HUNTER("Bargain Hunter", "Notes mention 'sale', 'deal', or 'discount' 3 times", R.drawable.ic_trophy);

        companion object {
            fun getByTitle(title: String): Badge? {
                return values().find { it.title.equals(title, ignoreCase = true) }
            }
        }
    }

    fun hasBadge(userId: Int, badge: Badge): Boolean {
        val awarded = dbHelper.getAwardedBadges(userId)
        return awarded.contains(badge.title)
    }

    fun getAwardedBadges(userId: Int): List<Badge> {
        val awardedTitles = dbHelper.getAwardedBadges(userId)
        return Badge.values().filter { awardedTitles.contains(it.title) }
    }

    fun checkAndAwardBadges(userId: Int, totalSpent: Double, maxBudget: Double) {
        if (totalSpent > 0 && totalSpent <= maxBudget) {
            dbHelper.awardBadge(userId, Badge.BUDGET_MASTER.title)
            if (totalSpent <= maxBudget * 0.8) {
                dbHelper.awardBadge(userId, Badge.BUDGET_HERO.title)
            }
        }
        dbHelper.checkAndAwardBadges(userId)
    }
}
