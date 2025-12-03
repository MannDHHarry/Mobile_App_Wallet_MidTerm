package y3.mobiledev.mywallet.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import y3.mobiledev.mywallet.dao.SubscriptionDao;
import y3.mobiledev.mywallet.dao.TransactionDao;
import y3.mobiledev.mywallet.dao.WalletDao;
import y3.mobiledev.mywallet.database.AppDatabase;
import y3.mobiledev.mywallet.models.Subscription;
import y3.mobiledev.mywallet.models.Transaction;
import y3.mobiledev.mywallet.models.Wallet;

public class SubscriptionRepository {

    private SubscriptionDao subscriptionDao;
    private WalletDao walletDao;
    private TransactionDao transactionDao;

    public SubscriptionRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        subscriptionDao = database.subscriptionDao();
        walletDao = database.walletDao();
        transactionDao = database.transactionDao();
    }

    /**
     * Get all active subscriptions for user
     */
    public LiveData<List<Subscription>> getActiveSubscriptionsByUser(int userId) {
        return subscriptionDao.getActiveSubscriptionsByUser(userId);
    }

    /**
     * Get all subscriptions (including inactive)
     */
    public LiveData<List<Subscription>> getAllSubscriptionsByUser(int userId) {
        return subscriptionDao.getAllSubscriptionsByUser(userId);
    }

    /**
     * Add new subscription
     */
    public Long addSubscription(Subscription subscription) {
        Future<Long> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return subscriptionDao.insert(subscription);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update subscription
     */
    public void updateSubscription(Subscription subscription) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            subscriptionDao.update(subscription);
        });
    }

    /**
     * Delete subscription
     */
    public boolean deleteSubscription(Subscription subscription) {
        Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                subscriptionDao.delete(subscription);
                return true;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Process subscription billing
     * Creates a transaction and updates wallet balance
     */
    public boolean processSubscriptionBilling(Subscription subscription) {
        Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // Get wallet
                Wallet wallet = walletDao.getWalletById(subscription.getWalletId());
                if (wallet == null) return false;

                // Check if wallet has sufficient balance
                if (wallet.getBalance() < subscription.getAmount()) {
                    return false; // Insufficient funds
                }

                // Create transaction for subscription payment
                Transaction transaction = new Transaction(
                        subscription.getUserId(),
                        subscription.getWalletId(),
                        1, // Default subscription category (you may want to create a specific one)
                        "Subscription: " + subscription.getName(),
                        subscription.getAmount(),
                        System.currentTimeMillis(),
                        true // Is expense
                );

                transactionDao.insert(transaction);

                // Update wallet balance
                double newBalance = wallet.getBalance() - subscription.getAmount();
                walletDao.updateBalance(wallet.getWalletId(), newBalance);

                // Calculate next billing date (1 month from current next_billing_date)
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(subscription.getNextBillingDate());
                cal.add(Calendar.MONTH, 1);
                long newNextBillingDate = cal.getTimeInMillis();

                // Update subscription next billing date
                subscriptionDao.updateNextBillingDate(
                        subscription.getSubscriptionId(),
                        newNextBillingDate
                );

                return true;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get subscriptions due for billing
     */
    public List<Subscription> getSubscriptionsDueForBilling() {
        Future<List<Subscription>> future = AppDatabase.databaseWriteExecutor.submit(
                new Callable<List<Subscription>>() {
                    @Override
                    public List<Subscription> call() throws Exception {
                        return subscriptionDao.getSubscriptionsDueForBilling(
                                System.currentTimeMillis()
                        );
                    }
                }
        );

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get subscriptions for reminder (7 days before billing)
     */
    public List<Subscription> getSubscriptionsForReminder() {
        Future<List<Subscription>> future = AppDatabase.databaseWriteExecutor.submit(
                new Callable<List<Subscription>>() {
                    @Override
                    public List<Subscription> call() throws Exception {
                        long currentTime = System.currentTimeMillis();

                        // 7 days from now
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DAY_OF_MONTH, 7);
                        long reminderTime = cal.getTimeInMillis();

                        return subscriptionDao.getSubscriptionsForReminder(
                                currentTime,
                                reminderTime
                        );
                    }
                }
        );

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Toggle subscription active status
     */
    public void toggleSubscriptionStatus(int subscriptionId, boolean isActive) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            subscriptionDao.updateActiveStatus(subscriptionId, isActive);
        });
    }

    /**
     * Get subscription by ID
     */
    public Subscription getSubscriptionById(int subscriptionId) {
        Future<Subscription> future = AppDatabase.databaseWriteExecutor.submit(
                new Callable<Subscription>() {
                    @Override
                    public Subscription call() throws Exception {
                        return subscriptionDao.getSubscriptionById(subscriptionId);
                    }
                }
        );

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}