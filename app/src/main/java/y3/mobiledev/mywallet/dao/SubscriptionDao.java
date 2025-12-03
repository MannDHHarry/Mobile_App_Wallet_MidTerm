package y3.mobiledev.mywallet.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import y3.mobiledev.mywallet.models.Subscription;

@Dao
public interface SubscriptionDao {

    @Insert
    long insert(Subscription subscription);

    @Update
    void update(Subscription subscription);

    @Delete
    void delete(Subscription subscription);

    // Get all active subscriptions for user
    @Query("SELECT * FROM subscriptions WHERE user_id = :userId AND is_active = 1 ORDER BY next_billing_date ASC")
    LiveData<List<Subscription>> getActiveSubscriptionsByUser(int userId);

    // Get all subscriptions (including inactive) for user
    @Query("SELECT * FROM subscriptions WHERE user_id = :userId ORDER BY is_active DESC, next_billing_date ASC")
    LiveData<List<Subscription>> getAllSubscriptionsByUser(int userId);

    // Get subscription by ID
    @Query("SELECT * FROM subscriptions WHERE subscription_id = :subscriptionId")
    Subscription getSubscriptionById(int subscriptionId);

    // Get subscriptions due for billing (next_billing_date <= current time)
    @Query("SELECT * FROM subscriptions WHERE is_active = 1 AND next_billing_date <= :currentTime")
    List<Subscription> getSubscriptionsDueForBilling(long currentTime);

    // Get subscriptions due for reminder (7 days before billing)
    @Query("SELECT * FROM subscriptions WHERE is_active = 1 AND next_billing_date BETWEEN :reminderTime AND :currentTime")
    List<Subscription> getSubscriptionsForReminder(long reminderTime, long currentTime);

    // Update next billing date after processing
    @Query("UPDATE subscriptions SET next_billing_date = :newDate WHERE subscription_id = :subscriptionId")
    void updateNextBillingDate(int subscriptionId, long newDate);

    // Toggle subscription active status
    @Query("UPDATE subscriptions SET is_active = :isActive WHERE subscription_id = :subscriptionId")
    void updateActiveStatus(int subscriptionId, boolean isActive);

    // Get count of active subscriptions
    @Query("SELECT COUNT(*) FROM subscriptions WHERE user_id = :userId AND is_active = 1")
    int getActiveSubscriptionCount(int userId);
}