package y3.mobiledev.mywallet;

import android.util.Log;
import y3.mobiledev.mywallet.Models.Category;
import y3.mobiledev.mywallet.Models.Transaction;
import y3.mobiledev.mywallet.Models.Wallet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SampleDataProvider {

    public static List<Wallet> createSampleWallets(int userId) {
        List<Wallet> wallets = new ArrayList<>();
        wallets.add(new Wallet(1, userId, "Personal", android.R.drawable.ic_menu_myplaces, 500.00, 15));
        wallets.add(new Wallet(2, userId, "Business", android.R.drawable.ic_menu_myplaces, 1200.00, 12));
        wallets.add(new Wallet(3, userId, "Savings", android.R.drawable.ic_menu_myplaces, 643.23, 5));
        Log.d("SampleDataProvider", "Created " + wallets.size() + " wallets for userId: " + userId);
        return wallets;
    }

    public static List<Transaction> createSampleTransactions(int userId) {
        List<Transaction> transactions = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        // Today's transactions
        transactions.add(new Transaction(1, userId, "Utilities", "paid wifi bill for home internet service",
                23.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_orange));
        transactions.add(new Transaction(2, userId, "Transport", "taxi to office",
                15.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_blue));

        // Yesterday's transactions
        cal.add(Calendar.DAY_OF_MONTH, -1);
        transactions.add(new Transaction(3, userId, "Utilities", "electricity bill",
                45.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_orange));
        transactions.add(new Transaction(4, userId, "Food", "lunch at restaurant",
                30.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_pink));

        // Earlier transactions
        cal.add(Calendar.DAY_OF_MONTH, -2);
        transactions.add(new Transaction(5, userId, "Entertainment", "movie tickets for family",
                50.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_purple));

        cal.add(Calendar.DAY_OF_MONTH, -1);
        transactions.add(new Transaction(6, userId, "Transport", "fuel for car",
                60.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_blue));

        // Income transaction
        cal.set(Calendar.DAY_OF_MONTH, 1);
        transactions.add(new Transaction(7, userId, "Salary", "monthly salary from company",
                2500.00, cal.getTime(), false, android.R.drawable.ic_dialog_info, R.color.category_green));
        transactions.add(new Transaction(8, userId, "Freelance", "project payment from client",
                500.00, cal.getTime(), false, android.R.drawable.ic_dialog_info, R.color.category_green));

        // More earlier transactions
        cal.add(Calendar.DAY_OF_MONTH, -5);
        transactions.add(new Transaction(9, userId, "Shopping", "groceries for the week",
                120.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_teal));
        transactions.add(new Transaction(10, userId, "Healthcare", "doctor consultation",
                80.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_pink));

        Log.d("SampleDataProvider", "Created " + transactions.size() + " transactions for userId: " + userId);
        return transactions;
    }

    public static List<Category> createSampleExpenseCategories(int userId) {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(1, userId, "Food", R.drawable.ic_launcher_foreground, R.color.category_orange, false));
        categories.add(new Category(2, userId, "Transport", R.drawable.ic_launcher_foreground, R.color.category_blue, false));
        categories.add(new Category(3, userId, "Utilities", R.drawable.ic_launcher_foreground, R.color.category_orange, false));
        categories.add(new Category(4, userId, "Shopping", R.drawable.ic_launcher_foreground, R.color.category_teal, false));
        categories.add(new Category(5, userId, "Entertainment", R.drawable.ic_launcher_foreground, R.color.category_purple, false));
        categories.add(new Category(6, userId, "Healthcare", R.drawable.ic_launcher_foreground, R.color.category_pink, false));
        categories.add(new Category(7, userId, "Bills", R.drawable.ic_launcher_foreground, R.color.category_orange, false));
        Log.d("SampleDataProvider", "Created " + categories.size() + " expense categories for userId: " + userId);
        return categories;
    }

    public static List<Category> createSampleIncomeCategories(int userId) {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(8, userId, "Salary", R.drawable.ic_launcher_foreground, R.color.category_green, true));
        categories.add(new Category(9, userId, "Freelance", R.drawable.ic_launcher_foreground, R.color.category_green, true));
        categories.add(new Category(10, userId, "Investment", R.drawable.ic_launcher_foreground, R.color.category_green, true));
        categories.add(new Category(11, userId, "Gift", R.drawable.ic_launcher_foreground, R.color.category_green, true));
        categories.add(new Category(12, userId, "Other Income", R.drawable.ic_launcher_foreground, R.color.category_green, true));
        Log.d("SampleDataProvider", "Created " + categories.size() + " income categories for userId: " + userId);
        return categories;
    }
}
