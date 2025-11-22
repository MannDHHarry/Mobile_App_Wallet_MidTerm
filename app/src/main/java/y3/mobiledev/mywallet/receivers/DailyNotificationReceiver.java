package y3.mobiledev.mywallet.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import y3.mobiledev.mywallet.helpers.NotificationDataManager;
import y3.mobiledev.mywallet.helpers.NotificationHelper;
import y3.mobiledev.mywallet.helpers.NotificationScheduler;
import y3.mobiledev.mywallet.helpers.TransactionManager;
import y3.mobiledev.mywallet.models.TransactionGroup;

public class DailyNotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "DailyNotifReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            return;
        }

        Log.d(TAG, "Daily summary alarm triggered");

        // Step 1: Check if we have valid user data
        if (!NotificationDataManager.hasData(context)) {
            Log.d(TAG, "No user data → skipping notification (will reschedule anyway)");
            rescheduleNext(context);
            return;
        }

        int userId = NotificationDataManager.getUserId(context);
        Log.d(TAG, "Processing daily summary for user ID: " + userId);

        // Step 2: Load cached transaction groups
        List<TransactionGroup> groups = NotificationDataManager.loadTransactionGroups(context);
        if (groups.isEmpty()) {
            Log.d(TAG, "No transaction groups loaded → showing empty summary");
            showEmptySummaryAndReschedule(context);
            return;
        }

        // Step 3: Extract today's transactions
        TransactionGroup todayGroup = TransactionManager.getTodayGroup(groups);
        if (todayGroup == null || todayGroup.getTransactions().isEmpty()) {
            Log.d(TAG, "No transactions today");
            showEmptySummaryAndReschedule(context);
            return;
        }

        // Step 4: Calculate summary
        TransactionManager.DailySummary summary = TransactionManager.calculateDailySummary(todayGroup);

        Log.d(TAG, String.format("Today's summary → Income: $%.2f (%d), Expense: $%.2f (%d), Net: $%.2f",
                summary.getTotalIncome(),
                summary.getIncomeCount(),
                summary.getTotalExpense(),
                summary.getExpenseCount(),
                summary.getTotalIncome() - summary.getTotalExpense()));

        // Step 5: Show notification
        NotificationHelper.createNotificationChannel(context);
        NotificationHelper.showDailySummaryNotification(
                context,
                summary.getTotalIncome(),
                summary.getTotalExpense(),
                summary.getIncomeCount(),
                summary.getExpenseCount()
        );

        Log.d(TAG, "Daily summary notification shown");

        // Step 6: Always reschedule for tomorrow (critical!)
        rescheduleNext(context);
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
