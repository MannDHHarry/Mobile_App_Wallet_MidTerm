package y3.mobiledev.mywallet.models;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;

/**
 * POJO for joining Transaction with Category data
 * This is used when displaying transactions in UI
 */

public class TransactionWithCategory {

    // Transaction fields
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

    // Category fields (from JOIN)
    @ColumnInfo(name = "categoryName")
    private String categoryName;

    @ColumnInfo(name = "categoryIcon")
    private int categoryIcon;

    @ColumnInfo(name = "categoryColor")
    private int categoryColor;

    @ColumnInfo(name = "receipt_photo_uri")
    private String receiptPhotoUri;


    // Constructor
    public TransactionWithCategory(int transactionId, int userId, int walletId, int categoryId,
                                   String description, double amount, long date, boolean isExpense,
                                   String categoryName, int categoryIcon, int categoryColor,
                                   String receiptPhotoUri) {

        this.transactionId = transactionId;
        this.userId = userId;
        this.walletId = walletId;
        this.categoryId = categoryId;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.isExpense = isExpense;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.categoryColor = categoryColor;
        this.receiptPhotoUri = receiptPhotoUri;
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
    public String getCategoryName() { return categoryName; }
    public int getCategoryIcon() { return categoryIcon; }
    public int getCategoryColor() { return categoryColor; }

    // Setters
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public void setUserId(int userId) { this.userId = userId; }

    public void setWalletId(int walletId) { this.walletId = walletId; }

    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public void setDescription(String description) { this.description = description; }

    public void setAmount(double amount) { this.amount = amount; }

    public void setDate(long date) { this.date = date; }

    public void setExpense(boolean expense) { isExpense = expense; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setCategoryIcon(int categoryIcon) { this.categoryIcon = categoryIcon; }
    public void setCategoryColor(int categoryColor) { this.categoryColor = categoryColor; }

    public String getReceiptPhotoUri() {
        return receiptPhotoUri;
    }

    public void setReceiptPhotoUri(String receiptPhotoUri) {
        this.receiptPhotoUri = receiptPhotoUri;
    }


    // Convert to Transaction object (without category details)
    @Ignore
    public Transaction toTransaction() {
        return new Transaction(transactionId, userId, walletId, categoryId,
                description, amount, date, isExpense, System.currentTimeMillis(),
                receiptPhotoUri);
    }

}