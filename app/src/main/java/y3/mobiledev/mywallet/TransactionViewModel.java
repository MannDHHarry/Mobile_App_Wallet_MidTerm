package y3.mobiledev.mywallet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import y3.mobiledev.mywallet.Helpers.TransactionManager;
import y3.mobiledev.mywallet.Models.Category;
import y3.mobiledev.mywallet.Models.Transaction;
import y3.mobiledev.mywallet.Models.TransactionGroup;
import y3.mobiledev.mywallet.Models.User;
import y3.mobiledev.mywallet.Models.Wallet;

public class TransactionViewModel extends ViewModel {
    // All app data
    private final MutableLiveData<List<Transaction>> allTransactions = new MutableLiveData<>();
    private final MutableLiveData<List<Wallet>> allWallets = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> allExpenseCategories = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> allIncomeCategories = new MutableLiveData<>();

    // User-specific filtered data
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();
    private final MutableLiveData<List<TransactionGroup>> transactionGroups = new MutableLiveData<>();
    private final MutableLiveData<List<Wallet>> wallets = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> expenseCategories = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> incomeCategories = new MutableLiveData<>();

    private static final int CUSTOM_EXPENSE_START = 1000;
    private static final int CUSTOM_INCOME_START = 2000;
    private int nextCustomExpenseId = CUSTOM_EXPENSE_START;
    private int nextCustomIncomeId = CUSTOM_INCOME_START;

    public TransactionViewModel() {
        initializeSampleData();
    }

    private void initializeSampleData() {
        // Create data only for User 1 (User 2 will be empty)
        List<Transaction> sampleTrans = new ArrayList<>(SampleDataProvider.createSampleTransactions(1));
        List<Wallet> sampleWals = new ArrayList<>(SampleDataProvider.createSampleWallets(1));
        List<Category> sampleExpenseCats = new ArrayList<>(SampleDataProvider.createSampleExpenseCategories(1));
        List<Category> sampleIncomeCats = new ArrayList<>(SampleDataProvider.createSampleIncomeCategories(1));

        allTransactions.setValue(sampleTrans);
        allWallets.setValue(sampleWals);
        allExpenseCategories.setValue(sampleExpenseCats);
        allIncomeCategories.setValue(sampleIncomeCats);
    }

    // ===== PUBLIC GETTERS =====
    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<List<Transaction>> getTransactions() { return transactions; }
    public LiveData<List<TransactionGroup>> getTransactionGroups() { return transactionGroups; }
    public LiveData<List<Wallet>> getWallets() { return wallets; }
    public LiveData<List<Category>> getExpenseCategories() { return expenseCategories; }
    public LiveData<List<Category>> getIncomeCategories() { return incomeCategories; }

    public List<Wallet> getAllWallets() {
        return allWallets.getValue() != null ? allWallets.getValue() : new ArrayList<>();
    }

    /**
     * Call when user logs in or registers
     */
    public void initializeUserData(User user) {
        currentUser.setValue(user);
        int userId = user.getUserId();

        // Filter data by userId
        List<Transaction> userTransactions = filterTransactionsByUser(userId);
        transactions.setValue(userTransactions);
        transactionGroups.setValue(TransactionManager.groupTransactionsByDate(userTransactions));

        List<Wallet> userWallets = filterWalletsByUser(userId);
        wallets.setValue(userWallets);

        List<Category> defaultExpense = new ArrayList<>(allExpenseCategories.getValue() != null ?
                allExpenseCategories.getValue() : new ArrayList<>());
        List<Category> defaultIncome = new ArrayList<>(allIncomeCategories.getValue() != null ?
                allIncomeCategories.getValue() : new ArrayList<>());

        expenseCategories.setValue(defaultExpense);
        incomeCategories.setValue(defaultIncome);

    }

    /**
     * Call when user logs out
     */
    public void clearUserData() {
        currentUser.setValue(null);
        transactions.setValue(new ArrayList<>());
        transactionGroups.setValue(new ArrayList<>());
        wallets.setValue(new ArrayList<>());
        expenseCategories.setValue(new ArrayList<>());
        incomeCategories.setValue(new ArrayList<>());
    }

    // ===== TRANSACTION METHODS =====
    public void addTransaction(Transaction transaction) {
        List<Transaction> allTrans = new ArrayList<>(allTransactions.getValue() != null ?
                allTransactions.getValue() : new ArrayList<>());
        allTrans.add(transaction);
        allTransactions.setValue(allTrans);

        List<Transaction> userTrans = new ArrayList<>(transactions.getValue() != null ?
                transactions.getValue() : new ArrayList<>());
        userTrans.add(transaction);
        transactions.setValue(userTrans);
        transactionGroups.setValue(TransactionManager.groupTransactionsByDate(userTrans));
    }

    // ===== WALLET METHODS =====

    public void addWalletDirect(List<Wallet> updatedWallets) {
        // Update all wallets
        allWallets.setValue(updatedWallets);

        // Re-filter for current user
        int userId = currentUser.getValue().getUserId();
        List<Wallet> userWallets = filterWalletsByUser(userId);
        wallets.setValue(userWallets);
    }

    public void updateWalletBalance(int walletId, double amount, boolean isExpense) {
        int userId = currentUser.getValue().getUserId();

        List<Wallet> allWallets_list = new ArrayList<>(allWallets.getValue() != null ?
                allWallets.getValue() : new ArrayList<>());
        Double updatedBalance = null;
        for (Wallet w : allWallets_list) {
            if (w.getWalletId() == walletId && w.getUserId() == userId) {
                updatedBalance = isExpense ? w.getBalance() - amount : w.getBalance() + amount;
                w.setBalance(updatedBalance);
                break;
            }
        }
        allWallets.setValue(allWallets_list);

        List<Wallet> userWallets_list = new ArrayList<>(wallets.getValue() != null ?
                wallets.getValue() : new ArrayList<>());
        if (updatedBalance != null) {
            for (Wallet w : userWallets_list) {
                if (w.getWalletId() == walletId) {
                    // Apply the same computed balance to avoid double subtraction/addition
                    w.setBalance(updatedBalance);
                    break;
                }
            }
        }
        wallets.setValue(userWallets_list);
    }

    public void updateWallet(Wallet wallet) {
        int userId = currentUser.getValue().getUserId();

        // Update in allWallets
        List<Wallet> allWallets_list = new ArrayList<>(allWallets.getValue() != null ?
                allWallets.getValue() : new ArrayList<>());
        for (int i = 0; i < allWallets_list.size(); i++) {
            if (allWallets_list.get(i).getWalletId() == wallet.getWalletId() &&
                    allWallets_list.get(i).getUserId() == userId) {
                allWallets_list.set(i, wallet);
                break;
            }
        }
        allWallets.setValue(allWallets_list);

        // Update in userWallets
        List<Wallet> userWallets_list = new ArrayList<>(wallets.getValue() != null ?
                wallets.getValue() : new ArrayList<>());
        for (int i = 0; i < userWallets_list.size(); i++) {
            if (userWallets_list.get(i).getWalletId() == wallet.getWalletId()) {
                userWallets_list.set(i, wallet);
                break;
            }
        }
        wallets.setValue(userWallets_list);
    }

    public void deleteWallet(Wallet wallet) {
        int userId = currentUser.getValue().getUserId();

        // Delete from allWallets
        List<Wallet> allWallets_list = new ArrayList<>(allWallets.getValue() != null ?
                allWallets.getValue() : new ArrayList<>());
        allWallets_list.removeIf(w -> w.getWalletId() == wallet.getWalletId() && w.getUserId() == userId);
        allWallets.setValue(allWallets_list);

        // Delete from userWallets
        List<Wallet> userWallets_list = new ArrayList<>(wallets.getValue() != null ?
                wallets.getValue() : new ArrayList<>());
        userWallets_list.removeIf(w -> w.getWalletId() == wallet.getWalletId());
        wallets.setValue(userWallets_list);
    }

    // ===== CATEGORY METHODS =====
    public Category addExpenseCategory(String name, int colorResId, int iconResId) {
        int userId = currentUser.getValue().getUserId();

        if (categoryExists(name, false)) {
            return null;
        }

        Category newCategory = new Category(nextCustomExpenseId++, userId, name,
                iconResId, colorResId, false);

        List<Category> allExpense = new ArrayList<>(allExpenseCategories.getValue() != null ?
                allExpenseCategories.getValue() : new ArrayList<>());
        allExpense.add(newCategory);
        allExpenseCategories.setValue(allExpense);

        List<Category> userExpense = new ArrayList<>(expenseCategories.getValue() != null ?
                expenseCategories.getValue() : new ArrayList<>());
        userExpense.add(newCategory);
        expenseCategories.setValue(userExpense);

        return newCategory;
    }

    public Category addIncomeCategory(String name, int colorResId, int iconResId) {
        int userId = currentUser.getValue().getUserId();

        if (categoryExists(name, true)) {
            return null;
        }

        Category newCategory = new Category(nextCustomIncomeId++, userId, name,
                iconResId, colorResId, true);

        List<Category> allIncome = new ArrayList<>(allIncomeCategories.getValue() != null ?
                allIncomeCategories.getValue() : new ArrayList<>());
        allIncome.add(newCategory);
        allIncomeCategories.setValue(allIncome);

        List<Category> userIncome = new ArrayList<>(incomeCategories.getValue() != null ?
                incomeCategories.getValue() : new ArrayList<>());
        userIncome.add(newCategory);
        incomeCategories.setValue(userIncome);

        return newCategory;
    }

    public boolean categoryExists(String name, boolean isIncome) {
        List<Category> list = isIncome ? incomeCategories.getValue() : expenseCategories.getValue();
        if (list == null) return false;
        for (Category cat : list) {
            if (cat.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean deleteCategory(Category category) {
        if (category.isIncome()) {
            List<Category> allIncome = new ArrayList<>(allIncomeCategories.getValue() != null ?
                    allIncomeCategories.getValue() : new ArrayList<>());
            allIncome.remove(category);
            allIncomeCategories.setValue(allIncome);

            List<Category> userIncome = new ArrayList<>(incomeCategories.getValue() != null ?
                    incomeCategories.getValue() : new ArrayList<>());
            boolean removed = userIncome.remove(category);
            if (removed) {
                incomeCategories.setValue(userIncome);
            }
            return removed;
        } else {
            List<Category> allExpense = new ArrayList<>(allExpenseCategories.getValue() != null ?
                    allExpenseCategories.getValue() : new ArrayList<>());
            allExpense.remove(category);
            allExpenseCategories.setValue(allExpense);

            List<Category> userExpense = new ArrayList<>(expenseCategories.getValue() != null ?
                    expenseCategories.getValue() : new ArrayList<>());
            boolean removed = userExpense.remove(category);
            if (removed) {
                expenseCategories.setValue(userExpense);
            }
            return removed;
        }
    }

    public boolean updateCategory(Category category) {
        if (category.isIncome()) {
            List<Category> userIncome = new ArrayList<>(incomeCategories.getValue() != null ?
                    incomeCategories.getValue() : new ArrayList<>());
            for (int i = 0; i < userIncome.size(); i++) {
                if (userIncome.get(i).getCategoryId() == category.getCategoryId()) {
                    userIncome.set(i, category);
                    incomeCategories.setValue(userIncome);
                    return true;
                }
            }
        } else {
            List<Category> userExpense = new ArrayList<>(expenseCategories.getValue() != null ?
                    expenseCategories.getValue() : new ArrayList<>());
            for (int i = 0; i < userExpense.size(); i++) {
                if (userExpense.get(i).getCategoryId() == category.getCategoryId()) {
                    userExpense.set(i, category);
                    expenseCategories.setValue(userExpense);
                    return true;
                }
            }
        }
        return false;
    }

    // ===== HELPER FILTER METHODS =====
    private List<Transaction> filterTransactionsByUser(int userId) {
        List<Transaction> all = allTransactions.getValue();
        if (all == null) return new ArrayList<>();
        return all.stream()
                .filter(t -> t.getUserId() == userId)
                .collect(Collectors.toList());
    }

    private List<Wallet> filterWalletsByUser(int userId) {
        List<Wallet> all = allWallets.getValue();
        if (all == null) return new ArrayList<>();
        return all.stream()
                .filter(w -> w.getUserId() == userId)
                .collect(Collectors.toList());
    }

}