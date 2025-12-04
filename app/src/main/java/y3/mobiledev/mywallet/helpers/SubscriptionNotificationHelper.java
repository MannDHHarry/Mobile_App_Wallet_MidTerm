package y3.mobiledev.mywallet.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import y3.mobiledev.mywallet.MainActivity;
import y3.mobiledev.mywallet.models.Subscription;

public class SubscriptionNotificationHelper {

    public static final String CHANNEL_ID = "subscription_channel";
    private static final String CHANNEL_NAME = "Subscription Management";
    private static final String CHANNEL_DESCRIPTION = "Notifications for subscription billing and reminders";

    public static void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(CHANNEL_DESCRIPTION);
        channel.enableVibration(true);
        channel.enableLights(true);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Show notification when subscription is successfully billed
     */
    public static void showBillingSuccessNotification(Context context,
                                                      Subscription subscription) {
        String title = "Subscription Charged";
        String content = String.format(Locale.US,
                "%s subscription charged: $%.2f",
                subscription.getName(),
                subscription.getAmount());

        showNotification(context, title, content,
                subscription.getSubscriptionId());
    }

    /**
     * Show notification when subscription billing fails
     */
    public static void showBillingFailureNotification(Context context,
                                                      Subscription subscription) {
        String title = "Subscription Payment Failed";
        String content = String.format(Locale.US,
                "Failed to charge %s ($%.2f). Insufficient funds in wallet.",
                subscription.getName(),
                subscription.getAmount());

        showNotification(context, title, content,
                subscription.getSubscriptionId() + 1000);
    }

    /**
     * Show reminder notification (7 days before billing)
     */
    public static void showReminderNotification(Context context,
                                                Subscription subscription) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.US);
        String billingDate = dateFormat.format(new Date(subscription.getNextBillingDate()));

        String title = "Subscription Reminder";
        String content = String.format(Locale.US,
                "%s will be charged on %s ($%.2f). Ensure sufficient balance.",
                subscription.getName(),
                billingDate,
                subscription.getAmount());

        showNotification(context, title, content,
                subscription.getSubscriptionId() + 2000);
    }

    private static void showNotification(Context context, String title,
                                         String content, int notificationId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notificationId, intent, pendingIntentFlags
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_recent_history)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }
    }
}