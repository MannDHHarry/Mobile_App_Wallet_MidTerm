package y3.mobiledev.mywallet.helpers;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import y3.mobiledev.mywallet.receivers.DailyNotificationReceiver;
public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";
    private static final int REQUEST_CODE = 2001;

    // 10:30 PM daily
    private static final int TARGET_HOUR = 22;
    private static final int TARGET_MINUTE = 0;

    private static final String TIME_PATTERN = "EEE, MMM d 'at' h:mm a z";

    public static void scheduleDailyNotification(Context context) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        PendingIntent pendingIntent = createPendingIntent(context);
        long triggerTimeMillis = calculateNextTriggerTime();

        // Minimal production-safe log
        Log.d(TAG, "Daily notification scheduled for " +
                new SimpleDateFormat(TIME_PATTERN, Locale.getDefault())
                        .format(triggerTimeMillis));

        scheduleAlarmSafely(alarmManager, triggerTimeMillis, pendingIntent);
    }

    public static void cancelDailyNotification(Context context) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = createPendingIntent(context);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public static boolean isNotificationScheduled(Context context) {
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                new Intent(context, DailyNotificationReceiver.class),
                PendingIntent.FLAG_NO_CREATE | getImmutableFlag()
        ) != null;
    }

    public static String getNextScheduledTimeString(Context context) {
        Calendar nextTrigger = calculateNextTriggerCalendar();
        SimpleDateFormat formatter = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
        return formatter.format(nextTrigger.getTime());
    }

    //Helper Function

    private static PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent(context, DailyNotificationReceiver.class);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | getImmutableFlag());
    }

    private static int getImmutableFlag() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
    }

    private static Calendar calculateNextTriggerCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, TARGET_HOUR);
        calendar.set(Calendar.MINUTE, TARGET_MINUTE);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return calendar;
    }

    private static long calculateNextTriggerTime() {
        return calculateNextTriggerCalendar().getTimeInMillis();
    }

    private static void scheduleAlarmSafely(AlarmManager alarmManager,
                                            long triggerTimeMillis,
                                            PendingIntent pendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
                    Log.w(TAG, "SCHEDULE_EXACT_ALARM permission missing — using inexact alarm");
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to schedule alarm: " + e.getMessage());
        }
    }
}
