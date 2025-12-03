package y3.mobiledev.mywallet.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "subscriptions",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Wallet.class,
                        parentColumns = "wallet_id",
                        childColumns = "wallet_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("user_id"),
                @Index("wallet_id")
        })
public class Subscription {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "subscription_id")
    private int subscriptionId;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "wallet_id")
    private int walletId;

    @ColumnInfo(name = "name")
    private String name; // e.g., "Netflix", "Spotify"

    @ColumnInfo(name = "amount")
    private double amount;

    @ColumnInfo(name = "start_date")
    private long startDate; // First subscription date

    @ColumnInfo(name = "next_billing_date")
    private long nextBillingDate; // Next time to charge

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "is_active")
    private boolean isActive; // Can pause/cancel subscriptions

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // Primary Constructor - for Room
    public Subscription(int subscriptionId, int userId, int walletId, String name,
                        double amount, long startDate, long nextBillingDate,
                        String notes, boolean isActive, long createdAt) {
        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.walletId = walletId;
        this.name = name;
        this.amount = amount;
        this.startDate = startDate;
        this.nextBillingDate = nextBillingDate;
        this.notes = notes != null ? notes : "";
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // Constructor for creating new subscriptions
    @Ignore
    public Subscription(int userId, int walletId, String name, double amount,
                        long startDate, String notes) {
        this.userId = userId;
        this.walletId = walletId;
        this.name = name;
        this.amount = amount;
        this.startDate = startDate;
        this.nextBillingDate = calculateNextBillingDate(startDate);
        this.notes = notes != null ? notes : "";
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
    }

    // Calculate next billing date (1 month from start)
    private long calculateNextBillingDate(long fromDate) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(fromDate);
        cal.add(java.util.Calendar.MONTH, 1);
        return cal.getTimeInMillis();
    }

    // Getters
    public int getSubscriptionId() { return subscriptionId; }
    public int getUserId() { return userId; }
    public int getWalletId() { return walletId; }
    public String getName() { return name; }
    public double getAmount() { return amount; }
    public long getStartDate() { return startDate; }
    public long getNextBillingDate() { return nextBillingDate; }
    public String getNotes() { return notes; }
    public boolean isActive() { return isActive; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setSubscriptionId(int subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
    public void setUserId(int userId) { this.userId = userId; }
    public void setWalletId(int walletId) { this.walletId = walletId; }
    public void setName(String name) { this.name = name; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setStartDate(long startDate) { this.startDate = startDate; }
    public void setNextBillingDate(long nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }
    public void setNotes(String notes) {
        this.notes = notes != null ? notes : "";
    }
    public void setActive(boolean active) { isActive = active; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}