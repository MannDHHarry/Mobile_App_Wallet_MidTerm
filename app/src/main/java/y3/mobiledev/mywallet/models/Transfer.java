package y3.mobiledev.mywallet.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "transfers",
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
                        childColumns = "from_wallet_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Wallet.class,
                        parentColumns = "wallet_id",
                        childColumns = "to_wallet_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("user_id"),
                @Index("from_wallet_id"),
                @Index("to_wallet_id"),
                @Index("date")
        })
public class Transfer {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transfer_id")
    private int transferId;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "from_wallet_id")
    private int fromWalletId;

    @ColumnInfo(name = "to_wallet_id")
    private int toWalletId;

    @ColumnInfo(name = "amount")
    private double amount;

    @ColumnInfo(name = "date")
    private long date;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // Primary Constructor (for Room)
    public Transfer(int transferId, int userId, int fromWalletId, int toWalletId,
                    double amount, long date, long createdAt) {
        this.transferId = transferId;
        this.userId = userId;
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amount = amount;
        this.date = date;
        this.createdAt = createdAt;
    }

    // Constructor for creating new transfers
    @Ignore
    public Transfer(int userId, int fromWalletId, int toWalletId,
                    double amount, long date) {
        this.userId = userId;
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amount = amount;
        this.date = date;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public int getTransferId() { return transferId; }
    public int getUserId() { return userId; }
    public int getFromWalletId() { return fromWalletId; }
    public int getToWalletId() { return toWalletId; }
    public double getAmount() { return amount; }
    public long getDate() { return date; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setTransferId(int transferId) { this.transferId = transferId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setFromWalletId(int fromWalletId) { this.fromWalletId = fromWalletId; }
    public void setToWalletId(int toWalletId) { this.toWalletId = toWalletId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(long date) { this.date = date; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}