package y3.mobiledev.mywallet.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

import y3.mobiledev.mywallet.helpers.SubscriptionNotificationHelper;
import y3.mobiledev.mywallet.helpers.SubscriptionScheduler;
import y3.mobiledev.mywallet.models.Subscription;
import y3.mobiledev.mywallet.repository.SubscriptionRepository;

public class SubscriptionBillingReceiver extends BroadcastReceiver {

    private static final String TAG = "SubscriptionBilling";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) return;

        Log.d(TAG, "Subscription billing check triggered");

        // Process due subscriptions
        SubscriptionRepository repository = new SubscriptionRepository(
                (android.app.Application) context.getApplicationContext()
        );

        List<Subscription> dueSubscriptions = repository.getSubscriptionsDueForBilling();

        if (dueSubscriptions != null && !dueSubscriptions.isEmpty()) {
            for (Subscription subscription : dueSubscriptions) {
                processSubscription(context, repository, subscription);
            }
        }

        // Check for reminders (7 days before)
        List<Subscription> reminderSubscriptions = repository.getSubscriptionsForReminder();

        if (reminderSubscriptions != null && !reminderSubscriptions.isEmpty()) {
            for (Subscription subscription : reminderSubscriptions) {
                SubscriptionNotificationHelper.showReminderNotification(
                        context, subscription
                );
            }
        }

        // Reschedule next check
        SubscriptionScheduler.scheduleDailyCheck(context);
    }

    private void processSubscription(Context context, SubscriptionRepository repository,
                                     Subscription subscription) {
        Log.d(TAG, "Processing subscription: " + subscription.getName());

        boolean success = repository.processSubscriptionBilling(subscription);

        if (success) {
            // Show success notification
            SubscriptionNotificationHelper.showBillingSuccessNotification(
                    context, subscription
            );
            Log.d(TAG, "Subscription billed successfully: " + subscription.getName());
        } else {
            // Show failure notification (insufficient funds)
            SubscriptionNotificationHelper.showBillingFailureNotification(
                    context, subscription
            );
            Log.w(TAG, "Subscription billing failed: " + subscription.getName());
        }
    }
}