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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import y3.mobiledev.mywallet.helpers.SpendingAnalyzer;
import y3.mobiledev.mywallet.helpers.TransactionManager;
import y3.mobiledev.mywallet.models.Category;
import y3.mobiledev.mywallet.models.SpendingAnalysisResult;
import y3.mobiledev.mywallet.models.Transaction;
import y3.mobiledev.mywallet.models.TransactionGroup;
import y3.mobiledev.mywallet.models.TransactionWithCategory;
import y3.mobiledev.mywallet.models.User;
import y3.mobiledev.mywallet.models.Wallet;
import y3.mobiledev.mywallet.repository.CategoryRepository;
import y3.mobiledev.mywallet.repository.TransactionRepository;
import y3.mobiledev.mywallet.repository.UserRepository;
import y3.mobiledev.mywallet.repository.WalletRepository;
import y3.mobiledev.mywallet.helpers.NotificationDataManager;

// Import Subscription
import y3.mobiledev.mywallet.models.Subscription;
import y3.mobiledev.mywallet.repository.SubscriptionRepository;

public class TransactionViewModel extends AndroidViewModel {

    // Repositories
    private UserRepository userRepository;
    private WalletRepository walletRepository;
    private CategoryRepository categoryRepository;
    private TransactionRepository transactionRepository;

    private SubscriptionRepository subscriptionRepository;


    // Current user
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();


    // User-specific Data
    private LiveData<List<Wallet>> wallets;
    private LiveData<List<Category>> expenseCategories;
    private LiveData<List<Category>> incomeCategories;
    private LiveData<List<TransactionWithCategory>> transactionsWithCategory;


    // Grouped transactions for UI
    private LiveData<List<TransactionGroup>> transactionGroups;

    private Observer<List<TransactionGroup>> transactionGroupsObserver;

    //Subscription Live data
    private LiveData<List<Subscription>> activeSubscriptions;
    private LiveData<List<Subscription>> allSubscriptions;

    // Spending Analysis
    private final MutableLiveData<SpendingAnalysisResult> spendingInsights = new MutableLiveData<>();
    private final ExecutorService analysisExecutor = Executors.newSingleThreadExecutor();
    private SpendingAnalysisResult cachedAnalysisResult;
    private androidx.lifecycle.Observer<List<TransactionWithCategory>> transactionsObserver;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        // Initialize repositories
        userRepository = new UserRepository(application);
        walletRepository = new WalletRepository(application);
        categoryRepository = new CategoryRepository(application);
        transactionRepository = new TransactionRepository(application);
        subscriptionRepository = new SubscriptionRepository(application);
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

        //Subscription
        activeSubscriptions = subscriptionRepository.getActiveSubscriptionsByUser(userId);
        allSubscriptions = subscriptionRepository.getAllSubscriptionsByUser(userId);

        // Transform transactions into grouped format
        transactionGroups = Transformations.switchMap(transactionsWithCategory, list -> {
            if (list == null) {
                return new MutableLiveData<>(new ArrayList<>());
            }
            return new MutableLiveData<>(TransactionManager.groupByDateRich(list));
        });

        setupNotificationDataSync(user);
        
        // Observe transactions for automatic analysis
        setupSpendingAnalysis();
    }

    private void setupNotificationDataSync(User user) {
        // Remove existing observer if any
        if (transactionGroupsObserver != null && transactionGroups != null) {
            transactionGroups.removeObserver(transactionGroupsObserver);
        }

        // Create new observer
        transactionGroupsObserver = groups -> {
            if (groups != null && user != null) {
                Log.d("TransactionViewModel", "Saving " + groups.size() +
                        " transaction groups for notifications");

                NotificationDataManager.saveTransactionGroups(
                        getApplication().getApplicationContext(),
                        groups,
                        user.getUserId()
                );
            }
        };

        // Observe forever (not lifecycle-aware, but we manage it manually)
        if (transactionGroups != null) {
            transactionGroups.observeForever(transactionGroupsObserver);
        }
    }

    //Clear data when user log out
    public void clearUserData() {

        //Remove Observer before clearing
        if (transactionGroupsObserver != null && transactionGroups != null) {
            transactionGroups.removeObserver(transactionGroupsObserver);
            transactionGroupsObserver = null;
        }

        NotificationDataManager.clearData(getApplication().getApplicationContext());
        Log.d("TransactionViewModel", "Notification data cleared on logout");

        currentUser.setValue(null);
        wallets = null;
        expenseCategories = null;
        incomeCategories = null;
        transactionsWithCategory = null;
        transactionGroups = null;

        //Subscription
        activeSubscriptions = null;
        allSubscriptions = null;
        
        // Clear analysis and remove observer
        if (transactionsObserver != null && transactionsWithCategory != null) {
            transactionsWithCategory.removeObserver(transactionsObserver);
            transactionsObserver = null;
        }
        spendingInsights.setValue(null);
        cachedAnalysisResult = null;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (transactionGroupsObserver != null && transactionGroups != null) {
            transactionGroups.removeObserver(transactionGroupsObserver);
        }
        analysisExecutor.shutdown();
    }

    // Transaction Related Methods

    public void addTransaction(int walletId, int categoryId, String description, double amount,
                               long date, boolean isExpense, String receiptPhotoUri) {
        User user = currentUser.getValue();
        if (user == null) return;

        Transaction transaction = new Transaction(
                user.getUserId(),
                walletId,
                categoryId,
                description,
                amount,
                date,
                isExpense,
                receiptPhotoUri  // ← NEW parameter
        );

        transactionRepository.addTransaction(transaction);
        // Analysis will be triggered automatically by the observer when LiveData updates
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

    //Subscription Methods
    public void addSubscription(int walletId, String name, double amount,
                                long startDate, String notes) {
        User user = currentUser.getValue();
        if (user == null) return;

        Subscription subscription = new Subscription(
                user.getUserId(),
                walletId,
                name,
                amount,
                startDate,
                notes
        );

        subscriptionRepository.addSubscription(subscription);
    }

    public void updateSubscription(Subscription subscription) {
        subscriptionRepository.updateSubscription(subscription);
    }

    public void deleteSubscription(Subscription subscription) {
        subscriptionRepository.deleteSubscription(subscription);
    }

    public void toggleSubscriptionStatus(int subscriptionId, boolean isActive) {
        subscriptionRepository.toggleSubscriptionStatus(subscriptionId, isActive);
    }


    // Getters
    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<List<Wallet>> getWallets() { return wallets; }
    public LiveData<List<Category>> getExpenseCategories() { return expenseCategories; }
    public LiveData<List<Category>> getIncomeCategories() { return incomeCategories; }
    public LiveData<List<TransactionWithCategory>> getTransactionsWithCategory() { return transactionsWithCategory; }
    public LiveData<List<TransactionGroup>> getTransactionGroups() { return transactionGroups; }

    public LiveData<List<Subscription>> getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public LiveData<List<Subscription>> getAllSubscriptions() {
        return allSubscriptions;
    }

    // Spending Analysis Methods

    /**
     * Setup automatic spending analysis when transactions change
     */
    private void setupSpendingAnalysis() {
        // Remove existing observer if any
        if (transactionsObserver != null && transactionsWithCategory != null) {
            try {
                transactionsWithCategory.removeObserver(transactionsObserver);
            } catch (Exception e) {
                Log.e("TransactionViewModel", "Error removing observer", e);
            }
        }

        if (transactionsWithCategory != null) {
            transactionsObserver = transactions -> {
                try {
                    // Only analyze if we have transactions and executor is running
                    if (transactions != null && !transactions.isEmpty() && !analysisExecutor.isShutdown()) {
                        // Pass transactions directly to avoid LiveData thread issues
                        analyzeSpendingPatterns(transactions);
                    }
                } catch (Exception e) {
                    Log.e("TransactionViewModel", "Error in transactions observer", e);
                }
            };
            try {
                transactionsWithCategory.observeForever(transactionsObserver);
            } catch (Exception e) {
                Log.e("TransactionViewModel", "Error setting up observer", e);
            }
        }
    }

    /**
     * Analyze spending patterns with given transactions list
     */
    private void analyzeSpendingPatterns(List<TransactionWithCategory> transactions) {
        // Check if executor is still running
        if (analysisExecutor.isShutdown()) {
            return;
        }
        
        // Create a copy to avoid concurrent modification issues
        final List<TransactionWithCategory> transactionsCopy = new ArrayList<>(transactions);
        
        analysisExecutor.execute(() -> {
            try {
                if (transactionsCopy == null || transactionsCopy.isEmpty()) {
                    spendingInsights.postValue(null);
                    return;
                }

                SpendingAnalysisResult result = SpendingAnalyzer.analyzeSpendingPatterns(transactionsCopy);
                if (result != null) {
                    cachedAnalysisResult = result;
                    spendingInsights.postValue(result);
                    
                    Log.d("TransactionViewModel", "Spending analysis completed. Insights: " + 
                            (result.getInsights() != null ? result.getInsights().size() : 0));
                }
            } catch (Exception e) {
                Log.e("TransactionViewModel", "Error analyzing spending patterns", e);
                e.printStackTrace();
                // Don't crash - just log the error
            }
        });
    }

    /**
     * Trigger spending analysis in background thread (public method for manual trigger)
     */
    public void triggerSpendingAnalysis() {
        if (transactionsWithCategory != null && !analysisExecutor.isShutdown()) {
            List<TransactionWithCategory> transactions = transactionsWithCategory.getValue();
            if (transactions != null) {
                analyzeSpendingPatterns(transactions);
            }
        }
    }

    /**
     * Manually trigger analysis (for refresh)
     */
    public void analyzeSpendingPatterns() {
        triggerSpendingAnalysis();
    }

    /**
     * Get spending insights LiveData
     */
    public LiveData<SpendingAnalysisResult> getSpendingInsights() {
        return spendingInsights;
    }

    /**
     * Get cached analysis result (synchronous)
     */
    public SpendingAnalysisResult getCachedAnalysisResult() {
        return cachedAnalysisResult;
    }

}