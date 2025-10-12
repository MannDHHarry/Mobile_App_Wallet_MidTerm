package y3.mobiledev.mywallet;

public class Wallet {
    private int walletId;
    private String name;
    private int iconResId; // Resource ID for icon
    private double balance;
    private int transactionCount; // For sorting by frequency

    public Wallet(int walletId, String name, int iconResId, double balance, int transactionCount) {
        this.walletId = walletId;
        this.name = name;
        this.iconResId = iconResId;
        this.balance = balance;
        this.transactionCount = transactionCount;
    }

    // Getters
    public int getWalletId() {
        return walletId;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public double getBalance() {
        return balance;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    // Setters
    public void setWalletId(int walletId) {
        this.walletId = walletId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }
}