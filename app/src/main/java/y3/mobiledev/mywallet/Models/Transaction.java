package y3.mobiledev.mywallet.Models;

import java.util.Date;

public class Transaction {
    private int transactionId;
    private int userId;
    private String category;
    private String description;
    private double amount;
    private Date date;
    private boolean isExpense;
    private int categoryIconResId;
    private int categoryColor;

    public Transaction(int transactionId, int userId, String category, String description,
                       double amount, Date date, boolean isExpense,
                       int categoryIconResId, int categoryColor) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.category = category != null ? category : "";
        this.description = description != null ? description : "";
        this.amount = amount;
        this.date = date != null ? new Date(date.getTime()) : new Date();
        this.isExpense = isExpense;
        this.categoryIconResId = categoryIconResId;
        this.categoryColor = categoryColor;
    }

    // Backward compatibility
    public Transaction(int transactionId, String category, String description,
                       double amount, Date date, boolean isExpense,
                       int categoryIconResId, int categoryColor) {
        this(transactionId, 1, category, description, amount, date, isExpense,
                categoryIconResId, categoryColor);
    }


    // Getters
    public int getTransactionId() { return transactionId; }
    public int getUserId() { return userId; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public Date getDate() { return new Date(date.getTime()); }
    public boolean isExpense() { return isExpense; }
    public int getCategoryIconResId() { return categoryIconResId; }
    public int getCategoryColor() { return categoryColor; }


    // Setters (only for fields that need updating)
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public void setCategory(String category) { this.category = category != null ? category : ""; }
    public void setDescription(String description) { this.description = description != null ? description : ""; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(Date date) { this.date = date != null ? new Date(date.getTime()) : new Date(); }
    public void setExpense(boolean expense) { this.isExpense = expense; }
    public void setCategoryIconResId(int categoryIconResId) { this.categoryIconResId = categoryIconResId; }
    public void setCategoryColor(int categoryColor) { this.categoryColor = categoryColor; }
}