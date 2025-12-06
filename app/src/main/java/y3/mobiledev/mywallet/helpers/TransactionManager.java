package y3.mobiledev.mywallet.helpers;

import android.content.Context;
import android.util.Log;

import y3.mobiledev.mywallet.models.Transaction;
import y3.mobiledev.mywallet.models.TransactionGroup;
import y3.mobiledev.mywallet.models.TransactionWithCategory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class TransactionManager {

    public static final String TAG = "TRANSACTION_DEBUG";

    public static List<TransactionGroup> groupTransactionsByDate(List<Transaction> transactions) {
        Log.e(TAG, "OLD BROKEN METHOD CALLED — THIS SHOULD NOT HAPPEN!");
        return new ArrayList<>(); // force empty
    }

    /**
     * Groups transactions by date with localized headers
     * @param transactions List of transactions to group
     * @param context Context for string resources
     * @return List of transaction groups with localized headers
     */
    public static List<TransactionGroup> groupByDateRich(List<TransactionWithCategory> transactions, Context context) {

        Log.e(TAG, "groupByDateRich CALLED — CORRECT METHOD! Count: "
                + (transactions != null ? transactions.size() : "NULL"));
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }


        List<TransactionWithCategory> today = new ArrayList<>();
        List<TransactionWithCategory> yesterday = new ArrayList<>();
        List<TransactionWithCategory> earlier = new ArrayList<>();

        for (TransactionWithCategory t : transactions) {
            String key = DateManager.getGroupKey(new Date(t.getDate()));
            switch (key) {
                case DateManager.KEY_TODAY:
                    today.add(t);
                    break;
                case DateManager.KEY_YESTERDAY:
                    yesterday.add(t);
                    break;
                case DateManager.KEY_EARLIER:
                    earlier.add(t);
                    break;
            }
        }

        // Sort newest first
        Comparator<TransactionWithCategory> desc = (a, b) -> Long.compare(b.getDate(), a.getDate());
        today.sort(desc);
        yesterday.sort(desc);
        earlier.sort(desc);

        List<TransactionGroup> groups = new ArrayList<>();
        if (!today.isEmpty()) {
            String header = DateManager.getLocalizedHeader(context, DateManager.KEY_TODAY);
            groups.add(new TransactionGroup(header, today));
        }
        if (!yesterday.isEmpty()) {
            String header = DateManager.getLocalizedHeader(context, DateManager.KEY_YESTERDAY);
            groups.add(new TransactionGroup(header, yesterday));
        }
        if (!earlier.isEmpty()) {
            String header = DateManager.getLocalizedHeader(context, DateManager.KEY_EARLIER);
            groups.add(new TransactionGroup(header, earlier));
        }

        Log.e(TAG, "groupByDateRich returning " + groups.size() + " groups");
        return groups;
    }

    /**
     * @deprecated Use groupByDateRich(transactions, context) instead
     */
    @Deprecated
    public static List<TransactionGroup> groupByDateRich(List<TransactionWithCategory> transactions) {

        Log.e(TAG, "groupByDateRich (deprecated) CALLED — Count: "
                + (transactions != null ? transactions.size() : "NULL"));
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }


        List<TransactionWithCategory> today = new ArrayList<>();
        List<TransactionWithCategory> yesterday = new ArrayList<>();
        List<TransactionWithCategory> earlier = new ArrayList<>();

        for (TransactionWithCategory t : transactions) {
            String header = DateManager.getGroupHeader(new Date(t.getDate()));
            switch (header) {
                case "Today":
                    today.add(t);
                    break;
                case "Yesterday":
                    yesterday.add(t);
                    break;
                case "Earlier":
                    earlier.add(t);
                    break;
            }
        }

        // Sort newest first
        Comparator<TransactionWithCategory> desc = (a, b) -> Long.compare(b.getDate(), a.getDate());
        today.sort(desc);
        yesterday.sort(desc);
        earlier.sort(desc);

        List<TransactionGroup> groups = new ArrayList<>();
        if (!today.isEmpty())     groups.add(new TransactionGroup("Today", today));
        if (!yesterday.isEmpty()) groups.add(new TransactionGroup("Yesterday", yesterday));
        if (!earlier.isEmpty())   groups.add(new TransactionGroup("Earlier", earlier));

        Log.e(TAG, "groupByDateRich returning " + groups.size() + " groups");
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

   //New Methods for Notification System
   public static TransactionGroup getTodayGroup(List<TransactionGroup> groups, Context context) {
       if (groups == null || groups.isEmpty()) {
           Log.d(TAG, "getTodayGroup: groups is null or empty");
           return null;
       }

       String todayHeader = DateManager.getLocalizedHeader(context, DateManager.KEY_TODAY);
       for (TransactionGroup group : groups) {
           if (group != null && todayHeader.equals(group.getHeader())) {
               Log.d(TAG, "getTodayGroup: Found Today group with " +
                       group.getTransactions().size() + " transactions");
               return group;
           }
       }

       Log.d(TAG, "getTodayGroup: No Today group found");
       return null;
   }

   /**
    * @deprecated Use getTodayGroup(groups, context) instead
    */
   @Deprecated
   public static TransactionGroup getTodayGroup(List<TransactionGroup> groups) {
       if (groups == null || groups.isEmpty()) {
           Log.d(TAG, "getTodayGroup: groups is null or empty");
           return null;
       }

       for (TransactionGroup group : groups) {
           if (group != null && "Today".equals(group.getHeader())) {
               Log.d(TAG, "getTodayGroup: Found Today group with " +
                       group.getTransactions().size() + " transactions");
               return group;
           }
       }

       Log.d(TAG, "getTodayGroup: No Today group found");
       return null;
   }

    public static DailySummary calculateDailySummary(TransactionGroup todayGroup) {
        double totalIncome = 0.0;
        double totalExpense = 0.0;
        int incomeCount = 0;
        int expenseCount = 0;

        if (todayGroup == null || todayGroup.getTransactions() == null) {
            Log.d(TAG, "calculateDailySummary: No transactions for today");
            return new DailySummary(0, 0, 0, 0);
        }

        for (TransactionWithCategory transaction : todayGroup.getTransactions()) {
            if (transaction.isExpense()) {
                totalExpense += transaction.getAmount();
                expenseCount++;
            } else {
                totalIncome += transaction.getAmount();
                incomeCount++;
            }
        }

//        Log.d(TAG, "calculateDailySummary: Income=$" + totalIncome +
//                " (" + incomeCount + "), Expense=$" + totalExpense +
//                " (" + expenseCount + ")");

        return new DailySummary(totalIncome, totalExpense, incomeCount, expenseCount);
    }

    public static class DailySummary {
        private final double totalIncome;
        private final double totalExpense;
        private final int incomeCount;
        private final int expenseCount;

        public DailySummary(double totalIncome, double totalExpense,
                            int incomeCount, int expenseCount) {
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.incomeCount = incomeCount;
            this.expenseCount = expenseCount;
        }

        public double getTotalIncome() {
            return totalIncome;
        }

        public double getTotalExpense() {
            return totalExpense;
        }

        public int getIncomeCount() {
            return incomeCount;
        }

        public int getExpenseCount() {
            return expenseCount;
        }

        public double getNetAmount() {
            return totalIncome - totalExpense;
        }

        public boolean hasTransactions() {
            return (incomeCount + expenseCount) > 0;
        }

        public int getTotalTransactionCount() {
            return incomeCount + expenseCount;
        }

        @Override
        public String toString() {
            return "DailySummary{" +
                    "income=$" + totalIncome +
                    ", expense=$" + totalExpense +
                    ", incomeCount=" + incomeCount +
                    ", expenseCount=" + expenseCount +
                    ", net=$" + getNetAmount() +
                    '}';
        }
    }

}
