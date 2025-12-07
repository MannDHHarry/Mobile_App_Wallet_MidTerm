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
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.helpers.LocaleHelper;

public class NotificationHelper {
    public static final String CHANNEL_ID = "daily_summary_channel";
    public static final String SPENDING_ALERT_CHANNEL_ID = "spending_alert_channel";
    public static final int NOTIFICATION_ID = 1001;
    public static final int SPENDING_ALERT_NOTIFICATION_ID = 1002;
    public static final int WELCOME_NOTIFICATION_ID = 1003;

    public static void createNotificationChannel(Context context) {
        // Wrap context with locale to get localized strings
        Context localizedContext = LocaleHelper.onAttach(context);

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                localizedContext.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(localizedContext.getString(R.string.notification_channel_description));
        channel.enableVibration(true);
        channel.enableLights(true);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }

        // Create spending alert channel
        NotificationChannel spendingChannel = new NotificationChannel(
                SPENDING_ALERT_CHANNEL_ID,
                localizedContext.getString(R.string.notification_spending_alert_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        spendingChannel.setDescription(localizedContext.getString(R.string.notification_spending_alert_channel_description));
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
        // Wrap context with locale to get localized strings
        Context localizedContext = LocaleHelper.onAttach(context);

        double netAmount = totalIncome - totalExpense;
        String title = localizedContext.getString(R.string.notification_daily_summary_title);
        String content = buildNotificationContent(localizedContext, totalIncome, totalExpense, incomeCount, expenseCount, netAmount);
        String shortContent = getShortContent(localizedContext, totalIncome, totalExpense, netAmount);

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

    protected static String buildNotificationContent(Context context,
                                           double income,
                                           double expense,
                                           int incomeCount,
                                           int expenseCount,
                                           double netAmount) {

        if (incomeCount == 0 && expenseCount == 0) {
            return context.getString(R.string.notification_daily_summary_no_transactions);
        }

        StringBuilder sb = new StringBuilder();

        String incomeText = context.getString(R.string.notification_daily_summary_income,
                CurrencyUtils.formatPlainAmount(income),
                incomeCount,
                incomeCount == 1 ? "" : "s");
        sb.append(" ").append(incomeText).append("\n");

        String expenseText = context.getString(R.string.notification_daily_summary_expenses,
                CurrencyUtils.formatPlainAmount(expense),
                expenseCount,
                expenseCount == 1 ? "" : "s");
        sb.append(" ").append(expenseText).append("\n");

        if (netAmount >= 0) {
            sb.append(" ").append(context.getString(R.string.notification_daily_summary_saved,
                    CurrencyUtils.formatPlainAmount(netAmount)));
        } else {
            sb.append(" ").append(context.getString(R.string.notification_daily_summary_overspent,
                    CurrencyUtils.formatPlainAmount(Math.abs(netAmount))));
        }

        return sb.toString();
    }

    protected static String getShortContent(Context context, double income, double expense, double netAmount) {
        if (income == 0 && expense == 0) {
            return context.getString(R.string.notification_daily_summary_short_no_transactions);
        }

        String netSign = netAmount >= 0 ? "+" : "-";
        return String.format(Locale.getDefault(),
                context.getString(R.string.notification_daily_summary_short),
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

        // Wrap context with locale to get localized strings
        Context localizedContext = LocaleHelper.onAttach(context);

        String title = localizedContext.getString(R.string.notification_welcome_title);
        String content = localizedContext.getString(R.string.notification_welcome_content);

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
