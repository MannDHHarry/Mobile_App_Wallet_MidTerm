package y3.mobiledev.mywallet.models;

import java.util.List;

public class TransactionGroup {
    private String header; // "Today", "Yesterday", "Earlier"
    private List<TransactionWithCategory> transactions;

    public TransactionGroup(String header, List<TransactionWithCategory> transactions) {
        this.header = header;
        this.transactions = transactions;
    }

    public String getHeader() {
        return header;
    }

    public List<TransactionWithCategory> getTransactions() {
        return transactions;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setTransactions(List<TransactionWithCategory> transactions) {
        this.transactions = transactions;
    }
}