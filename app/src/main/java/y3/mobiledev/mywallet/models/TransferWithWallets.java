package y3.mobiledev.mywallet.models;

import androidx.room.ColumnInfo;

/**
 * POJO for joining Transfer with Wallet names
 */
public class TransferWithWallets {

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

    @ColumnInfo(name = "from_wallet_name")
    private String fromWalletName;

    @ColumnInfo(name = "to_wallet_name")
    private String toWalletName;

    public TransferWithWallets(int transferId, int userId, int fromWalletId,
                               int toWalletId, double amount, long date,
                               String fromWalletName, String toWalletName) {
        this.transferId = transferId;
        this.userId = userId;
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amount = amount;
        this.date = date;
        this.fromWalletName = fromWalletName;
        this.toWalletName = toWalletName;
    }

    // Getters
    public int getTransferId() { return transferId; }
    public int getUserId() { return userId; }
    public int getFromWalletId() { return fromWalletId; }
    public int getToWalletId() { return toWalletId; }
    public double getAmount() { return amount; }
    public long getDate() { return date; }
    public String getFromWalletName() { return fromWalletName; }
    public String getToWalletName() { return toWalletName; }

    // Setters
    public void setTransferId(int transferId) { this.transferId = transferId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setFromWalletId(int fromWalletId) { this.fromWalletId = fromWalletId; }
    public void setToWalletId(int toWalletId) { this.toWalletId = toWalletId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(long date) { this.date = date; }
    public void setFromWalletName(String fromWalletName) { this.fromWalletName = fromWalletName; }
    public void setToWalletName(String toWalletName) { this.toWalletName = toWalletName; }

    // Convert to Transfer object
    public Transfer toTransfer() {
        return new Transfer(transferId, userId, fromWalletId, toWalletId,
                amount, date, System.currentTimeMillis());
    }
}