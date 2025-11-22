package y3.mobiledev.mywallet;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import y3.mobiledev.mywallet.helpers.TransactionManager;
import y3.mobiledev.mywallet.models.Category;
import y3.mobiledev.mywallet.models.Transaction;
import y3.mobiledev.mywallet.models.TransactionGroup;
import y3.mobiledev.mywallet.models.TransactionWithCategory;
import y3.mobiledev.mywallet.models.User;
import y3.mobiledev.mywallet.models.Wallet;
import y3.mobiledev.mywallet.repository.CategoryRepository;
import y3.mobiledev.mywallet.repository.TransactionRepository;
import y3.mobiledev.mywallet.repository.UserRepository;
import y3.mobiledev.mywallet.repository.WalletRepository;

public class TransactionViewModel extends AndroidViewModel {

    // Repositories
    private UserRepository userRepository;
    private WalletRepository walletRepository;
    private CategoryRepository categoryRepository;
    private TransactionRepository transactionRepository;


    // Current user
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();


    // User-specific Data
    private LiveData<List<Wallet>> wallets;
    private LiveData<List<Category>> expenseCategories;
    private LiveData<List<Category>> incomeCategories;
    private LiveData<List<TransactionWithCategory>> transactionsWithCategory;


    // Grouped transactions for UI
    private LiveData<List<TransactionGroup>> transactionGroups;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        // Initialize repositories
        userRepository = new UserRepository(application);
        walletRepository = new WalletRepository(application);
        categoryRepository = new CategoryRepository(application);
        transactionRepository = new TransactionRepository(application);
    }

    //Initialize User Data as user log in
    public void initializeUserData(User user) {
        currentUser.setValue(user);
        int userId = user.getUserId();

        // Set up LiveData for Current User
        wallets = walletRepository.getWalletsByUser(userId);
        expenseCategories = categoryRepository.getActiveExpenseCategories(userId);
        incomeCategories = categoryRepository.getActiveIncomeCategories(userId);
        transactionsWithCategory = transactionRepository.getTransactionsWithCategoryByUser(userId);

        // Transform transactions into grouped format
        transactionGroups = Transformations.switchMap(transactionsWithCategory, list -> {
            if (list == null) {
                return new MutableLiveData<>(new ArrayList<>());
            }
            return new MutableLiveData<>(TransactionManager.groupByDateRich(list));
        });
    }

    //Clear data when user log out
    public void clearUserData() {
        currentUser.setValue(null);
        wallets = null;
        expenseCategories = null;
        incomeCategories = null;
        transactionsWithCategory = null;
        transactionGroups = null;
    }

    // Transaction Related Methods

    public void addTransaction(int walletId, int categoryId, String description, double amount,
                               long date, boolean isExpense)

    {
        //Call this in AddTransaction Fragment

        User user = currentUser.getValue();
        if (user == null) return;

        Transaction transaction = new Transaction( user.getUserId(), walletId, categoryId,
                                                    description, amount, date, isExpense );

        transactionRepository.addTransaction(transaction);
    }

    public void updateTransaction(Transaction oldTransaction, Transaction newTransaction) {
        transactionRepository.updateTransaction(oldTransaction, newTransaction);
    }

    public void deleteTransaction(Transaction transaction) {
        transactionRepository.deleteTransaction(transaction);
    }

    // Wallet Related Methods

    public void addWallet(String name, int iconResId, double initialBalance) {
        User user = currentUser.getValue();
        if (user == null) return;
        Wallet wallet = new Wallet(user.getUserId(), name, iconResId, initialBalance);
        walletRepository.addWallet(wallet);
    }

    public void updateWallet(Wallet wallet) {
        walletRepository.updateWallet(wallet);
    }

    public boolean deleteWallet(Wallet wallet) {
        return walletRepository.deleteWallet(wallet);
    }

    public boolean hasWallets() {
        User user = currentUser.getValue();
        if (user == null) return false;
        return walletRepository.hasWallets(user.getUserId());
    }

    //Category Related Methods

    public Long addExpenseCategory(String name, int iconResId, int colorResId) {
        User user = currentUser.getValue();
        if (user == null) return null;

        Category category = new Category(user.getUserId(), name, iconResId, colorResId, false, false);
        return categoryRepository.addCategory(category);
    }

    public Long addIncomeCategory(String name, int iconResId, int colorResId) {
        User user = currentUser.getValue();
        if (user == null) return null;

        Category category = new Category(user.getUserId(), name, iconResId, colorResId, true, false);
        return categoryRepository.addCategory(category);
    }

    public void updateCategory(Category category) {
        categoryRepository.updateCategory(category);
    }
    // If there are  transactions with this category , not allow Delete so Archive
    public boolean archiveCategory(int categoryId) {
        return categoryRepository.archiveCategory(categoryId);
    }

    public void unarchiveCategory(int categoryId) {
        categoryRepository.unarchiveCategory(categoryId);
    }

    public boolean deleteCategory(int categoryId) {
        return categoryRepository.deleteCategory(categoryId);
    }

    public int getCategoryTransactionCount(int categoryId) {
        return categoryRepository.getTransactionCount(categoryId);
    }

    // Getters
    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<List<Wallet>> getWallets() { return wallets; }
    public LiveData<List<Category>> getExpenseCategories() { return expenseCategories; }
    public LiveData<List<Category>> getIncomeCategories() { return incomeCategories; }
    public LiveData<List<TransactionWithCategory>> getTransactionsWithCategory() { return transactionsWithCategory; }
    public LiveData<List<TransactionGroup>> getTransactionGroups() { return transactionGroups; }

}