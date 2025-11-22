package y3.mobiledev.mywallet.helpers;

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


    public static List<TransactionGroup> groupByDateRich(List<TransactionWithCategory> transactions) {

        Log.e(TAG, "groupByDateRich CALLED — CORRECT METHOD! Count: "
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




}