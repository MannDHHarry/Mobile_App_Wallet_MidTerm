package y3.mobiledev.mywallet.receivers;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

import y3.mobiledev.mywallet.helpers.LocaleHelper;
import y3.mobiledev.mywallet.helpers.NotificationDataManager;
import y3.mobiledev.mywallet.helpers.NotificationHelper;
import y3.mobiledev.mywallet.helpers.NotificationScheduler;
import y3.mobiledev.mywallet.models.TransactionWithCategory;
import y3.mobiledev.mywallet.repository.TransactionRepository;

public class DailyNotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "DailyNotifReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            return;
        }

        // Wrap context with locale to get localized strings for notifications
        Context localizedContext = LocaleHelper.onAttach(context);

        Log.d(TAG, "Daily summary alarm triggered");

        // Step 1: Check if we have valid user data
        if (!NotificationDataManager.hasData(localizedContext)) {
            Log.d(TAG, "No user data → skipping notification (will reschedule anyway)");
            rescheduleNext(localizedContext);
            return;
        }

        int userId = NotificationDataManager.getUserId(localizedContext);
        Log.d(TAG, "Processing daily summary for user ID: " + userId);

        // Step 2: Fetch FRESH data from database (not cached!)
        TransactionRepository repository = new TransactionRepository(
                (Application) localizedContext.getApplicationContext()
        );
        
        List<TransactionWithCategory> todayTransactions = repository.getTodayTransactionsSync(userId);
        
        if (todayTransactions == null || todayTransactions.isEmpty()) {
            Log.d(TAG, "No transactions today (fresh from database)");
            showEmptySummaryAndReschedule(localizedContext);
            return;
        }

        // Step 3: Calculate summary from fresh data
        double totalIncome = 0.0;
        double totalExpense = 0.0;
        int incomeCount = 0;
        int expenseCount = 0;

        for (TransactionWithCategory transaction : todayTransactions) {
            if (transaction.isExpense()) {
                totalExpense += transaction.getAmount();
                expenseCount++;
            } else {
                totalIncome += transaction.getAmount();
                incomeCount++;
            }
        }

        Log.d(TAG, String.format("Today's summary (FRESH) → Income: $%.2f (%d), Expense: $%.2f (%d), Net: $%.2f",
                totalIncome,
                incomeCount,
                totalExpense,
                expenseCount,
                totalIncome - totalExpense));

        // Step 4: Show notification
        NotificationHelper.createNotificationChannel(localizedContext);
        NotificationHelper.showDailySummaryNotification(
                localizedContext,
                totalIncome,
                totalExpense,
                incomeCount,
                expenseCount
        );

        Log.d(TAG, "Daily summary notification shown with fresh data");

        // Step 5: Always reschedule for tomorrow (critical!)
        rescheduleNext(localizedContext);
    }

    private static void showEmptySummaryAndReschedule(Context context) {
        NotificationHelper.createNotificationChannel(context);
        NotificationHelper.showDailySummaryNotification(context, 0, 0, 0, 0);
        rescheduleNext(context);
    }

    private static void rescheduleNext(Context context) {
        NotificationScheduler.scheduleDailyNotification(context);
        String nextTime = NotificationScheduler.getNextScheduledTimeString(context);
        Log.d(TAG, "Rescheduled for tomorrow: " + nextTime);
    }
}
