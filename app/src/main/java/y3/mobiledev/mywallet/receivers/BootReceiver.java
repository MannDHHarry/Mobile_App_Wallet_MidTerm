package y3.mobiledev.mywallet.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import y3.mobiledev.mywallet.helpers.NotificationDataManager;
import y3.mobiledev.mywallet.helpers.NotificationScheduler;

public final class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    private static final String ACTION_BOOT_COMPLETED = Intent.ACTION_BOOT_COMPLETED;
    private static final String ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON";
    private static final String ACTION_HTC_QUICKBOOT = "com.htc.intent.action.QUICKBOOT_POWERON";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            return;
        }

        // Check if this is a boot-related broadcast
        if (!isBootAction(action)) {
            return;
        }

        Log.d(TAG, "Device boot detected: " + action);

        // Only reschedule if user has previously saved notification data
        if (NotificationDataManager.hasData(context)) {
            Log.d(TAG, "User data found — rescheduling daily notification");

            NotificationScheduler.scheduleDailyNotification(context);

            String nextTime = NotificationScheduler.getNextScheduledTimeString(context);
            Log.d(TAG, "Daily notification rescheduled for: " + nextTime);
        } else {
            Log.d(TAG, "No saved user data — skipping notification reschedule");
        }
    }

    private static boolean isBootAction(String action) {
        return ACTION_BOOT_COMPLETED.equals(action) ||
                ACTION_QUICKBOOT_POWERON.equals(action) ||
                ACTION_HTC_QUICKBOOT.equals(action);
    }
}
