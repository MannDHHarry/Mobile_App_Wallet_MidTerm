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
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import y3.mobiledev.mywallet.helpers.SpendingAnalyzer;
import y3.mobiledev.mywallet.helpers.TransactionManager;
import y3.mobiledev.mywallet.models.Category;
import y3.mobiledev.mywallet.models.SpendingAnalysisResult;
import y3.mobiledev.mywallet.models.Transaction;
import y3.mobiledev.mywallet.models.TransactionGroup;
import y3.mobiledev.mywallet.models.TransactionWithCategory;
import y3.mobiledev.mywallet.models.Transfer;
import y3.mobiledev.mywallet.models.TransferWithWallets;
import y3.mobiledev.mywallet.models.User;
import y3.mobiledev.mywallet.models.Wallet;
import y3.mobiledev.mywallet.repository.CategoryRepository;
import y3.mobiledev.mywallet.repository.TransactionRepository;
import y3.mobiledev.mywallet.repository.TransferRepository;
import y3.mobiledev.mywallet.repository.UserRepository;
import y3.mobiledev.mywallet.repository.WalletRepository;
import y3.mobiledev.mywallet.helpers.CurrencyUtils;
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
    private TransferRepository transferRepository;
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

    private LiveData<List<TransferWithWallets>> transfersWithWallets;   // ← ADD THIS LINE

    //Subscription Live data
    private LiveData<List<Subscription>> activeSubscriptions;
    private LiveData<List<Subscription>> allSubscriptions;

    // Spending Analysis
    private final MutableLiveData<SpendingAnalysisResult> spendingInsights = new MutableLiveData<>();
    private final MutableLiveData<List<String>> statisticsAlerts = new MutableLiveData<>();
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
        transferRepository = new TransferRepository(application);
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

        transfersWithWallets = transferRepository.getTransfersWithWalletsByUser(userId);  // ← ADD THIS

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

    public void executeTransfer(int fromWalletId, int toWalletId, double amount, long date) {
        User user = currentUser.getValue();
        if (user == null) return;

        Transfer transfer = new Transfer(
                user.getUserId(),
                fromWalletId,
                toWalletId,
                amount,
                date
        );

        transferRepository.executeTransfer(transfer);
    }

    /**
     * Delete transfer and reverse wallet balances
     */
    public void deleteTransfer(Transfer transfer) {
        transferRepository.deleteTransfer(transfer);
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

    /* public LiveData<List<TransferWithWallets>> getTransfersWithWallets() {
        User user = currentUser.getValue();
        if (user == null) return new MutableLiveData<>(new ArrayList<>());
        return transferRepository.getTransfersWithWalletsByUser(user.getUserId());
    } */

    public LiveData<List<TransferWithWallets>> getTransfersWithWallets() {
        return transfersWithWallets != null ? transfersWithWallets : new MutableLiveData<>(new ArrayList<>());
    }


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
                    updateStatisticsAlerts(transactions);
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

    private void updateStatisticsAlerts(List<TransactionWithCategory> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            statisticsAlerts.postValue(Collections.singletonList("Add transactions to see spending alerts."));
            return;
        }

        Calendar currentMonthStart = Calendar.getInstance();
        currentMonthStart.set(Calendar.DAY_OF_MONTH, 1);
        currentMonthStart.set(Calendar.HOUR_OF_DAY, 0);
        currentMonthStart.set(Calendar.MINUTE, 0);
        currentMonthStart.set(Calendar.SECOND, 0);
        currentMonthStart.set(Calendar.MILLISECOND, 0);

        long currentStart = currentMonthStart.getTimeInMillis();
        long now = System.currentTimeMillis();

        Calendar previousMonthStart = (Calendar) currentMonthStart.clone();
        previousMonthStart.add(Calendar.MONTH, -1);
        long previousStart = previousMonthStart.getTimeInMillis();
        long previousEnd = currentStart;

        double currentExpense = 0;
        double previousExpense = 0;
        double currentIncome = 0;
        double previousIncome = 0;

        Map<String, Double> currentCategoryTotals = new HashMap<>();
        Map<String, Double> previousCategoryTotals = new HashMap<>();

        for (TransactionWithCategory transaction : transactions) {
            long date = transaction.getDate();
            boolean inCurrent = date >= currentStart && date <= now;
            boolean inPrevious = !inCurrent && date >= previousStart && date < previousEnd;

            if (!inCurrent && !inPrevious) {
                continue;
            }

            double amount = transaction.getAmount();
            boolean isExpense = transaction.isExpense();

            if (isExpense) {
                if (inCurrent) {
                    currentExpense += amount;
                } else {
                    previousExpense += amount;
                }
            } else {
                if (inCurrent) {
                    currentIncome += amount;
                } else {
                    previousIncome += amount;
                }
            }

            if (isExpense) {
                String category = transaction.getCategoryName() != null ? transaction.getCategoryName() : "Other";
                Map<String, Double> target = inCurrent ? currentCategoryTotals : previousCategoryTotals;
                target.put(category, target.getOrDefault(category, 0.0) + amount);
            }
        }

        List<String> alerts = new ArrayList<>();

        if (previousExpense > 0) {
            double expenseChange = ((currentExpense - previousExpense) / previousExpense) * 100;
            if (expenseChange >= 20) {
                alerts.add(String.format(Locale.US,
                        "Expenses increased by %.1f%% vs last month.", expenseChange));
            } else if (expenseChange <= -20) {
                alerts.add(String.format(Locale.US,
                        "Great! Expenses dropped %.1f%% vs last month.", Math.abs(expenseChange)));
            }
        } else if (currentExpense > 0) {
            alerts.add("Tracking spending for the first time this month.");
        }

        if (previousIncome > 0) {
            double incomeChange = ((currentIncome - previousIncome) / previousIncome) * 100;
            if (incomeChange <= -20) {
                alerts.add(String.format(Locale.US,
                        "Income decreased by %.1f%% vs last month.", Math.abs(incomeChange)));
            }
        } else if (currentIncome == 0) {
            alerts.add("No income recorded this month yet.");
        }

        double netCurrent = currentIncome - currentExpense;
        if (netCurrent < 0) {
            alerts.add(String.format(Locale.US,
                    "You are overspending by %s this month.",
                    CurrencyUtils.formatPlainAmount(Math.abs(netCurrent))));
        }

        List<String> categoryAlerts = new ArrayList<>();
        for (Map.Entry<String, Double> entry : currentCategoryTotals.entrySet()) {
            String category = entry.getKey();
            double currentValue = entry.getValue();
            double previousValue = previousCategoryTotals.getOrDefault(category, 0.0);

            if (previousValue > 0) {
                double changePct = ((currentValue - previousValue) / previousValue) * 100;
                if (changePct >= 30) {
                    categoryAlerts.add(String.format(Locale.US,
                            "%s spending up %.1f%% vs last month (%s).",
                            category,
                            changePct,
                            CurrencyUtils.formatPlainAmount(currentValue)));
                }
            } else if (currentValue >= 1) {
                categoryAlerts.add(String.format(Locale.US,
                        "%s spending reached %s this month.",
                        category,
                        CurrencyUtils.formatPlainAmount(currentValue)));
            }
        }

        for (int i = 0; i < Math.min(2, categoryAlerts.size()); i++) {
            alerts.add(categoryAlerts.get(i));
        }

        if (alerts.isEmpty()) {
            alerts.add("Spending looks stable compared to last month.");
        }

        statisticsAlerts.postValue(alerts);
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

    public LiveData<List<String>> getStatisticsAlerts() {
        return statisticsAlerts;
    }
}