package y3.mobiledev.mywallet.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.util.Locale;

import y3.mobiledev.mywallet.MainActivity;

public class NotificationHelper {
    public static final String CHANNEL_ID = "daily_summary_channel";
    public static final int NOTIFICATION_ID = 1001;

    private static final String CHANNEL_NAME = "Daily Financial Summary";
    private static final String CHANNEL_DESCRIPTION = "Shows daily income and expense summary at 10 PM";

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

        sb.append(String.format(Locale.US, " Income: $%,.2f (%d transaction%s)\n",
                income, incomeCount, incomeCount == 1 ? "" : "s"));

        sb.append(String.format(Locale.US, " Expenses: $%,.2f (%d transaction%s)\n",
                expense, expenseCount, expenseCount == 1 ? "" : "s"));

        if (netAmount >= 0) {
            sb.append(String.format(Locale.US, " You saved $%,.2f today!", netAmount));
        } else {
            sb.append(String.format(Locale.US, " You overspent by $%,.2f today", Math.abs(netAmount)));
        }

        return sb.toString();
    }

    protected static String getShortContent(double income, double expense, double netAmount) {
        if (income == 0 && expense == 0) {
            return "No transactions today. Tap to add one!";
        }

        String netSign = netAmount >= 0 ? "+" : "-";
        return String.format(Locale.US, "Income: $%,.2f | Expenses: $%,.2f | Net: %s$%,.2f",
                income, expense, netSign, Math.abs(netAmount));
    }
}
