package y3.mobiledev.mywallet.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "wallets",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("user_id")})
public class Wallet {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "wallet_id")
    private int walletId;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "icon_res_id")
    private int iconResId;

    @ColumnInfo(name = "balance")
    private double balance;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // ✅ PRIMARY CONSTRUCTOR - Room will use this one
    public Wallet(int walletId, int userId, String name, int iconResId,
                  double balance, long createdAt) {
        this.walletId = walletId;
        this.userId = userId;
        this.name = name;
        this.iconResId = iconResId;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    // ✅ IGNORED - For creating new wallets
    @Ignore
    public Wallet(int userId, String name, int iconResId, double balance) {
        this.userId = userId;
        this.name = name;
        this.iconResId = iconResId;
        this.balance = balance;
        this.createdAt = System.currentTimeMillis();
    }

    // ✅ IGNORED - Backward compatibility
    @Ignore
    public Wallet(int walletId, int userId, String name, int iconResId,
                  double balance, int transactionCount) {
        this(walletId, userId, name, iconResId, balance, System.currentTimeMillis());
    }

    // Getters
    public int getWalletId() { return walletId; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public int getIconResId() { return iconResId; }
    public double getBalance() { return balance; }
    public long getCreatedAt() { return createdAt; }

    // For backward compatibility
    @Ignore
    public int getTransactionCount() { return 0; }

    // Setters
    public void setWalletId(int walletId) { this.walletId = walletId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
    public void setBalance(double balance) { this.balance = balance; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @Ignore
    public void setTransactionCount(int count) { } // No-op for compatibility
}