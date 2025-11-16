package y3.mobiledev.mywallet.Models;

import java.util.List;

public class TransactionGroup {
    private String header; // "Today", "Yesterday", "Earlier"
    private List<Transaction> transactions;

    public TransactionGroup(String header, List<Transaction> transactions) {
        this.header = header;
        this.transactions = transactions;
    }

    public String getHeader() {
        return header;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}