package y3.mobiledev.mywallet.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.Locale;

import y3.mobiledev.mywallet.MainActivity;

public class NotificationHelper {
    public static final String CHANNEL_ID = "daily_summary_channel";
    public static final String SPENDING_ALERT_CHANNEL_ID = "spending_alert_channel";
    public static final int NOTIFICATION_ID = 1001;
    public static final int SPENDING_ALERT_NOTIFICATION_ID = 1002;
    public static final int WELCOME_NOTIFICATION_ID = 1003;

    private static final String CHANNEL_NAME = "Daily Financial Summary";
    private static final String CHANNEL_DESCRIPTION = "Shows daily income and expense summary at 10 PM";
    private static final String SPENDING_ALERT_CHANNEL_NAME = "Spending Alerts";
    private static final String SPENDING_ALERT_CHANNEL_DESCRIPTION = "Alerts about unusual spending patterns";

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

        // Create spending alert channel
        NotificationChannel spendingChannel = new NotificationChannel(
                SPENDING_ALERT_CHANNEL_ID,
                SPENDING_ALERT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        spendingChannel.setDescription(SPENDING_ALERT_CHANNEL_DESCRIPTION);
        spendingChannel.enableVibration(true);
        spendingChannel.enableLights(true);

        if (manager != null) {
            manager.createNotificationChannel(spendingChannel);
        }
    }

    public static void showDailySummaryNotification(Context context,
                                                    double totalIncome,
                                                    double totalExpense,
                                                    int incomeCount,
                                                    int expenseCount) {

        double netAmount = totalIncome - totalExpense;
        String title = "Today's Financial Summary";
        String content = buildNotificationContent(totalIncome, totalExpense, incomeCount, expenseCount, netAmount);
        String shortContent = getShortContent(totalIncome, totalExpense, netAmount);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("navigate_to", "statistics");

        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_agenda) // Replace with your app's icon
                .setContentTitle(title)
                .setContentText(shortContent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    protected static String buildNotificationContent(double income,
                                           double expense,
                                           int incomeCount,
                                           int expenseCount,
                                           double netAmount) {

        if (incomeCount == 0 && expenseCount == 0) {
            return "No transactions recorded today.\nTap to add your first transaction!";
        }

        StringBuilder sb = new StringBuilder();

        sb.append(String.format(Locale.US, " Income: %s (%d transaction%s)\n",
                CurrencyUtils.formatPlainAmount(income),
                incomeCount,
                incomeCount == 1 ? "" : "s"));

        sb.append(String.format(Locale.US, " Expenses: %s (%d transaction%s)\n",
                CurrencyUtils.formatPlainAmount(expense),
                expenseCount,
                expenseCount == 1 ? "" : "s"));

        if (netAmount >= 0) {
            sb.append(String.format(Locale.US, " You saved %s today!",
                    CurrencyUtils.formatPlainAmount(netAmount)));
        } else {
            sb.append(String.format(Locale.US, " You overspent by %s today",
                    CurrencyUtils.formatPlainAmount(Math.abs(netAmount))));
        }

        return sb.toString();
    }

    protected static String getShortContent(double income, double expense, double netAmount) {
        if (income == 0 && expense == 0) {
            return "No transactions today. Tap to add one!";
        }

        String netSign = netAmount >= 0 ? "+" : "-";
        return String.format(Locale.US,
                "Income: %s | Expenses: %s | Net: %s%s",
                CurrencyUtils.formatPlainAmount(income),
                CurrencyUtils.formatPlainAmount(expense),
                netSign,
                CurrencyUtils.formatPlainAmount(Math.abs(netAmount)));
    }

    /**
     * Show spending alert notification for unusual spending patterns
     */
    public static void showSpendingAlertNotification(Context context, String title, String message) {
        createNotificationChannel(context); // Ensure channel exists

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, SPENDING_ALERT_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(SPENDING_ALERT_NOTIFICATION_ID, builder.build());
        }
    }

    /**
     * Show weekly/monthly insights notification
     */
    public static void showInsightsNotification(Context context, String title, List<String> insights) {
        createNotificationChannel(context);

        StringBuilder content = new StringBuilder();
        for (int i = 0; i < Math.min(insights.size(), 5); i++) {
            content.append(insights.get(i));
            if (i < Math.min(insights.size(), 5) - 1) {
                content.append("\n");
            }
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, SPENDING_ALERT_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle(title)
                .setContentText(insights.isEmpty() ? "No insights available" : insights.get(0))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content.toString()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(SPENDING_ALERT_NOTIFICATION_ID + 1, builder.build());
        }
    }

    /**
     * Show welcome notification after user grants notification permission
     */
    public static void showWelcomeNotification(Context context) {
        createNotificationChannel(context);

        String title = "Welcome to MyWallet!";
        String content = "You're all set! We'll send you a daily summary of your transactions at 10:00 PM.";

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(WELCOME_NOTIFICATION_ID, builder.build());
        }
    }
}
