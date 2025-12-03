package y3.mobiledev.mywallet.helpers;

import java.util.List;
import y3.mobiledev.mywallet.models.TransactionGroup;
import y3.mobiledev.mywallet.models.TransactionWithCategory;

public class NotificationTestHelper {

    public static TransactionGroup createTodayGroupWithTransactions(List<TransactionWithCategory> transactions) {
        return new TransactionGroup("Today", transactions);
    }

    /** Creates a single fake transaction with receiptPhotoUri support */
    public static TransactionWithCategory createTransaction(
            long dateMillis,
            double amount,
            boolean isExpense,
            String description,
            String categoryName,
            int categoryIcon,
            int categoryColor) {

        return new TransactionWithCategory(
                999, // transactionId
                1,   // userId
                1,   // walletId
                10,  // categoryId
                description != null ? description : "",
                amount,
                dateMillis,
                isExpense,
                categoryName != null ? categoryName : (isExpense ? "Food" : "Salary"),
                categoryIcon,
                categoryColor,
                null  // receiptPhotoUri → safe default (can be changed if needed in tests)
        );
    }

    public static TransactionWithCategory createTransactionWithPhoto(
            long dateMillis,
            double amount,
            boolean isExpense,
            String description,
            String categoryName,
            int categoryIcon,
            int categoryColor,
            String receiptPhotoUri) {

        return new TransactionWithCategory(
                999, 1, 1, 10,
                description != null ? description : "",
                amount,
                dateMillis,
                isExpense,
                categoryName != null ? categoryName : (isExpense ? "Food" : "Salary"),
                categoryIcon,
                categoryColor,
                receiptPhotoUri
        );
    }

    /** Helper: Create today's date at 00:00 for consistent grouping */
    public static long todayMidnightMillis() {
        return System.currentTimeMillis() - (System.currentTimeMillis() % 86400000L);
    }

    /** Creates a transaction for "today" (no photo) */
    public static TransactionWithCategory createTodayTransaction(double amount, boolean isExpense) {
        return createTransaction(
                todayMidnightMillis() + 12 * 60 * 60 * 1000, // noon today
                amount,
                isExpense,
                isExpense ? "Lunch" : "Freelance payment",
                isExpense ? "Food" : "Salary",
                android.R.drawable.ic_menu_info_details,
                0xFF6200EE
        );
    }

    /** Creates a transaction with a fake receipt photo URI */
    public static TransactionWithCategory createTodayTransactionWithPhoto(double amount, boolean isExpense) {
        return createTransactionWithPhoto(
                todayMidnightMillis() + 14 * 60 * 60 * 1000,
                amount,
                isExpense,
                isExpense ? "Taxi receipt" : "Bonus received",
                isExpense ? "Transport" : "Income",
                android.R.drawable.ic_menu_camera,
                0xFFFF5722,
                "content://media/external/images/media/12345" // fake URI
        );
    }

    // The rest stays exactly the same
    public static TransactionManager.DailySummary calculateTodaySummary(List<TransactionWithCategory> todayTransactions) {
        TransactionGroup todayGroup = createTodayGroupWithTransactions(todayTransactions);
        return TransactionManager.calculateDailySummary(todayGroup);
    }

    public static String expectedNotificationContent(double income, double expense, int incomeCount, int expenseCount, double net) {
        return NotificationHelper.buildNotificationContent(income, expense, incomeCount, expenseCount, net);
    }

    public static String expectedShortContent(double income, double expense, double net) {
        return NotificationHelper.getShortContent(income, expense, net);
    }
}