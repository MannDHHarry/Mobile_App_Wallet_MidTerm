package y3.mobiledev.mywallet.helpers;

import java.util.List;

import y3.mobiledev.mywallet.models.TransactionGroup;
import y3.mobiledev.mywallet.models.TransactionWithCategory;
import y3.mobiledev.mywallet.helpers.NotificationHelper;

public class NotificationTestHelper {
    public static TransactionGroup createTodayGroupWithTransactions(
            List<TransactionWithCategory> transactions) {
        return new TransactionGroup("Today", transactions);
    }

    /**
     * Creates a single fake transaction
     */
    public static TransactionWithCategory createTransaction(
            long dateMillis,
            double amount,
            boolean isExpense,
            String description,
            String categoryName,
            int categoryIcon,
            int categoryColor) {

        // Using real constructor - assuming it matches your model
        return new TransactionWithCategory(
                999,                    // transactionId
                1,                      // userId

/// Skip walletId if not used in summary logic
        1,                      // walletId
                10,                     // categoryId
                description != null ? description : "",
                amount,
                dateMillis,
                isExpense,
                categoryName != null ? categoryName : (isExpense ? "Food" : "Salary"),
                categoryIcon,
                categoryColor
        );
    }

    /**
     * Helper: Create today's date at 00:00 for consistent grouping
     */
    public static long todayMidnightMillis() {
        return System.currentTimeMillis() - (System.currentTimeMillis() % 86400000L);
    }

    /**
     * Creates a transaction for "today"
     */
    public static TransactionWithCategory createTodayTransaction(double amount, boolean isExpense) {
        return createTransaction(
                todayMidnightMillis() + 12 * 60 * 60 * 1000, // noon today
                amount,
                isExpense,
                isExpense ? "Lunch" : "Freelance",
                isExpense ? "Food" : "Salary",
                android.R.drawable.ic_menu_info_details,
                0xFF6200EE
        );
    }

    /**
     * Run full summary calculation using real production logic
     */
    public static TransactionManager.DailySummary calculateTodaySummary(List<TransactionWithCategory> todayTransactions) {
        TransactionGroup todayGroup = createTodayGroupWithTransactions(todayTransactions);
        return TransactionManager.calculateDailySummary(todayGroup);
    }

    /**
     * Generate expected notification content (for comparison)
     */
    public static String expectedNotificationContent(double income, double expense, int incomeCount, int expenseCount, double net) {
        return NotificationHelper.buildNotificationContent(income, expense, incomeCount, expenseCount, net);
    }

    public static String expectedShortContent(double income, double expense, double net) {
        return NotificationHelper.getShortContent(income, expense, net);
    }
}
