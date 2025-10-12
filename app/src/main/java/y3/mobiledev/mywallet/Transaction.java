package y3.mobiledev.mywallet;

import java.util.Date;

public class Transaction {
    private int transactionId;
    private String category;
    private String description;
    private double amount;
    private Date date;
    private boolean isExpense; // true for expense, false for income
    private int categoryIconResId;
    private int categoryColor; // Color resource ID

    public Transaction(int transactionId, String category, String description,
                       double amount, Date date, boolean isExpense,
                       int categoryIconResId, int categoryColor) {
        this.transactionId = transactionId;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.isExpense = isExpense;
        this.categoryIconResId = categoryIconResId;
        this.categoryColor = categoryColor;
    }

    // Getters
    public int getTransactionId() {
        return transactionId;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public boolean isExpense() {
        return isExpense;
    }

    public int getCategoryIconResId() {
        return categoryIconResId;
    }

    public int getCategoryColor() {
        return categoryColor;
    }

    // Setters
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setExpense(boolean expense) {
        isExpense = expense;
    }

    public void setCategoryIconResId(int categoryIconResId) {
        this.categoryIconResId = categoryIconResId;
    }

    public void setCategoryColor(int categoryColor) {
        this.categoryColor = categoryColor;
    }
}