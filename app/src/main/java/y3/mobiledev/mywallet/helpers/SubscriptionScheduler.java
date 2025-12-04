package y3.mobiledev.mywallet.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

import y3.mobiledev.mywallet.receivers.SubscriptionBillingReceiver;

public class SubscriptionScheduler {

    private static final String TAG = "SubscriptionScheduler";
    private static final int REQUEST_CODE = 3001;

    // Check at 9:00 AM daily
    private static final int TARGET_HOUR = 9;
    private static final int TARGET_MINUTE = 0;

    public static void scheduleDailyCheck(Context context) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        PendingIntent pendingIntent = createPendingIntent(context);
        long triggerTimeMillis = calculateNextTriggerTime();

        Log.d(TAG, "Scheduling subscription check for: " +
                new java.text.SimpleDateFormat("EEE, MMM d 'at' h:mm a",
                        java.util.Locale.getDefault()).format(triggerTimeMillis));

        scheduleAlarmSafely(alarmManager, triggerTimeMillis, pendingIntent);
    }

    public static void cancelDailyCheck(Context context) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = createPendingIntent(context);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        Log.d(TAG, "Subscription check cancelled");
    }

    private static PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent(context, SubscriptionBillingReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags);
    }

    private static long calculateNextTriggerTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, TARGET_HOUR);
        calendar.set(Calendar.MINUTE, TARGET_MINUTE);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If time has passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return calendar.getTimeInMillis();
    }

    private static void scheduleAlarmSafely(AlarmManager alarmManager,
                                            long triggerTimeMillis,
                                            PendingIntent pendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTimeMillis,
                            pendingIntent
                    );
                } else {
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTimeMillis,
                            pendingIntent
                    );
                    Log.w(TAG, "SCHEDULE_EXACT_ALARM permission missing");
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                );
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                );
            } else {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                );
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to schedule alarm: " + e.getMessage());
        }
    }
}