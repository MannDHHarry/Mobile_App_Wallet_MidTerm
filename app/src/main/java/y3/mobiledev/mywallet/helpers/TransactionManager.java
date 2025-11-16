package y3.mobiledev.mywallet.Helpers;

import y3.mobiledev.mywallet.Models.Transaction;
import y3.mobiledev.mywallet.Models.TransactionGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TransactionManager {

    public static List<TransactionGroup> groupTransactionsByDate(List<Transaction> transactions) {
        List<Transaction> todayTransactions = new ArrayList<>();
        List<Transaction> yesterdayTransactions = new ArrayList<>();
        List<Transaction> earlierTransactions = new ArrayList<>();

        for (Transaction transaction : transactions) {
            String groupHeader = DateManager.getGroupHeader(transaction.getDate());
            switch (groupHeader) {
                case "Today":
                    todayTransactions.add(transaction);
                    break;
                case "Yesterday":
                    yesterdayTransactions.add(transaction);
                    break;
                case "Earlier":
                    earlierTransactions.add(transaction);
                    break;
            }
        }

        // Sort transactions within each group by date (newest first)
        Comparator<Transaction> dateComparator = (t1, t2) -> t2.getDate().compareTo(t1.getDate());
        todayTransactions.sort(dateComparator);
        yesterdayTransactions.sort(dateComparator);
        earlierTransactions.sort(dateComparator);

        List<TransactionGroup> groups = new ArrayList<>();
        if (!todayTransactions.isEmpty()) {
            groups.add(new TransactionGroup("Today", todayTransactions));
        }
        if (!yesterdayTransactions.isEmpty()) {
            groups.add(new TransactionGroup("Yesterday", yesterdayTransactions));
        }
        if (!earlierTransactions.isEmpty()) {
            groups.add(new TransactionGroup("Earlier", earlierTransactions));
        }

        return groups;
    }

    public static String truncateToWords(String text, int maxWords, boolean addEllipsis) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.split("\\s+");
        if (words.length <= maxWords) {
            return text;
        }

        StringBuilder truncated = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            truncated.append(words[i]).append(" ");
        }

        String result = truncated.toString().trim();
        return addEllipsis ? result + "..." : result;
    }



    public static void addTransactionToGroups(Transaction newTransaction, List<TransactionGroup> transactionGroups) {
        String groupHeader = DateManager.getGroupHeader(newTransaction.getDate());

        TransactionGroup targetGroup = null;
        for (TransactionGroup group : transactionGroups) {
            if (group.getHeader().equals(groupHeader)) {
                targetGroup = group;
                break;
            }
        }

        if (targetGroup == null) {
            List<Transaction> newList = new ArrayList<>();
            newList.add(newTransaction);
            targetGroup = new TransactionGroup(groupHeader, newList);
            if (groupHeader.equals("Today")) {
                transactionGroups.add(0, targetGroup);
            } else if (groupHeader.equals("Yesterday")) {
                int insertIndex = transactionGroups.stream()
                        .anyMatch(g -> g.getHeader().equals("Today")) ? 1 : 0;
                transactionGroups.add(insertIndex, targetGroup);
            } else {
                transactionGroups.add(targetGroup);
            }
        } else {
            targetGroup.getTransactions().add(newTransaction);
            // Re-sort group to maintain newest-first order
            targetGroup.getTransactions().sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));
        }
    }
}