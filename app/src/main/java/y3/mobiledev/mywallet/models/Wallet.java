package y3.mobiledev.mywallet.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Wallet {
    private int walletId;
    private int userId;
    private String name;
    private int iconResId;
    private double balance;
    private int transactionCount;

    public Wallet(int walletId, int userId, String name, int iconResId,
                  double balance, int transactionCount) {
        this.walletId = walletId;
        this.userId = userId;
        this.name = name;
        this.iconResId = iconResId;
        this.balance = balance;
        this.transactionCount = transactionCount;
    }

    // Backward compatibility
    public Wallet(int walletId, String name, int iconResId, double balance, int transactionCount) {
        this(walletId, 1, name, iconResId, balance, transactionCount);
    }


    // Getters
    public int getWalletId() { return walletId; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public int getIconResId() { return iconResId; }
    public double getBalance() { return balance; }
    public int getTransactionCount() { return transactionCount; }


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