package y3.mobiledev.mywallet.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "transactions",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Category.class,
                        parentColumns = "category_id",
                        childColumns = "category_id",
                        onDelete = ForeignKey.RESTRICT
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
                @Index("category_id"),
                @Index("wallet_id"),
                @Index("date")
        })

public class Transaction {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transaction_id")
    private int transactionId;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "wallet_id")
    private int walletId;

    @ColumnInfo(name = "category_id")
    private int categoryId;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "amount")
    private double amount;

    @ColumnInfo(name = "date")
    private long date;

    @ColumnInfo(name = "is_expense")
    private boolean isExpense;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "receipt_photo_uri")
    private String receiptPhotoUri;

    // Primary Constructor - for Room Db

    public Transaction(int transactionId, int userId, int walletId, int categoryId,
                       String description, double amount, long date, boolean isExpense,
                       long createdAt , String receiptPhotoUri) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.walletId = walletId;
        this.categoryId = categoryId;
        this.description = description != null ? description : "";
        this.amount = amount;
        this.date = date;
        this.isExpense = isExpense;
        this.createdAt = createdAt;
        this.receiptPhotoUri = receiptPhotoUri;

    }

    // Constructor for creating new transactions
    @Ignore
    public Transaction(int userId, int walletId, int categoryId, String description,
                       double amount, long date, boolean isExpense, String receiptPhotoUri) {
        this.userId = userId;
        this.walletId = walletId;
        this.categoryId = categoryId;
        this.description = description != null ? description : "";
        this.amount = amount;
        this.date = date;
        this.isExpense = isExpense;
        this.createdAt = System.currentTimeMillis();
        this.receiptPhotoUri = receiptPhotoUri;

    }

    @Ignore
    public Transaction(int transactionId, int userId, String category, String description,
                       double amount, Date dateObj, boolean isExpense,
                       int categoryIconResId, int categoryColor) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.categoryId = 0;
        this.description = description;
        this.amount = amount;
        this.date = dateObj != null ? dateObj.getTime() : System.currentTimeMillis();
        this.isExpense = isExpense;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public int getTransactionId() { return transactionId; }
    public int getUserId() { return userId; }
    public int getWalletId() { return walletId; }
    public int getCategoryId() { return categoryId; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public long getDate() { return date; }
    public boolean isExpense() { return isExpense; }
    public long getCreatedAt() { return createdAt; }

    // Backward compatibility
    @Ignore
    public Date getDateAsDate() { return new Date(date); }

    @Ignore
    public String getCategory() { return ""; }
    @Ignore
    public int getCategoryIconResId() { return 0; }
    @Ignore
    public int getCategoryColor() { return 0; }

    // Setters
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public void setUserId(int userId) { this.userId = userId; }

    public void setWalletId(int walletId) { this.walletId = walletId; }

    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public void setAmount(double amount) { this.amount = amount; }

    public void setDate(long date) { this.date = date; }

    public void setExpense(boolean expense) { isExpense = expense; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Backward compatibility setters
    @Ignore
    public void setDate(Date dateObj) {
        this.date = dateObj != null ? dateObj.getTime() : System.currentTimeMillis();
    }
    @Ignore
    public void setCategory(String category) { }
    @Ignore
    public void setCategoryIconResId(int iconResId) { }
    @Ignore
    public void setCategoryColor(int color) { }

    @Ignore
    public Transaction(int userId, int walletId, int categoryId, String description,
                       double amount, long date, boolean isExpense) {
        this(userId, walletId, categoryId, description, amount, date, isExpense, null);
    }

    public String getReceiptPhotoUri() {
        return receiptPhotoUri;
    }

    public void setReceiptPhotoUri(String receiptPhotoUri) {
        this.receiptPhotoUri = receiptPhotoUri;
    }

}
