package y3.mobiledev.mywallet;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import y3.mobiledev.mywallet.TransactionAdapter;
import y3.mobiledev.mywallet.WalletAdapter;
import y3.mobiledev.mywallet.Transaction;
import y3.mobiledev.mywallet.TransactionGroup;
import y3.mobiledev.mywallet.Wallet;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Views
    private TextView tvUserName, tvTotalBalance, tvIncome, tvExpense, tvTimePeriod;
    private TextView btnViewAllTransactions, btnShowMoreWallets;
    private ImageButton btnSearch, btnAddWallet;
    private RecyclerView rvWallets, rvTransactions;
    private FloatingActionButton fabAddTransaction;
    private BottomNavigationView bottomNavigation;
    private View emptyStateWallets, emptyStateTransactions;

    // Adapters
    private WalletAdapter walletAdapter;
    private TransactionAdapter transactionAdapter;

    // Data
    private List<Wallet> allWallets;
    private List<Wallet> displayedWallets;
    private List<TransactionGroup> transactionGroups;
    private boolean showingAllWallets = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initViews();

        // Setup sample data
        setupSampleData();

        // Setup RecyclerViews
        setupWalletsRecyclerView();
        setupTransactionsRecyclerView();

        // Setup listeners
        setupListeners();

        // Update UI
        updateBalanceCard();
        updateTimePeriod();
    }

    private void initViews() {
        // Top bar
        tvUserName = findViewById(R.id.tvUserName);
        btnSearch = findViewById(R.id.btnSearch);

        // Balance card
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvTimePeriod = findViewById(R.id.tvTimePeriod);

        // Wallets section
        btnAddWallet = findViewById(R.id.btnAddWallet);
        rvWallets = findViewById(R.id.rvWallets);
        btnShowMoreWallets = findViewById(R.id.btnShowMoreWallets);
        emptyStateWallets = findViewById(R.id.emptyStateWallets);

        // Transactions section
        btnViewAllTransactions = findViewById(R.id.btnViewAllTransactions);
        rvTransactions = findViewById(R.id.rvTransactions);
        emptyStateTransactions = findViewById(R.id.emptyStateTransactions);

        // FAB and bottom nav
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupSampleData() {
        // Sample user name
        tvUserName.setText("Syed Nomi");

        // Create sample wallets (sorted by frequency - transaction count)
        allWallets = new ArrayList<>();
        allWallets.add(new Wallet(1, "Business", android.R.drawable.ic_menu_myplaces, 500.00, 15)); // Most used
        allWallets.add(new Wallet(2, "Personal", android.R.drawable.ic_menu_myplaces, 1200.00, 12));
        allWallets.add(new Wallet(3, "Savings", android.R.drawable.ic_menu_myplaces, 643.23, 5));
        allWallets.add(new Wallet(4, "Emergency", android.R.drawable.ic_menu_myplaces, 1000.00, 2));
        allWallets.add(new Wallet(5, "Investment", android.R.drawable.ic_menu_myplaces, 5000.00, 1));

        // Show first 3 wallets initially
        displayedWallets = new ArrayList<>();
        for (int i = 0; i < Math.min(3, allWallets.size()); i++) {
            displayedWallets.add(allWallets.get(i));
        }

        // Show "Show More" button if more than 3 wallets
        if (allWallets.size() > 3) {
            btnShowMoreWallets.setVisibility(View.VISIBLE);
        }

        // Create sample transactions
        List<Transaction> allTransactions = createSampleTransactions();

        // Group transactions by date (Today, Yesterday, Earlier)
        transactionGroups = groupTransactionsByDate(allTransactions);
    }

    private List<Transaction> createSampleTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        // Today's transactions
        transactions.add(new Transaction(1, "Utilities", "paid wifi bill for home internet service",
                23.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_orange));

        transactions.add(new Transaction(2, "Transport", "taxi to office",
                15.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_blue));

        // Yesterday's transactions
        cal.add(Calendar.DAY_OF_MONTH, -1);
        transactions.add(new Transaction(3, "Utilities", "electricity bill",
                45.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_orange));

        transactions.add(new Transaction(4, "Food", "lunch at restaurant",
                30.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_pink));

        // Earlier transactions
        cal.add(Calendar.DAY_OF_MONTH, -2);
        transactions.add(new Transaction(5, "Entertainment", "movie tickets for family",
                50.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_purple));

        cal.add(Calendar.DAY_OF_MONTH, -1);
        transactions.add(new Transaction(6, "Transport", "fuel for car",
                60.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_blue));

        // Income transaction
        cal.set(Calendar.DAY_OF_MONTH, 1); // First day of month
        transactions.add(new Transaction(7, "Salary", "monthly salary from company",
                2500.00, cal.getTime(), false, android.R.drawable.ic_dialog_info, R.color.category_green));

        transactions.add(new Transaction(8, "Freelance", "project payment from client",
                500.00, cal.getTime(), false, android.R.drawable.ic_dialog_info, R.color.category_green));

        // More earlier transactions
        cal.add(Calendar.DAY_OF_MONTH, -5);
        transactions.add(new Transaction(9, "Shopping", "groceries for the week",
                120.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_teal));

        transactions.add(new Transaction(10, "Healthcare", "doctor consultation",
                80.00, cal.getTime(), true, android.R.drawable.ic_dialog_info, R.color.category_pink));

        return transactions;
    }

    private List<TransactionGroup> groupTransactionsByDate(List<Transaction> transactions) {
        List<TransactionGroup> groups = new ArrayList<>();
        List<Transaction> todayTransactions = new ArrayList<>();
        List<Transaction> yesterdayTransactions = new ArrayList<>();
        List<Transaction> earlierTransactions = new ArrayList<>();

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        for (Transaction transaction : transactions) {
            Calendar transactionCal = Calendar.getInstance();
            transactionCal.setTime(transaction.getDate());
            transactionCal.set(Calendar.HOUR_OF_DAY, 0);
            transactionCal.set(Calendar.MINUTE, 0);
            transactionCal.set(Calendar.SECOND, 0);
            transactionCal.set(Calendar.MILLISECOND, 0);

            if (transactionCal.equals(today)) {
                todayTransactions.add(transaction);
            } else if (transactionCal.equals(yesterday)) {
                yesterdayTransactions.add(transaction);
            } else {
                earlierTransactions.add(transaction);
            }
        }

        // Add groups (only if they have transactions)
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

    private void setupWalletsRecyclerView() {
        walletAdapter = new WalletAdapter(this, displayedWallets, new WalletAdapter.OnWalletClickListener() {
            @Override
            public void onWalletClick(Wallet wallet) {
                Toast.makeText(MainActivity.this, "Clicked: " + wallet.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Navigate to wallet detail screen
            }
        });

        rvWallets.setLayoutManager(new LinearLayoutManager(this));
        rvWallets.setAdapter(walletAdapter);
        rvWallets.setNestedScrollingEnabled(false);

        // Check if empty
        if (allWallets.isEmpty()) {
            rvWallets.setVisibility(View.GONE);
            emptyStateWallets.setVisibility(View.VISIBLE);
            btnShowMoreWallets.setVisibility(View.GONE);
        } else {
            rvWallets.setVisibility(View.VISIBLE);
            emptyStateWallets.setVisibility(View.GONE);
        }
    }

    private void setupTransactionsRecyclerView() {
        transactionAdapter = new TransactionAdapter(this, transactionGroups,
                new TransactionAdapter.OnTransactionClickListener() {
                    @Override
                    public void onTransactionClick(Transaction transaction) {
                        Toast.makeText(MainActivity.this,
                                "Clicked: " + transaction.getCategory(), Toast.LENGTH_SHORT).show();
                        // TODO: Navigate to transaction detail/edit screen
                    }
                });

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);
        rvTransactions.setNestedScrollingEnabled(false);

        // Check if empty
        if (transactionGroups.isEmpty()) {
            rvTransactions.setVisibility(View.GONE);
            emptyStateTransactions.setVisibility(View.VISIBLE);
        } else {
            rvTransactions.setVisibility(View.VISIBLE);
            emptyStateTransactions.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        // Search button
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Search clicked", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to search/filter screen
            }
        });

        // Add wallet button
        btnAddWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Add Wallet clicked", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to add wallet screen
            }
        });

        // Show More/Less wallets button
        btnShowMoreWallets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleWalletDisplay();
            }
        });

        // View All Transactions button
        btnViewAllTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "View All Transactions", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to all transactions screen
            }
        });

        // FAB - Add Transaction
        fabAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Add Transaction", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to add transaction screen
            }
        });

        // Bottom Navigation
        bottomNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int id = item.getItemId();

                        if (id == R.id.nav_home) {
                            // Already on home
                            return true;
                        } else if (id == R.id.nav_statistics) {
                            Toast.makeText(MainActivity.this, "Statistics", Toast.LENGTH_SHORT).show();
                            // TODO: Navigate to statistics screen
                            return true;
                        } else if (id == R.id.nav_categories) {
                            Toast.makeText(MainActivity.this, "Categories", Toast.LENGTH_SHORT).show();
                            // TODO: Navigate to categories screen
                            return true;
                        } else if (id == R.id.nav_more) {
                            Toast.makeText(MainActivity.this, "More", Toast.LENGTH_SHORT).show();
                            // TODO: Navigate to more/settings screen
                            return true;
                        }
                        return false;
                    }
                });

        // Set Home as selected by default
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        // Empty state wallet add button
        View btnAddFirstWallet = emptyStateWallets.findViewById(R.id.btnAddFirstWallet);
        if (btnAddFirstWallet != null) {
            btnAddFirstWallet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Add First Wallet", Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to add wallet screen
                }
            });
        }
    }

    private void toggleWalletDisplay() {
        if (showingAllWallets) {
            // Show only first 3
            displayedWallets.clear();
            for (int i = 0; i < Math.min(3, allWallets.size()); i++) {
                displayedWallets.add(allWallets.get(i));
            }
            btnShowMoreWallets.setText(R.string.show_more);
            showingAllWallets = false;
        } else {
            // Show all wallets
            displayedWallets.clear();
            displayedWallets.addAll(allWallets);
            btnShowMoreWallets.setText(R.string.show_less);
            showingAllWallets = true;
        }
        walletAdapter.updateWallets(displayedWallets);
    }

    private void updateBalanceCard() {
        // Calculate total balance
        double totalBalance = 0;
        for (Wallet wallet : allWallets) {
            totalBalance += wallet.getBalance();
        }

        // Calculate this month's income and expense
        double totalIncome = 0;
        double totalExpense = 0;

        Calendar firstDayOfMonth = Calendar.getInstance();
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        firstDayOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        firstDayOfMonth.set(Calendar.MINUTE, 0);
        firstDayOfMonth.set(Calendar.SECOND, 0);

        for (TransactionGroup group : transactionGroups) {
            for (Transaction transaction : group.getTransactions()) {
                if (transaction.getDate().after(firstDayOfMonth.getTime())) {
                    if (transaction.isExpense()) {
                        totalExpense += transaction.getAmount();
                    } else {
                        totalIncome += transaction.getAmount();
                    }
                }
            }
        }

        // Update UI
        tvTotalBalance.setText(String.format(Locale.US, "$%,.2f", totalBalance));
        tvIncome.setText(String.format(Locale.US, "$%,.0f", totalIncome));
        tvExpense.setText(String.format(Locale.US, "$%,.0f", totalExpense));
    }

    private void updateTimePeriod() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", Locale.US);
        String currentMonth = dateFormat.format(new Date());
        tvTimePeriod.setText(getString(R.string.this_month, currentMonth));
    }
}